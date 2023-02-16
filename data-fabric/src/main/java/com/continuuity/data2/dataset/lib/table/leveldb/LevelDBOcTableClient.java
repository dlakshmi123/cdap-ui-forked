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

package com.continuuity.data2.dataset.lib.table.leveldb;

import com.continuuity.data.table.Scanner;
import com.continuuity.data2.dataset.lib.table.BackedByVersionedStoreOcTableClient;
import com.continuuity.data2.dataset.lib.table.ConflictDetection;
import com.continuuity.tephra.Transaction;

import java.io.IOException;
import java.util.NavigableMap;
import javax.annotation.Nullable;

/**
 * A table client based on LevelDB.
 */
public class LevelDBOcTableClient extends BackedByVersionedStoreOcTableClient {

  private final LevelDBOcTableCore core;
  private Transaction tx;
  private long persistedVersion;

  public LevelDBOcTableClient(String tableName, LevelDBOcTableService service) throws IOException {
    this(tableName, ConflictDetection.ROW, service);
  }

  public LevelDBOcTableClient(String tableName, ConflictDetection level, LevelDBOcTableService service)
    throws IOException {
    super(tableName, level);
    this.core = new LevelDBOcTableCore(tableName, service);
  }

  // TODO this is the same for all OcTableClient implementations -> promote to base class
  @Override
  public void startTx(Transaction tx) {
    super.startTx(tx);
    this.tx = tx;
  }

  @Override
  protected void persist(NavigableMap<byte[], NavigableMap<byte[], byte[]>> changes) throws Exception {
    persistedVersion = tx == null ? System.currentTimeMillis() : tx.getWritePointer();
    core.persist(changes, persistedVersion);
  }

  @Override
  protected void undo(NavigableMap<byte[], NavigableMap<byte[], byte[]>> persisted) throws Exception {
    core.undo(persisted, persistedVersion);
  }

  @Override
  protected NavigableMap<byte[], byte[]> getPersisted(byte[] row, @Nullable byte[][] columns) throws Exception {
    return core.getRow(row, columns, null, null, columns == null ? Integer.MAX_VALUE : columns.length, tx);
  }

  @Override
  protected NavigableMap<byte[], byte[]> getPersisted(byte[] row, byte[] startColumn, byte[] stopColumn, int limit)
    throws Exception {
    return core.getRow(row, null, startColumn, stopColumn, limit, tx);
  }

  @Override
  protected Scanner scanPersisted(byte[] startRow, byte[] stopRow) throws Exception {
    return core.scan(startRow, stopRow, null, null, tx);
  }
}
