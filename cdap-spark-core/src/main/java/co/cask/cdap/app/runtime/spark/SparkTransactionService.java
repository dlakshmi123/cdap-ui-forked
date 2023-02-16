/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.app.runtime.spark;

import co.cask.cdap.data2.transaction.Transactions;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import co.cask.http.NettyHttpService;
import co.cask.tephra.Transaction;
import co.cask.tephra.TransactionCodec;
import co.cask.tephra.TransactionSystemClient;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractIdleService;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Provides transaction management for Spark job and stage executors. It also expose an endpoint for stage executors
 * to get {@link Transaction} information associated with the stage. For detail design, please refer to the
 * <a href="https://wiki.cask.co/display/CE/Spark+Revamp">design documentation</a>.
 */
final class SparkTransactionService extends AbstractIdleService {

  private static final Logger LOG = LoggerFactory.getLogger(SparkTransactionService.class);

  private final TransactionSystemClient txClient;

  // Map from StageId to JobId. It is needed because Spark JobId is only available on the driver.
  // In the executor node, there is only StageId. The Spark StageId is unique across job, so it's ok to use a map.
  private final ConcurrentMap<Integer, Integer> stageToJob;
  private final ConcurrentMap<Integer, JobTransaction> jobTransactions;

  private final NettyHttpService httpServer;

  SparkTransactionService(TransactionSystemClient txClient) {
    this.txClient = txClient;
    this.stageToJob = new ConcurrentHashMap<>();
    this.jobTransactions = new ConcurrentHashMap<>();
    this.httpServer = NettyHttpService.builder()
      .addHttpHandlers(Collections.singleton(new SparkTransactionHandler()))
      .build();
  }

  @Override
  protected void startUp() throws Exception {
    httpServer.startAndWait();
  }

  @Override
  protected void shutDown() throws Exception {
    httpServer.stopAndWait();
  }

  /**
   * Returns the base {@link URI} for talking to this service remotely through HTTP.
   */
  URI getBaseURI() {
    InetSocketAddress bindAddress = httpServer.getBindAddress();
    if (bindAddress == null) {
      throw new IllegalStateException("SparkTransactionService hasn't been started");
    }
    return URI.create(String.format("http://%s:%d", bindAddress.getHostName(), bindAddress.getPort()));
  }

  /**
   * Notifies the given job execution started.
   *
   * @param jobId the unique id that identifies the job.
   * @param stageIds set of stage ids that are associated with the given job.
   */
  void jobStarted(Integer jobId, Set<Integer> stageIds) {
    jobStarted(new JobTransaction(jobId, stageIds, null));
  }

