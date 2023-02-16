/*
 * Copyright 2012-2014 Continuuity, Inc.
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
package com.continuuity.metrics.runtime;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data2.OperationException;
import com.continuuity.internal.migrate.TableMigrator;
import com.continuuity.metrics.MetricsConstants;
import com.continuuity.metrics.process.KafkaMetricsProcessorServiceFactory;
import com.continuuity.watchdog.election.MultiLeaderElection;
import com.continuuity.watchdog.election.PartitionChangeHandler;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import org.apache.twill.zookeeper.ZKClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Metrics processor service that processes events from Kafka.
 */
public final class KafkaMetricsProcessorService extends AbstractExecutionThreadService {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaMetricsProcessorService.class);

  private final MultiLeaderElection multiElection;
  private final TableMigrator tableMigrator;
  private final SettableFuture<?> completion;

  @Inject
  public KafkaMetricsProcessorService(CConfiguration conf, TableMigrator tableMigrator,
                                      ZKClientService zkClientService,
                                      KafkaMetricsProcessorServiceFactory kafkaMetricsProcessorServiceFactory) {
    this.tableMigrator = tableMigrator;
    int partitionSize = conf.getInt(MetricsConstants.ConfigKeys.KAFKA_PARTITION_SIZE,
                                    MetricsConstants.DEFAULT_KAFKA_PARTITION_SIZE);
    multiElection = new MultiLeaderElection(
      zkClientService, "metrics-processor", partitionSize,
      createPartitionChangeHandler(kafkaMetricsProcessorServiceFactory));
    this.completion = SettableFuture.create();
  }

  @Override
  protected Executor executor() {
    return new Executor() {
      @Override
      public void execute(Runnable command) {
        Thread t = new Thread(command, getServiceName());
        t.setDaemon(true);
        t.start();
      }
    };
  }

  @Override
  protected void startUp() throws Exception {
    try {
      LOG.info("Starting Metrics Processor ...");
      tableMigrator.migrateIfRequired();
    } catch (OperationException e) {
      LOG.error("Error while checking for the necessity of, or execution of, a metrics table update", e);
      Throwables.propagate(e);
    }
    multiElection.start();
  }

  @Override
  protected void run() throws Exception {
    completion.get();
  }

  @Override
  protected void triggerShutdown() {
    completion.set(null);
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.info("Stopping Metrics Processor ...");
    try {
      multiElection.stop();
    } catch (Exception e) {
      LOG.error("Exception while shutting down.", e);
      throw Throwables.propagate(e);
    }
  }

  private PartitionChangeHandler createPartitionChangeHandler(final KafkaMetricsProcessorServiceFactory factory) {
    return new PartitionChangeHandler() {

      private com.continuuity.metrics.process.KafkaMetricsProcessorService service;

      @Override
      public void partitionsChanged(Set<Integer> partitions) {
        LOG.info("Metrics Kafka partition changed {}", partitions);
        try {
          if (service != null) {
            service.stopAndWait();
          }
          if (partitions.isEmpty() || !multiElection.isRunning()) {
            service = null;
          } else {
            service = factory.create(partitions);
            service.startAndWait();
          }
        } catch (Throwable t) {
          LOG.error("Failed to change Kafka partition.", t);
          completion.setException(t);
        }
      }
    };
  }
}
