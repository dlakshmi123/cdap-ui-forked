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

import co.cask.cdap.proto.id.NamespacedEntityId;
import co.cask.cdap.proto.id.ProgramRunId;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Helper classs to store program context for lineage writing.
 */
public class ProgramContext {
  private final AtomicReference<ProgramRunId> runRef = new AtomicReference<>();
  private final AtomicReference<NamespacedEntityId> componentIdRef = new AtomicReference<>();

  public void initContext(ProgramRunId run) {
    runRef.set(run);
  }

  public void initContext(ProgramRunId run, NamespacedEntityId componentId) {
    runRef.set(run);
    componentIdRef.set(componentId);
  }

  @Nullable
  public ProgramRunId getRun() {
    return runRef.get();
  }

  @Nullable
  public NamespacedEntityId getComponentId() {
    return componentIdRef.get();
  }
}
