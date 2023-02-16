package com.continuuity.api.data.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.StatusCode;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.data.batch.SplitReader;
import com.continuuity.data.dataset.DataSetTestBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.SortedSet;

public class KeyValueTableTest extends DataSetTestBase {

  static KeyValueTable kvTable;

  static final byte[] key1 = Bytes.toBytes("key1");
  static final byte[] key2 = Bytes.toBytes("key2");
  static final byte[] key3 = Bytes.toBytes("key3");
  static final byte[] val1 = Bytes.toBytes("val1");
  static final byte[] val2 = Bytes.toBytes("val2");
  static final byte[] val3 = Bytes.toBytes("val3");

  @BeforeClass
  public static void configure() throws Exception {
    DataSet kv = new KeyValueTable("test");
    DataSet t1 = new KeyValueTable("t1");
    DataSet t2 = new KeyValueTable("t2");
    DataSet tBatch = new KeyValueTable("tBatch");
    setupInstantiator(Lists.newArrayList(kv, t1, t2, tBatch));
    kvTable = instantiator.getDataSet("test");
  }

  @Test
  public void testSyncWriteReadSwapDelete() throws OperationException {

    // this test runs all operations synchronously
    newTransaction(Mode.Sync);

    // write a value and read it back
    kvTable.write(key1, val1);
    Assert.assertArrayEquals(val1, kvTable.read(key1));

    // update the value and read it back
    kvTable.write(key1, val2);
    Assert.assertArrayEquals(val2, kvTable.read(key1));

    // attempt to swap, expecting old value
    try {
      kvTable.swap(key1, val1, val3);
      Assert.fail("swap should have failed");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.WRITE_CONFLICT, e.getStatus());
      Assert.assertArrayEquals(val2, kvTable.read(key1));
    }

    // swap the value and read it back
    kvTable.swap(key1, val2, val3);
    Assert.assertArrayEquals(val3, kvTable.read(key1));

