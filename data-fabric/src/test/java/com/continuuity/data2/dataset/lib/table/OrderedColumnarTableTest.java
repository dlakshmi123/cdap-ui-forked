package com.continuuity.data2.dataset.lib.table;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.OperationResult;
import com.continuuity.common.utils.ImmutablePair;
import com.continuuity.data.table.Scanner;
import com.continuuity.data2.dataset.api.DataSetManager;
import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.TransactionAware;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.inmemory.InMemoryTxSystemClient;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Base test for OrderedColumnarTable.
 * @param <T> table type
 */
public abstract class OrderedColumnarTableTest<T extends OrderedColumnarTable> {
  static final byte[] R1 = Bytes.toBytes("r1");
  static final byte[] R2 = Bytes.toBytes("r2");
  static final byte[] R3 = Bytes.toBytes("r3");
  static final byte[] R4 = Bytes.toBytes("r4");
  static final byte[] R5 = Bytes.toBytes("r5");

  static final byte[] C1 = Bytes.toBytes("c1");
  static final byte[] C2 = Bytes.toBytes("c2");
  static final byte[] C3 = Bytes.toBytes("c3");
  static final byte[] C4 = Bytes.toBytes("c4");
  static final byte[] C5 = Bytes.toBytes("c5");

  static final byte[] V1 = Bytes.toBytes("v1");
  static final byte[] V2 = Bytes.toBytes("v2");
  static final byte[] V3 = Bytes.toBytes("v3");
  static final byte[] V4 = Bytes.toBytes("v4");
  static final byte[] V5 = Bytes.toBytes("v5");

  static final byte[] L1 = Bytes.toBytes(1L);
  static final byte[] L2 = Bytes.toBytes(2L);
  static final byte[] L3 = Bytes.toBytes(3L);
  static final byte[] L4 = Bytes.toBytes(4L);
  static final byte[] L5 = Bytes.toBytes(5L);

  protected TransactionSystemClient txClient;

  protected abstract T getTable(String name, ConflictDetection conflictLevel) throws Exception;
  protected abstract DataSetManager getTableManager() throws Exception;

  protected T getTable(String name) throws Exception {
    return getTable(name, ConflictDetection.ROW);
  }

  @Before
  public void before() {
    txClient = new InMemoryTxSystemClient(new InMemoryTransactionManager());
  }

  @After
  public void after() {
    txClient = null;
  }

  @Test
  public void testCreate() throws Exception {
    DataSetManager manager = getTableManager();
    Assert.assertFalse(manager.exists("myTable"));
    manager.create("myTable");
    Assert.assertTrue(manager.exists("myTable"));
    // creation of non-existing table should do nothing
    manager.create("myTable");
  }

  @Test
  public void testBasicGetPutWithTx() throws Exception {
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      // write r1->c1,v1 but not commit
      myTable1.put(R1, a(C1), a(V1));
      // verify can see changes inside tx
      verify(a(C1, V1), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(null, myTable1.get(R1, C2));
      verify(null, myTable1.get(R2, C1));
      verify(a(C1, V1), myTable1.get(R1));

      // start new tx (doesn't see changes of the tx1)
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      // verify doesn't see changes of tx1
      verify(a(), myTable2.get(R1, a(C1, C2)));
      verify(null, myTable2.get(R1, C1));
      verify(null, myTable2.get(R1, C2));
      verify(a(), myTable2.get(R1));
      // write r2->c2,v2 in tx2
      myTable2.put(R2, a(C2), a(V2));
      // verify can see own changes
      verify(a(C2, V2), myTable2.get(R2, a(C1, C2)));
      verify(null, myTable2.get(R2, C1));
      verify(V2, myTable2.get(R2, C2));
      verify(a(C2, V2), myTable2.get(R2));

      // verify tx1 cannot see changes of tx2
      verify(a(), myTable1.get(R2, a(C1, C2)));
      verify(null, myTable1.get(R2, C1));
      verify(null, myTable1.get(R2, C2));
      verify(a(), myTable1.get(R2));

      // committing tx1 in stages to check races are handled well
      // * first, flush operations of table
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());

      // start tx3 and verify that changes of tx1 are not visible yet (even though they are flushed)
      Transaction tx3 = txClient.startShort();
      OrderedColumnarTable myTable3 = getTable("myTable");
      ((TransactionAware) myTable3).startTx(tx3);
      verify(a(), myTable3.get(R1, a(C1, C2)));
      verify(null, myTable3.get(R1, C1));
      verify(null, myTable3.get(R1, C2));
      verify(a(), myTable3.get(R1));
      Assert.assertTrue(txClient.canCommit(tx3, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable3).commitTx());
      Assert.assertTrue(txClient.commit(tx3));

      // * second, make tx visible
      Assert.assertTrue(txClient.commit(tx1));
      // start tx4 and verify that changes of tx1 are now visible
      // NOTE: table instance can be re-used in series of transactions
      Transaction tx4 = txClient.startShort();
      ((TransactionAware) myTable3).startTx(tx4);
      verify(a(C1, V1), myTable3.get(R1, a(C1, C2)));
      verify(V1, myTable3.get(R1, C1));
      verify(null, myTable3.get(R1, C2));
      verify(a(C1, V1), myTable3.get(R1));

      // but tx2 still doesn't see committed changes of tx2
      verify(a(), myTable2.get(R1, a(C1, C2)));
      verify(null, myTable2.get(R1, C1));
      verify(null, myTable2.get(R1, C2));
      verify(a(), myTable2.get(R1));
      // and tx4 doesn't see changes of tx2
      verify(a(), myTable3.get(R2, a(C1, C2)));
      verify(null, myTable3.get(R2, C1));
      verify(null, myTable3.get(R2, C2));
      verify(a(), myTable3.get(R2));

      // committing tx4
      Assert.assertTrue(txClient.canCommit(tx4, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable3).commitTx());
      Assert.assertTrue(txClient.commit(tx4));

      // do change in tx2 that is conflicting with tx1
      myTable2.put(R1, a(C1), a(V2));
      // change is OK and visible inside tx2
      verify(a(C1, V2), myTable2.get(R1, a(C1, C2)));
      verify(V2, myTable2.get(R1, C1));
      verify(null, myTable2.get(R1, C2));
      verify(a(C1, V2), myTable2.get(R1));

      // cannot commit: conflict should be detected
      Assert.assertFalse(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));

