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

package com.continuuity.gateway.handlers;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.module.DatasetDefinitionRegistry;
import com.continuuity.api.dataset.module.DatasetModule;
import com.continuuity.app.store.ServiceStore;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.datafabric.ReactorDatasetNamespace;
import com.continuuity.data2.datafabric.dataset.DatasetsUtil;
import com.continuuity.data2.dataset2.DatasetDefinitionRegistryFactory;
import com.continuuity.data2.dataset2.DatasetFramework;
import com.continuuity.data2.dataset2.DefaultDatasetDefinitionRegistry;
import com.continuuity.data2.dataset2.InMemoryDatasetFramework;
import com.continuuity.data2.dataset2.NamespacedDatasetFramework;
import com.continuuity.data2.dataset2.lib.kv.NoTxKeyValueTable;
import com.continuuity.tephra.TransactionFailureException;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * DataSetService Store implements ServiceStore using DataSets without Transaction.
 */
public final class DatasetServiceStore implements ServiceStore {
  private final NoTxKeyValueTable table;

  @Inject
  public DatasetServiceStore(CConfiguration cConf, DatasetDefinitionRegistryFactory dsRegistryFactory,
                             @Named("serviceModule") DatasetModule datasetModule) throws Exception {
    DatasetFramework dsFramework =
      new NamespacedDatasetFramework(new InMemoryDatasetFramework(dsRegistryFactory),
                                     new ReactorDatasetNamespace(cConf, DataSetAccessor.Namespace.SYSTEM));
    dsFramework.addModule("basicKVTable", datasetModule);
    table = DatasetsUtil.getOrCreateDataset(dsFramework, Constants.Service.SERVICE_INSTANCE_TABLE_NAME,
                                            NoTxKeyValueTable.class.getName(),
                                            DatasetProperties.EMPTY, null);
  }

  @Override
  public synchronized Integer getServiceInstance(final String serviceName) throws TransactionFailureException {
    String count = Bytes.toString(table.get(Bytes.toBytes(serviceName)));
    return (count != null) ? Integer.valueOf(count) : null;
  }

  @Override
  public synchronized void setServiceInstance(final String serviceName, final int instances) {
    table.put(Bytes.toBytes(serviceName), Bytes.toBytes(String.valueOf(instances)));
  }
}
