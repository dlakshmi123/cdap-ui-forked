package com.continuuity.internal.app.services;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.annotation.Tick;
import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.FlowletContext;
import com.continuuity.api.flow.flowlet.FlowletException;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import com.continuuity.app.services.AppFabricService;
import com.continuuity.app.services.AuthToken;
import com.continuuity.app.services.ProgramDescriptor;
import com.continuuity.app.services.ProgramId;
import com.continuuity.app.store.StoreFactory;
import com.continuuity.test.internal.DefaultId;
import com.continuuity.test.internal.TestHelper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import org.apache.twill.filesystem.LocationFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DeployRunStopTest {

  private static AppFabricService.Iface server;
  private static LocationFactory lf;
  private static StoreFactory sFactory;
  private static AtomicInteger instanceCount = new AtomicInteger(0);
  private static AtomicInteger messageCount = new AtomicInteger(0);
  private static Semaphore messageSemaphore = new Semaphore(0);

  /**
   *
   */
  public static final class GenSinkApp implements Application {

    @Override
    public ApplicationSpecification configure() {
      return ApplicationSpecification.Builder.with()
        .setName("GenSinkApp")
        .setDescription("GenSinkApp desc")
        .noStream()
        .noDataSet()
        .withFlows().add(new GenSinkFlow())
        .noProcedure()
        .noMapReduce()
        .noWorkflow()
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
     *
     */
    public static final class GenFlowlet extends AbstractFlowlet {

      private OutputEmitter<String> output;
      private int i;

      @Tick(delay = 1L, unit = TimeUnit.NANOSECONDS)
      public void generate() throws Exception {
        if (i < 10000) {
          output.emit("Testing " + ++i);
          if (i == 1000) {
            throw new IllegalStateException("1000 hitted");
          }
        }
      }
    }

    /**
     *
     */
    public static final class SinkFlowlet extends AbstractFlowlet {

      private static final Logger LOG = LoggerFactory.getLogger(SinkFlowlet.class);

      @Override
      public void initialize(FlowletContext context) throws FlowletException {
        instanceCount.incrementAndGet();
      }

      public void process(String text) {
        if (messageCount.incrementAndGet() == 5000) {
          messageSemaphore.release();
        } else if (messageCount.get() == 9999) {
          messageSemaphore.release();
        }
      }
    }
  }

  @Test @Ignore
  public void testDeployRunStop() throws Exception {
    TestHelper.deployApplication(GenSinkApp.class);

    AuthToken token = new AuthToken("12345");
    ProgramId flowIdentifier = new ProgramId(DefaultId.ACCOUNT.getId(), "GenSinkApp", "GenSinkFlow");
    server.start(token, new ProgramDescriptor(flowIdentifier, ImmutableMap.<String, String>of()));

    messageSemaphore.tryAcquire(5, TimeUnit.SECONDS);

    server.setFlowletInstances(token, flowIdentifier, "SinkFlowlet", (short) 3);

    messageSemaphore.tryAcquire(5, TimeUnit.SECONDS);

    // TODO: The flow test need to reform later using the new test framework.
    // Sleep one extra seconds to consume any unconsumed items (there shouldn't be, but this is for catching error).
    TimeUnit.SECONDS.sleep(1);

    server.stop(token, flowIdentifier);

    Assert.assertEquals(9999, messageCount.get());
    Assert.assertEquals(3, instanceCount.get());
  }

  @BeforeClass
  public static void before() throws Exception {
    final Injector injector = TestHelper.getInjector();

    server = injector.getInstance(AppFabricService.Iface.class);

    // Create location factory.
    lf = injector.getInstance(LocationFactory.class);

    // Create store
    sFactory = injector.getInstance(StoreFactory.class);
  }
}
