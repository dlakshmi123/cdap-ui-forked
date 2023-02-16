/*
 * Copyright © 2015-2016 Cask Data, Inc.
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

package co.cask.cdap.data2.transaction.coprocessor.hbase10cdh;

import co.cask.cdap.data2.increment.hbase10cdh.IncrementTxFilter;
import co.cask.cdap.data2.transaction.coprocessor.DefaultTransactionStateCacheSupplier;
import co.cask.cdap.data2.util.hbase.HTable10CDHNameConverter;
import co.cask.tephra.Transaction;
import co.cask.tephra.coprocessor.TransactionStateCache;
import co.cask.tephra.hbase10cdh.coprocessor.CellSkipFilter;
import co.cask.tephra.hbase10cdh.coprocessor.TransactionProcessor;
import com.google.common.base.Supplier;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.regionserver.ScanType;

/**
 * Implementation of the {@link co.cask.tephra.hbase10cdh.coprocessor.TransactionProcessor}
 * coprocessor that uses {@link co.cask.cdap.data2.transaction.coprocessor.DefaultTransactionStateCache}
 * to automatically refresh transaction state.
 */
public class DefaultTransactionProcessor extends TransactionProcessor {
  @Override
  protected Supplier<TransactionStateCache> getTransactionStateCacheSupplier(RegionCoprocessorEnvironment env) {
    String sysConfigTablePrefix =
        new HTable10CDHNameConverter().getSysConfigTablePrefix(env.getRegion().getTableDesc());
    return new DefaultTransactionStateCacheSupplier(sysConfigTablePrefix, env.getConfiguration());
  }

  @Override
  protected Filter getTransactionFilter(Transaction tx, ScanType scanType, Filter filter) {
    IncrementTxFilter incrementTxFilter = new IncrementTxFilter(tx, ttlByFamily, allowEmptyValues, scanType, filter);
    return new CellSkipFilter(incrementTxFilter);
  }
}
