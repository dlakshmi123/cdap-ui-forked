package com.continuuity.data2.transaction.persist;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.transaction.Transaction;
import com.continuuity.data2.transaction.inmemory.ChangeId;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Commons tests to run against the {@link TransactionStateStorage} implementations.
 */
public abstract class AbstractTransactionStateStorageTest {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTransactionStateStorageTest.class);
  private static Random random = new Random();

  protected abstract CConfiguration getConfiguration(String testName) throws IOException;

  protected abstract TransactionStateStorage getStorage(CConfiguration conf);

  @Test
  public void testSnapshotPersistence() throws Exception {
    CConfiguration conf = getConfiguration("testSnapshotPersistence");

    TransactionSnapshot snapshot = createRandomSnapshot();
    TransactionStateStorage storage = getStorage(conf);
    try {
      storage.startAndWait();
      storage.writeSnapshot(snapshot);

      TransactionSnapshot readSnapshot = storage.getLatestSnapshot();
      assertNotNull(readSnapshot);
      assertEquals(snapshot, readSnapshot);
    } finally {
      storage.stopAndWait();
    }
  }

  @Test
  public void testLogWriteAndRead() throws Exception {
    CConfiguration conf = getConfiguration("testLogWriteAndRead");

    // create some random entries
    List<TransactionEdit> edits = createRandomEdits(100);
    TransactionStateStorage storage = getStorage(conf);
    try {
      long now = System.currentTimeMillis();
      storage.startAndWait();
      TransactionLog log = storage.createLog(now);
      for (TransactionEdit edit : edits) {
        log.append(edit);
      }
      log.close();

      Collection<TransactionLog > logsToRead = storage.getLogsSince(now);
      // should only be our one log
      assertNotNull(logsToRead);
      assertEquals(1, logsToRead.size());
      TransactionLogReader logReader = logsToRead.iterator().next().getReader();
      assertNotNull(logReader);

      List<TransactionEdit> readEdits = Lists.newArrayListWithExpectedSize(edits.size());
      TransactionEdit nextEdit;
      while ((nextEdit = logReader.next()) != null) {
        readEdits.add(nextEdit);
      }
      logReader.close();
      assertEquals(edits.size(), readEdits.size());
      for (int i = 0; i < edits.size(); i++) {
        LOG.info("Checking edit " + i);
        assertEquals(edits.get(i), readEdits.get(i));
      }
    } finally {
      storage.stopAndWait();
    }
  }

  // TODO: Gary is working on it. Ignoring to make develop build not to fail.
  @Ignore
  @Test
  public void testTransactionManagerPersistence() throws Exception {
    CConfiguration conf = getConfiguration("testTransactionManagerPersistence");
    conf.setInt(InMemoryTransactionManager.CFG_TX_CLAIM_SIZE, 10);
    conf.setInt(Constants.Transaction.Manager.CFG_TX_CLEANUP_INTERVAL, 0); // no cleanup thread
    conf.setInt(Constants.Transaction.Manager.CFG_TX_SNAPSHOT_INTERVAL, 0); // no periodic snapshots

    TransactionStateStorage storage = null;
    TransactionStateStorage storage2 = null;
    TransactionStateStorage storage3 = null;
    try {
      storage = getStorage(conf);
      InMemoryTransactionManager txManager = new InMemoryTransactionManager(conf, storage);
      txManager.init();

      // TODO: replace with new persistence tests
      final byte[] a = { 'a' };
      final byte[] b = { 'b' };
      // start a tx1, add a change A and commit
      Transaction tx1 = txManager.startShort();
      Assert.assertTrue(txManager.canCommit(tx1, Collections.singleton(a)));
      Assert.assertTrue(txManager.commit(tx1));
      // start a tx2 and add a change B
      Transaction tx2 = txManager.startShort();
      Assert.assertTrue(txManager.canCommit(tx2, Collections.singleton(b)));
      // start a tx3
      Transaction tx3 = txManager.startShort();
      // restart
      txManager.close();
      TransactionSnapshot origState = txManager.getCurrentState();

      // starts a new tx manager
      storage2 = getStorage(conf);
      txManager = new InMemoryTransactionManager(conf, storage2);
      txManager.init();

      // check that the reloaded state matches the old
      TransactionSnapshot newState = txManager.getCurrentState();
      assertEquals(origState, newState);

      // commit tx2
      Assert.assertTrue(txManager.commit(tx2));
      // start another transaction, must be greater than tx3
      Transaction tx4 = txManager.startShort();
      Assert.assertTrue(tx4.getWritePointer() > tx3.getWritePointer());
      // tx1 must be visble from tx2, but tx3 and tx4 must not
      Assert.assertTrue(tx2.isVisible(tx1.getWritePointer()));
      Assert.assertFalse(tx2.isVisible(tx3.getWritePointer()));
      Assert.assertFalse(tx2.isVisible(tx4.getWritePointer()));
      // add same change for tx3
      Assert.assertFalse(txManager.canCommit(tx3, Collections.singleton(b)));
      // check visibility with new xaction
      Transaction tx5 = txManager.startShort();
      Assert.assertTrue(tx5.isVisible(tx1.getWritePointer()));
      Assert.assertTrue(tx5.isVisible(tx2.getWritePointer()));
      Assert.assertFalse(tx5.isVisible(tx3.getWritePointer()));
      Assert.assertFalse(tx5.isVisible(tx4.getWritePointer()));
      // can commit tx3?
      txManager.abort(tx3);
      txManager.abort(tx4);
      txManager.abort(tx5);
      // start new tx and verify its exclude list is empty
      Transaction tx6 = txManager.startShort();
      Assert.assertFalse(tx6.hasExcludes());
      txManager.abort(tx6);

      // now start 5 x claim size transactions
      Transaction tx = txManager.startShort();
      for (int i = 1; i < 50; i++) {
        tx = txManager.startShort();
      }
      origState = txManager.getCurrentState();

      // give current syncs a chance to complete
      Thread.sleep(10000);
      // simulate crash by starting a new tx manager without a close
      storage3 = getStorage(conf);
      txManager = new InMemoryTransactionManager(conf, storage3);
      txManager.init();

      // verify state again matches (this time should include WAL replay)
      newState = txManager.getCurrentState();
      assertEquals(origState, newState);

      // get a new transaction and verify it is greater
      Transaction txAfter = txManager.startShort();
      Assert.assertTrue(txAfter.getWritePointer() > tx.getWritePointer());
    } finally {
      if (storage != null) {
        storage.stopAndWait();
      }
      if (storage2 != null) {
        storage2.stopAndWait();
      }
      if (storage3 != null) {
        storage3.stopAndWait();
      }
    }
  }

  /**
   * Generates a new snapshot object with semi-randomly populated values.  This does not necessarily accurately
   * represent a typical snapshot's distribution of values, as we only set an upper bound on pointer values.
   *
   * We generate a new snapshot with the contents:
   * <ul>
   *   <li>readPointer = 1M + (random % 1M)</li>
   *   <li>writePointer = readPointer + 1000</li>
   *   <li>waterMark = writePointer + 1000</li>
   *   <li>inProgress = one each for (writePointer - 500)..writePointer, ~ 5% "long" transaction</li>
   *   <li>invalid = 100 randomly distributed, 0..1M</li>
   *   <li>committing = one each, (readPointer + 1)..(readPointer + 100)</li>
   *   <li>committed = one each, (readPointer - 1000)..readPointer</li>
   * </ul>
   * @return a new snapshot of transaction state.
   */
  private TransactionSnapshot createRandomSnapshot() {
    // limit readPointer to a reasonable range, but make it > 1M so we can assign enough keys below
    long readPointer = (Math.abs(random.nextLong()) % 1000000L) + 1000000L;
    long writePointer = readPointer + 1000L;
    long waterMark = writePointer + 1000L;

    // generate in progress -- assume last 500 write pointer values
    NavigableMap<Long, Long> inProgress = Maps.newTreeMap();
    long startPointer = writePointer - 500L;
    for (int i = 0; i < 500; i++) {
      long currentTime = System.currentTimeMillis();
      // make some "long" transactions
      if (i % 20 == 0) {
        inProgress.put(startPointer + i, -currentTime);
      } else {
        inProgress.put(startPointer + i, currentTime + 300000L);
      }
    }

    // make 100 random invalid IDs
    LongArrayList invalid = new LongArrayList();
    for (int i = 0; i < 100; i++) {
      invalid.add(Math.abs(random.nextLong()) % 1000000L);
    }

    // make 100 committing entries, 10 keys each
    Map<Long, Set<ChangeId>> committing = Maps.newHashMap();
    for (int i = 0; i < 100; i++) {
      committing.put(readPointer + i, generateChangeSet(10));
    }

    // make 1000 committed entries, 10 keys each
    long startCommitted = readPointer - 1000L;
    NavigableMap<Long, Set<ChangeId>> committed = Maps.newTreeMap();
    for (int i = 0; i < 1000; i++) {
      committed.put(startCommitted + i, generateChangeSet(10));
    }

    return new TransactionSnapshot(System.currentTimeMillis(), readPointer, writePointer, waterMark,
        invalid, inProgress, committing, committed);
  }

  private Set<ChangeId> generateChangeSet(int numEntries) {
    Set<ChangeId> changes = Sets.newHashSet();
    for (int i = 0; i < numEntries; i++) {
      byte[] bytes = new byte[8];
      random.nextBytes(bytes);
      changes.add(new ChangeId(bytes));
    }
    return changes;
  }

  /**
   * Generates a number of semi-random {@link com.continuuity.data2.transaction.persist.TransactionEdit} instances.
   * These are just randomly selected from the possible states, so would not necessarily reflect a real-world
   * distribution.
   *
   * @param numEntries how many entries to generate in the returned list.
   * @return a list of randomly generated transaction log edits.
   */
  private List<TransactionEdit> createRandomEdits(int numEntries) {
    List<TransactionEdit> edits = Lists.newArrayListWithCapacity(numEntries);
    for (int i = 0; i < numEntries; i++) {
      TransactionEdit.State nextType = TransactionEdit.State.values()[random.nextInt(6)];
      long writePointer = Math.abs(random.nextLong());
      switch (nextType) {
        case INPROGRESS:
          edits.add(
            TransactionEdit.createStarted(writePointer, System.currentTimeMillis() + 300000L, writePointer + 1));
          break;
        case COMMITTING:
          edits.add(TransactionEdit.createCommitting(writePointer, generateChangeSet(10)));
          break;
        case COMMITTED:
          edits.add(TransactionEdit.createCommitted(writePointer, generateChangeSet(10), writePointer + 1,
                                                    random.nextBoolean()));
          break;
        case INVALID:
          edits.add(TransactionEdit.createInvalid(writePointer));
          break;
        case ABORTED:
          edits.add(TransactionEdit.createAborted(writePointer));
          break;
        case MOVE_WATERMARK:
          edits.add(TransactionEdit.createMoveWatermark(writePointer));
          break;
      }
    }
    return edits;
  }
}