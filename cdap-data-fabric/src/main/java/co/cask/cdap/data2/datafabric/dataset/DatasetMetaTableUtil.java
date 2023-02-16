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

package co.cask.cdap.data2.datafabric.dataset;

import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetInstanceMDS;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetTypeMDS;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.DatasetManagementException;
import co.cask.cdap.data2.dataset2.SingleTypeModule;

import java.io.IOException;

/**
 * Utility for working with dataset metadata table.
 */
public class DatasetMetaTableUtil {
  public static final String META_TABLE_NAME = "datasets.type";
  public static final String INSTANCE_TABLE_NAME = "datasets.instance";

  private final DatasetFramework framework;

  public DatasetMetaTableUtil(DatasetFramework framework) {
    this.framework = framework;
  }

  public void init() throws DatasetManagementException {
    addTypes(framework);
  }

  public DatasetTypeMDS getTypeMetaTable() throws DatasetManagementException, IOException {
    return (DatasetTypeMDS) DatasetsUtil.getOrCreateDataset(framework, META_TABLE_NAME,
                                                            DatasetTypeMDS.class.getName(),
                                                            DatasetProperties.EMPTY, null, null);
  }

  public DatasetInstanceMDS getInstanceMetaTable() throws DatasetManagementException, IOException {
    return (DatasetInstanceMDS) DatasetsUtil.getOrCreateDataset(framework, INSTANCE_TABLE_NAME,
                                                                DatasetInstanceMDS.class.getName(),
                                                                DatasetProperties.EMPTY, null, null);
  }

  /**
   * Adds datasets and types to the given {@link DatasetFramework} used by dataset service mds.
   * @param datasetFramework framework to add types and datasets to
   */
  public static void setupDatasets(DatasetFramework datasetFramework) throws IOException, DatasetManagementException {
    addTypes(datasetFramework);
    datasetFramework.addInstance(DatasetTypeMDS.class.getName(),
                                 DatasetMetaTableUtil.META_TABLE_NAME, DatasetProperties.EMPTY);
    datasetFramework.addInstance(DatasetInstanceMDS.class.getName(),
                                 DatasetMetaTableUtil.INSTANCE_TABLE_NAME, DatasetProperties.EMPTY);
  }

  private static void addTypes(DatasetFramework framework) throws DatasetManagementException {
    framework.addModule("typeMDSModule", new SingleTypeModule(DatasetTypeMDS.class));
    framework.addModule("instanceMDSModule", new SingleTypeModule(DatasetInstanceMDS.class));
  }
}
