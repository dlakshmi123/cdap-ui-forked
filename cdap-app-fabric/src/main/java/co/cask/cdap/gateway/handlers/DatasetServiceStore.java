/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.gateway.handlers;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.app.store.ServiceStore;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.data2.datafabric.dataset.DatasetsUtil;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.lib.kv.NoTxKeyValueTable;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.RestartServiceInstancesStatus;
import co.cask.cdap.proto.RestartServiceInstancesStatus.RestartStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ranges;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Set;

/**
 * DatasetService Store implements ServiceStore using Datasets without Transaction.
 */
public final class DatasetServiceStore extends AbstractIdleService implements ServiceStore {
  private static final Gson GSON = new Gson();

  private final DatasetFramework dsFramework;
  private NoTxKeyValueTable table;

  @Inject
  public DatasetServiceStore(@Named("local.ds.framework") DatasetFramework dsFramework) throws Exception {
    this.dsFramework = dsFramework;
  }

  @Override
  public synchronized Integer getServiceInstance(final String serviceName) {
    String count = Bytes.toString(table.get(Bytes.toBytes(serviceName)));
    return (count != null) ? Integer.valueOf(count) : null;
  }

  @Override
  public synchronized void setServiceInstance(final String serviceName, final int instances) {
    table.put(Bytes.toBytes(serviceName), Bytes.toBytes(String.valueOf(instances)));
  }

  @Override
  protected void startUp() throws Exception {
    Id.DatasetInstance serviceStoreDatasetInstanceId =
      Id.DatasetInstance.from(Constants.SYSTEM_NAMESPACE, Constants.Service.SERVICE_INSTANCE_TABLE_NAME);
    table = DatasetsUtil.getOrCreateDataset(dsFramework, serviceStoreDatasetInstanceId,
                                            NoTxKeyValueTable.class.getName(),
                                            DatasetProperties.EMPTY, null, null);
  }

  @Override
  protected void shutDown() throws Exception {
    table.close();
  }

  @Override
  public synchronized void setRestartInstanceRequest(String serviceName, long startTimeMs, long endTimeMs,
                                                     boolean isSuccess, int instanceId) {
    Preconditions.checkNotNull(serviceName, "Service name should not be null.");
    Preconditions.checkArgument(instanceId >= 0, "Instance id has to be greater than or equal to zero.");

    RestartStatus status = isSuccess ? RestartStatus.SUCCESS : RestartStatus.FAILURE;
    RestartServiceInstancesStatus restartStatus =
      new RestartServiceInstancesStatus(serviceName, startTimeMs, endTimeMs, status, ImmutableSet.of(instanceId));
    String toJson = GSON.toJson(restartStatus, RestartServiceInstancesStatus.class);

    table.put(Bytes.toBytes(serviceName + "-restart"), Bytes.toBytes(toJson));
  }

  @Override
  public synchronized void setRestartAllInstancesRequest(String serviceName, long startTimeMs, long endTimeMs,
                                                         boolean isSuccess) {
    Preconditions.checkNotNull(serviceName, "Service name should not be null.");

    RestartStatus status = isSuccess ? RestartStatus.SUCCESS : RestartStatus.FAILURE;
    int instanceCount = (this.getServiceInstance(serviceName) == null) ? 0 : this.getServiceInstance(serviceName);
    Set<Integer> instancesToRestart = Ranges.closedOpen(0, instanceCount).asSet(DiscreteDomains.integers());

    RestartServiceInstancesStatus restartStatus =
      new RestartServiceInstancesStatus(serviceName, startTimeMs, endTimeMs, status, instancesToRestart);
    String toJson = GSON.toJson(restartStatus, RestartServiceInstancesStatus.class);

    table.put(Bytes.toBytes(serviceName + "-restart"), Bytes.toBytes(toJson));
  }

  @Override
  public synchronized RestartServiceInstancesStatus getLatestRestartInstancesRequest(String serviceName)
      throws IllegalStateException {
    String jsonString = Bytes.toString(table.get(Bytes.toBytes(serviceName + "-restart")));
    if (jsonString == null) {
      throw new IllegalStateException("Unable to find latest restart request for " + serviceName);
    }
    return GSON.fromJson(jsonString, RestartServiceInstancesStatus.class);
  }
}
