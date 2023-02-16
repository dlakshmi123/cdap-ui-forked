package com.continuuity.internal.app.runtime.flow;

import com.continuuity.api.flow.flowlet.Callback;
import com.continuuity.api.flow.flowlet.FailurePolicy;
import com.continuuity.api.flow.flowlet.FailureReason;
import com.continuuity.api.flow.flowlet.Flowlet;
import com.continuuity.api.flow.flowlet.InputContext;
import com.continuuity.app.queue.InputDatum;
import com.continuuity.common.logging.LoggingContext;
import com.continuuity.common.logging.LoggingContextAccessor;
import com.continuuity.data2.transaction.TransactionContext;
import com.continuuity.data2.transaction.TransactionFailureException;
import com.continuuity.internal.app.queue.SingleItemQueueReader;
import com.continuuity.internal.app.runtime.DataFabricFacade;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class responsible invoking process methods one by one and commit the post process transaction.
 */
final class FlowletProcessDriver extends AbstractExecutionThreadService {

  private static final Logger LOG = LoggerFactory.getLogger(FlowletProcessDriver.class);

  private final Flowlet flowlet;
  private final BasicFlowletContext flowletContext;
  private final LoggingContext loggingContext;
  private final Collection<ProcessSpecification> processSpecs;
  private final Callback txCallback;
  private final AtomicReference<CountDownLatch> suspension;
  private final CyclicBarrier suspendBarrier;
  private final AtomicInteger inflight;
  private final DataFabricFacade dataFabricFacade;
  private Thread runnerThread;

  FlowletProcessDriver(Flowlet flowlet, BasicFlowletContext flowletContext,
                       Collection<ProcessSpecification> processSpecs,
                       Callback txCallback, DataFabricFacade dataFabricFacade) {
    this.flowlet = flowlet;
    this.flowletContext = flowletContext;
    this.loggingContext = flowletContext.getLoggingContext();
    this.processSpecs = processSpecs;
    this.txCallback = txCallback;
    this.dataFabricFacade = dataFabricFacade;
    this.inflight = new AtomicInteger(0);

    this.suspension = new AtomicReference<CountDownLatch>();
    this.suspendBarrier = new CyclicBarrier(2);
  }

  @Override
  protected void startUp() throws Exception {
    runnerThread = Thread.currentThread();
    flowletContext.getSystemMetrics().gauge("process.instance", 1);
  }

  @Override
  protected void shutDown() throws Exception {
    flowletContext.close();
  }

  @Override
  protected void triggerShutdown() {
    runnerThread.interrupt();
  }

  @Override
  protected String getServiceName() {
    return getClass().getSimpleName() + "-" + flowletContext.getName() + "-" + flowletContext.getInstanceId();
  }

