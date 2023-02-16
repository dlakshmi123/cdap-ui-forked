package com.continuuity.data2.transaction;

import com.continuuity.api.common.Bytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public abstract class TransactionSystemTest {

  public static final byte[] C1 = Bytes.toBytes("change1");
  public static final byte[] C2 = Bytes.toBytes("change2");
  public static final byte[] C3 = Bytes.toBytes("change3");
  public static final byte[] C4 = Bytes.toBytes("change4");

  protected abstract TransactionSystemClient getClient();

  @Test
  public void testCommitRaceHandling() {
    TransactionSystemClient client1 = getClient();
    TransactionSystemClient client2 = getClient();

    Transaction tx1 = client1.startShort();
    Transaction tx2 = client2.startShort();

    Assert.assertTrue(client1.canCommit(tx1, $(C1, C2)));
    // second one also can commit even thought there are conflicts with first since first one hasn't committed yet
    Assert.assertTrue(client2.canCommit(tx2, $(C2, C3)));

    Assert.assertTrue(client1.commit(tx1));

    // now second one should not commit, since there are conflicts with tx1 that has been committed
    Assert.assertFalse(client2.commit(tx2));
  }

  @Test
  public void testMultipleCommitsAtSameTime() {
    // We want to check that if two txs finish at same time (wrt tx manager) they do not overwrite changesets of each
    // other in tx manager used for conflicts detection (we had this bug)
    // NOTE: you don't have to use multiple clients for that
    TransactionSystemClient client1 = getClient();
    TransactionSystemClient client2 = getClient();
    TransactionSystemClient client3 = getClient();
    TransactionSystemClient client4 = getClient();
    TransactionSystemClient client5 = getClient();

    Transaction tx1 = client1.startShort();
    Transaction tx2 = client2.startShort();
    Transaction tx3 = client3.startShort();
    Transaction tx4 = client4.startShort();
    Transaction tx5 = client5.startShort();

    Assert.assertTrue(client1.canCommit(tx1, $(C1)));
    Assert.assertTrue(client1.commit(tx1));

    Assert.assertTrue(client2.canCommit(tx2, $(C2)));
    Assert.assertTrue(client2.commit(tx2));

    // verifying conflicts detection
    Assert.assertFalse(client3.canCommit(tx3, $(C1)));
    Assert.assertFalse(client4.canCommit(tx4, $(C2)));
    Assert.assertTrue(client5.canCommit(tx5, $(C3)));
  }

  @Test
  public void testCommitTwice() {
    TransactionSystemClient client = getClient();
    Transaction tx = client.startShort();

    Assert.assertTrue(client.canCommit(tx, $(C1, C2)));
    Assert.assertTrue(client.commit(tx));
    // cannot commit twice same tx
    Assert.assertFalse(client.commit(tx));
  }

  @Test
  public void testAbortTwice() {
    TransactionSystemClient client = getClient();
    Transaction tx = client.startShort();

    Assert.assertTrue(client.canCommit(tx, $(C1, C2)));
    client.abort(tx);
    // abort of not active tx has no affect
    client.abort(tx);
  }

  @Test
  public void testReuseTx() {
    TransactionSystemClient client = getClient();
    Transaction tx = client.startShort();

    Assert.assertTrue(client.canCommit(tx, $(C1, C2)));
    Assert.assertTrue(client.commit(tx));
    // can't re-use same tx again
    Assert.assertFalse(client.canCommit(tx, $(C3, C4)));
    Assert.assertFalse(client.commit(tx));
    // abort of not active tx has no affect
    client.abort(tx);
  }

  @Test
  public void testUseNotStarted() {
    TransactionSystemClient client = getClient();
    Transaction tx1 = client.startShort();
    Assert.assertTrue(client.commit(tx1));

    // we know this is one is older than current writePointer and was not used
    Transaction txOld = new Transaction(tx1.getReadPointer(), tx1.getWritePointer() - 1,
                                        new long[] {}, new long[] {}, Transaction.NO_TX_IN_PROGRESS);
    Assert.assertFalse(client.canCommit(txOld, $(C3, C4)));
    Assert.assertFalse(client.commit(txOld));
    // abort of not active tx has no affect
    client.abort(txOld);

    // we know this is one is newer than current readPointer and was not used
    Transaction txNew = new Transaction(tx1.getReadPointer(), tx1.getWritePointer() + 1,
                                        new long[] {}, new long[] {}, Transaction.NO_TX_IN_PROGRESS);
    Assert.assertFalse(client.canCommit(txNew, $(C3, C4)));
    Assert.assertFalse(client.commit(txNew));
    // abort of not active tx has no affect
    client.abort(txNew);
  }

  @Test
  public void testAbortAfterCommit() {
    TransactionSystemClient client = getClient();
    Transaction tx = client.startShort();

    Assert.assertTrue(client.canCommit(tx, $(C1, C2)));
    Assert.assertTrue(client.commit(tx));
    // abort of not active tx has no affect
    client.abort(tx);
  }

  private Collection<byte[]> $(byte[]... val) {
    return Arrays.asList(val);
  }
}
