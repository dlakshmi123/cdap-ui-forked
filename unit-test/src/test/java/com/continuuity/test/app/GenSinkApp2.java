package com.continuuity.test.app;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.annotation.Batch;
import com.continuuity.api.annotation.Output;
import com.continuuity.api.annotation.ProcessInput;
import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.GeneratorFlowlet;
import com.continuuity.api.flow.flowlet.InputContext;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 *
 */
public final class GenSinkApp2 implements Application {

  @Override
  public ApplicationSpecification configure() {
    return ApplicationSpecification.Builder.with()
      .setName("GenSinkApp")
      .setDescription("GenSinkApp desc")
      .noStream()
      .noDataSet()
      .withFlows().add(new GenSinkFlow())
      .noProcedure()
      .build();
  }

  /**
   *
   */
  public static final class GenSinkFlow implements Flow {

    @Override
    public FlowSpecification configure() {
      return FlowSpecification.Builder.with()
        .setName("GenSinkFlow")
        .setDescription("GenSinkFlow desc")
        .withFlowlets()
        .add(new GenFlowlet())
        .add(new SinkFlowlet())
        .connect()
        .from(new GenFlowlet()).to(new SinkFlowlet())
        .build();
    }
  }

  /**
   * @param <T>
   * @param <U>
   */
  public abstract static class GenFlowletBase<T, U> extends AbstractFlowlet implements GeneratorFlowlet {

    protected OutputEmitter<T> output;

    @Output("batch")
    protected OutputEmitter<U> batchOutput;
  }

  /**
   *
   */
  public static final class GenFlowlet extends GenFlowletBase<String, Integer> {

    private int i;

    @Override
    public void generate() throws Exception {
      if (i < 100) {
        output.emit("Testing " + ++i);
        batchOutput.emit(i);
        if (i == 10) {
          throw new IllegalStateException("10 hitted");
        }
        return;
      }
    }
  }

  /**
   * @param <T>
   * @param <U>
   */
  public abstract static class SinkFlowletBase<T, U> extends AbstractFlowlet {
    private static final Logger LOG = LoggerFactory.getLogger(SinkFlowlet.class);

    public void process(T event, InputContext context) throws InterruptedException {
      LOG.info(event.toString());
    }

    @Batch(10)
    @ProcessInput("batch")
    public void processBatch(Iterator<U> events) {
      while (events.hasNext()) {
        LOG.info(events.next().toString());
      }
    }
  }

  /**
   *
   */
  public static final class SinkFlowlet extends SinkFlowletBase<String, Integer> {
  }
}

