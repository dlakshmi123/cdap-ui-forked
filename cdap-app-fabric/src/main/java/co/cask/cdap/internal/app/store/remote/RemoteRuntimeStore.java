/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.internal.app.store.remote;

import co.cask.cdap.api.workflow.WorkflowToken;
import co.cask.cdap.app.store.RuntimeStore;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.internal.remote.RemoteOpsClient;
import co.cask.cdap.proto.WorkflowNodeStateDetail;
import co.cask.cdap.proto.id.ProgramRunId;
import com.google.inject.Inject;
import org.apache.twill.discovery.DiscoveryServiceClient;

/**
 * Implementation of RuntimeStore, which uses an HTTP Client to execute the actual store operations in a remote
 * server.
 */
public class RemoteRuntimeStore extends RemoteOpsClient implements RuntimeStore {

  @Inject
  RemoteRuntimeStore(DiscoveryServiceClient discoveryClient) {
    super(discoveryClient, Constants.Service.REMOTE_SYSTEM_OPERATION);
  }

  @Override
  public void updateWorkflowToken(ProgramRunId workflowRunId, WorkflowToken token) {
    executeRequest("updateWorkflowToken", workflowRunId, token);
  }

  @Override
  public void addWorkflowNodeState(ProgramRunId workflowRunId, WorkflowNodeStateDetail nodeStateDetail) {
    executeRequest("addWorkflowNodeState", workflowRunId, nodeStateDetail);
  }
}
