/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.logging.run;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.conf.KafkaConstants;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data.DistributedDataSetAccessor;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.distributed.TransactionServiceClient;
import com.continuuity.internal.kafka.client.ZKKafkaClientService;
import com.continuuity.kafka.client.KafkaClientService;
import com.continuuity.logging.LoggingConfiguration;
import com.continuuity.logging.save.LogSaver;
import com.continuuity.watchdog.election.MultiLeaderElection;
import com.continuuity.weave.api.AbstractWeaveRunnable;
import com.continuuity.weave.api.WeaveContext;
import com.continuuity.weave.api.WeaveRunnableSpecification;
import com.continuuity.weave.common.Services;
import com.continuuity.weave.zookeeper.RetryStrategies;
import com.continuuity.weave.zookeeper.ZKClientService;
import com.continuuity.weave.zookeeper.ZKClientServices;
import com.continuuity.weave.zookeeper.ZKClients;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Weave wrapper for running LogSaver through Weave.
 */
public final class LogSaverWeaveRunnable extends AbstractWeaveRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(LogSaverWeaveRunnable.class);

  private LogSaver logSaver;
  private CountDownLatch runLatch;

  private String name;
  private String hConfName;
  private String cConfName;
  private ZKClientService zkClientService;
  private KafkaClientService kafkaClientService;
  private MultiLeaderElection multiElection;

  public LogSaverWeaveRunnable(String name, String hConfName, String cConfName) {
    this.name = name;
    this.hConfName = hConfName;
    this.cConfName = cConfName;
  }

  @Override
  public WeaveRunnableSpecification configure() {
    return WeaveRunnableSpecification.Builder.with()
      .setName(name)
      .withConfigs(ImmutableMap.of(
        "hConf", hConfName,
        "cConf", cConfName
      ))
      .build();
  }

  @Override
  public void initialize(WeaveContext context) {
    super.initialize(context);

    runLatch = new CountDownLatch(1);
    name = context.getSpecification().getName();
    Map<String, String> configs = context.getSpecification().getConfigs();

    LOG.info("Initialize runnable: " + name);
    try {
      // Load configuration
      Configuration hConf = new Configuration();
      hConf.clear();
      hConf.addResource(new File(configs.get("hConf")).toURI().toURL());

      CConfiguration cConf = CConfiguration.create();
      cConf.clear();
      cConf.addResource(new File(configs.get("cConf")).toURI().toURL());
      String baseDir = cConf.get(LoggingConfiguration.LOG_BASE_DIR);
      if (baseDir != null) {
        if (baseDir.startsWith("/")) {
          baseDir = baseDir.substring(1);
        }
        cConf.set(LoggingConfiguration.LOG_BASE_DIR, cConf.get(Constants.CFG_HDFS_NAMESPACE) + "/" + baseDir);
      }

      DataSetAccessor dataSetAccessor = new DistributedDataSetAccessor(cConf, hConf);
      TransactionSystemClient txClient = new TransactionServiceClient(cConf);

      // Initialize ZK client
      String zookeeper = cConf.get(Constants.CFG_ZOOKEEPER_ENSEMBLE);
      if (zookeeper == null) {
        LOG.error("No zookeeper quorum provided.");
        throw new IllegalStateException("No zookeeper quorum provided.");
      }

      zkClientService =
        ZKClientServices.delegate(
          ZKClients.reWatchOnExpire(
            ZKClients.retryOnFailure(
              ZKClientService.Builder.of(zookeeper).build(),
              RetryStrategies.exponentialDelay(500, 2000, TimeUnit.MILLISECONDS)
            )
          ));

      // Initialize Kafka client
      String kafkaZKNamespace = cConf.get(KafkaConstants.ConfigKeys.ZOOKEEPER_NAMESPACE_CONFIG);
      kafkaClientService = new ZKKafkaClientService(
        kafkaZKNamespace == null
          ? zkClientService
          : ZKClients.namespace(zkClientService, "/" + kafkaZKNamespace)
      );


      logSaver = new LogSaver(dataSetAccessor, txClient, kafkaClientService, hConf, cConf);

      int numPartitions = Integer.parseInt(cConf.get(LoggingConfiguration.NUM_PARTITIONS,
                                                     LoggingConfiguration.DEFAULT_NUM_PARTITIONS));
      LOG.info("Num partitions = {}", numPartitions);
      multiElection = new MultiLeaderElection(zkClientService, "log-saver", numPartitions, logSaver);

      LOG.info("Runnable initialized: " + name);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public void run() {
    LOG.info("Starting runnable " + name);

    // Note: logSaver has to start before leader election starts, and stop before leader election stops
    Futures.getUnchecked(Services.chainStart(zkClientService, kafkaClientService, logSaver));
    // Start leader election only after logSaver is started.
    multiElection.startAndWait();

    LOG.info("Runnable started " + name);

    try {
      runLatch.await();

      LOG.info("Runnable stopped " + name);
    } catch (InterruptedException e) {
      LOG.error("Waiting on latch interrupted");
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void stop() {
    LOG.info("Stopping runnable " + name);

    // Note: logSaver has to start before leader election starts, and stop before leader election stops
    Futures.getUnchecked(Services.chainStart(logSaver, multiElection, kafkaClientService, zkClientService));
    runLatch.countDown();
  }
}
