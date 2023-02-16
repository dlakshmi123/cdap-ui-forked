/*
 * Copyright © 2014 Cask Data, Inc.
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
package co.cask.cdap.data2.transaction.queue.inmemory;

import co.cask.cdap.common.guice.ConfigModule;
import co.cask.cdap.common.guice.DiscoveryRuntimeModule;
import co.cask.cdap.common.guice.LocationRuntimeModule;
import co.cask.cdap.data.runtime.DataFabricModules;
import co.cask.cdap.data.runtime.DataSetServiceModules;
import co.cask.cdap.data.runtime.DataSetsModules;
import co.cask.cdap.data.runtime.TransactionMetricsModule;
import co.cask.cdap.data2.datafabric.dataset.service.DatasetService;
import co.cask.cdap.data2.datafabric.dataset.service.executor.DatasetOpExecutor;
import co.cask.cdap.data2.queue.QueueClientFactory;
import co.cask.cdap.data2.transaction.queue.QueueAdmin;
import co.cask.cdap.data2.transaction.queue.QueueTest;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.tephra.TransactionExecutorFactory;
import co.cask.tephra.TransactionManager;
import co.cask.tephra.TransactionSystemClient;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.BeforeClass;

/**
 * In-memory queue tests.
 */
public class InMemoryQueueTest extends QueueTest {

  private static Injector injector;

  @BeforeClass
  public static void init() throws Exception {

    injector = Guice.createInjector(
      new ConfigModule(),
      new LocationRuntimeModule().getInMemoryModules(),
      new DiscoveryRuntimeModule().getInMemoryModules(),
      new DataFabricModules().getInMemoryModules(),
      new TransactionMetricsModule());
    // transaction manager is a "service" and must be started
    transactionManager = injector.getInstance(TransactionManager.class);
    transactionManager.startAndWait();
    txSystemClient = injector.getInstance(TransactionSystemClient.class);
    queueClientFactory = injector.getInstance(QueueClientFactory.class);
    queueAdmin = injector.getInstance(QueueAdmin.class);
    streamAdmin = injector.getInstance(StreamAdmin.class);
    executorFactory = injector.getInstance(TransactionExecutorFactory.class);
  }
}
