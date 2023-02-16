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

package co.cask.cdap.app.stream;

import co.cask.cdap.api.data.stream.StreamWriter;
import co.cask.cdap.proto.Id;
import com.google.inject.assistedinject.Assisted;

import java.util.List;

/**
 * Factory to create {@link StreamWriter} objects
 */
public interface StreamWriterFactory {
  /**
   * @param namespace the namespace that the {@link StreamWriter} belongs to
   * @param owners the owners of the {@link StreamWriter}
   * @return a {@link StreamWriter} for the specified namespaceId
   */
  StreamWriter create(@Assisted("namespace") Id.Namespace namespace, @Assisted("owners") List<Id> owners);
}

