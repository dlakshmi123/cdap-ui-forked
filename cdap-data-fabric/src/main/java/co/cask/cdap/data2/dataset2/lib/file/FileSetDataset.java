/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

import co.cask.cdap.api.dataset.DataSetException;
import co.cask.cdap.api.dataset.DatasetContext;
import co.cask.cdap.api.dataset.DatasetSpecification;
import co.cask.cdap.api.dataset.lib.FileSet;
import co.cask.cdap.api.dataset.lib.FileSetArguments;
import co.cask.cdap.api.dataset.lib.FileSetProperties;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.namespace.NamespacedLocationFactory;
import co.cask.cdap.proto.Id;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.twill.filesystem.Location;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of file dataset.
 */
public final class FileSetDataset implements FileSet {

  private final Map<String, String> properties;
  private final Map<String, String> runtimeArguments;

  private final Location baseLocation;
  private final List<Location> inputLocations;
  private final Location outputLocation;

  private final String inputFormatClassName;
  private final String outputFormatClassName;

  /**
   * Constructor.
   * @param datasetContext the context for the dataset
   * @param cConf the CDAP configuration
   * @param spec the dataset specification
   * @param namespacedLocationFactory a factory for namespaced {@link Location}
   * @param runtimeArguments the runtime arguments
   */
  public FileSetDataset(DatasetContext datasetContext, CConfiguration cConf,
                        DatasetSpecification spec,
                        NamespacedLocationFactory namespacedLocationFactory,
                        @Nonnull Map<String, String> runtimeArguments) throws IOException {

    Preconditions.checkNotNull(datasetContext, "Dataset context must not be null");
    Preconditions.checkNotNull(runtimeArguments, "Runtime arguments must not be null");

    String namespaceId = datasetContext.getNamespaceId();
    String dataDir = cConf.get(Constants.Dataset.DATA_DIR, Constants.Dataset.DEFAULT_DATA_DIR);
    String basePath = determineBasePath(spec);
    Location namespaceHomeLocation = namespacedLocationFactory.get(Id.Namespace.from(namespaceId));
    this.baseLocation = namespaceHomeLocation.append(dataDir).append(basePath);
    this.properties = spec.getProperties();
    this.runtimeArguments = runtimeArguments;
    this.inputFormatClassName = FileSetProperties.getInputFormat(properties);
    this.outputFormatClassName = FileSetProperties.getOutputFormat(properties);
    this.outputLocation = determineOutputLocation();
    this.inputLocations = determineInputLocations();
  }

  /**
   * If the properties do not contain a base path, generate one from the dataset name.
   * This is package visible, because FileSetAdmin needs it, too.
   * TODO: Ideally, this should be done in configure(), but currently it cannot because of CDAP-1721
   */
  static String determineBasePath(DatasetSpecification spec) {
    String basePath = FileSetProperties.getBasePath(spec.getProperties());
    if (basePath == null) {
      basePath = spec.getName().replace('.', '/');
    }
    return basePath;
  }

  private Location determineOutputLocation() {
    String outputPath = FileSetArguments.getOutputPath(runtimeArguments);
    return outputPath == null ? baseLocation : createLocation(outputPath);
  }

  private List<Location> determineInputLocations() {
    Collection<String> inputPaths = FileSetArguments.getInputPaths(runtimeArguments);
    if (inputPaths == null) {
      return Collections.singletonList(baseLocation);
    } else {
      List<Location> locations = Lists.newLinkedList();
      for (String path : inputPaths) {
        locations.add(createLocation(path));
      }
      return locations;
    }
  }

  private Location createLocation(String relativePath) {
    try {
      return baseLocation.append(relativePath);
    } catch (IOException e) {
      throw new DataSetException("Error constructing path from base '" + baseLocation.toURI().getPath() +
                                   "' and relative path '" + relativePath + "'", e);
    }
  }

  @Override
  public Location getBaseLocation() {
    return baseLocation;
  }

  @Override
  public List<Location> getInputLocations() {
    return Lists.newLinkedList(inputLocations);
  }

  @Override
  public Location getOutputLocation() {
    return outputLocation;
  }

  @Override
  public Location getLocation(String relativePath) {
    return createLocation(relativePath);
  }

  @Override
  public void close() throws IOException {
    // no-op - nothing to do
  }

  @Override
  public String getInputFormatClassName() {
    return inputFormatClassName;
  }

  @Override
  public Map<String, String> getInputFormatConfiguration() {
    return getInputFormatConfiguration(inputLocations);
  }

  @Override
  public Map<String, String> getInputFormatConfiguration(Iterable<? extends Location> inputLocs) {
    String inputs = Joiner.on(',').join(Iterables.transform(inputLocs, new Function<Location, String>() {
      @Override
      public String apply(@Nullable Location location) {
        return getFileSystemPath(location);
      }
    }));
    Map<String, String> config = Maps.newHashMap();
    config.putAll(FileSetProperties.getInputProperties(properties));
    config.putAll(FileSetProperties.getInputProperties(runtimeArguments));
    config.put("mapred.input.dir", inputs);
    return ImmutableMap.copyOf(config);
  }

  @Override
  public String getOutputFormatClassName() {
    return outputFormatClassName;
  }

  @Override
  public Map<String, String> getOutputFormatConfiguration() {
    Map<String, String> config = Maps.newHashMap();
    config.putAll(FileSetProperties.getOutputProperties(properties));
    config.putAll(FileSetProperties.getOutputProperties(runtimeArguments));
    config.put(FileOutputFormat.OUTDIR, getFileSystemPath(outputLocation));
    return ImmutableMap.copyOf(config);
  }

  @Override
  public Map<String, String> getRuntimeArguments() {
    return runtimeArguments;
  }

  private String getFileSystemPath(Location loc) {
    return loc.toURI().getPath();
  }
}
