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

package co.cask.cdap.data2.datafabric.dataset.service.executor;

import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.data2.datafabric.dataset.DatasetType;
import co.cask.cdap.data2.datafabric.dataset.RemoteDatasetFramework;
import co.cask.cdap.data2.dataset2.DatasetManagementException;
import co.cask.cdap.proto.DatasetTypeMeta;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;

import java.io.IOException;

/**
 * In-memory implementation of {@link DatasetOpExecutor}.
 */
public class InMemoryDatasetOpExecutor extends AbstractIdleService implements DatasetOpExecutor {

  private final RemoteDatasetFramework client;

  @Inject
  public InMemoryDatasetOpExecutor(RemoteDatasetFramework client) {
    this.client = client;
  }

  @Override
  public boolean exists(String instanceName) throws Exception {
    return getAdmin(instanceName).exists();
  }

  @Override
  public DatasetSpecification create(String instanceName, DatasetTypeMeta typeMeta, DatasetProperties props)
    throws Exception {

    DatasetType type = client.getDatasetType(typeMeta, null);

    if (type == null) {
      throw new IllegalArgumentException("Dataset type cannot be instantiated for provided type meta: " + typeMeta);
    }

    DatasetSpecification spec = type.configure(instanceName, props);
    DatasetAdmin admin = type.getAdmin(spec);
    admin.create();

    return spec;
  }

  @Override
  public void drop(DatasetSpecification spec, DatasetTypeMeta typeMeta) throws Exception {
    DatasetType type = client.getDatasetType(typeMeta, null);

    if (type == null) {
      throw new IllegalArgumentException("Dataset type cannot be instantiated for provided type meta: " + typeMeta);
    }

    DatasetAdmin admin = type.getAdmin(spec);
    admin.drop();
  }

  @Override
  public void truncate(String instanceName) throws Exception {
    getAdmin(instanceName).truncate();
  }

  @Override
  public void upgrade(String instanceName) throws Exception {
    getAdmin(instanceName).upgrade();
  }

  @Override
  protected void startUp() throws Exception {

  }

  @Override
  protected void shutDown() throws Exception {

  }

  private DatasetAdmin getAdmin(String instanceName) throws IOException, DatasetManagementException {
    return client.getAdmin(instanceName, null);
  }
}