      // rolling back tx2 changes and aborting tx
      ((TransactionAware) myTable2).rollbackTx();
      txClient.abort(tx2);

      // verifying that none of the changes of tx2 made it to be visible to other txs
      // NOTE: table instance can be re-used in series of transactions
      Transaction tx5 = txClient.startShort();
      ((TransactionAware) myTable3).startTx(tx5);
      verify(a(C1, V1), myTable3.get(R1, a(C1, C2)));
      verify(V1, myTable3.get(R1, C1));
      verify(null, myTable3.get(R1, C2));
      verify(a(C1, V1), myTable3.get(R1));
      verify(a(), myTable3.get(R2, a(C1, C2)));
      verify(null, myTable3.get(R2, C1));
      verify(null, myTable3.get(R2, C2));
      verify(a(), myTable3.get(R2));
      Assert.assertTrue(txClient.canCommit(tx5, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable3).commitTx());
      Assert.assertTrue(txClient.commit(tx5));

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testBasicCompareAndSwapWithTx() throws Exception {
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      // write r1->c1,v1 but not commit
      myTable1.put(R1, a(C1), a(V1));
      // write r1->c2,v2 but not commit
      Assert.assertTrue(myTable1.compareAndSwap(R1, C2, null, V5));
      // verify compare and swap result visible inside tx before commit
      verify(a(C1, V1, C2, V5), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(V5, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, V1, C2, V5), myTable1.get(R1));
      // these should fail
      Assert.assertFalse(myTable1.compareAndSwap(R1, C1, null, V1));
      Assert.assertFalse(myTable1.compareAndSwap(R1, C1, V2, V1));
      Assert.assertFalse(myTable1.compareAndSwap(R1, C2, null, V2));
      Assert.assertFalse(myTable1.compareAndSwap(R1, C2, V2, V1));
      // but this should succeed
      Assert.assertTrue(myTable1.compareAndSwap(R1, C2, V5, V2));

      // start new tx (doesn't see changes of the tx1)
      Transaction tx2 = txClient.startShort();

      // committing tx1 in stages to check races are handled well
      // * first, flush operations of table
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());

      // check that tx2 doesn't see changes (even though they were flushed) of tx1 by trying to compareAndSwap
      // assuming current value is null
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      Assert.assertTrue(myTable2.compareAndSwap(R1, C1, null, V3));

      // start tx3 and verify same thing again
      Transaction tx3 = txClient.startShort();
      OrderedColumnarTable myTable3 = getTable("myTable");
      ((TransactionAware) myTable3).startTx(tx3);
      Assert.assertTrue(myTable3.compareAndSwap(R1, C1, null, V2));

      // * second, make tx visible
      Assert.assertTrue(txClient.commit(tx1));

