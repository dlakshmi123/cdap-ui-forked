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

package co.cask.cdap.template.etl.batch.sink;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.templates.plugins.PluginConfig;

import javax.annotation.Nullable;

/**
 * Abstract config for TimePartitionedFileSetSink
 */
public abstract class TPFSSinkConfig extends PluginConfig {

  @Description(TimePartitionedFileSetSink.TPFS_NAME_DESC)
  protected String name;

  @Description(TimePartitionedFileSetSink.BASE_PATH_DESC)
  @Nullable
  protected String basePath;

  public TPFSSinkConfig(String name, @Nullable String basePath) {
    this.name = name;
    this.basePath = basePath;
  }
}
