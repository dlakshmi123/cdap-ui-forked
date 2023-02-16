package com.continuuity.data.operation.executor.omid;

import com.continuuity.data.engine.memory.MemoryOVCTableHandle;
import com.continuuity.data.operation.executor.OperationExecutor;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.data.table.OVCTableHandle;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestMemoryOmidExecutorLikeAFlow extends TestOmidExecutorLikeAFlow {

  private static final Injector injector =
      Guice.createInjector(new DataFabricModules().getInMemoryModules());

  private static final OmidTransactionalOperationExecutor executor =
      (OmidTransactionalOperationExecutor) injector.getInstance(
          OperationExecutor.class);

  private static final OVCTableHandle handle = executor.getTableHandle();

  @Override
  protected OmidTransactionalOperationExecutor getOmidExecutor() {
    return executor;
  }

  @Override
  protected OVCTableHandle getTableHandle() {
    return handle;
  }

  @Override
  protected int getNumIterations() {
    return 100;
  }

  @Override
  public void testInjection() {
    assertTrue(injector.getInstance(OVCTableHandle.class) instanceof MemoryOVCTableHandle);
  }
}
