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

package com.continuuity.app.program;

import com.continuuity.api.Application;
import com.continuuity.common.lang.ApiResourceListHolder;
import com.continuuity.common.lang.ClassLoaders;
import com.google.common.base.Objects;

import java.io.File;
import java.io.IOException;

/**
 * Represents the archive that is uploaded by the user using the deployment
 * service.
 */
public final class Archive {
  /**
   * Class loader for holding.
   */
  private final ClassLoader classLoader;
  private final String mainClassName;

  public Archive(File unpackedJarFolder, String mainClassName) throws IOException {
    this.classLoader = ClassLoaders.newProgramClassLoader(
      unpackedJarFolder, ApiResourceListHolder.getResourceList(),
      Objects.firstNonNull(Thread.currentThread().getContextClassLoader(), Archive.class.getClassLoader()));
    this.mainClassName = mainClassName;
  }

  @SuppressWarnings("unchecked")
  public Class<Application> getMainClass() throws ClassNotFoundException {
    return (Class<Application>) classLoader.loadClass(mainClassName);
  }
}