  /**
   * Suspend the running of flowlet. This method will block until the flowlet running thread actually suspended.
   */
  public void suspend() {
    if (suspension.compareAndSet(null, new CountDownLatch(1))) {
      try {
        suspendBarrier.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (BrokenBarrierException e) {
        LOG.error("Exception during suspend: " + flowletContext, e);
      }
    }
  }

  /**
   * Resume the running of flowlet.
   */
  public void resume() {
    CountDownLatch latch = suspension.getAndSet(null);
    if (latch != null) {
      suspendBarrier.reset();
      latch.countDown();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void run() {
    LoggingContextAccessor.setLoggingContext(loggingContext);

    initFlowlet();

    // Insert all into priority queue, ordered by next deque time.
    BlockingQueue<FlowletProcessEntry<?>> processQueue =
      new PriorityBlockingQueue<FlowletProcessEntry<?>>(processSpecs.size());
    for (ProcessSpecification<?> spec : processSpecs) {
      processQueue.offer(FlowletProcessEntry.create(spec));
    }
    List<FlowletProcessEntry<?>> processList = Lists.newArrayListWithExpectedSize(processSpecs.size() * 2);

    while (isRunning()) {
      CountDownLatch suspendLatch = suspension.get();
      if (suspendLatch != null) {
        try {
          waitForInflight(processQueue);
          suspendBarrier.await();
          suspendLatch.await();
        } catch (Exception e) {
          // Simply continue and let the isRunning() check to deal with that.
          continue;
        }
      }

      try {
        // If the queue head need to wait, we had to wait.
        processQueue.peek().await();
      } catch (InterruptedException e) {
        // Triggered by shutdown, simply continue and let the isRunning() check to deal with that.
        continue;
      }

      processList.clear();
      processQueue.drainTo(processList);

      for (FlowletProcessEntry<?> entry : processList) {
        if (!entry.shouldProcess()) {
          processQueue.offer(entry);
          continue;
        }

        ProcessMethod processMethod = entry.getProcessSpec().getProcessMethod();
        if (processMethod.needsInput()) {
          flowletContext.getSystemMetrics().gauge("process.tuples.attempt.read", 1);
        }

        // Begin transaction and dequeue
        TransactionContext txContext = dataFabricFacade.createTransactionManager();
        try {
          txContext.start();

          InputDatum input = entry.getProcessSpec().getQueueReader().dequeue();
          if (!input.needProcess()) {
            entry.backOff();
            // End the transaction if nothing in the queue
            txContext.finish();
            processQueue.offer(entry);
            continue;
          }
          // Resetting back-off time to minimum back-off time,
          // since an entry to process was de-queued and most likely more entries will follow.
          entry.resetBackOff();

          if (!entry.isRetry()) {
            // Only increment the inflight count for non-retry entries.
            // The inflight would get decrement when the transaction committed successfully or input get ignored.
            // See the processMethodCallback function.
            inflight.getAndIncrement();
          }

          // Call the process method and commit the transaction. The current process entry will put
          // back to queue in the postProcess method (either a retry copy or itself).
          ProcessMethod.ProcessResult result =
            processMethod.invoke(input, wrapInputDecoder(input, entry.getProcessSpec().getInputDecoder()));
          postProcess(processMethodCallback(processQueue, entry, input), txContext, input, result);

        } catch (Throwable t) {
          LOG.error("Unexpected exception: {}", flowletContext, t);
          try {
            txContext.abort();
          } catch (TransactionFailureException e) {
            LOG.error("Fail to abort transaction: {}", flowletContext, e);
          }
        }
      }
    }
    waitForInflight(processQueue);

    destroyFlowlet();
  }

  private <T> Function<ByteBuffer, T> wrapInputDecoder(final InputDatum input,
                                                       final Function<ByteBuffer, T> inputDecoder) {
    final String eventsMetricsName = "process.events.in";
    final String eventsMetricsTag = input.getInputContext().getOrigin();
    return new Function<ByteBuffer, T>() {
      @Override
      public T apply(ByteBuffer byteBuffer) {
        flowletContext.getSystemMetrics().gauge(eventsMetricsName, 1, eventsMetricsTag);
        flowletContext.getSystemMetrics().gauge("process.tuples.read", 1, eventsMetricsTag);
        return inputDecoder.apply(byteBuffer);
      }
    };
  }

  private void postProcess(ProcessMethodCallback callback, TransactionContext txContext,
                           InputDatum input, ProcessMethod.ProcessResult result) {
    InputContext inputContext = input.getInputContext();
    Throwable failureCause = null;
    try {
      if (result.isSuccess()) {
        // If it is a retry input, force the dequeued entries into current transaction.
        if (input.getRetry() > 0) {
          input.reclaim();
        }
        txContext.finish();
      } else {
        failureCause = result.getCause();
        txContext.abort();
      }
    } catch (TransactionFailureException e) {
      LOG.error("Transaction operation failed: {}", e.getMessage(), e);
      failureCause = e;
      try {
        if (result.isSuccess()) {
          txContext.abort();
        }
      } catch (TransactionFailureException ex) {
        LOG.error("Fail to abort transaction: {}", inputContext, ex);
      }
    }

    if (failureCause == null) {
      callback.onSuccess(result.getEvent(), inputContext);
    } else {
      callback.onFailure(result.getEvent(), inputContext,
                         new FailureReason(FailureReason.Type.USER, failureCause.getMessage(), failureCause),
                         createInputAcknowledger(input));
    }
  }

  private InputAcknowledger createInputAcknowledger(final InputDatum input) {
    return new InputAcknowledger() {
      @Override
      public void ack() throws TransactionFailureException {
        TransactionContext txContext = dataFabricFacade.createTransactionManager();
        txContext.start();
        input.reclaim();
        txContext.finish();
      }
    };
  }

  /**
   * Wait for all inflight processes in the queue.
   * @param processQueue list of inflight processes
   */
  @SuppressWarnings("unchecked")
  private void waitForInflight(BlockingQueue<FlowletProcessEntry<?>> processQueue) {
    List<FlowletProcessEntry> processList = Lists.newArrayListWithCapacity(processQueue.size());
    boolean hasRetry;

    do {
      hasRetry = false;
      processList.clear();
      processQueue.drainTo(processList);

      for (FlowletProcessEntry<?> entry : processList) {
        if (!entry.isRetry()) {
          processQueue.offer(entry);
          continue;
        }
        hasRetry = true;
        ProcessMethod processMethod = entry.getProcessSpec().getProcessMethod();

        TransactionContext txContext = dataFabricFacade.createTransactionManager();
        try {
          txContext.start();
          InputDatum input = entry.getProcessSpec().getQueueReader().dequeue();
          flowletContext.getSystemMetrics().gauge("process.tuples.attempt.read", input.size());

          // Call the process method and commit the transaction
          ProcessMethod.ProcessResult result =
            processMethod.invoke(input, wrapInputDecoder(input, entry.getProcessSpec().getInputDecoder()));
          postProcess(processMethodCallback(processQueue, entry, input), txContext, input, result);

        } catch (Throwable t) {
          LOG.error("Unexpected exception: {}", flowletContext, t);
          try {
            txContext.abort();
          } catch (TransactionFailureException e) {
            LOG.error("Fail to abort transaction: {}", flowletContext, e);
          }
        }
      }
    } while (hasRetry || inflight.get() != 0);
  }

  private void initFlowlet() {
    try {
      LOG.info("Initializing flowlet: " + flowletContext);
      flowlet.initialize(flowletContext);
      LOG.info("Flowlet initialized: " + flowletContext);
    } catch (Throwable t) {
      LOG.error("Flowlet throws exception during flowlet initialize: " + flowletContext, t);
      throw Throwables.propagate(t);
    }
  }

  private void destroyFlowlet() {
    try {
      LOG.info("Destroying flowlet: " + flowletContext);
      flowlet.destroy();
      LOG.info("Flowlet destroyed: " + flowletContext);
    } catch (Throwable t) {
      LOG.error("Flowlet throws exception during flowlet destroy: " + flowletContext, t);
    }
  }

  private <T> ProcessMethodCallback processMethodCallback(final BlockingQueue<FlowletProcessEntry<?>> processQueue,
                                                          final FlowletProcessEntry<T> processEntry,
                                                          final InputDatum input) {
    // If it is generator flowlet, processCount is 1.
    final int processedCount = processEntry.getProcessSpec().getProcessMethod().needsInput() ? input.size() : 1;

    return new ProcessMethodCallback() {
      @Override
      public void onSuccess(Object object, InputContext inputContext) {
        try {
          flowletContext.getSystemMetrics().gauge("process.events.processed", processedCount);
          txCallback.onSuccess(object, inputContext);
        } catch (Throwable t) {
          LOG.error("Exception on onSuccess call: {}", flowletContext, t);
        } finally {
          enqueueEntry();
          inflight.decrementAndGet();
        }
      }

      @Override
      public void onFailure(Object inputObject, InputContext inputContext, FailureReason reason,
                            InputAcknowledger inputAcknowledger) {

        LOG.warn("Process failure: {}, {}, input: {}", flowletContext, reason.getMessage(), input, reason.getCause());
        FailurePolicy failurePolicy;
        try {
          flowletContext.getSystemMetrics().gauge("process.errors", 1);
          failurePolicy = txCallback.onFailure(inputObject, inputContext, reason);
          if (failurePolicy == null) {
            failurePolicy = FailurePolicy.RETRY;
            LOG.info("Callback returns null for failure policy. Default to {}.", failurePolicy);
          }
        } catch (Throwable t) {
          LOG.error("Exception on onFailure call: {}", flowletContext, t);
          failurePolicy = FailurePolicy.RETRY;
        }

        if (input.getRetry() >= processEntry.getProcessSpec().getProcessMethod().getMaxRetries()) {
          LOG.info("Too many retries, ignores the input: {}", input);
          failurePolicy = FailurePolicy.IGNORE;
        }

        if (failurePolicy == FailurePolicy.RETRY) {
          FlowletProcessEntry retryEntry = processEntry.isRetry() ?
            processEntry :
            FlowletProcessEntry.create(processEntry.getProcessSpec(),
                                       new ProcessSpecification<T>(new SingleItemQueueReader(input),
                                                                   processEntry.getProcessSpec().getInputDecoder(),
                                                                   processEntry.getProcessSpec().getProcessMethod(),
                                                                   null));
          processQueue.offer(retryEntry);

        } else if (failurePolicy == FailurePolicy.IGNORE) {
          try {
            flowletContext.getSystemMetrics().gauge("process.events.processed", processedCount);
            inputAcknowledger.ack();
          } catch (TransactionFailureException e) {
            LOG.error("Fatal problem, fail to ack an input: {}", flowletContext, e);
          } finally {
            enqueueEntry();
            inflight.decrementAndGet();
          }
        }
      }

      private void enqueueEntry() {
        processQueue.offer(processEntry.resetRetry());
      }
    };
  }
}
