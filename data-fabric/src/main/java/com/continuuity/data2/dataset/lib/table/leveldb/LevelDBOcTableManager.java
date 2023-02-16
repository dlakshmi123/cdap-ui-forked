package com.continuuity.data2.dataset.lib.table.leveldb;

import com.continuuity.data2.dataset.api.DataSetManager;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Manages LevelDB tables.
 */
public class LevelDBOcTableManager implements DataSetManager {

  private static final Logger LOG = LoggerFactory.getLogger(LevelDBOcTableManager.class);

  private final LevelDBOcTableService service;

  @Inject
  public LevelDBOcTableManager(LevelDBOcTableService service) throws IOException {
    this.service = service;
  }

  @Override
  public boolean exists(String name) throws Exception {
    try {
      service.getTable(name);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void create(String name) throws Exception {
    service.ensureTableExists(name);
  }

  @Override
  public void drop(String name) throws Exception {
    service.dropTable(name);
  }

  @Override
  public void truncate(String name) throws Exception {
    drop(name);
    create(name);
  }

}
