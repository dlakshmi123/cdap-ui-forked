/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.data2.metadata.writer;

import co.cask.cdap.proto.Id;

/**
 * Interface implemented by classes that need program context information.
 */
public interface ProgramContextAware {
  /**
   * Initalize with program run information.

   * @param run program run
   */
  void initContext(Id.Run run);

  /**
   * Intialize with program run and program component (i.e, flowet Id, etc.) information.

   * @param run program run
   * @param componentId program component
   */
  void initContext(Id.Run run, Id.NamespacedId componentId);
}
