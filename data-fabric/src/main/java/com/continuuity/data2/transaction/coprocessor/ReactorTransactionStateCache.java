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

package com.continuuity.data2.transaction.coprocessor;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data2.transaction.snapshot.SnapshotCodecV1;
import com.continuuity.data2.transaction.snapshot.SnapshotCodecV2;
import com.continuuity.data2.util.hbase.ConfigurationTable;
import com.continuuity.tephra.coprocessor.TransactionStateCache;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.io.IOException;

/**
 * Extends the {@link TransactionStateCache} implementation for
 * transaction coprocessors with a version that reads transaction configuration properties from
 * {@link ConfigurationTable}.  This allows the coprocessors to pick up configuration changes without requiring
 * a restart.
 */
public class ReactorTransactionStateCache extends TransactionStateCache {
  // Reactor versions of coprocessors must reference snapshot classes so they get included in generated jar file
  // DO NOT REMOVE
  private static final SnapshotCodecV1 codecV1 = null;
  private static final SnapshotCodecV2 codecV2 = null;

  private String tableNamespace;
  private ConfigurationTable configTable;

  public ReactorTransactionStateCache(String tableNamespace) {
    this.tableNamespace = tableNamespace;
  }

  @Override
  public void setConf(Configuration conf) {
    super.setConf(conf);
    this.configTable = new ConfigurationTable(conf);
  }

  protected Configuration getSnapshotConfiguration() throws IOException {
    CConfiguration cConf = configTable.read(ConfigurationTable.Type.DEFAULT, tableNamespace);
    Configuration txConf = HBaseConfiguration.create();
    cConf.copyTxProperties(txConf);
    return txConf;
  }
}
