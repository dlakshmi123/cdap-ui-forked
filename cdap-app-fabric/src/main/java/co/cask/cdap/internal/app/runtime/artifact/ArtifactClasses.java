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

package co.cask.cdap.internal.app.runtime.artifact;

import co.cask.cdap.api.templates.plugins.PluginClass;
import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

/**
 * Classes contained in an artifact, such as plugin classes and application classes.
 */
public class ArtifactClasses {
  private final Set<ApplicationClass> apps;
  private final Set<PluginClass> plugins;

  private ArtifactClasses(Set<ApplicationClass> apps, Set<PluginClass> plugins) {
    this.apps = apps;
    this.plugins = plugins;
  }

  public Set<ApplicationClass> getApps() {
    return apps;
  }

  public Set<PluginClass> getPlugins() {
    return plugins;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArtifactClasses that = (ArtifactClasses) o;

    return Objects.equals(apps, that.apps) && Objects.equals(plugins, that.plugins);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apps, plugins);
  }

  @Override
  public String toString() {
    return "ArtifactClasses{" +
      "apps=" + apps +
      ", plugins=" + plugins +
      '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to more easily add application and plugin classes, and in the future, program and dataset classes.
   */
  public static class Builder {
    private final ImmutableSet.Builder<ApplicationClass> appsBuilder;
    private final ImmutableSet.Builder<PluginClass> pluginsBuilder;

    private Builder() {
      appsBuilder = ImmutableSet.builder();
      pluginsBuilder = ImmutableSet.builder();
    }

    public Builder addApps(ApplicationClass... apps) {
      for (ApplicationClass app : apps) {
        appsBuilder.add(app);
      }
      return this;
    }

    public Builder addApps(Iterable<ApplicationClass> apps) {
      appsBuilder.addAll(apps);
      return this;
    }

    public Builder addApp(ApplicationClass app) {
      appsBuilder.add(app);
      return this;
    }

    public Builder addPlugins(PluginClass... plugins) {
      for (PluginClass plugin : plugins) {
        pluginsBuilder.add(plugin);
      }
      return this;
    }

    public Builder addPlugins(Iterable<PluginClass> plugins) {
      pluginsBuilder.addAll(plugins);
      return this;
    }

    public Builder addPlugin(PluginClass plugin) {
      pluginsBuilder.add(plugin);
      return this;
    }

    public ArtifactClasses build() {
      return new ArtifactClasses(appsBuilder.build(), pluginsBuilder.build());
    }
  }
}
