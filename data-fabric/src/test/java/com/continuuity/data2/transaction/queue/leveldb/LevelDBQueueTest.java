/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data2.transaction.queue.leveldb;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data.runtime.DataFabricLevelDBModule;
import com.continuuity.data2.dataset.lib.table.leveldb.LevelDBOcTableService;
import com.continuuity.data2.queue.QueueClientFactory;
import com.continuuity.data2.transaction.TransactionExecutorFactory;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.queue.QueueAdmin;
import com.continuuity.data2.transaction.queue.QueueTest;
import com.continuuity.data2.transaction.queue.StreamAdmin;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.BeforeClass;

/**
 * LevelDB queue tests.
 */
public class LevelDBQueueTest extends QueueTest {

  @BeforeClass
  public static void init() throws Exception {
    CConfiguration conf = CConfiguration.create();
    conf.unset(Constants.CFG_DATA_LEVELDB_DIR);
    Injector injector = Guice.createInjector(new DataFabricLevelDBModule(conf));
    // transaction manager is a "service" and must be started
    transactionManager = injector.getInstance(InMemoryTransactionManager.class);
    transactionManager.startAndWait();
    txSystemClient = injector.getInstance(TransactionSystemClient.class);
    queueClientFactory = injector.getInstance(QueueClientFactory.class);
    queueAdmin = injector.getInstance(QueueAdmin.class);
    streamAdmin = injector.getInstance(StreamAdmin.class);
    executorFactory = injector.getInstance(TransactionExecutorFactory.class);
    LevelDBOcTableService.getInstance().clearTables();
  }
}
