package com.continuuity.internal.app.services;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.FlowletContext;
import com.continuuity.api.flow.flowlet.FlowletException;
import com.continuuity.api.flow.flowlet.GeneratorFlowlet;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import com.continuuity.app.services.AppFabricService;
import com.continuuity.app.services.AuthToken;
import com.continuuity.app.services.FlowDescriptor;
import com.continuuity.app.services.FlowIdentifier;
import com.continuuity.app.store.StoreFactory;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.test.app.DefaultId;
import com.continuuity.test.app.TestHelper;
import com.continuuity.test.app.guice.AppFabricTestModule;
import com.continuuity.weave.filesystem.LocationFactory;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
        .build();
    }


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

    public static final class GenFlowlet extends AbstractFlowlet implements GeneratorFlowlet {

      private OutputEmitter<String> output;
      private int i;

      @Override
      public void generate() throws Exception {
        if (i < 10000) {
          output.emit("Testing " + ++i);
          if (i == 1000) {
            throw new IllegalStateException("1000 hitted");
          }
        }
      }
    }

    public static final class SinkFlowlet extends AbstractFlowlet {

      private static final Logger LOG = LoggerFactory.getLogger(SinkFlowlet.class);

      @Override
      public void initialize(FlowletContext context) throws FlowletException {
        instanceCount.incrementAndGet();
      }

      public void process(String text) throws InterruptedException {
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
    FlowIdentifier flowIdentifier = new FlowIdentifier(DefaultId.ACCOUNT.getId(), "GenSinkApp", "GenSinkFlow", 1);
    server.start(token, new FlowDescriptor(flowIdentifier, ImmutableMap.<String, String>of()));

    messageSemaphore.tryAcquire(5, TimeUnit.SECONDS);

    server.setInstances(token, flowIdentifier, "SinkFlowlet", (short) 3);

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
    CConfiguration configuration = CConfiguration.create();
    configuration.set(Constants.CFG_APP_FABRIC_OUTPUT_DIR, System.getProperty("java.io.tmpdir") + "/app");
    configuration.set(Constants.CFG_APP_FABRIC_TEMP_DIR, System.getProperty("java.io.tmpdir") + "/temp");

    final Injector injector = Guice.createInjector(new AppFabricTestModule(configuration));

    server = injector.getInstance(AppFabricService.Iface.class);

    // Create location factory.
    lf = injector.getInstance(com.continuuity.weave.filesystem.LocationFactory.class);

    // Create store
    sFactory = injector.getInstance(StoreFactory.class);
  }
}