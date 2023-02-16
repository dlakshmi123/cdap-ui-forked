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
package com.continuuity.data.runtime;

import com.continuuity.data.DataSetAccessor;
import com.continuuity.data.LocalDataSetAccessor;
import com.continuuity.data.stream.InMemoryStreamCoordinator;
import com.continuuity.data.stream.StreamCoordinator;
import com.continuuity.data.stream.StreamFileWriterFactory;
import com.continuuity.data2.dataset.lib.table.leveldb.LevelDBOcTableService;
import com.continuuity.data2.queue.QueueClientFactory;
import com.continuuity.data2.transaction.queue.QueueAdmin;
import com.continuuity.data2.transaction.queue.leveldb.LevelDBQueueAdmin;
import com.continuuity.data2.transaction.queue.leveldb.LevelDBQueueClientFactory;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.continuuity.data2.transaction.stream.StreamConsumerFactory;
import com.continuuity.data2.transaction.stream.StreamConsumerStateStoreFactory;
import com.continuuity.data2.transaction.stream.leveldb.LevelDBStreamConsumerStateStoreFactory;
import com.continuuity.data2.transaction.stream.leveldb.LevelDBStreamFileAdmin;
import com.continuuity.data2.transaction.stream.leveldb.LevelDBStreamFileConsumerFactory;
import com.continuuity.metadata.MetaDataTable;
import com.continuuity.metadata.SerializingMetaDataTable;
import com.continuuity.tephra.runtime.TransactionModules;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * DataFabricLocalModule defines the Local/HyperSQL bindings for the data fabric.
 */
public class DataFabricLevelDBModule extends AbstractModule {

  @Override
  public void configure() {

    // bind meta data store
    bind(MetaDataTable.class).to(SerializingMetaDataTable.class).in(Singleton.class);

    bind(LevelDBOcTableService.class).toInstance(LevelDBOcTableService.getInstance());

    bind(DataSetAccessor.class).to(LocalDataSetAccessor.class).in(Singleton.class);
    bind(QueueClientFactory.class).to(LevelDBQueueClientFactory.class).in(Singleton.class);
    bind(QueueAdmin.class).to(LevelDBQueueAdmin.class).in(Singleton.class);

    // Stream bindings.
    bind(StreamCoordinator.class).to(InMemoryStreamCoordinator.class).in(Singleton.class);

    bind(StreamConsumerStateStoreFactory.class).to(LevelDBStreamConsumerStateStoreFactory.class).in(Singleton.class);
    bind(StreamAdmin.class).to(LevelDBStreamFileAdmin.class).in(Singleton.class);
    bind(StreamConsumerFactory.class).to(LevelDBStreamFileConsumerFactory.class).in(Singleton.class);
    bind(StreamFileWriterFactory.class).to(LocationStreamFileWriterFactory.class).in(Singleton.class);

    // bind transactions
    install(new TransactionModules().getInMemoryModules());
  }
}
