/*
 * Copyright 2015 Cask Data, Inc.
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
package co.cask.cdap.data2.metadata.service;

import co.cask.cdap.data2.metadata.dataset.BusinessMetadataRecord;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.metadata.MetadataRecord;
import co.cask.cdap.proto.metadata.MetadataSearchTargetType;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link BusinessMetadataStore} used in memory mode.
 */
public class NoOpBusinessMetadataStore implements BusinessMetadataStore {

  @Override
  public void setProperties(Id.NamespacedId entityId, Map<String, String> properties) {
    // NO-OP
  }

  @Override
  public void addTags(Id.NamespacedId entityId, String... tagsToAdd) {
    // NO-OP
  }

  @Override
  public MetadataRecord getMetadata(Id.NamespacedId entityId) {
    return null;
  }

  @Override
  public Set<MetadataRecord> getMetadata(Set<Id.NamespacedId> entityIds) {
    return null;
  }

  @Override
  public Map<String, String> getProperties(Id.NamespacedId entityId) {
    return null;
  }

  @Override
  public Set<String> getTags(Id.NamespacedId entityId) {
    return null;
  }

  @Override
  public void removeMetadata(Id.NamespacedId entityId) {
    // NO-OP
  }

  @Override
  public void removeProperties(Id.NamespacedId entityId) {
    // NO-OP
  }

  @Override
  public void removeProperties(Id.NamespacedId entityId, String... keys) {
    // NO-OP
  }

  @Override
  public void removeTags(Id.NamespacedId entityId) {
    // NO-OP
  }

  @Override
  public void removeTags(Id.NamespacedId entityId, String... tagsToRemove) {
    // NO-OP
  }

  @Override
  public Iterable<BusinessMetadataRecord> searchMetadata(String searchQuery) {
    return null;
  }

  @Override
  public Iterable<BusinessMetadataRecord> searchMetadataOnType(String searchQuery, MetadataSearchTargetType type) {
    return null;
  }
}
