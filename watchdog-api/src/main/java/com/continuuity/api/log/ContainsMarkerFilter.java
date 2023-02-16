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

package com.continuuity.api.log;

/**
 * Filter that checks if specific marker exists in log message.
 */
public class ContainsMarkerFilter implements LogMarkerFilter {
  /**
   * Constructor that takes marker value to look for in log messages.
   * @param marker marker value
   */
  public ContainsMarkerFilter(String marker) {
    // TODO
  }
}
