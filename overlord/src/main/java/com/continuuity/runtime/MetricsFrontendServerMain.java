package com.continuuity.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.logging.guice.LoggingModules;
import com.continuuity.metrics2.frontend.MetricsFrontendServerInterface;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main server class for metrics frontend server.
 */
public class MetricsFrontendServerMain {
  private static final Logger Log =
    LoggerFactory.getLogger(MetricsFrontendServerMain.class);

  /**
   * Metrics frontend server object main.
   * @param args from command line.
   */
  public void doMain(String args[]) {
    try {
      // Load our configuration from our resource files
      CConfiguration configuration = CConfiguration.create();

      final Injector injector
        = Guice.createInjector(new MetricsModules().getDistributedModules(),
                               new LoggingModules().getDistributedModules(),
                               new DataFabricModules().getDistributedModules(),
                               new LocationRuntimeModule().getDistributedModules(),
                               new ConfigModule(configuration));
      final MetricsFrontendServerInterface server
        = injector.getInstance(MetricsFrontendServerInterface.class);
      server.start(args, configuration);
    } catch (Exception e) {
      Log.error("Metrics frontend server failed to start. Reason : {}",
                e.getMessage());
    }
  }

  /**
   * Metric frontend server main.
   */
  public static void main(String[] args) {
    MetricsFrontendServerMain serviceMain = new MetricsFrontendServerMain();
    serviceMain.doMain(args);
  }
}
