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

package com.continuuity;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.annotation.Property;
import com.continuuity.api.workflow.AbstractWorkflowAction;
import com.continuuity.api.workflow.Workflow;
import com.continuuity.api.workflow.WorkflowActionSpecification;
import com.continuuity.api.workflow.WorkflowContext;
import com.continuuity.api.workflow.WorkflowSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Simple workflow, that sleeps inside a CustomAction, This class is used for testing the workflow status.
 */
public class SleepingWorkflowApp implements Application {

  @Override
  public ApplicationSpecification configure() {
    return ApplicationSpecification.Builder.with()
      .setName("SleepWorkflowApp")
      .setDescription("SleepWorkflowApp")
      .noStream()
      .noDataSet()
      .noFlow()
      .noProcedure()
      .noMapReduce()
      .withWorkflows().add(new SleepWorkflow()).build();
  }

  /**
   *
   */
  public static class SleepWorkflow implements Workflow {

    @Override
    public WorkflowSpecification configure() {
      return WorkflowSpecification.Builder.with()
        .setName("SleepWorkflow")
        .setDescription("FunWorkflow description")
        .onlyWith(new CustomAction("verify"))
        .build();
    }
  }


  /**
   *
   */
  public static final class CustomAction extends AbstractWorkflowAction {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAction.class);

    private final String name;

    @Property
    private final boolean condition = true;

    public CustomAction(String name) {
      this.name = name;
    }

    @Override
    public WorkflowActionSpecification configure() {
      return WorkflowActionSpecification.Builder.with()
        .setName(name)
        .setDescription(name)
        .build();
    }

    @Override
    public void initialize(WorkflowContext context) throws Exception {
      super.initialize(context);
      LOG.info("Custom action initialized: " + context.getSpecification().getName());
    }

    @Override
    public void destroy() {
      super.destroy();
      LOG.info("Custom action destroyed: " + getContext().getSpecification().getName());
    }

    @Override
    public void run() {
      LOG.info("Custom action run");
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      LOG.info("Custom run completed.");
    }
  }

}
