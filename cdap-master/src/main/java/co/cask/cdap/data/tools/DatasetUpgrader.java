/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.data.tools;

import co.cask.cdap.api.dataset.DatasetAdmin;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.api.dataset.lib.FileSetProperties;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.namespace.NamespacedLocationFactory;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetInstanceMDSUpgrader;
import co.cask.cdap.data2.datafabric.dataset.service.mds.DatasetTypeMDSUpgrader;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.lib.hbase.AbstractHBaseDataSetAdmin;
import co.cask.cdap.data2.dataset2.lib.table.hbase.HBaseTableAdmin;
import co.cask.cdap.data2.util.TableId;
import co.cask.cdap.data2.util.hbase.HBaseTableUtil;
import co.cask.cdap.data2.util.hbase.HTableNameConverter;
import co.cask.cdap.data2.util.hbase.HTableNameConverterFactory;
import co.cask.cdap.proto.DatasetSpecificationSummary;
import co.cask.cdap.proto.Id;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Handles upgrade for System and User Datasets
 */
public class DatasetUpgrader extends AbstractUpgrader {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetUpgrader.class);

  private final CConfiguration cConf;
  private final Configuration hConf;
  private final LocationFactory locationFactory;
  private final HBaseTableUtil hBaseTableUtil;
  private final DatasetFramework dsFramework;
  private final Pattern userTablePrefix;
  private final DatasetInstanceMDSUpgrader datasetInstanceMDSUpgrader;
  private final DatasetTypeMDSUpgrader datasetTypeMDSUpgrader;

  @Inject
  private DatasetUpgrader(CConfiguration cConf, Configuration hConf, LocationFactory locationFactory,
                          NamespacedLocationFactory namespacedLocationFactory,
                          HBaseTableUtil hBaseTableUtil, DatasetFramework dsFramework,
                          DatasetInstanceMDSUpgrader datasetInstanceMDSUpgrader,
                          DatasetTypeMDSUpgrader datasetTypeMDSUpgrader) {

    super(locationFactory, namespacedLocationFactory);
    this.cConf = cConf;
    this.hConf = hConf;
    this.locationFactory = locationFactory;
    this.hBaseTableUtil = hBaseTableUtil;
    this.dsFramework = dsFramework;
    this.datasetInstanceMDSUpgrader = datasetInstanceMDSUpgrader;
    this.datasetTypeMDSUpgrader = datasetTypeMDSUpgrader;
    this.userTablePrefix = Pattern.compile(String.format("^%s\\.user\\..*", cConf.get(Constants.Dataset.TABLE_PREFIX)));
  }

  @Override
  public void upgrade() throws Exception {
    // Upgrade system dataset
    upgradeSystemDatasets();

    // Upgrade all user hbase tables
    upgradeUserTables();

    // Upgrade the datasets meta meta table
    datasetTypeMDSUpgrader.upgrade();

    // Upgrade the datasets instance meta table
    datasetInstanceMDSUpgrader.upgrade();

    // upgrade all the filesets base paths
    for (DatasetSpecification fileSetSpec : datasetInstanceMDSUpgrader.getFileSetsSpecs()) {
      upgradeFileSet(fileSetSpec);
    }
  }

  protected DatasetInstanceMDSUpgrader getDatasetInstanceMDSUpgrader() {
    return datasetInstanceMDSUpgrader;
  }

  protected DatasetTypeMDSUpgrader getDatasetTypeMDSUpgrader() {
    return datasetTypeMDSUpgrader;
  }

  /**
   * Upgrades the {@link FileSet} and also its embedded filesets if any by moving the base path under
   * namespaced directory
   *
   * @param dsSpec the {@link DatasetSpecification} of the {@link FileSet} to be upgraded
   * @throws IOException
   */
  private void upgradeFileSet(DatasetSpecification dsSpec) throws IOException {
    String basePath = FileSetProperties.getBasePath(dsSpec.getProperties());
    if (basePath != null) {
      Location oldLocation = locationFactory.create(basePath);
      Location newlocation = namespacedLocationFactory.get(Constants.DEFAULT_NAMESPACE_ID)
        .append(cConf.get(Constants.Dataset.DATA_DIR)).append(basePath);
      LOG.info("Upgrading base path for dataset {} from {} to {}", dsSpec.getName(), oldLocation, newlocation);
      renameLocation(oldLocation, newlocation);
    } else {
      LOG.info("The basepath for files {} is null. No files will be moved");
    }
  }

  private void upgradeSystemDatasets() throws Exception {

    // Upgrade all datasets in system namespace
    for (DatasetSpecificationSummary spec : dsFramework.getInstances(Constants.DEFAULT_NAMESPACE_ID)) {
      LOG.info("Upgrading dataset: {}, spec: {}", spec.getName(), spec.toString());
      DatasetAdmin admin = dsFramework.getAdmin(Id.DatasetInstance.from(Constants.DEFAULT_NAMESPACE_ID, spec.getName()),
                                                null);
      // we know admin is not null, since we are looping over existing datasets
      admin.upgrade();
      LOG.info("Upgraded dataset: {}", spec.getName());
    }
  }

  private void upgradeUserTables() throws Exception {
    HBaseAdmin hAdmin = new HBaseAdmin(hConf);

    for (HTableDescriptor desc : hAdmin.listTables(userTablePrefix)) {
      HTableNameConverter hTableNameConverter = new HTableNameConverterFactory().get();
      TableId tableId = hTableNameConverter.from(desc);
      LOG.info("Upgrading hbase table: {}, desc: {}", tableId, desc);

      final boolean supportsIncrement = HBaseTableAdmin.supportsReadlessIncrements(desc);
      final boolean transactional = HBaseTableAdmin.isTransactional(desc);
      DatasetAdmin admin = new AbstractHBaseDataSetAdmin(tableId, hConf, hBaseTableUtil) {
        @Override
        protected CoprocessorJar createCoprocessorJar() throws IOException {
          return HBaseTableAdmin.createCoprocessorJarInternal(cConf,
                                                              locationFactory,
                                                              hBaseTableUtil,
                                                              transactional,
                                                              supportsIncrement);
        }

        @Override
        protected boolean upgradeTable(HTableDescriptor tableDescriptor) {
          // we don't do any other changes apart from coprocessors upgrade
          return false;
        }

        @Override
        public void create() throws IOException {
          // no-op
          throw new UnsupportedOperationException("This DatasetAdmin is only used for upgrade() operation");
        }
      };
      admin.upgrade();
      LOG.info("Upgraded hbase table: {}", tableId);
    }
  }

}
