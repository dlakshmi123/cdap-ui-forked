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
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.DatasetSpecification;
import com.continuuity.api.dataset.table.Table;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.IOException;

/**
 * DatasetDefinition for {@link IndexedObjectStore}.
 */
@Beta
public class IndexedObjectStoreDefinition
  extends AbstractDatasetDefinition<IndexedObjectStore, DatasetAdmin> {

  private final DatasetDefinition<? extends Table, ?> tableDef;
  private final DatasetDefinition<? extends ObjectStore, ?> objectStoreDef;

  public IndexedObjectStoreDefinition(String name,
                                      DatasetDefinition<? extends Table, ?> tableDef,
                                      DatasetDefinition<? extends ObjectStore, ?> objectStoreDef) {
    super(name);
    Preconditions.checkArgument(tableDef != null, "Table definition is required");
    Preconditions.checkArgument(objectStoreDef != null, "ObjectStore definition is required");
    this.tableDef = tableDef;
    this.objectStoreDef = objectStoreDef;
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    return DatasetSpecification.builder(instanceName, getName())
      .properties(properties.getProperties())
      .datasets(tableDef.configure("index", properties),
                objectStoreDef.configure("data", properties))
      .build();
  }

  @Override
  public DatasetAdmin getAdmin(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    return new CompositeDatasetAdmin(Lists.newArrayList(
      tableDef.getAdmin(spec.getSpecification("index"), classLoader),
      objectStoreDef.getAdmin(spec.getSpecification("data"), classLoader)
    ));
  }

  @Override
  public IndexedObjectStore<?> getDataset(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    DatasetSpecification tableSpec = spec.getSpecification("index");
    DatasetSpecification objectStoreSpec = spec.getSpecification("data");

    Table index = tableDef.getDataset(tableSpec, classLoader);
    ObjectStore<?> objectStore = objectStoreDef.getDataset(objectStoreSpec, classLoader);

    return new IndexedObjectStore(spec.getName(), objectStore, index);
  }

}
