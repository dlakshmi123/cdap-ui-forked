/*
 * Copyright © 2017 Cask Data, Inc.
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

package co.cask.cdap.internal.app.runtime.schedule.trigger;

import co.cask.cdap.api.schedule.Trigger;

/**
 * A Trigger builder that builds a {@link OrTrigger}.
 */
public class OrTriggerBuilder extends AbstractCompositeTriggerBuilder {

  public OrTriggerBuilder(Trigger... triggers) {
    super(Type.OR, triggers);
  }

  @Override
  public OrTrigger build(String namespace, String applicationName, String applicationVersion) {
    return new OrTrigger(getBuiltTriggers(namespace, applicationName, applicationVersion));
  }
}
