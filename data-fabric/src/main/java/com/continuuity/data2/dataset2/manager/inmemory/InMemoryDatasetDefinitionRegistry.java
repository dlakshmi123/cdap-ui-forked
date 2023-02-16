package com.continuuity.data2.dataset2.manager.inmemory;

import com.continuuity.internal.data.dataset.DatasetDefinition;
import com.continuuity.internal.data.dataset.module.DatasetDefinitionRegistry;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Simple implementation of {@link DatasetDefinitionRegistry} that keeps state in memory.
 */
public class InMemoryDatasetDefinitionRegistry implements DatasetDefinitionRegistry {
  private Map<String, DatasetDefinition> datasetTypes = Maps.newHashMap();

  @Override
  public <T extends DatasetDefinition> T get(String datasetType) {
    return (T) datasetTypes.get(datasetType);
  }

  @Override
  public void add(DatasetDefinition def) {
    datasetTypes.put(def.getName(), def);
  }
}
