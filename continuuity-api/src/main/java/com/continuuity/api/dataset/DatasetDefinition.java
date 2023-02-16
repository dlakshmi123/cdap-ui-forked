/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.api.dataset;

import com.continuuity.api.annotation.Beta;

import java.io.IOException;

/**
 * Defines named dataset type implementation.
 *
 * The dataset implementation defines:
 * <ul>
 *   <li>
 *     a way to configure new dataset instance by using {@link #configure(String, DatasetProperties)} method
 *   </li>
 *   <li>
 *     a way to perform administrative operations on the dataset instance (e.g. create, drop etc.) by providing
 *     implementation of {@link DatasetAdmin} via {@link #getAdmin(DatasetSpecification, ClassLoader)} method
 *   </li>
 *   <li>
 *     a way to perform operations to manipulate data of the dataset instance (e.g. read, write etc.) by providing
 *     implementation of {@link Dataset} via {@link #getDataset(DatasetSpecification, ClassLoader)} method
 *   </li>
 * </ul>
 *
 * @param <D> defines data operations that can be performed on this dataset instance
 * @param <A> defines administrative operations that can be performed on this dataset instance
 */
@Beta
public interface DatasetDefinition<D extends Dataset, A extends DatasetAdmin> {

  /**
   * @return name of this dataset implementation
   */
  String getName();

  /**
   * Configures new instance of the dataset.
   * @param instanceName name of the instance
   * @param properties instance configuration properties
   * @return instance of {@link DatasetSpecification} that fully describes dataset instance.
   *         The {@link DatasetSpecification} can be used to create {@link DatasetAdmin} and {@link Dataset} to perform
   *         administrative and data operations respectively, see {@link #getAdmin(DatasetSpecification, ClassLoader)}
   *         and {@link #getDataset(DatasetSpecification, ClassLoader)}.
   */
  DatasetSpecification configure(String instanceName, DatasetProperties properties);

  /**
   * Provides dataset admin to be used to perform administrative operations on the dataset instance defined by passed
   * {@link DatasetSpecification}.
   *
   * @param spec specification of the dataset instance.
   * @param classLoader classloader to use when executing admin operations
   * @return dataset admin to perform administrative operations
   * @throws IOException
   */
  A getAdmin(DatasetSpecification spec, ClassLoader classLoader) throws IOException;

  /**
   * Provides dataset to be used to perform data operations on the dataset instance data defined by passed
   * {@link DatasetSpecification}.
   *
   * @param spec specification of the dataset instance.
   * @param classLoader classloader to use when executing dataset operations
   * @return dataset to perform object operations
   * @throws IOException
   */
  D getDataset(DatasetSpecification spec, ClassLoader classLoader) throws IOException;
}
