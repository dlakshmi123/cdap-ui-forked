/*
 * Copyright © 2015-2016 Cask Data, Inc.
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

package co.cask.cdap.proto.security;

import co.cask.cdap.api.annotation.Beta;
import co.cask.cdap.proto.id.EntityId;

import java.util.Set;

/**
 * Request for granting a principal permission to perform certain actions on an entity.
 */
@Beta
public class GrantRequest extends AuthorizationRequest {

  public GrantRequest(Authorizable authorizable, Principal principal, Set<Action> actions) {
    super(authorizable, principal, actions);
    if (principal == null) {
      throw new IllegalArgumentException("principal is required");
    }
    if (actions == null) {
      throw new IllegalArgumentException("actions is required");
    }
  }

  public GrantRequest(EntityId entityId, Principal principal, Set<Action> actions) {
    this(Authorizable.fromEntityId(entityId), principal, actions);
  }
}