  /**
   * Notifies the given job execution started with an explicit {@link Transaction}.
   *
   * @param jobId the unique id that identifies the job.
   * @param stageIds set of stage ids that are associated with the given job.
   * @param transaction the {@link Transaction} to use for the job.
   */
  void jobStarted(Integer jobId, Set<Integer> stageIds, Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction cannot be null for explicit transaction");
    }
    jobStarted(new JobTransaction(jobId, stageIds, transaction));
  }

  /**
   * Notifies the job represented by the given {@link JobTransaction} started.
   */
  private void jobStarted(JobTransaction jobTransaction) {
    Integer jobId = jobTransaction.getJobId();

    // Remember the job Id. We won't start a new transaction here until a stage requested for it.
    // This is because there can be job that doesn't need transaction or explicit transaction is being used
    JobTransaction existingJobTx = jobTransactions.putIfAbsent(jobId, jobTransaction);
    if (existingJobTx != null) {
      // Shouldn't happen as Spark generates unique Job Id.
      // If that really happen, just log and return
      LOG.error("Job already running: {}", existingJobTx);
      return;
    }

    // Build the stageId => jobId map first instead of putting to the concurrent map one by one
    Map<Integer, Integer> stageToJob = new HashMap<>();
    for (Integer stageId : jobTransaction.getStageIds()) {
      stageToJob.put(stageId, jobId);
    }
    this.stageToJob.putAll(stageToJob);
  }

  /**
   * Notifies the given job execution completed.
   *
   * @param jobId the unique id that identifies the job.
   * @param succeeded {@code true} if the job execution completed successfully.
   */
  void jobEnded(Integer jobId, boolean succeeded) {
    JobTransaction jobTransaction = jobTransactions.remove(jobId);
    if (jobTransaction == null) {
      // Shouldn't happen, otherwise something very wrong. Can't do much, just log and return
      LOG.error("Transaction for job {} not found.", jobId);
      return;
    }

    // Cleanup the stage to job map
    stageToJob.keySet().removeAll(jobTransaction.getStageIds());

    // Complete the transaction
    jobTransaction.completed(succeeded);
  }

  /**
   * HTTP Handler to provide the Spark stage execution transaction lookup service.
   */
  public final class SparkTransactionHandler extends AbstractHttpHandler {

    private final TransactionCodec txCodec = new TransactionCodec();

    /**
     * Handler method to get a serialized {@link Transaction} for the given stage.
     */
    @GET
    @Path("/spark/stages/{stage}/transaction")
    public void getTransaction(HttpRequest request, HttpResponder responder, @PathParam("stage") int stageId) {
      // Lookup the jobId from the stageId
      Integer jobId = stageToJob.get(stageId);
      if (jobId == null) {
        // If the JobId is not there, it's either the job hasn't been registered yet (because it's async) or
        // the job is already finished. For either case, return 404 and let the client to handle retry if necessary.
        responder.sendString(HttpResponseStatus.NOT_FOUND, "JobId not found for stage " + stageId);
        return;
      }

      // Get the transaction
      JobTransaction jobTransaction = jobTransactions.get(jobId);
      if (jobTransaction == null) {
        // The only reason we can find the jobId from the stageToJob map but not the job transaction is because
        // the job is completed, hence the transaction get removed. In normal case, it shouldn't happen
        // as a job won't complete if there are still stages running and
        // this method only gets called from stage running in executor node.
        responder.sendString(HttpResponseStatus.GONE,
                             "No transaction associated with the stage " + stageId + " of job " + jobId);
        return;
      }

      Transaction transaction = jobTransaction.getTransaction();
      if (transaction == null) {
        // Job failed to start a transaction. Response with GONE as well so that the stage execution can fail itself
        responder.sendString(HttpResponseStatus.GONE,
                             "Failed to start transaction for stage " + stageId + " of job " + jobId);
        return;
      }

      // Serialize the transaction and send it back
      try {
        responder.sendByteArray(HttpResponseStatus.OK, txCodec.encode(transaction), null);
      } catch (IOException e) {
        // Shouldn't happen
        LOG.error("Failed to encode Transaction {}", jobTransaction, e);
        responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                             "Failed to encode transaction: " + e.getMessage());
      }
    }
  }

  /**
   * A private class for handling the {@link Transaction} lifecycle for a job.
   */
  private final class JobTransaction {
    private final Integer jobId;
    private final Set<Integer> stageIds;
    private final boolean ownTransaction;
    private volatile Optional<Transaction> transaction;

    JobTransaction(Integer jobId, Set<Integer> stageIds, @Nullable Transaction transaction) {
      this.jobId = jobId;
      this.stageIds = ImmutableSet.copyOf(stageIds);
      this.ownTransaction = transaction == null;
      this.transaction = transaction == null ? null : Optional.of(transaction);
    }

    Integer getJobId() {
      return jobId;
    }

    Set<Integer> getStageIds() {
      return stageIds;
    }

    /**
     * Returns the {@link Transaction} associated with the job. If transaction hasn't been started, a new long
     * transaction will be started.
     *
     * @return the job's {@link Transaction} or {@code null} if it failed to start transaction for the job.
     */
    @Nullable
    public Transaction getTransaction() {
      Optional<Transaction> tx = transaction;
      if (tx == null) {
        // double-checked locking
        synchronized (this) {
          tx = transaction;
          if (tx == null) {
            try {
              tx = transaction = Optional.of(txClient.startLong());
            } catch (Throwable t) {
              LOG.error("Failed to start transaction for job {}", jobId, t);
              tx = transaction = Optional.absent();
            }
          }
        }
      }
      return tx.orNull();
    }

    /**
     * Completes the job {@link Transaction} by either committing or invalidating the transaction, based on the
     * job result.
     *
     * @param succeeded {@code true} if the job execution completed successfully.
     */
    public void completed(boolean succeeded) {
      // If this service doesn't own the transaction, then nothing to do. It is for the explicit transaction case.
      if (!ownTransaction) {
        return;
      }

      try {
        Optional<Transaction> tx = transaction;
        if (tx == null || !tx.isPresent()) {
          // No transaction was started for the job, hence nothing to do.
          return;
        }

        Transaction jobTx = tx.get();
        if (succeeded) {
          LOG.debug("Committing transaction for job {}", jobId);
          if (!txClient.commit(jobTx)) {
            // If failed to commit (which it shouldn't since there is no conflict detection), invalidate the tx
            Transactions.invalidateQuietly(txClient, jobTx);
          }
        } else {
          LOG.debug("Aborting transaction for job {}", jobId);
          txClient.invalidate(jobTx.getWritePointer());
        }
      } catch (Throwable t) {
        LOG.error("Failed to {} transaction for job {}", succeeded ? "commit" : "invalidate", jobId, t);
      }
    }

    @Override
    public String toString() {
      return "JobTransaction{" +
        "jobId=" + jobId +
        ", stageIds=" + stageIds +
        ", ownTransaction=" + ownTransaction +
        ", transaction=" + (transaction == null ? null : transaction.orNull()) +
        '}';
    }
  }
}
