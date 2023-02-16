/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.api;

import co.cask.cdap.api.app.ApplicationSpecification;

import java.util.Map;

/**
 * This interface represents a context for a processor or elements of a processor.
 */
public interface RuntimeContext {

  /**
   * @return The application specification
   */
  ApplicationSpecification getApplicationSpecification();

  /**
   * @return A map of argument key and value.
   */
  Map<String, String> getRuntimeArguments();
}
