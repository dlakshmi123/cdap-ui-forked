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
package com.continuuity.data2.transaction.stream.leveldb;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.stream.StreamCoordinator;
import com.continuuity.data2.transaction.queue.leveldb.LevelDBStreamAdmin;
import com.continuuity.data2.transaction.stream.AbstractStreamFileAdmin;
import com.continuuity.data2.transaction.stream.StreamConsumerStateStoreFactory;
import com.google.inject.Inject;
import org.apache.twill.filesystem.LocationFactory;

/**
 * A file based {@link com.continuuity.data2.transaction.stream.StreamAdmin} that uses LevelDB for maintaining
 * consumer states information.
 */
public final class LevelDBStreamFileAdmin extends AbstractStreamFileAdmin {

  @Inject
  LevelDBStreamFileAdmin(LocationFactory locationFactory, CConfiguration cConf, StreamCoordinator streamCoordinator,
                         StreamConsumerStateStoreFactory stateStoreFactory, LevelDBStreamAdmin oldStreamAdmin) {
    super(locationFactory, cConf, streamCoordinator, stateStoreFactory, oldStreamAdmin);
  }
}