      // verify that tx2 cannot commit because of the conflicts...
      Assert.assertFalse(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));
      ((TransactionAware) myTable2).rollbackTx();
      txClient.abort(tx2);

      // start tx4 and verify that changes of tx1 are now visible
      Transaction tx4 = txClient.startShort();
      OrderedColumnarTable myTable4 = getTable("myTable");
      ((TransactionAware) myTable4).startTx(tx4);
      verify(a(C1, V1, C2, V2), myTable4.get(R1, a(C1, C2)));
      verify(V1, myTable4.get(R1, C1));
      verify(V2, myTable4.get(R1, C2));
      verify(null, myTable4.get(R1, C3));
      verify(a(C2, V2), myTable4.get(R1, a(C2)));
      verify(a(C1, V1, C2, V2), myTable4.get(R1));

      // tx3 still cannot see tx1 changes
      Assert.assertTrue(myTable3.compareAndSwap(R1, C2, null, V5));
      // and it cannot commit because its changes cause conflicts
      Assert.assertFalse(txClient.canCommit(tx3, ((TransactionAware) myTable3).getTxChanges()));
      ((TransactionAware) myTable3).rollbackTx();
      txClient.abort(tx3);

      // verify we can do some ops with tx4 based on data written with tx1
      Assert.assertFalse(myTable4.compareAndSwap(R1, C1, null, V4));
      Assert.assertFalse(myTable4.compareAndSwap(R1, C2, null, V5));
      Assert.assertTrue(myTable4.compareAndSwap(R1, C1, V1, V3));
      Assert.assertTrue(myTable4.compareAndSwap(R1, C2, V2, V4));
      myTable4.delete(R1, a(C1));

      // committing tx4
      Assert.assertTrue(txClient.canCommit(tx4, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable4).commitTx());
      Assert.assertTrue(txClient.commit(tx4));

      // verifying the result contents in next transaction
      Transaction tx5 = txClient.startShort();
      // NOTE: table instance can be re-used in series of transactions
      ((TransactionAware) myTable4).startTx(tx5);
      verify(a(C2, V4), myTable4.get(R1, a(C1, C2)));
      verify(null, myTable4.get(R1, C1));
      verify(V4, myTable4.get(R1, C2));
      verify(a(C2, V4), myTable4.get(R1));
      Assert.assertTrue(txClient.canCommit(tx5, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable3).commitTx());
      Assert.assertTrue(txClient.commit(tx5));

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testBasicIncrementWithTx() throws Exception {
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    OrderedColumnarTable myTable1, myTable2, myTable3, myTable4;
    try {
      Transaction tx1 = txClient.startShort();
      myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      myTable1.put(R1, a(C1), a(L4));
      verify(a(C1), la(1L), myTable1.increment(R1, a(C1), la(-3L)));
      verify(a(C2), la(2L), myTable1.increment(R1, a(C2), la(2L)));
      // verify increment result visible inside tx before commit
      verify(a(C1, L1, C2, L2), myTable1.get(R1, a(C1, C2)));
      verify(L1, myTable1.get(R1, C1));
      verify(L2, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, L1), myTable1.get(R1, a(C1)));
      verify(a(C1, L1, C2, L2), myTable1.get(R1));
      // incrementing non-long value should fail
      myTable1.put(R1, a(C5), a(V5));
      try {
        myTable1.increment(R1, a(C5), la(5L));
        Assert.assertTrue(false);
      } catch (OperationException e) {}
      // previous increment should not do any change
      verify(a(C5, V5), myTable1.get(R1, a(C5)));
      verify(V5, myTable1.get(R1, C5));

      // start new tx (doesn't see changes of the tx1)
      Transaction tx2 = txClient.startShort();

      // committing tx1 in stages to check races are handled well
      // * first, flush operations of table
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());

      // check that tx2 doesn't see changes (even though they were flushed) of tx1
      // assuming current value is null
      myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      verify(a(), myTable2.get(R1, a(C1, C2, C5)));
      verify(null, myTable2.get(R1, C1));
      verify(null, myTable2.get(R1, C2));
      verify(null, myTable2.get(R1, C5));
      verify(a(), myTable2.get(R1));
      verify(a(C1), la(55L), myTable2.increment(R1, a(C1), la(55L)));

      // start tx3 and verify same thing again
      Transaction tx3 = txClient.startShort();
      myTable3 = getTable("myTable");
      ((TransactionAware) myTable3).startTx(tx3);
      verify(a(), myTable3.get(R1, a(C1, C2, C5)));
      verify(null, myTable3.get(R1, C1));
      verify(null, myTable3.get(R1, C2));
      verify(null, myTable3.get(R1, C5));
      verify(a(), myTable3.get(R1));
      verify(a(C1), la(4L), myTable3.increment(R1, a(C1), la(4L)));

      // * second, make tx visible
      Assert.assertTrue(txClient.commit(tx1));

      // verify that tx2 cannot commit because of the conflicts...
      Assert.assertFalse(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));
      ((TransactionAware) myTable2).rollbackTx();
      txClient.abort(tx2);

      // start tx4 and verify that changes of tx1 are now visible
      Transaction tx4 = txClient.startShort();
      myTable4 = getTable("myTable");
      ((TransactionAware) myTable4).startTx(tx4);
      verify(a(C1, L1, C2, L2, C5, V5), myTable4.get(R1, a(C1, C2, C3, C4, C5)));
      verify(a(C2, L2), myTable4.get(R1, a(C2)));
      verify(L1, myTable4.get(R1, C1));
      verify(L2, myTable4.get(R1, C2));
      verify(null, myTable4.get(R1, C3));
      verify(V5, myTable4.get(R1, C5));
      verify(a(C1, L1, C5, V5), myTable4.get(R1, a(C1, C5)));
      verify(a(C1, L1, C2, L2, C5, V5), myTable4.get(R1));

      // tx3 still cannot see tx1 changes, only its own
      verify(a(C1, L4), myTable3.get(R1, a(C1, C2, C5)));
      verify(L4, myTable3.get(R1, C1));
      verify(null, myTable3.get(R1, C2));
      verify(null, myTable3.get(R1, C5));
      verify(a(C1, L4), myTable3.get(R1));
      // and it cannot commit because its changes cause conflicts
      Assert.assertFalse(txClient.canCommit(tx3, ((TransactionAware) myTable3).getTxChanges()));
      ((TransactionAware) myTable3).rollbackTx();
      txClient.abort(tx3);

      // verify we can do some ops with tx4 based on data written with tx1
      verify(a(C1, C2, C3), la(3L, 3L, 5L), myTable4.increment(R1, a(C1, C2, C3), la(2L, 1L, 5L)));
      myTable4.delete(R1, a(C2));
      verify(a(C4), la(3L), myTable4.increment(R1, a(C4), la(3L)));
      myTable4.delete(R1, a(C1));

      // committing tx4
      Assert.assertTrue(txClient.canCommit(tx4, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable4).commitTx());
      Assert.assertTrue(txClient.commit(tx4));

      // verifying the result contents in next transaction
      Transaction tx5 = txClient.startShort();
      // NOTE: table instance can be re-used in series of transactions
      ((TransactionAware) myTable4).startTx(tx5);
      verify(a(C3, L5, C4, L3, C5, V5), myTable4.get(R1, a(C1, C2, C3, C4, C5)));
      verify(null, myTable4.get(R1, C1));
      verify(null, myTable4.get(R1, C2));
      verify(L5, myTable4.get(R1, C3));
      verify(L3, myTable4.get(R1, C4));
      verify(V5, myTable4.get(R1, C5));
      verify(a(C3, L5, C4, L3, C5, V5), myTable4.get(R1));
      Assert.assertTrue(txClient.canCommit(tx5, ((TransactionAware) myTable3).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable3).commitTx());
      Assert.assertTrue(txClient.commit(tx5));

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testBasicDeleteWithTx() throws Exception {
    // we will test 3 different delete column ops and one delete row op
    // * delete column with delete
    // * delete column with put null value
    // * delete column with put byte[0] value
    // * delete row with delete
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      // write smth and commit
      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      myTable1.put(R1, a(C1, C2), a(V1, V2));
      myTable1.put(R2, a(C1, C2), a(V2, V3));
      myTable1.put(R3, a(C1, C2), a(V3, V4));
      myTable1.put(R4, a(C1, C2), a(V4, V5));
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx1));

      // Now, we will test delete ops
      // start new tx2
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      // verify tx2 sees changes of tx1
      verify(a(C1, V1, C2, V2), myTable2.get(R1, a(C1, C2)));
      // verify tx2 sees changes of tx1
      verify(a(C1, V2, C2, V3), myTable2.get(R2));
      // delete c1, r2
      myTable2.delete(R1, a(C1));
      myTable2.delete(R2);
      // same as delete a column
      myTable2.put(R3, C1, null);
      // same as delete a column
      myTable2.put(R4, C1, new byte[0]);
      // verify can see deletes in own changes before commit
      verify(a(C2, V2), myTable2.get(R1, a(C1, C2)));
      verify(null, myTable2.get(R1, C1));
      verify(V2, myTable2.get(R1, C2));
      verify(a(), myTable2.get(R2));
      verify(a(C2, V4), myTable2.get(R3));
      verify(a(C2, V5), myTable2.get(R4));
      // overwrite c2 and write new value to c1
      myTable2.put(R1, a(C1, C2), a(V3, V4));
      myTable2.put(R2, a(C1, C2), a(V4, V5));
      myTable2.put(R3, a(C1, C2), a(V1, V2));
      myTable2.put(R4, a(C1, C2), a(V2, V3));
      // verify can see changes in own changes before commit
      verify(a(C1, V3, C2, V4), myTable2.get(R1, a(C1, C2, C3)));
      verify(V3, myTable2.get(R1, C1));
      verify(V4, myTable2.get(R1, C2));
      verify(null, myTable2.get(R1, C3));
      verify(a(C1, V4, C2, V5), myTable2.get(R2));
      verify(a(C1, V1, C2, V2), myTable2.get(R3));
      verify(a(C1, V2, C2, V3), myTable2.get(R4));
      // delete c2 and r2
      myTable2.delete(R1, a(C2));
      myTable2.delete(R2);
      myTable2.put(R1, C2, null);
      myTable2.put(R1, C2, new byte[0]);
      // verify that delete is there (i.e. not reverted to whatever was persisted before)
      verify(a(C1, V3), myTable2.get(R1, a(C1, C2)));
      verify(V3, myTable2.get(R1, C1));
      verify(null, myTable2.get(R1, C2));
      verify(a(), myTable2.get(R2));
      verify(V1, myTable2.get(R3, C1));
      verify(V2, myTable2.get(R4, C1));

      // start tx3 and verify that changes of tx2 are not visible yet
      Transaction tx3 = txClient.startShort();
      // NOTE: table instance can be re-used between tx
      ((TransactionAware) myTable1).startTx(tx3);
      verify(a(C1, V1, C2, V2), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(V2, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, V1, C2, V2), myTable1.get(R1));
      verify(a(C1, V2, C2, V3), myTable1.get(R2));
      verify(a(C1, V3, C2, V4), myTable1.get(R3));
      verify(a(C1, V4, C2, V5), myTable1.get(R4));
      Assert.assertTrue(txClient.canCommit(tx3, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx3));

      // starting tx4 before committing tx2 so that we can check conflicts are detected wrt deletes
      Transaction tx4 = txClient.startShort();
      // starting tx5 before committing tx2 so that we can check conflicts are detected wrt deletes
      Transaction tx5 = txClient.startShort();

      // commit tx2 in stages to see how races are handled wrt delete ops
      Assert.assertTrue(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable2).commitTx());

      // start tx6 and verify that changes of tx2 are not visible yet (even though they are flushed)
      Transaction tx6 = txClient.startShort();
      // NOTE: table instance can be re-used between tx
      ((TransactionAware) myTable1).startTx(tx6);
      verify(a(C1, V1, C2, V2), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(V2, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, V1, C2, V2), myTable1.get(R1));
      verify(a(C1, V2, C2, V3), myTable1.get(R2));
      verify(a(C1, V3, C2, V4), myTable1.get(R3));
      verify(a(C1, V4, C2, V5), myTable1.get(R4));

      Assert.assertTrue(txClient.canCommit(tx6, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx6));

      // make tx2 visible
      Assert.assertTrue(txClient.commit(tx2));

      // start tx7 and verify that changes of tx2 are now visible
      Transaction tx7 = txClient.startShort();
      // NOTE: table instance can be re-used between tx
      ((TransactionAware) myTable1).startTx(tx7);
      verify(a(C1, V3), myTable1.get(R1, a(C1, C2)));
      verify(a(C1, V3), myTable1.get(R1));
      verify(V3, myTable1.get(R1, C1));
      verify(null, myTable1.get(R1, C2));
      verify(a(C1, V3), myTable1.get(R1, a(C1, C2)));
      verify(a(), myTable1.get(R2));
      verify(V1, myTable1.get(R3, C1));
      verify(V2, myTable1.get(R4, C1));

      Assert.assertTrue(txClient.canCommit(tx6, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx7));

      // but not visible to tx4 that we started earlier than tx2 became visible
      // NOTE: table instance can be re-used between tx
      ((TransactionAware) myTable1).startTx(tx4);
      verify(a(C1, V1, C2, V2), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(V2, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, V1, C2, V2), myTable1.get(R1));
      verify(a(C1, V2, C2, V3), myTable1.get(R2));
      verify(a(C1, V3, C2, V4), myTable1.get(R3));
      verify(a(C1, V4, C2, V5), myTable1.get(R4));

      // writing to deleted column, to check conflicts are detected (delete-write conflict)
      myTable1.put(R1, a(C2), a(V5));
      Assert.assertFalse(txClient.canCommit(tx4, ((TransactionAware) myTable1).getTxChanges()));
      ((TransactionAware) myTable1).rollbackTx();
      txClient.abort(tx4);

      // deleting changed column, to check conflicts are detected (write-delete conflict)
      // NOTE: table instance can be re-used between tx
      ((TransactionAware) myTable1).startTx(tx5);
      verify(a(C1, V1, C2, V2), myTable1.get(R1, a(C1, C2)));
      verify(V1, myTable1.get(R1, C1));
      verify(V2, myTable1.get(R1, C2));
      verify(null, myTable1.get(R1, C3));
      verify(a(C1, V1, C2, V2), myTable1.get(R1));
      verify(a(C1, V2, C2, V3), myTable1.get(R2));
      verify(a(C1, V3, C2, V4), myTable1.get(R3));
      verify(a(C1, V4, C2, V5), myTable1.get(R4));
      // NOTE: we are verifying conflict in one operation only. We may want to test each...
      myTable1.delete(R1, a(C1));
      Assert.assertFalse(txClient.canCommit(tx5, ((TransactionAware) myTable1).getTxChanges()));
      ((TransactionAware) myTable1).rollbackTx();
      txClient.abort(tx5);

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testBasicScanWithTx() throws Exception {
    // todo: make work with tx well (merge with buffer, conflicts) and add tests for that
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      // write r1...r5 and commit
      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      myTable1.put(R1, a(C1), a(V1));
      myTable1.put(R2, a(C2), a(V2));
      myTable1.put(R3, a(C3, C4), a(V3, V4));
      myTable1.put(R4, a(C4), a(V4));
      myTable1.put(R5, a(C5), a(V5));
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx1));

      // Now, we will test scans
      // currently not testing races/conflicts/etc as this logic is not there for scans yet; so using one same tx
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      // bounded scan
      verify(a(R2, R3, R4),
             aa(a(C2, V2),
                a(C3, V3, C4, V4),
                a(C4, V4)),
             myTable2.scan(R2, R5));
      // open start scan
      verify(a(R1, R2, R3),
             aa(a(C1, V1),
                a(C2, V2),
                a(C3, V3, C4, V4)),
             myTable2.scan(null, R4));
      // open end scan
      verify(a(R3, R4, R5),
             aa(a(C3, V3, C4, V4),
                a(C4, V4),
                a(C5, V5)),
             myTable2.scan(R3, null));
      // open ends scan
      verify(a(R1, R2, R3, R4, R5),
             aa(a(C1, V1),
                a(C2, V2),
                a(C3, V3, C4, V4),
                a(C4, V4),
                a(C5, V5)),
             myTable2.scan(null, null));

      // adding/changing/removing some columns
      myTable2.put(R2, a(C1, C2, C3), a(V4, V3, V2));
      myTable2.delete(R3, a(C4));

      Assert.assertTrue(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable2).commitTx());
      Assert.assertTrue(txClient.commit(tx2));

      // Checking that changes are reflected in new scans in new tx
      Transaction tx3 = txClient.startShort();
      // NOTE: table can be re-used betweet tx
      ((TransactionAware) myTable1).startTx(tx3);

      verify(a(R2, R3, R4),
             aa(a(C1, V4, C2, V3, C3, V2),
                a(C3, V3),
                a(C4, V4)),
             myTable1.scan(R2, R5));

      Assert.assertTrue(txClient.canCommit(tx3, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx3));

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testBasicColumnRangeWithTx() throws Exception {
    // todo: test more tx logic (or add to get/put unit-test)
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      // write test data and commit
      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      myTable1.put(R1, a(C1, C2, C3, C4, C5), a(V1, V2, V3, V4, V5));
      myTable1.put(R2, a(C1), a(V2));
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx1));

      // Now, we will test column range get
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      // bounded range
      verify(a(C2, V2, C3, V3, C4, V4),
             myTable2.get(R1, C2, C5, Integer.MAX_VALUE));
      // open start range
      verify(a(C1, V1, C2, V2, C3, V3),
             myTable2.get(R1, null, C4, Integer.MAX_VALUE));
      // open end range
      verify(a(C3, V3, C4, V4, C5, V5),
             myTable2.get(R1, C3, null, Integer.MAX_VALUE));
      // open ends range
      verify(a(C1, V1, C2, V2, C3, V3, C4, V4, C5, V5),
             myTable2.get(R1, null, null, Integer.MAX_VALUE));

      // same with limit
      // bounded range with limit
      verify(a(C2, V2),
             myTable2.get(R1, C2, C5, 1));
      // open start range with limit
      verify(a(C1, V1, C2, V2),
             myTable2.get(R1, null, C4, 2));
      // open end range with limit
      verify(a(C3, V3, C4, V4),
             myTable2.get(R1, C3, null, 2));
      // open ends range with limit
      verify(a(C1, V1, C2, V2, C3, V3, C4, V4),
             myTable2.get(R1, null, null, 4));

      // adding/changing/removing some columns
      myTable2.put(R1, a(C1, C2, C3), a(V4, V3, V2));
      myTable2.delete(R1, a(C4));

      Assert.assertTrue(txClient.canCommit(tx2, ((TransactionAware) myTable2).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable2).commitTx());
      Assert.assertTrue(txClient.commit(tx2));

      // Checking that changes are reflected in new scans in new tx
      Transaction tx3 = txClient.startShort();
      // NOTE: table can be re-used betweet tx
      ((TransactionAware) myTable1).startTx(tx3);

      verify(a(C2, V3, C3, V2),
             myTable1.get(R1, C2, C5, Integer.MAX_VALUE));

      Assert.assertTrue(txClient.canCommit(tx3, ((TransactionAware) myTable1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());
      Assert.assertTrue(txClient.commit(tx3));

    } finally {
      manager.drop("myTable");
    }
  }

  @Test
  public void testTxUsingMultipleTables() throws Exception {
    DataSetManager manager = getTableManager();
    manager.create("table1");
    manager.create("table2");
    manager.create("table3");
    manager.create("table4");

    try {
      // We will be changing:
      // * table1 and table2 in tx1
      // * table2 and table3 in tx2 << will conflict with first one
      // * table3 and table4 in tx3

      Transaction tx1 = txClient.startShort();
      Transaction tx2 = txClient.startShort();
      Transaction tx3 = txClient.startShort();

      // Write data in tx1 and commit
      OrderedColumnarTable table1_1 = getTable("table1");
      ((TransactionAware) table1_1).startTx(tx1);
      // write r1->c1,v1 but not commit
      table1_1.put(R1, a(C1), a(V1));
      OrderedColumnarTable table2_1 = getTable("table2");
      ((TransactionAware) table2_1).startTx(tx1);
      // write r1->c1,v2 but not commit
      table2_1.put(R1, a(C1), a(V2));
      // verify writes inside same tx
      verify(a(C1, V1), table1_1.get(R1, a(C1)));
      verify(a(C1, V2), table2_1.get(R1, a(C1)));
      // commit tx1
      Assert.assertTrue(txClient.canCommit(tx1, ImmutableList.copyOf(
        Iterables.concat(((TransactionAware) table1_1).getTxChanges(),
                         ((TransactionAware) table2_1).getTxChanges()))));
      Assert.assertTrue(((TransactionAware) table1_1).commitTx());
      Assert.assertTrue(((TransactionAware) table2_1).commitTx());
      Assert.assertTrue(txClient.commit(tx1));

      // Write data in tx2 and check that cannot commit because of conflicts
      OrderedColumnarTable table2_2 = getTable("table2");
      ((TransactionAware) table2_2).startTx(tx2);
      // write r1->c1,v1 but not commit
      table2_2.put(R1, a(C1), a(V2));
      OrderedColumnarTable table3_2 = getTable("table3");
      ((TransactionAware) table3_2).startTx(tx2);
      // write r1->c1,v2 but not commit
      table3_2.put(R1, a(C1), a(V3));
      // verify writes inside same tx
      verify(a(C1, V2), table2_2.get(R1, a(C1)));
      verify(a(C1, V3), table3_2.get(R1, a(C1)));
      // try commit tx2
      Assert.assertFalse(txClient.canCommit(tx2, ImmutableList.copyOf(
        Iterables.concat(((TransactionAware) table2_2).getTxChanges(),
                         ((TransactionAware) table3_2).getTxChanges()))));
      Assert.assertTrue(((TransactionAware) table2_2).rollbackTx());
      Assert.assertTrue(((TransactionAware) table3_2).rollbackTx());
      txClient.abort(tx2);

      // Write data in tx3 and check that can commit (no conflicts)
      OrderedColumnarTable table3_3 = getTable("table3");
      ((TransactionAware) table3_3).startTx(tx3);
      // write r1->c1,v1 but not commit
      table3_3.put(R1, a(C1), a(V3));
      OrderedColumnarTable table4_3 = getTable("table4");
      ((TransactionAware) table4_3).startTx(tx3);
      // write r1->c1,v2 but not commit
      table4_3.put(R1, a(C1), a(V4));
      // verify writes inside same tx
      verify(a(C1, V3), table3_3.get(R1, a(C1)));
      verify(a(C1, V4), table4_3.get(R1, a(C1)));
      // commit tx3
      Assert.assertTrue(txClient.canCommit(tx3, ImmutableList.copyOf(
        Iterables.concat(((TransactionAware) table3_3).getTxChanges(),
                         ((TransactionAware) table4_3).getTxChanges()))));
      Assert.assertTrue(((TransactionAware) table3_3).commitTx());
      Assert.assertTrue(((TransactionAware) table4_3).commitTx());
      Assert.assertTrue(txClient.commit(tx3));

    } finally {
      manager.drop("table1");
      manager.drop("table2");
      manager.drop("table3");
      manager.drop("table4");
    }
  }

  @Test
  public void testConflictsOnRowLevel() throws Exception {
    testConflictDetection(ConflictDetection.ROW);
  }

  @Test
  public void testConflictsOnColumnLevel() throws Exception {
    testConflictDetection(ConflictDetection.COLUMN);
  }

  private void testConflictDetection(ConflictDetection level) throws Exception {
    // we use tableX_Y format for variables which means "tableX that is used in tx Y"
    DataSetManager manager = getTableManager();
    manager.create("table1");
    manager.create("table2");
    try {
      // 1) Test conflicts when using different tables

      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable table1_1 = getTable("table1", level);
      ((TransactionAware) table1_1).startTx(tx1);
      // write r1->c1,v1 but not commit
      table1_1.put(R1, a(C1), a(V1));

      // start new tx
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable table2_2 = getTable("table2", level);
      ((TransactionAware) table2_2).startTx(tx2);

      // change in tx2 same data but in different table
      table2_2.put(R1, a(C1), a(V2));

      // start new tx
      Transaction tx3 = txClient.startShort();
      OrderedColumnarTable table1_3 = getTable("table1", level);
      ((TransactionAware) table1_3).startTx(tx3);

      // change in tx3 same data in same table as tx1
      table1_3.put(R1, a(C1), a(V2));

      // committing tx1
      Assert.assertTrue(txClient.canCommit(tx1, ((TransactionAware) table1_1).getTxChanges()));
      Assert.assertTrue(((TransactionAware) table1_1).commitTx());
      Assert.assertTrue(txClient.commit(tx1));

      // no conflict should be when committing tx2
      Assert.assertTrue(txClient.canCommit(tx2, ((TransactionAware) table2_2).getTxChanges()));

      // but conflict should be when committing tx3
      Assert.assertFalse(txClient.canCommit(tx3, ((TransactionAware) table1_3).getTxChanges()));
      ((TransactionAware) table1_3).rollbackTx();
      txClient.abort(tx3);

      // 2) Test conflicts when using different rows
      Transaction tx4 = txClient.startShort();
      OrderedColumnarTable table1_4 = getTable("table1", level);
      ((TransactionAware) table1_4).startTx(tx4);
      // write r1->c1,v1 but not commit
      table1_4.put(R1, a(C1), a(V1));

      // start new tx
      Transaction tx5 = txClient.startShort();
      OrderedColumnarTable table1_5 = getTable("table1", level);
      ((TransactionAware) table1_5).startTx(tx5);

      // change in tx5 same data but in different row
      table1_5.put(R2, a(C1), a(V2));

      // start new tx
      Transaction tx6 = txClient.startShort();
      OrderedColumnarTable table1_6 = getTable("table1", level);
      ((TransactionAware) table1_6).startTx(tx6);

      // change in tx6 in same row as tx1
      table1_6.put(R1, a(C2), a(V2));

      // committing tx4
      Assert.assertTrue(txClient.canCommit(tx4, ((TransactionAware) table1_4).getTxChanges()));
      Assert.assertTrue(((TransactionAware) table1_4).commitTx());
      Assert.assertTrue(txClient.commit(tx4));

      // no conflict should be when committing tx5
      Assert.assertTrue(txClient.canCommit(tx5, ((TransactionAware) table1_5).getTxChanges()));

      // but conflict should be when committing tx6 iff we resolve on row level
      if (level == ConflictDetection.ROW) {
        Assert.assertFalse(txClient.canCommit(tx6, ((TransactionAware) table1_6).getTxChanges()));
        ((TransactionAware) table1_6).rollbackTx();
        txClient.abort(tx6);
      } else {
        Assert.assertTrue(txClient.canCommit(tx6, ((TransactionAware) table1_6).getTxChanges()));
      }

      // 3) Test conflicts when using different columns
      Transaction tx7 = txClient.startShort();
      OrderedColumnarTable table1_7 = getTable("table1", level);
      ((TransactionAware) table1_7).startTx(tx7);
      // write r1->c1,v1 but not commit
      table1_7.put(R1, a(C1), a(V1));

      // start new tx
      Transaction tx8 = txClient.startShort();
      OrderedColumnarTable table1_8 = getTable("table1", level);
      ((TransactionAware) table1_8).startTx(tx8);

      // change in tx8 same data but in different column
      table1_8.put(R1, a(C2), a(V2));

      // start new tx
      Transaction tx9 = txClient.startShort();
      OrderedColumnarTable table1_9 = getTable("table1", level);
      ((TransactionAware) table1_9).startTx(tx9);

      // change in tx9 same column in same column as tx1
      table1_9.put(R1, a(C1), a(V2));

      // committing tx7
      Assert.assertTrue(txClient.canCommit(tx7, ((TransactionAware) table1_7).getTxChanges()));
      Assert.assertTrue(((TransactionAware) table1_7).commitTx());
      Assert.assertTrue(txClient.commit(tx7));

      // no conflict should be when committing tx8 iff we resolve on column level
      if (level == ConflictDetection.COLUMN) {
        Assert.assertTrue(txClient.canCommit(tx8, ((TransactionAware) table1_8).getTxChanges()));
      } else {
        Assert.assertFalse(txClient.canCommit(tx8, ((TransactionAware) table1_8).getTxChanges()));
        ((TransactionAware) table1_8).rollbackTx();
        txClient.abort(tx8);
      }

      // but conflict should be when committing tx9
      Assert.assertFalse(txClient.canCommit(tx9, ((TransactionAware) table1_9).getTxChanges()));
      ((TransactionAware) table1_9).rollbackTx();
      txClient.abort(tx9);

    } finally {
      // NOTE: we are doing our best to cleanup junk between tests to isolate errors, but we are not going to be
      //       crazy about it
      manager.drop("table1");
      manager.drop("table2");
    }
  }

  @Test
  public void testRollingBackPersistedChanges() throws Exception {
    DataSetManager manager = getTableManager();
    manager.create("myTable");
    try {
      // write and commit one row/column
      Transaction tx0 = txClient.startShort();
      OrderedColumnarTable myTable0 = getTable("myTable");
      ((TransactionAware) myTable0).startTx(tx0);
      myTable0.put(R2, a(C2), a(V2));
      Assert.assertTrue(txClient.canCommit(tx0, ((TransactionAware) myTable0).getTxChanges()));
      Assert.assertTrue(((TransactionAware) myTable0).commitTx());
      Assert.assertTrue(txClient.commit(tx0));
      ((TransactionAware) myTable0).postTxCommit();

      Transaction tx1 = txClient.startShort();
      OrderedColumnarTable myTable1 = getTable("myTable");
      ((TransactionAware) myTable1).startTx(tx1);
      // write r1->c1,v1 but not commit
      myTable1.put(R1, a(C1), a(V1));
      // also overwrite the value from tx0
      myTable1.put(R2, a(C2), a(V3));
      // verify can see changes inside tx
      verify(a(C1, V1), myTable1.get(R1, a(C1)));

      // persisting changes
      Assert.assertTrue(((TransactionAware) myTable1).commitTx());

      // let's pretend that after persisting changes we still got conflict when finalizing tx, so
      // rolling back changes
      Assert.assertTrue(((TransactionAware) myTable1).rollbackTx());

      // making tx visible
      txClient.abort(tx1);

      // start new tx
      Transaction tx2 = txClient.startShort();
      OrderedColumnarTable myTable2 = getTable("myTable");
      ((TransactionAware) myTable2).startTx(tx2);

      // verify don't see rolled back changes
      verify(a(), myTable2.get(R1, a(C1)));
      // verify we still see the previous value
      verify(a(C2, V2), myTable2.get(R2, a(C2)));

    } finally {
      manager.drop("myTable");
    }
  }

  // todo: verify changing different cols of one row causes conflict
  // todo: check race: table committed, but txClient doesn't know yet (visibility, conflict detection)
  // todo: test overwrite + delete, that delete doesn't cause getting from persisted again

  void verify(byte[][] expected, OperationResult<Map<byte[], byte[]>> actual) {
    Preconditions.checkArgument(expected.length % 2 == 0, "expected [key,val] pairs in first param");
    if (expected.length == 0) {
      Assert.assertTrue(actual.isEmpty());
      return;
    }
    Assert.assertFalse("result is empty, but expected " + expected.length / 2 + " columns", actual.isEmpty());
    verify(expected, actual.getValue());
  }

  void verify(byte[][] expected, Map<byte[], byte[]> rowMap) {
    Assert.assertEquals(expected.length / 2, rowMap.size());
    for (int i = 0; i < expected.length; i += 2) {
      byte[] key = expected[i];
      byte[] val = expected[i + 1];
      Assert.assertArrayEquals(val, rowMap.get(key));
    }
  }

  void verify(byte[][] expectedColumns, long[] expectedValues, Map<byte[], Long> actual) {
    Assert.assertEquals(expectedColumns.length, actual.size());
    for (int i = 0; i < expectedColumns.length; i++) {
      Assert.assertEquals(expectedValues[i], (long) actual.get(expectedColumns[i]));
    }
  }

  void verify(byte[] expected, byte[] actual) {
    Assert.assertArrayEquals(expected, actual);
  }

  void verify(byte[][] expectedRows, byte[][][] expectedRowMaps, Scanner scan) {
    for (int i = 0; i < expectedRows.length; i++) {
      ImmutablePair<byte[], Map<byte[], byte[]>> next = scan.next();
      Assert.assertArrayEquals(expectedRows[i], next.getFirst());
      verify(expectedRowMaps[i], next.getSecond());
    }

    // nothing is left in scan
    Assert.assertNull(scan.next());
  }

  static long[] la(long... elems) {
    return elems;
  }

  // to array
  static byte[][] a(byte[]... elems) {
    return elems;
  }

  // to array of array
  static byte[][][] aa(byte[][]... elems) {
    return elems;
  }
}