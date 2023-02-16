package com.continuuity.data2.dataset2.lib.table.hbase;

import com.continuuity.api.common.Bytes;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.hbase.HBaseTestBase;
import com.continuuity.data.hbase.HBaseTestFactory;
import com.continuuity.data2.dataset2.lib.table.BufferingOrederedTableTest;
import com.continuuity.data2.dataset2.lib.table.ConflictDetection;
import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.inmemory.DetachedTxSystemClient;
import com.continuuity.data2.util.hbase.HBaseTableUtil;
import com.continuuity.data2.util.hbase.HBaseTableUtilFactory;
import com.continuuity.internal.data.dataset.DatasetInstanceProperties;
import com.continuuity.internal.data.dataset.DatasetInstanceSpec;
import org.apache.twill.filesystem.LocalLocationFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class HBaseOrderedTableTest extends BufferingOrederedTableTest<HBaseOrderedTable> {
  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  private static HBaseTestBase testHBase;
  private static HBaseTableUtil hBaseTableUtil = new HBaseTableUtilFactory().get();

  @BeforeClass
  public static void beforeClass() throws Exception {
    testHBase = new HBaseTestFactory().get();
    testHBase.startHBase();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    testHBase.stopHBase();
  }

  @Override
  protected HBaseOrderedTable getTable(String name, ConflictDetection conflictLevel) throws Exception {
    // ttl=-1 means "keep data forever"
    return new HBaseOrderedTable(name, testHBase.getConfiguration(), conflictLevel, -1);
  }

  @Override
  protected HBaseOrderedTableAdmin getTableAdmin(String name) throws Exception {
    return getAdmin(name, DatasetInstanceProperties.EMPTY);
  }

  private HBaseOrderedTableAdmin getAdmin(String name, DatasetInstanceProperties props) throws IOException {
    DatasetInstanceSpec spec = new HBaseOrderedTableDefinition("foo").configure(name, props);
    return new HBaseOrderedTableAdmin(spec, testHBase.getConfiguration(), hBaseTableUtil,
                                      CConfiguration.create(), new LocalLocationFactory(tmpFolder.newFolder()));
  }

  @Test
  public void testTTL() throws Exception {
    // for the purpose of this test it is fine not to configure ttl when creating table: we want to see if it
    // applies on reading
    int ttl = 1000;
    DatasetInstanceProperties props =
      new DatasetInstanceProperties.Builder().property("ttl", String.valueOf(ttl)).build();
    getAdmin("ttl", props).create();
    HBaseOrderedTable table = new HBaseOrderedTable("ttl", testHBase.getConfiguration(), ConflictDetection.ROW, ttl);

    DetachedTxSystemClient txSystemClient = new DetachedTxSystemClient();
    Transaction tx = txSystemClient.startShort();
    table.startTx(tx);
    table.put(b("row1"), b("col1"), b("val1"));
    table.commitTx();

    TimeUnit.SECONDS.sleep(2);

    tx = txSystemClient.startShort();
    table.startTx(tx);
    table.put(b("row2"), b("col2"), b("val2"));
    table.commitTx();

    // now, we should not see first as it should have expired, but see the last one
    tx = txSystemClient.startShort();
    table.startTx(tx);
    Assert.assertNull(table.get(b("row1"), b("col1")));
    Assert.assertArrayEquals(b("val2"), table.get(b("row2"), b("col2")));

    // if ttl is 30 sec, it should see both
    table = new HBaseOrderedTable("ttl", testHBase.getConfiguration(), ConflictDetection.ROW, ttl * 30);
    tx = txSystemClient.startShort();
    table.startTx(tx);
    Assert.assertArrayEquals(b("val1"), table.get(b("row1"), b("col1")));
    Assert.assertArrayEquals(b("val2"), table.get(b("row2"), b("col2")));

    // if ttl is -1 (unlimited), it should see both
    table = new HBaseOrderedTable("ttl", testHBase.getConfiguration(), ConflictDetection.ROW, -1);
    tx = txSystemClient.startShort();
    table.startTx(tx);
    Assert.assertArrayEquals(b("val1"), table.get(b("row1"), b("col1")));
    Assert.assertArrayEquals(b("val2"), table.get(b("row2"), b("col2")));
  }

  private static byte[] b(String s) {
    return Bytes.toBytes(s);
  }

}
