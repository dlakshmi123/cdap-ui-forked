package com.continuuity.data2.transaction;

import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.continuuity.data2.transaction.inmemory.InMemoryTxSystemClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Tests the transaction executor.
 */
public class TransactionExecutorTest {

  static final Injector INJECTOR = Guice.createInjector(Modules.override(
    new DataFabricModules().getInMemoryModules()).with(new AbstractModule() {
        @Override
        protected void configure() {
          InMemoryTransactionManager txManager = new InMemoryTransactionManager();
          txManager.startAndWait();
          bind(InMemoryTransactionManager.class).toInstance(txManager);
          bind(TransactionSystemClient.class).to(DummyTxClient.class).in(Singleton.class);
        }
      }));

  static final DummyTxClient TX_CLIENT = (DummyTxClient) INJECTOR.getInstance(TransactionSystemClient.class);
  static final TransactionExecutorFactory FACTORY = INJECTOR.getInstance(TransactionExecutorFactory.class);

  final DummyTxAware ds1 = new DummyTxAware(), ds2 = new DummyTxAware();
  final Collection<TransactionAware> txAwares = ImmutableList.<TransactionAware>of(ds1, ds2);

  private TransactionExecutor getExecutor() {
    return FACTORY.createExecutor(txAwares);
  }

  static final byte[] A = { 'a' };
  static final byte[] B = { 'b' };

  final TransactionExecutor.Function<Integer, Integer> testFunction =
    new TransactionExecutor.Function<Integer, Integer>() {
      @Override
      public Integer apply(@Nullable Integer input) {
        ds1.addChange(A);
        ds2.addChange(B);
        if (input == null) {
          throw new RuntimeException("function failed");
        }
        return input * input;
      }
  };

  @Before
  public void resetTxAwares() {
    ds1.reset();
    ds2.reset();
  }