    // delete the value and verify its gone
    kvTable.delete(key1);
    Assert.assertNull(kvTable.read(key1));
  }

  @Test
  public void testASyncWriteReadSwapDelete() throws OperationException {

    // defer all writes until commit
    newTransaction(Mode.Batch);
    // write a value
    kvTable.write(key2, val1);
    // value should not be visible yet
    Assert.assertNull(kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // verify synchronously
    newTransaction(Mode.Sync);
    // verify that the value is now visible
    Assert.assertArrayEquals(val1, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // defer all writes until commit
    newTransaction(Mode.Batch);
    // update the value
    kvTable.write(key2, val2);
    // value should not be visible yet
    Assert.assertArrayEquals(val1, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // verify synchronously
    newTransaction(Mode.Sync);
    // verify that the value is now visible
    Assert.assertArrayEquals(val2, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // defer all writes until commit
    newTransaction(Mode.Batch);
    // write a swap, this should not fail yet
    kvTable.swap(key2, val1, val3);
    // verify that the old value is still there
    Assert.assertArrayEquals(val2, kvTable.read(key2));
    // attempt to commit the transaction, should fail
    try {
      commitTransaction();
      Assert.fail("swap should have failed");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.WRITE_CONFLICT, e.getStatus());
      Assert.assertArrayEquals(val2, kvTable.read(key2));
    }

    // defer all writes until commit
    newTransaction(Mode.Batch);
    // swap the value
    kvTable.swap(key2, val2, val3);
    // new value should not be visible yet
    Assert.assertArrayEquals(val2, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // verify synchronously
    newTransaction(Mode.Sync);
    // verify the value was swapped
    Assert.assertArrayEquals(val3, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // defer all writes until commit
    newTransaction(Mode.Batch);
    // delete the value
    kvTable.delete(key2);
    // value should still be visible
    Assert.assertArrayEquals(val3, kvTable.read(key2));
    // commit the transaction
    commitTransaction();

    // verify synchronously
    newTransaction(Mode.Sync);
    // verify it is gone now
    Assert.assertNull(kvTable.read(key2));
    // commit the transaction
    commitTransaction();
  }

  @Test
  public void testTransactionAcrossTables() throws Exception {
    KeyValueTable table1 = instantiator.getDataSet("t1");
    KeyValueTable table2 = instantiator.getDataSet("t2");

    // write a value to table1 and verify it
    newTransaction(Mode.Sync);
    table1.write(key1, val1);
    Assert.assertArrayEquals(val1, table1.read(key1));
    table2.write(key2, val2);
    Assert.assertArrayEquals(val2, table2.read(key2));
    commitTransaction();

    // start a new transaction
    newTransaction(Mode.Batch);
    // add a write for table 1 to the transaction
    table1.write(key1, val2);
    // verify that the write is not effective yet
    Assert.assertArrayEquals(val1, table1.read(key1));
    // submit a delete for table 2
    table2.delete(key2);
    // verify that the delete is not effective yet
    Assert.assertArrayEquals(val2, table2.read(key2));
    // add a swap for a third table that should fail
    kvTable.swap(key3, val1, val1);

    // attempt to commit the transaction, should fail
    try {
      commitTransaction();
      Assert.fail("swap should have failed");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.WRITE_CONFLICT, e.getStatus());
    }

    // verify synchronously that old value are still there
    newTransaction(Mode.Sync);
    Assert.assertArrayEquals(val1, table1.read(key1));
    Assert.assertArrayEquals(val2, table2.read(key2));
    commitTransaction();
  }

  @Test
  public void testBatchReads() throws OperationException, InterruptedException {
    KeyValueTable t = instantiator.getDataSet("tBatch");

    // start a transaction
    newTransaction(Mode.Smart);
    // write 1000 random values to the table and remember them in a set
    SortedSet<Long> keysWritten = Sets.newTreeSet();
    Random rand = new Random(451);
    for (int i = 0; i < 1000; i++) {
      long keyLong = rand.nextLong();
      byte[] key = Bytes.toBytes(keyLong);
      t.write(key, key);
      keysWritten.add(keyLong);
    }
    // commit transaction
    commitTransaction();

    // start a sync transaction
    newTransaction(Mode.Sync);
    // get the splits for the table
    List<Split> splits = t.getSplits();
    // read each split and verify the keys
    SortedSet<Long> keysToVerify = Sets.newTreeSet(keysWritten);
    verifySplits(t, splits, keysToVerify);

    // start a sync transaction
    newTransaction(Mode.Sync);
    // get specific number of splits for a subrange
    keysToVerify = Sets.newTreeSet(keysWritten.subSet(0x10000000L, 0x40000000L));
    splits = t.getSplits(5, Bytes.toBytes(0x10000000L), Bytes.toBytes(0x40000000L));
    Assert.assertTrue(splits.size() <= 5);
    // read each split and verify the keys
    verifySplits(t, splits, keysToVerify);
  }

  // helper to verify that the split readers for the given splits return exactly a set of keys
  private void verifySplits(KeyValueTable t, List<Split> splits, SortedSet<Long> keysToVerify)
    throws OperationException, InterruptedException {
    // read each split and verify the keys, remove all read keys from the set
    for (Split split : splits) {
      SplitReader<byte[], byte[]> reader = t.createSplitReader(split);
      reader.initialize(split);
      while (reader.nextKeyValue()) {
        byte[] key = reader.getCurrentKey();
        byte[] value = reader.getCurrentValue();
        // verify each row has the two columns written
        Assert.assertArrayEquals(key, value);
        Assert.assertTrue(keysToVerify.remove(Bytes.toLong(key)));
      }
    }
    // verify all keys have been read
    if (!keysToVerify.isEmpty()) {
      System.out.println("Remaining [" + keysToVerify.size() + "]: " + keysToVerify);
    }
    Assert.assertTrue(keysToVerify.isEmpty());
  }

}