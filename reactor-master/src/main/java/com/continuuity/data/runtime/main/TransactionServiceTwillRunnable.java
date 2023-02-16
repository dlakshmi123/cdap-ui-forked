package com.continuuity.data.runtime.main;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.common.guice.KafkaClientModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.common.guice.ZKClientModule;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.twill.AbstractReactorTwillRunnable;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data.runtime.HDFSTransactionStateStorageProvider;
import com.continuuity.data.runtime.InMemoryTransactionManagerProvider;
import com.continuuity.data2.transaction.distributed.TransactionService;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.persist.HDFSTransactionStateStorage;
import com.continuuity.logging.guice.LoggingModules;
import com.continuuity.metrics.guice.MetricsClientRuntimeModule;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.apache.hadoop.conf.Configuration;
import org.apache.twill.api.TwillContext;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TwillRunnable to run Transaction Service through twill.
 */
public class TransactionServiceTwillRunnable extends AbstractReactorTwillRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceTwillRunnable.class);

  private ZKClientService zkClient;
  private KafkaClientService kafkaClient;
  private MetricsCollectionService metricsCollectionService;
  private TransactionService txService;

  public TransactionServiceTwillRunnable(String name, String cConfName, String hConfName) {
    super(name, cConfName, hConfName);
  }

  @Override
  protected void doInit(TwillContext context) {
    LOG.info("Initializing runnable {}", name);
    try {
      // Set the hostname of the machine so that cConf can be used to start internal services
      LOG.info("{} Setting host name to {}", name, context.getHost().getCanonicalHostName());
      getCConfiguration().set(Constants.Transaction.Container.ADDRESS, context.getHost().getCanonicalHostName());

      Injector injector = createGuiceInjector(getCConfiguration(), getConfiguration());

      //Get Zookeeper and Kafka Client Instances
      zkClient = injector.getInstance(ZKClientService.class);
      kafkaClient = injector.getInstance(KafkaClientService.class);

      // Get the metrics collection service
      metricsCollectionService = injector.getInstance(MetricsCollectionService.class);

      // Get the Transaction Service
      txService = injector.getInstance(TransactionService.class);

      LOG.info("Runnable initialized {}", name);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public void getServices(List<? super Service> services) {
    services.add(zkClient);
    services.add(kafkaClient);
    services.add(metricsCollectionService);
    services.add(txService);
  }

  static Injector createGuiceInjector(CConfiguration cConf, Configuration hConf) {
    return Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new IOModule(),
      new ZKClientModule(),
      new KafkaClientModule(),
      createDataFabricModule(cConf, hConf),
      new LocationRuntimeModule().getDistributedModules(),
      new DiscoveryRuntimeModule().getDistributedModules(),
      new MetricsClientRuntimeModule().getDistributedModules(),
      new LoggingModules().getDistributedModules()
    );
  }

  private static Module createDataFabricModule(CConfiguration cConf, Configuration hConf) {
    return Modules.override(new DataFabricModules(cConf, hConf).getDistributedModules()).with(new AbstractModule() {
      @Override
      protected void configure() {
        // Bind to provider that create new instances of storage and tx manager every time.
        bind(HDFSTransactionStateStorage.class).toProvider(HDFSTransactionStateStorageProvider.class);
        bind(InMemoryTransactionManager.class).toProvider(InMemoryTransactionManagerProvider.class);
      }
    });
  }
}
