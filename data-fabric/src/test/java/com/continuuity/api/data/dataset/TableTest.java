package com.continuuity.api.data.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.OperationResult;
import com.continuuity.api.data.batch.Split;
import com.continuuity.api.data.batch.SplitReader;
import com.continuuity.api.data.dataset.table.Delete;
import com.continuuity.api.data.dataset.table.Increment;
import com.continuuity.api.data.dataset.table.Read;
import com.continuuity.api.data.dataset.table.Row;
import com.continuuity.api.data.dataset.table.Scanner;
import com.continuuity.api.data.dataset.table.Swap;
import com.continuuity.api.data.dataset.table.Table;
import com.continuuity.api.data.dataset.table.Write;
import com.continuuity.data.dataset.DataSetTestBase;
import com.continuuity.data.operation.StatusCode;
import com.continuuity.data2.RuntimeTable;
import com.continuuity.data2.dataset.lib.table.BufferingOcTableClient;
import com.continuuity.data2.dataset.lib.table.ConflictDetection;
import com.continuuity.data2.transaction.TransactionContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;

/**
 * Data set table test.
 */
public class TableTest extends DataSetTestBase {

  static Table table;

  private static final byte[] key1 = Bytes.toBytes("KEY1");
  private static final byte[] key2 = Bytes.toBytes("KEY2");
  private static final byte[] key3 = Bytes.toBytes("KEY3");
  private static final byte[] key4 = Bytes.toBytes("key4");
  private static final byte[] col1 = Bytes.toBytes("col1");
  private static final byte[] col2 = Bytes.toBytes("col2");
  private static final byte[] col3 = Bytes.toBytes("col3");
  private static final byte[] val1 = Bytes.toBytes("VAL1");
  private static final byte[] val2 = Bytes.toBytes("VAL2");
  private static final byte[] val3 = Bytes.toBytes("VAL3");

  private static final byte[][] col12 = { col1, col2 };
  private static final byte[][] val12 = { val1, val2 };
  private static final byte[][] val13 = { val1, val3 };
  private static final byte[][] col23 = { col2, col3 };
  private static final byte[][] val22 = { val2, val2 };
  private static final byte[][] val23 = { val2, val3 };
  private static final byte[][] col123 = { col1, col2, col3 };
  private static final byte[][] val123 = { val1, val2, val3 };

  private static final byte[] c = { 'c' }, v = { 'v' };

  @BeforeClass
  public static void configure() throws Exception {
    DataSet kv = new Table("test");
    DataSet t1 = new Table("t1");
    DataSet t2 = new Table("t2");
    DataSet t3 = new Table("t3");
    DataSet t4 = new Table("t4");
    DataSet tBatch = new Table("tBatch");
    DataSet scanTable = new Table("scanTable");
    DataSet rowConflictTable = new Table("rowConflict", Table.ConflictDetection.ROW);
    DataSet columnConflictTable = new Table("columnConflict", Table.ConflictDetection.COLUMN);
    setupInstantiator(Lists.newArrayList(kv, t1, t2, t3, t4, tBatch, scanTable, rowConflictTable, columnConflictTable));
    table = instantiator.getDataSet("test");
  }

  public static void verifyColumns(OperationResult<Map<byte[], byte[]>> result,
                                   byte[][] columns, byte[][] expected) {
    Assert.assertEquals(columns.length, expected.length);
    Assert.assertFalse(result.isEmpty());
    Map<byte[], byte[]> colsMap = result.getValue();
    Assert.assertNotNull(colsMap);

    verify(columns, expected, colsMap);
  }

  private static void verify(byte[][] expectedCols, byte[][] expectedVals, Map<byte[], byte[]> toVerify) {
    Assert.assertEquals(expectedCols.length, toVerify.size());
    for (int i = 0; i < expectedCols.length; i++) {
      Assert.assertArrayEquals(expectedVals[i], toVerify.get(expectedCols[i]));
    }
  }

  public static void verifyColumn(OperationResult<Map<byte[], byte[]>> result,
                                  byte[] column, byte[] expected) {
    verifyColumns(result, new byte[][]{column}, new byte[][]{expected});
  }

