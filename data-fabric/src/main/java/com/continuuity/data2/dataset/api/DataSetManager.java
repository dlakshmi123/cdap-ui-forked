package com.continuuity.data2.dataset.api;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Common interface for managing dataset and non-tx ops on the dataset.
 */
//todo: define nice exception class?
public interface DataSetManager {
  /**
   * @param name dataset name
   * @return true if dataset with given name exists, otherwise false
   * @throws Exception if check fails
   */
  boolean exists(String name) throws Exception;

  /**
   * Creates dataset if doesn't exist. If dataset exists does nothing.
   * @param name name of the dataset to create
   * @throws Exception if creation fails
   */
  void create(String name) throws Exception;

  /**
   * Creates dataset if doesn't exist. If dataset exists does nothing.
   * @param name name of the dataset to create
   * @param name additional properties
   * @throws Exception if creation fails
   */
  void create(String name, @Nullable Properties props) throws Exception;

  /**
   * Wipes out dataset data.
   * @param name dataset name
   * @throws Exception if cleanup fails
   */
  void truncate(String name) throws Exception;

  /**
   * Deletes dataset from the system completely.
   * @param name dataset name
   * @throws Exception if deletion fails
   */
  void drop(String name) throws Exception;
}
