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

package com.continuuity.api.dataset.lib;

import com.continuuity.api.annotation.Beta;
import com.continuuity.api.data.batch.BatchReadable;
import com.continuuity.api.data.batch.BatchWritable;
import com.continuuity.api.dataset.Dataset;

/**
 * A dataset that stores objects of a particular class into a table.
 * <p>
 * Supported types are:
 * </p>
 * <p>
 * <ul>
 *   <li>a plain java class</li>
 *   <li>a parametrized class</li>
 *   <li>a static inner class of one of the above</li>
 * </ul>
 *</p>
 * Interfaces and not-static inner classes are not supported.
 *
 * @param <T> the type of objects in the store
 */
@Beta
public interface ObjectStore<T> extends Dataset, BatchReadable<byte[], T>, BatchWritable<byte[], T> {

  /**
   * Write an object with a given key.
   *
   * @param key the key of the object
   * @param object the object to be stored
   */
  void write(String key, T object);

  /**
   * Write an object with a given key.
   *
   * @param key the key of the object
   * @param object the object to be stored
   */
  void write(byte[] key, T object);

  /**
   * Read an object with a given key.
   * @param key the key of the object
   * @return the object if found, or null if not found
   */
  T read(String key);

  /**
   * Read an object with a given key.
   * @param key the key of the object
   * @return the object if found, or null if not found
   */
  T read(byte[] key);
}