  void verifyColumns(OperationResult<Map<byte[], byte[]>> result,
                    byte[][] columns, long[] expected) {
    byte[][] expectedBytes = new byte[expected.length][];
    for (int i = 0; i < expected.length; i++) {
      expectedBytes[i] = Bytes.toBytes(expected[i]);
    }
    verifyColumns(result, columns, expectedBytes);
  }

  public static void verifyColumn(OperationResult<Map<byte[], byte[]>> result,
                                  byte[] column, long expected) {
    verifyColumn(result, column, Bytes.toBytes(expected));
  }

  public static void verifyNull(OperationResult<Map<byte[], byte[]>> result,
                                byte[] column) {
    verifyNull(result, new byte[][] { column });
  }

  public static void verifyNull(OperationResult<Map<byte[], byte[]>> result,
                                byte[][] columns) {
    Assert.assertTrue(columns.length > 0);
    Assert.assertTrue(result.isEmpty());
  }

  private byte[][] makeArray(String prefix, Integer ... numbers) {
    byte[][] array = new byte[numbers.length][];
    int idx = 0;
    for (int i : numbers) {
      array[idx++] = (prefix + i).getBytes();
    }
    return array;
  }
  private byte[][] makeColumns(Integer ... numbers) {
    return makeArray("c", numbers);
  }
  private byte[][] makeValues(Integer ... numbers) {
    return makeArray("v", numbers);
  }

