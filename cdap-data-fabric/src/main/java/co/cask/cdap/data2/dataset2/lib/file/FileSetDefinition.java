/*
 * Copyright © 2014-2016 Cask Data, Inc.
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

package co.cask.cdap.data2.dataset2.lib.file;

import co.cask.cdap.api.dataset.DatasetContext;
import co.cask.cdap.api.dataset.DatasetDefinition;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.IncompatibleUpdateException;
import co.cask.cdap.api.dataset.Reconfigurable;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.api.dataset.lib.FileSetProperties;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.io.RootLocationFactory;
import co.cask.cdap.common.namespace.NamespacedLocationFactory;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Dataset definition for File datasets.
 */
public class FileSetDefinition implements DatasetDefinition<FileSet, FileSetAdmin>, Reconfigurable {

  @Inject
  private RootLocationFactory absoluteLocationFactory;

  @Inject
  private NamespacedLocationFactory namespacedLocationFactory;

  @Inject
  private CConfiguration cConf;

  private final String name;

  /**
   * Constructor with dataset type name.
   * @param name the type name to be used for this dataset definition.
   */
  public FileSetDefinition(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    Map<String, String> newProperties = new HashMap<>(properties.getProperties());
    newProperties.put(FileSetDataset.FILESET_VERSION_PROPERTY, FileSetDataset.FILESET_VERSION);
    return DatasetSpecification
      .builder(instanceName, getName())
      .properties(newProperties)
      .build();
  }

  @Override
  public DatasetSpecification reconfigure(String instanceName,
                                          DatasetProperties newProperties,
                                          DatasetSpecification currentSpec) throws IncompatibleUpdateException {
    boolean wasExternal = FileSetProperties.isDataExternal(currentSpec.getProperties());
    boolean isExternal = FileSetProperties.isDataExternal(newProperties.getProperties());
    String oldPath = FileSetProperties.getBasePath(currentSpec.getProperties());
    String newPath = FileSetProperties.getBasePath(newProperties.getProperties());

    // validate that we are not "internalizing" an external location
    if (wasExternal && !isExternal) {
      throw new IncompatibleUpdateException(
        String.format("Cannot convert external file set '%s' to non-external.", instanceName));
    }

    // allow change of path only if the dataset is external
    if (!Objects.equals(oldPath, newPath) && !isExternal) {
      throw new IncompatibleUpdateException(
        String.format("Cannot change the base path of non-external file set '%s'.", instanceName));
    }
    return configure(instanceName, newProperties);
  }

  @Override
  public FileSetAdmin getAdmin(DatasetContext datasetContext, DatasetSpecification spec,
                               ClassLoader classLoader) throws IOException {
    return new FileSetAdmin(datasetContext, cConf, absoluteLocationFactory, namespacedLocationFactory, spec);
  }

  @Override
  public FileSet getDataset(DatasetContext datasetContext, DatasetSpecification spec, Map<String, String> arguments,
                            ClassLoader classLoader) throws IOException {
    return new FileSetDataset(datasetContext, cConf, spec, absoluteLocationFactory, namespacedLocationFactory,
                              arguments == null ? Collections.<String, String>emptyMap() : arguments);
  }
}
