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

package com.continuuity.data2.transaction.queue.leveldb;

import com.continuuity.common.queue.QueueName;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.dataset.lib.table.leveldb.LevelDBOcTableService;
import com.continuuity.data2.transaction.queue.QueueConstants;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.continuuity.data2.transaction.stream.StreamConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * admin for streams in leveldb.
 */
@Singleton
public class LevelDBStreamAdmin extends LevelDBQueueAdmin implements StreamAdmin {

  private static final Logger LOG = LoggerFactory.getLogger(LevelDBStreamAdmin.class);

  @Inject
  public LevelDBStreamAdmin(DataSetAccessor dataSetAccessor, LevelDBOcTableService service) {
    super(dataSetAccessor, service, QueueConstants.QueueType.STREAM);
  }

  @Override
  public String getActualTableName(QueueName queueName) {
    if (queueName.isStream()) {
      // <reactor namespace>.system.stream.<account>.<stream name>
      return getTableNamePrefix() + "." + queueName.getFirstComponent();
    } else {
      throw new IllegalArgumentException("'" + queueName + "' is not a valid name for a stream.");
    }
  }

  @Override
  public boolean doDropTable(QueueName queueName) {
    // separate table for each stream, ok to drop
    return true;
  }

  @Override
  public boolean doTruncateTable(QueueName queueName) {
    // separate table for each stream, ok to truncate
    return true;
  }

  @Override
  public StreamConfig getConfig(String streamName) throws IOException {
    throw new UnsupportedOperationException("Not yet supported");
  }

  @Override
  public void updateConfig(StreamConfig config) throws IOException {
    throw new UnsupportedOperationException("Not yet supported");
  }

}
