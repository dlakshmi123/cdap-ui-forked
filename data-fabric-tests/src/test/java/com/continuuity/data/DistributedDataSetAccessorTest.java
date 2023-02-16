package com.continuuity.data;

import com.continuuity.data.hbase.HBaseTestBase;
import com.continuuity.data.hbase.HBaseTestFactory;
import com.continuuity.data2.dataset.api.DataSetClient;
import com.continuuity.data2.dataset.lib.table.OrderedColumnarTable;
import com.continuuity.data2.dataset.lib.table.hbase.HBaseOcTableClient;
import com.continuuity.data2.util.hbase.HBaseTableUtil;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.weave.filesystem.HDFSLocationFactory;
import org.apache.hadoop.conf.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 */
public class DistributedDataSetAccessorTest extends NamespacingDataSetAccessorTest {
  private static DataSetAccessor dsAccessor;

  private static HBaseTestBase testHBase;

  @BeforeClass
  public static void beforeClass() throws Exception {
    NamespacingDataSetAccessorTest.beforeClass();
    testHBase = new HBaseTestFactory().get();
    testHBase.startHBase();
    Configuration hConf = testHBase.getConfiguration();
    HBaseTableUtil tableUtil = new HBaseTableUtilFactory().get();
    dsAccessor = new DistributedDataSetAccessor(conf, hConf, new HDFSLocationFactory(hConf), tableUtil);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    testHBase.stopHBase();
  }

  @Override
  protected DataSetAccessor getDataSetAccessor() {
    return dsAccessor;
  }

  @Override
  protected String getRawName(DataSetClient dsClient) {
    if (dsClient instanceof OrderedColumnarTable) {
      return ((HBaseOcTableClient) dsClient).getHBaseTableName();
    }

    throw new RuntimeException("Unknown DataSetClient type: " + dsClient.getClass());
  }
}