  // attempt to compare and swap, expect failure
  void attemptSwap(Table tab, Swap swap) {
    try {
      tab.write(swap);
      Assert.fail("swap should have failed");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.WRITE_CONFLICT, e.getStatus());
    }

  }

  @Test
  public void testSyncWriteReadSwapDelete() throws Exception {

    // this test runs all operations synchronously
    TransactionContext txContext = newTransaction();

    OperationResult<Map<byte[], byte[]>> result;

    // write a value and read it back
    table.write(new Write(key1, col1, val2));
    result = table.read(new Read(key1, col1));
    verifyColumn(result, col1, val2);

    // update the value, and add two new columns, and read them back
    table.write(new Write(key1, col123, val123));
    // read explicitly all three columns
    result = table.read(new Read(key1, col123));
    verifyColumns(result, col123, val123);
    // read the range of all columns (start = stop = null)
    result = table.read(new Read(key1, null, null));
    verifyColumns(result, col123, val123);
    // read the range up to (but excluding) col2_ -> col1, col2
    result = table.read(new Read(key1, null, Bytes.toBytes("col2_")));
    verifyColumns(result, col12, val12);
    // read the range from col1 up to (but excluding) col3 -> col1, col2
    result = table.read(new Read(key1, col1, col3));
    verifyColumns(result, col12, val12);
    // read the range from col2 up to (but excluding) col3_ -> col2, col3
    result = table.read(new Read(key1, col2, Bytes.toBytes("col3_")));
    verifyColumns(result, col23, val23);
    // read the range from col2 on -> col2, col3
    result = table.read(new Read(key1, col2, null));
    verifyColumns(result, col23, val23);
    // read all columns
    result = table.read(new Read(key1));
    verifyColumns(result, col123, val123);
    // read the first 2 columns
    result = table.read(new Read(key1, 2));
    verifyColumns(result, col12, val12);

    // delete one column, verify that it is gone
    table.write(new Delete(key1, col1));
    result = table.read(new Read(key1, null, null));
    verifyColumns(result, col23, val23);

    // attempt to compare and swap, should fail because col2==VAL3
    attemptSwap(table, new Swap(key1, col2, val3, val1));
    // attempt to compare and swap, should fail because col1 is deleted
    attemptSwap(table, new Swap(key1, col1, val3, val1));
    // attempt to compare with null and swap, should fail because col2 exists
    attemptSwap(table, new Swap(key1, col2, null, val1));

    // compare and swap one column with a new value
    table.write(new Swap(key1, col2, val2, val1));
    result = table.read(new Read(key1, null, null));
    verifyColumns(result, col23, val13);

    // compare and swap one column with null
    table.write(new Swap(key1, col2, val1, null));
    result = table.read(new Read(key1, col2));
    verifyNull(result, col2);

    // compare and swap a null column with a new value
    table.write(new Swap(key1, col2, null, val2));
    result = table.read(new Read(key1, col2));
    verifyColumn(result, col2, val2);

    // delete all columns
    table.write(new Delete(key1));
    result = table.read(new Read(key1, col2));
    Assert.assertTrue(result.isEmpty());
  }



  @Test
  public void testIncrement() throws Exception {

    // this test runs all operations synchronously
    TransactionContext txContext = newTransaction();

    OperationResult<Map<byte[], byte[]>> result;

    // verify that there is no value when we start
    result = table.read(new Read(key2, col1));
    verifyNull(result, col1);
    result = table.read(new Read(key2, col2));
    verifyNull(result, col1);

    // increment one value
    table.write(new Increment(key2, col1, 5L));
    result = table.read(new Read(key2, col1));
    verifyColumn(result, col1, 5L);

    // increment two values
    table.write(new Increment(key2, col12, new long[]{2L, 3L}));
    result = table.read(new Read(key2, col12));
    verifyColumns(result, col12, new long[] { 7L, 3L });

    // write a val < 8-bytes to a column, then try to increment it -> fail
    table.write(new Write(key2, col3, val3));
    try {
      table.write(new Increment(key2, col3, 1L));
      Assert.fail("increment of 'VAL3' should have failed (not a long)");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.ILLEGAL_INCREMENT, e.getStatus());
    }
  }

  @Test
  public void testWriteReadSwapDelete() throws Exception {

    Table table = instantiator.getDataSet("t3");
    OperationResult<Map<byte[], byte[]>> result;

    // defer writes until commit or a read is performed
    TransactionContext txContext = newTransaction();

    // write three columns of one row
    table.write(new Write(key3, col123, val123));
    // increment another column of another row
    table.write(new Increment(key4, col1, 1L));
    // verify they are visible in the transaction
    result = table.read(new Read(key3, null, null));
    verifyColumns(result, col123, val123);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 1L);

    // commit xaction
    commitTransaction(txContext);

    // verify all are there with sync reads
    txContext = newTransaction();
    result = table.read(new Read(key3, null, null));
    verifyColumns(result, col123, val123);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 1L);
    commitTransaction(txContext);

    // start a new transaction
    txContext = newTransaction();
    // increment same column again
    table.write(new Increment(key4, col1, 1L));
    // delete one column
    table.write(new Delete(key3, col3));
    // swap one of the other columns
    table.write(new Swap(key3, col1, val1, val2));
    // verify writes are visible in the transaction
    result = table.read(new Read(key3, null, null));
    verifyColumns(result, col12, val22);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 2L);

    // commit xaction
    commitTransaction(txContext);

    // verify all are there with sync reads
    txContext = newTransaction();
    result = table.read(new Read(key3, null, null));
    verifyColumns(result, col12, val22);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 2L);
    commitTransaction(txContext);

    // start a new transaction
    txContext = newTransaction();
    // increment same column again
    table.write(new Increment(key4, col1, 1L));
    // delete another column
    table.write(new Delete(key3, col2));
    // verify writes are visible in transaction
    result = table.read(new Read(key3, null, null));
    verifyColumn(result, col1, val2);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 3L);

    try {
      // swap the remaining column with wrong value
      table.write(new Swap(key3, col1, val1, val2));
      Assert.fail("transaction should have failed due to swap");
    } catch (OperationException e) {
      Assert.assertEquals(StatusCode.WRITE_CONFLICT, e.getStatus());
    }

    // verify none was committed with sync reads
    table = instantiator.getDataSet("t3");
    txContext = newTransaction();
    result = table.read(new Read(key3, null, null));
    verifyColumns(result, col12, val22);
    result = table.read(new Read(key4, null, null));
    verifyColumn(result, col1, 2L);
  }

  @Test
  public void testTransactionAcrossTables() throws Exception {
    Table table1 = instantiator.getDataSet("t1");
    Table table2 = instantiator.getDataSet("t2");

    OperationResult<Map<byte[], byte[]>> result;

    // initialize the table with sync operations
    TransactionContext txContext = newTransaction();

    // write a value to table1 and verify it
    table1.write(new Write(key1, col1, val1));
    result = table1.read(new Read(key1, col1));
    verifyColumn(result, col1, val1);
    // increment a column in table2 and verify it
    table2.write(new Increment(key2, col2, 5L));
    result = table2.read(new Read(key2, col2));
    verifyColumn(result, col2, 5L);
    // write a value to another row in table2 and verify it
    table2.write(new Write(key3, col3, val3));
    result = table2.read(new Read(key3, col3));
    verifyColumn(result, col3, val3);

    commitTransaction(txContext);

    // start a new transaction
    table1 = instantiator.getDataSet("t1");
    table2 = instantiator.getDataSet("t2");
    txContext = newTransaction();
    // add a write for table 1 to the transaction
    table1.write(new Write(key1, col1, val2));
    // add an increment for the same column in table 2
    table2.write(new Increment(key2, col2, 3L));
    // submit a delete for table 2
    table2.write(new Delete(key3, col3));
    // add a swap for a third table that should fail
    attemptSwap(table, new Swap(Bytes.toBytes("non-exist"), col1, val1, val1));

    // verify old value are still there, synchronously
    table1 = instantiator.getDataSet("t1");
    table2 = instantiator.getDataSet("t2");
    txContext = newTransaction();

    result = table1.read(new Read(key1, col1));
    verifyColumn(result, col1, val1);
    result = table2.read(new Read(key2, col2));
    verifyColumn(result, col2, 5L);
    result = table2.read(new Read(key3, col3));
    verifyColumn(result, col3, val3);
  }

  @Test
  public void testTableWithoutDelegateCantOperate() throws OperationException {
    Table t = new Table("xyz");
    try {
      t.read(new Read(key1, col1));
      Assert.fail("Read should throw an exception when called before runtime");
    } catch (IllegalStateException e) {
      // expected
    }
    try {
      t.write(new Write(key1, col1, val1));
      Assert.fail("Write should throw an exception when called before runtime");
    } catch (IllegalStateException e) {
      // expected
    }
    try {
      t.write(new Write(key1, col1, val1));
      Assert.fail("Stage should throw an exception when called before " +
          "runtime");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void testColumnRange() throws Exception {
    Table table = instantiator.getDataSet("t4");
    // start a transaction
    TransactionContext txContext = newTransaction();

    // write a row with 10 columns
    byte[][] allColumns = makeColumns(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    byte[][] allValues =  makeValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    table.write(new Write(key1, allColumns, allValues));

    // read a column range of all columns
    OperationResult<Map<byte[], byte[]>> result;
    result = table.read(new Read(key1, null, null));
    verifyColumns(result, allColumns, allValues);

    // read a column range from 7 to the end
    result = table.read(new Read(key1, allColumns[7], null));
    verifyColumns(result, makeColumns(7, 8, 9), makeValues(7, 8, 9));

    // read a column range from the beginning to exclusive) 2, that is inclusive 1
    result = table.read(new Read(key1, null, allColumns[2]));
    verifyColumns(result, makeColumns(0, 1), makeValues(0, 1));

    // read a column range with limit
    result = table.read(new Read(key1, allColumns[2], null, 4));
    verifyColumns(result, makeColumns(2, 3, 4, 5), makeValues(2, 3, 4, 5));

    // read a column range with limit that return less then limit
    result = table.read(new Read(key1, allColumns[8], null, 4));
    verifyColumns(result, makeColumns(8, 9), makeValues(8, 9));
  }

  @Test
  public void testBatchReads() throws Exception {
    Table t = instantiator.getDataSet("tBatch");

    // start a transaction
    TransactionContext txContext = newTransaction();
    // write 1000 random values to the table and remember them in a set
    SortedSet<Long> keysWritten = Sets.newTreeSet();
    Random rand = new Random(451);
    for (int i = 0; i < 1000; i++) {
      long keyLong = rand.nextLong();
      byte[] key = Bytes.toBytes(keyLong);
      t.write(new Write(key, new byte[][]{c, key}, new byte[][]{key, v}));
      keysWritten.add(keyLong);
    }
    // commit transaction
    commitTransaction(txContext);

    // start a sync transaction
    txContext = newTransaction();
    // get the splits for the table
    List<Split> splits = t.getSplits();
    // read each split and verify the keys
    SortedSet<Long> keysToVerify = Sets.newTreeSet(keysWritten);
    verifySplits(t, splits, keysToVerify);

    // start a sync transaction
    txContext = newTransaction();
    // get specific number of splits for a subrange
    keysToVerify = Sets.newTreeSet(keysWritten.subSet(0x10000000L, 0x40000000L));
    splits = t.getSplits(5, Bytes.toBytes(0x10000000L), Bytes.toBytes(0x40000000L));
    Assert.assertTrue(splits.size() <= 5);
    // read each split and verify the keys
    verifySplits(t, splits, keysToVerify);
  }

  @Test
  public void testScan() throws Exception {
    // NOTE: the test is minimal: we'll be changing table API
    Table table = instantiator.getDataSet("scanTable");

    // start a transaction
    TransactionContext txContext = newTransaction();

    Write[] writes = new Write[5];
    for (int i = 0; i < writes.length; i++) {
      Write write = new Write(Bytes.toBytes("row" + i),
                           Bytes.toBytes("column" + i),
                           Bytes.toBytes("val" + i));
      table.write(write);
      writes[i] = write;
    }

    // commit transaction
    commitTransaction(txContext);

    // start a transaction
    txContext = newTransaction();

    Scanner scan;
    // test bounded scan
    scan = table.scan(writes[1].getRow(), writes[3].getRow());
    verify(scan, Arrays.copyOfRange(writes, 1, 3));
    scan.close();

    // test scan with open start
    scan = table.scan(null, writes[4].getRow());
    verify(scan, Arrays.copyOfRange(writes, 0, 4));
    scan.close();

    // test scan with open end
    scan = table.scan(writes[3].getRow(), null);
    verify(scan, Arrays.copyOfRange(writes, 3, writes.length));
    scan.close();

    // test unbounded scan
    scan = table.scan(null, null);
    verify(scan, writes);
    scan.close();

    // commit transaction
    commitTransaction(txContext);
  }

  @Test
  public void testConflictLevelParam() {
    Table rowConflictTable = instantiator.getDataSet("rowConflict");
    // hacky way to check that param was propagated to the oc table implementation
    Assert.assertEquals(
      ConflictDetection.ROW,
      ((BufferingOcTableClient) ((RuntimeTable) rowConflictTable.getDelegate()).getTxAware()).getConflictLevel());

    // test that only column conflicts are detected
    Table colConflictTable = instantiator.getDataSet("columnConflict");
    // hacky way to check that param was propagated to the oc table implementation
    Assert.assertEquals(
      ConflictDetection.COLUMN,
      ((BufferingOcTableClient) ((RuntimeTable) colConflictTable.getDelegate()).getTxAware()).getConflictLevel());
  }

  private void verify(Scanner scan, Write... expected) {
    int count = 0;
    while (true) {
      Row next = scan.next();
      if (next == null) {
        break;
      }
      Write toCompare = expected[count];
      Assert.assertArrayEquals(toCompare.getRow(), next.getRow());
      verify(toCompare.getColumns(), toCompare.getValues(), next.getColumns());
      count++;
    }
    Assert.assertEquals(expected.length, count);
  }




  // helper to verify that the split readers for the given splits return exactly a set of keys
  private void verifySplits(Table t, List<Split> splits, SortedSet<Long> keysToVerify)
    throws OperationException, InterruptedException {
    // read each split and verify the keys, remove all read keys from the set
    for (Split split : splits) {
      SplitReader<byte[], Map<byte[], byte[]>> reader = t.createSplitReader(split);
      reader.initialize(split);
      while (reader.nextKeyValue()) {
        byte[] key = reader.getCurrentKey();
        Map<byte[], byte[]> row = reader.getCurrentValue();
        // verify each row has the two columns written
        Assert.assertArrayEquals(key, row.get(c));
        Assert.assertArrayEquals(v, row.get(key));
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