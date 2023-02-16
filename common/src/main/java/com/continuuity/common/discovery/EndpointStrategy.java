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
package com.continuuity.common.discovery;

import org.apache.twill.discovery.Discoverable;

/**
 * This class helps picking up an endpoint from a list of Discoverable.
 */
public interface EndpointStrategy {

  /**
   * Picks a {@link Discoverable} using its strategy.
   * @return A {@link Discoverable} based on the stragegy or {@code null} if no endpoint can be found.
   */
  Discoverable pick();
}
