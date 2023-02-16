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
package com.continuuity.app.runtime.workflow;

import com.continuuity.api.workflow.WorkflowActionSpecification;
import com.google.common.util.concurrent.Service;

/**
 * A container class for holding workflow status.
 */
public final class WorkflowStatus {

  private  Service.State state;
  private  WorkflowActionSpecification currentAction;
  private  int currentStep;

  public WorkflowStatus(Service.State state, WorkflowActionSpecification currentAction, int currentStep) {
    this.state = state;
    this.currentAction = currentAction;
    this.currentStep = currentStep;
  }

  public Service.State getState() {
    return state;
  }

  public WorkflowActionSpecification getCurrentAction() {
    return currentAction;
  }
}
