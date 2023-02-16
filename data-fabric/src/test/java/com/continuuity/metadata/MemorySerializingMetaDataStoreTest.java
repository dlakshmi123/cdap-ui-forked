package com.continuuity.metadata;

import com.continuuity.api.data.OperationException;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.dataset.lib.table.OrderedColumnarTable;
import com.continuuity.data2.transaction.TransactionExecutorFactory;
import com.continuuity.data2.transaction.inmemory.InMemoryTransactionManager;
import com.google.common.base.Throwables;
import org.junit.BeforeClass;

/**
 * Memory metadata store tests.
 */
public class MemorySerializingMetaDataStoreTest extends MemoryMetaDataStoreTest {

  @BeforeClass
  public static void setupMDS() throws Exception {
    injector.getInstance(InMemoryTransactionManager.class).init();
    mds = new SerializingMetaDataTable(injector.getInstance(TransactionExecutorFactory.class),
                                        injector.getInstance(DataSetAccessor.class));
  }

  void clearMetaData() throws OperationException {
    try {
      injector.getInstance(DataSetAccessor.class)
              .getDataSetManager(OrderedColumnarTable.class, DataSetAccessor.Namespace.SYSTEM)
              .truncate(MetaDataTable.META_DATA_TABLE_NAME);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

}