  @Test
  public void testSuccessful() throws TransactionFailureException {
    // execute: add a change to ds1 and ds2
    Integer result = getExecutor().execute(testFunction, 10);
    // verify both are committed and post-committed
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertTrue(ds2.committed);
    Assert.assertTrue(ds1.postCommitted);
    Assert.assertTrue(ds2.postCommitted);
    Assert.assertFalse(ds1.rolledBack);
    Assert.assertFalse(ds2.rolledBack);
    Assert.assertTrue(100 == result);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Committed);
  }

  @Test
  public void testPostCommitFailure() throws TransactionFailureException {
    ds1.failPostCommitTxOnce = InduceFailure.ThrowException;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("post commit failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("post failure", e.getCause().getMessage());
    }
    // verify both are committed and post-committed
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertTrue(ds2.committed);
    Assert.assertTrue(ds1.postCommitted);
    Assert.assertTrue(ds2.postCommitted);
    Assert.assertFalse(ds1.rolledBack);
    Assert.assertFalse(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Committed);
  }

  @Test
  public void testPersistFailure() throws TransactionFailureException {
    ds1.failCommitTxOnce = InduceFailure.ThrowException;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("persist failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("persist failure", e.getCause().getMessage());
    }
    // verify both are rolled back and tx is aborted
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Aborted);
  }

  @Test
  public void testPersistFalse() throws TransactionFailureException {
    ds1.failCommitTxOnce = InduceFailure.ReturnFalse;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("persist failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertNull(e.getCause()); // in this case, the ds simply returned false
    }
    // verify both are rolled back and tx is aborted
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Aborted);
  }

  @Test
  public void testPersistAndRollbackFailure() throws TransactionFailureException {
    ds1.failCommitTxOnce = InduceFailure.ThrowException;
    ds1.failRollbackTxOnce = InduceFailure.ThrowException;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("persist failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("persist failure", e.getCause().getMessage());
    }
    // verify both are rolled back and tx is invalidated
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Invalidated);
  }

  @Test
  public void testPersistAndRollbackFalse() throws TransactionFailureException {
    ds1.failCommitTxOnce = InduceFailure.ReturnFalse;
    ds1.failRollbackTxOnce = InduceFailure.ReturnFalse;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("persist failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertNull(e.getCause()); // in this case, the ds simply returned false
    }
    // verify both are rolled back and tx is invalidated
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Invalidated);
  }

  @Test
  public void testCommitFalse() throws TransactionFailureException {
    TX_CLIENT.failCommitOnce = true;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("commit failed - exception should be thrown");
    } catch (TransactionConflictException e) {
      Assert.assertNull(e.getCause());
    }
    // verify both are rolled back and tx is aborted
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertTrue(ds1.committed);
    Assert.assertTrue(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Aborted);
  }

  @Test
  public void testCanCommitFalse() throws TransactionFailureException {
    TX_CLIENT.failCanCommitOnce = true;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("commit failed - exception should be thrown");
    } catch (TransactionConflictException e) {
      Assert.assertNull(e.getCause());
    }
    // verify both are rolled back and tx is aborted
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertTrue(ds2.checked);
    Assert.assertFalse(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Aborted);
  }

  @Test
  public void testChangesAndRollbackFailure() throws TransactionFailureException {
    ds1.failChangesTxOnce = InduceFailure.ThrowException;
    ds1.failRollbackTxOnce = InduceFailure.ThrowException;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("get changes failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("changes failure", e.getCause().getMessage());
    }
    // verify both are rolled back and tx is invalidated
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertTrue(ds1.checked);
    Assert.assertFalse(ds2.checked);
    Assert.assertFalse(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Invalidated);
  }

  @Test
  public void testFunctionAndRollbackFailure() throws TransactionFailureException {
    ds1.failRollbackTxOnce = InduceFailure.ReturnFalse;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, null);
      Assert.fail("function failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("function failed", e.getCause().getMessage());
    }
    // verify both are rolled back and tx is invalidated
    Assert.assertTrue(ds1.started);
    Assert.assertTrue(ds2.started);
    Assert.assertFalse(ds1.checked);
    Assert.assertFalse(ds2.checked);
    Assert.assertFalse(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertTrue(ds1.rolledBack);
    Assert.assertTrue(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Invalidated);
  }

  @Test
  public void testStartAndRollbackFailure() throws TransactionFailureException {
    ds1.failStartTxOnce = InduceFailure.ThrowException;
    // execute: add a change to ds1 and ds2
    try {
      getExecutor().execute(testFunction, 10);
      Assert.fail("start failed - exception should be thrown");
    } catch (TransactionFailureException e) {
      Assert.assertEquals("start failure", e.getCause().getMessage());
    }
    // verify both are not rolled back and tx is aborted
    Assert.assertTrue(ds1.started);
    Assert.assertFalse(ds2.started);
    Assert.assertFalse(ds1.checked);
    Assert.assertFalse(ds2.checked);
    Assert.assertFalse(ds1.committed);
    Assert.assertFalse(ds2.committed);
    Assert.assertFalse(ds1.postCommitted);
    Assert.assertFalse(ds2.postCommitted);
    Assert.assertFalse(ds1.rolledBack);
    Assert.assertFalse(ds2.rolledBack);
    Assert.assertEquals(TX_CLIENT.state, DummyTxClient.CommitState.Aborted);
  }

  enum InduceFailure { NoFailure, ReturnFalse, ThrowException }

  static class DummyTxAware implements TransactionAware {

    Transaction tx;
    boolean started = false;
    boolean committed = false;
    boolean checked = false;
    boolean rolledBack = false;
    boolean postCommitted = false;
    List<byte[]> changes = Lists.newArrayList();

    InduceFailure failStartTxOnce = InduceFailure.NoFailure;
    InduceFailure failChangesTxOnce = InduceFailure.NoFailure;
    InduceFailure failCommitTxOnce = InduceFailure.NoFailure;
    InduceFailure failPostCommitTxOnce = InduceFailure.NoFailure;
    InduceFailure failRollbackTxOnce = InduceFailure.NoFailure;

    void addChange(byte[] key) {
      changes.add(key);
    }

    void reset() {
      tx = null;
      started = false;
      checked = false;
      committed = false;
      rolledBack = false;
      postCommitted = false;
      changes.clear();
    }

    @Override
    public void startTx(Transaction tx) {
      reset();
      started = true;
      this.tx = tx;
      if (failStartTxOnce == InduceFailure.ThrowException) {
        failStartTxOnce = InduceFailure.NoFailure;
        throw new RuntimeException("start failure");
      }
    }

    @Override
    public Collection<byte[]> getTxChanges() {
      checked = true;
      if (failChangesTxOnce == InduceFailure.ThrowException) {
        failChangesTxOnce = InduceFailure.NoFailure;
        throw new RuntimeException("changes failure");
      }
      return ImmutableList.copyOf(changes);
    }

    @Override
    public boolean commitTx() throws Exception {
      committed = true;
      if (failCommitTxOnce == InduceFailure.ThrowException) {
        failCommitTxOnce = InduceFailure.NoFailure;
        throw new RuntimeException("persist failure");
      }
      if (failCommitTxOnce == InduceFailure.ReturnFalse) {
        failCommitTxOnce = InduceFailure.NoFailure;
        return false;
      }
      return true;
    }

    @Override
    public void postTxCommit() {
      postCommitted = true;
      if (failPostCommitTxOnce == InduceFailure.ThrowException) {
        failPostCommitTxOnce = InduceFailure.NoFailure;
        throw new RuntimeException("post failure");
      }
    }

    @Override
    public boolean rollbackTx() throws Exception {
      rolledBack = true;
      if (failRollbackTxOnce == InduceFailure.ThrowException) {
        failRollbackTxOnce = InduceFailure.NoFailure;
        throw new RuntimeException("rollback failure");
      }
      if (failRollbackTxOnce == InduceFailure.ReturnFalse) {
        failRollbackTxOnce = InduceFailure.NoFailure;
        return false;
      }
      return true;
    }

    @Override
    public String getName() {
      return "dummy";
    }
  }

  static class DummyTxClient extends InMemoryTxSystemClient {

    boolean failCanCommitOnce = false;
    boolean failCommitOnce = false;
    enum CommitState {
      Started, Committed, Aborted, Invalidated
    }
    CommitState state = CommitState.Started;

    @Inject
    DummyTxClient(InMemoryTransactionManager txmgr) {
      super(txmgr);
    }

    @Override
    public boolean canCommit(Transaction tx, Collection<byte[]> changeIds) throws TransactionNotInProgressException {
      if (failCanCommitOnce) {
        failCanCommitOnce = false;
        return false;
      } else {
        return super.canCommit(tx, changeIds);
      }
    }

    @Override
    public boolean commit(Transaction tx) throws TransactionNotInProgressException {
      if (failCommitOnce) {
        failCommitOnce = false;
        return false;
      } else {
        state = CommitState.Committed;
        return super.commit(tx);
      }
    }

    @Override
    public Transaction startLong() {
      state = CommitState.Started;
      return super.startLong();
    }

    @Override
    public Transaction startShort() {
      state = CommitState.Started;
      return super.startShort();
    }

    @Override
    public Transaction startShort(int timeout) {
      state = CommitState.Started;
      return super.startShort(timeout);
    }

    @Override
    public void abort(Transaction tx) {
      state = CommitState.Aborted;
      super.abort(tx);
    }

    @Override
    public boolean invalidate(long tx) {
      state = CommitState.Invalidated;
      return super.invalidate(tx);
    }
  }
}
