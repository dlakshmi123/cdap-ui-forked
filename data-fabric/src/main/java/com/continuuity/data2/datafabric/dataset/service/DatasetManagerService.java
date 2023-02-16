package com.continuuity.data2.datafabric.dataset.service;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.datafabric.ReactorDatasetNamespace;
import com.continuuity.data2.datafabric.dataset.type.DatasetModuleConflictException;
import com.continuuity.data2.dataset2.manager.NamespacedDatasetManager;
import com.continuuity.http.NettyHttpService;
import com.continuuity.data2.datafabric.dataset.instance.DatasetInstanceManager;
import com.continuuity.data2.datafabric.dataset.type.DatasetTypeManager;
import com.continuuity.data2.dataset2.manager.DatasetManager;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.internal.data.dataset.module.DatasetModule;
import com.google.common.base.Throwables;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;
import org.apache.twill.filesystem.LocationFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

/**
 * DatasetManagerService implemented using the common http netty framework.
 */
public class DatasetManagerService extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(DatasetManagerService.class);

  private final NettyHttpService httpService;
  private final DiscoveryService discoveryService;
  private Cancellable cancelDiscovery;

  private final DatasetInstanceManager instanceManager;
  private final DatasetTypeManager typeManager;

  private final DatasetManager mdsDatasetManager;
  private final NavigableMap<String, Class<? extends DatasetModule>> defaultModules;

  @Inject
  public DatasetManagerService(CConfiguration cConf,
                               LocationFactory locationFactory,
                               @Named(Constants.Dataset.Manager.ADDRESS) InetAddress hostname,
                               DiscoveryService discoveryService,
                               @Named("datasetMDS") DatasetManager mdsDatasetManager,
                               @Named("defaultDatasetModules")
                               NavigableMap<String, Class<? extends DatasetModule>> defaultModules,
                               TransactionSystemClient txSystemClient
  ) throws Exception {

    NettyHttpService.Builder builder = NettyHttpService.builder();

    // todo: refactor once DataSetAccessor is removed.
    this.mdsDatasetManager =
      new NamespacedDatasetManager(mdsDatasetManager,
                                   new ReactorDatasetNamespace(cConf, DataSetAccessor.Namespace.SYSTEM));
    this.defaultModules = defaultModules;

    this.typeManager = new DatasetTypeManager(mdsDatasetManager, txSystemClient, locationFactory);
    this.instanceManager = new DatasetInstanceManager(mdsDatasetManager, txSystemClient);

    builder.addHttpHandlers(ImmutableList.of(new DatasetTypeHandler(typeManager, locationFactory, cConf),
                                             new DatasetInstanceHandler(typeManager, instanceManager)));

    builder.setHost(hostname.getCanonicalHostName());
    builder.setPort(cConf.getInt(Constants.Dataset.Manager.PORT, Constants.Dataset.Manager.DEFAULT_PORT));

    builder.setConnectionBacklog(cConf.getInt(Constants.Dataset.Manager.BACKLOG_CONNECTIONS,
                                              Constants.Dataset.Manager.DEFAULT_BACKLOG));
    builder.setExecThreadPoolSize(cConf.getInt(Constants.Dataset.Manager.EXEC_THREADS,
                                               Constants.Dataset.Manager.DEFAULT_EXEC_THREADS));
    builder.setBossThreadPoolSize(cConf.getInt(Constants.Dataset.Manager.BOSS_THREADS,
                                               Constants.Dataset.Manager.DEFAULT_BOSS_THREADS));
    builder.setWorkerThreadPoolSize(cConf.getInt(Constants.Dataset.Manager.WORKER_THREADS,
                                                 Constants.Dataset.Manager.DEFAULT_WORKER_THREADS));

    this.httpService = builder.build();
    this.discoveryService = discoveryService;
  }

  @Override
  protected void startUp() throws Exception {
    LOG.info("Starting DatasetManagerService...");

    // adding default modules to init dataset manager used by mds (directly)
    for (Map.Entry<String, Class<? extends DatasetModule>> module : defaultModules.entrySet()) {
      mdsDatasetManager.register(module.getKey(), module.getValue());
    }

    typeManager.startAndWait();
    instanceManager.startAndWait();

    httpService.startAndWait();

    // adding default modules to be available in dataset manager service
    for (Map.Entry<String, Class<? extends DatasetModule>> module : defaultModules.entrySet()) {
      try {
        // NOTE: we assume default modules are always in classpath, hence passing null for jar location
        typeManager.addModule(module.getKey(), module.getValue().getName(), null);
      } catch (DatasetModuleConflictException e) {
        // perfectly fine: we need to add default modules only the very first time service is started
        LOG.info("Not adding " + module.getKey() + " module: it already exists");
      } catch (Throwable th) {
        LOG.error("Failed to add {} module. Aborting.", module.getKey(), th);
        throw Throwables.propagate(th);
      }
    }

    // Register the service
    cancelDiscovery = discoveryService.register(new Discoverable() {
      @Override
      public String getName() {
        return Constants.Service.DATASET_MANAGER;
      }

      @Override
      public InetSocketAddress getSocketAddress() {
        return httpService.getBindAddress();
      }
    });

    LOG.info("DatasetManagerService started successfully on {}", httpService.getBindAddress());
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping DatasetManagerService...");

    typeManager.stopAndWait();
    instanceManager.stopAndWait();

    // Unregister the service
    cancelDiscovery.cancel();
    // Wait for a few seconds for requests to stop
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      LOG.error("Interrupted while waiting...", e);
    }

    httpService.stopAndWait();
  }
}
