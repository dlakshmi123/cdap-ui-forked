/*
 * Copyright 2014 Cask, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.data2.dataset2;

import co.cask.cdap.api.dataset.Dataset;
import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.module.DatasetModule;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides access to the Datasets System.
 *
 * Typical usage example:
 * <tt>
 *   DatasetFramework datasetFramework = ...;
 *   datasetFramework.addModule("myDatasets", MyDatasetModule.class);
 *   datasetFramework.addInstance("myTable", "table", DatasetProperties.EMPTY);
 *   TableAdmin admin = datasetFramework.getAdmin("myTable");
 *   admin.create();
 *   Table table = datasetFramework.getDataset("myTable");
 *   try {
 *     table.write("key", "value");
 *   } finally {
 *     table.close();
 *   }
 * </tt>
 */
// todo: use dataset instead of dataset instance in namings
public interface DatasetFramework {

  /**
   * Adds dataset types by adding dataset module to the system.
   * @param moduleName dataset module name
   * @param module dataset module
   * @throws ModuleConflictException when module with same name is already registered or this module registers a type
   *         with a same name as one of the already registered by another module types
   * @throws DatasetManagementException in case of problems
   */
  void addModule(String moduleName, DatasetModule module)
    throws DatasetManagementException;

  /**
   * Deletes dataset module and its types from the system.
   * @param moduleName dataset module name
   * @throws ModuleConflictException when module cannot be deleted because of its dependant modules or instances
   * @throws DatasetManagementException
   */
  void deleteModule(String moduleName)
    throws DatasetManagementException;

  /**
   * Deletes dataset modules and its types from the system.
   * @throws ModuleConflictException when some of modules can't be deleted because of its dependant modules or instances
   * @throws DatasetManagementException
   */
  void deleteAllModules()
    throws DatasetManagementException;

  /**
   * Adds information about dataset instance to the system.
   *
   * This uses
   * {@link co.cask.cdap.api.dataset.DatasetDefinition#configure(String, DatasetProperties)}
   * method to build {@link co.cask.cdap.api.dataset.DatasetSpecification} which describes dataset instance
   * and later used to initialize {@link DatasetAdmin} and {@link Dataset} for the dataset instance.
   *
   * @param datasetTypeName dataset instance type name
   * @param datasetInstanceName dataset instance name
   * @param props dataset instance properties
   * @throws InstanceConflictException if dataset instance with this name already exists
   * @throws IOException when creation of dataset instance using its admin fails
   * @throws DatasetManagementException
   */
  void addInstance(String datasetTypeName, String datasetInstanceName, DatasetProperties props)
    throws DatasetManagementException, IOException;

  /**
   * Updates the existing dataset instance in the system.
   *
   * This uses
   * {@link co.cask.cdap.api.dataset.DatasetDefinition#configure(String, DatasetProperties)}
   * method to build {@link co.cask.cdap.api.dataset.DatasetSpecification} with new properties,
   * which describes dataset instance and {@link DatasetAdmin} is used to upgrade
   * {@link Dataset} for the dataset instance.
   * @param datasetInstanceName dataset instance name
   * @param props dataset instance properties
   * @throws IOException when creation of dataset instance using its admin fails
   * @throws DatasetManagementException
   */
  void updateInstance(String datasetInstanceName, DatasetProperties props)
    throws DatasetManagementException, IOException;

  /**
   * @return a collection of {@link co.cask.cdap.api.dataset.DatasetSpecification}s for all datasets
   */
  Collection<DatasetSpecification> getInstances() throws DatasetManagementException;

  /**
   * @return {@link DatasetSpecification} of the dataset or {@code null} if dataset not not exist
   */
  @Nullable
  DatasetSpecification getDatasetSpec(String name) throws DatasetManagementException;

  /**
   * @return true if instance exists, false otherwise
   * @throws DatasetManagementException
   */
  boolean hasInstance(String instanceName) throws DatasetManagementException;

  /**
   * @return true if type exists, false otherwise
   * @throws DatasetManagementException
   */
  boolean hasType(String typeName) throws DatasetManagementException;

  /**
   * Deletes dataset instance from the system.
   *
   * @param datasetInstanceName dataset instance name
   * @throws InstanceConflictException if dataset instance cannot be deleted because of its dependencies
   * @throws IOException when deletion of dataset instance using its admin fails
   * @throws DatasetManagementException
   */
  void deleteInstance(String datasetInstanceName) throws DatasetManagementException, IOException;

  /**
   * Deletes all dataset instances from the system.
   *
   * @throws IOException when deletion of dataset instance using its admin fails
   * @throws DatasetManagementException
   */
  void deleteAllInstances() throws DatasetManagementException, IOException;

  /**
   * Gets dataset instance admin to be used to perform administrative operations.
   * @param datasetInstanceName dataset instance name
   * @param <T> dataset admin type
   * @param classLoader classLoader to be used to load classes or {@code null} to use system classLoader
   * @return instance of dataset admin or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link DatasetAdmin}
   */
  @Nullable
  <T extends DatasetAdmin> T getAdmin(String datasetInstanceName, @Nullable ClassLoader classLoader)
    throws DatasetManagementException, IOException;

  /**
   * Gets dataset to be used to perform data operations.
   * @param datasetInstanceName dataset instance name
   * @param <T> dataset type to be returned
   * @param arguments runtime arguments for the dataset instance
   * @param classLoader classLoader to be used to load classes or {@code null} to use system classLoader
   * @return instance of dataset or {@code null} if dataset instance of this name doesn't exist.
   * @throws DatasetManagementException when there's trouble getting dataset meta info
   * @throws IOException when there's trouble to instantiate {@link co.cask.cdap.api.dataset.Dataset}
   */
  @Nullable
  <T extends Dataset> T getDataset(String datasetInstanceName, @Nullable Map<String, String> arguments,
                                   @Nullable ClassLoader classLoader)
    throws DatasetManagementException, IOException;
}
