package com.continuuity.performance.tx;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.runtime.DataFabricDistributedModule;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

/**
 * TxProvider for benchmarks that use HBase.
 */
public class HBaseTxProvider extends TxProvider {

  String zkQuorum = null;

  @Override
  public void configure(CConfiguration config) {
    zkQuorum = config.get("zk");
  }

  @Override
  public TransactionSystemClient create() {
    Configuration hbaseConf = HBaseConfiguration.create();
    if (zkQuorum != null) {
      hbaseConf.set(HConstants.ZOOKEEPER_QUORUM, zkQuorum);
    }
    hbaseConf.set("hbase.defaults.for.version.skip", "true");
    Module module = new DataFabricDistributedModule(hbaseConf);
    Injector injector = Guice.createInjector(module);
    return injector.getInstance(Key.get(TransactionSystemClient.class));
  }

}
