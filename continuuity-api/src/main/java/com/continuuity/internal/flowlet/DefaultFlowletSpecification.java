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

package com.continuuity.internal.flowlet;

import com.continuuity.api.ResourceSpecification;
import com.continuuity.api.flow.flowlet.FailurePolicy;
import com.continuuity.api.flow.flowlet.FlowletSpecification;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public final class DefaultFlowletSpecification implements FlowletSpecification {

  private final String className;
  private final String name;
  private final String description;
  private final FailurePolicy failurePolicy;
  private final Set<String> dataSets;
  private final Map<String, String> properties;
  private final ResourceSpecification resources;

  public DefaultFlowletSpecification(String name, String description,
                                     FailurePolicy failurePolicy, Set<String> dataSets,
                                     Map<String, String> properties, ResourceSpecification resources) {
    this(null, name, description, failurePolicy, dataSets, properties, resources);
  }

  public DefaultFlowletSpecification(String className, String name,
                                     String description, FailurePolicy failurePolicy,
                                     Set<String> dataSets, Map<String, String> properties,
                                     ResourceSpecification resources) {
    this.className = className;
    this.name = name;
    this.description = description;
    this.failurePolicy = failurePolicy;
    this.dataSets = ImmutableSet.copyOf(dataSets);
    this.properties = properties == null ? ImmutableMap.<String, String>of() : ImmutableMap.copyOf(properties);
    this.resources = resources;
  }

  @Override
  public String getClassName() {
    return className;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public FailurePolicy getFailurePolicy() {
    return failurePolicy;
  }

  @Override
  public Set<String> getDataSets() {
    return dataSets;
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public String getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public ResourceSpecification getResources() {
    return resources;
  }
}
