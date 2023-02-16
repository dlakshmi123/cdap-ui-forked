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
package com.continuuity.internal.workflow;

import com.continuuity.api.mapreduce.MapReduceSpecification;
import com.continuuity.api.schedule.Schedule;
import com.continuuity.api.workflow.WorkflowActionSpecification;
import com.continuuity.api.workflow.WorkflowSpecification;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

/**
 *
 */
public final class DefaultWorkflowSpecification implements WorkflowSpecification {

  private final String className;
  private final String name;
  private final String description;
  private final List<WorkflowActionSpecification> actions;
  private final Map<String, MapReduceSpecification> mapReduces;
  private final List<Schedule> schedules;

  public DefaultWorkflowSpecification(String name, String description, List<WorkflowActionSpecification> actions,
                                      Map<String, MapReduceSpecification> mapReduces, List<Schedule> schedules) {
    this(null, name, description, actions, mapReduces, schedules);
  }

  public DefaultWorkflowSpecification(String className, WorkflowSpecification spec) {
    this(className, spec.getName(), spec.getDescription(),
         spec.getActions(), spec.getMapReduce(), spec.getSchedules());
  }

  public DefaultWorkflowSpecification(String className, String name, String description,
                                      List<WorkflowActionSpecification> actions,
                                      Map<String, MapReduceSpecification> mapReduces,
                                      List<Schedule> schedules) {
    this.className = className;
    this.name = name;
    this.description = description;
    this.actions = ImmutableList.copyOf(actions);
    this.mapReduces = ImmutableMap.copyOf(mapReduces);
    this.schedules = ImmutableList.copyOf(schedules);
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
  public List<WorkflowActionSpecification> getActions() {
    return actions;
  }

  @Override
  public Map<String, MapReduceSpecification> getMapReduce() {
    return mapReduces;
  }

  @Override
  public List<Schedule> getSchedules() {
    return schedules;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(WorkflowSpecification.class)
      .add("name", name)
      .add("class", className)
      .add("actions", actions)
      .add("mapReduces", mapReduces)
      .add("schedules", schedules)
      .toString();
  }
}
