/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.common.guice;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.runtime.RuntimeModule;
import com.continuuity.weave.filesystem.HDFSLocationFactory;
import com.continuuity.weave.filesystem.LocalLocationFactory;
import com.continuuity.weave.filesystem.LocationFactory;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.File;

/**
 * Provides Guice bindings for LocationFactory in different runtime environment.
 */
public final class LocationRuntimeModule extends RuntimeModule {

  @Override
  public Module getInMemoryModules() {
    return new LocalLocationModule();
  }

  @Override
  public Module getSingleNodeModules() {
    return new LocalLocationModule();
  }

  @Override
  public Module getDistributedModules() {
    return new HDFSLocationModule();
  }

  private static final class LocalLocationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(LocationFactory.class).to(LocalLocationFactory.class);
    }

    @Provides
    @Singleton
    private LocalLocationFactory providesLocalLocationFactory(CConfiguration cConf) {
      return new LocalLocationFactory(new File(cConf.get(Constants.CFG_LOCAL_DATA_DIR)));
    }
  }

  private static final class HDFSLocationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(LocationFactory.class).to(HDFSLocationFactory.class);
    }

    @Provides
    @Singleton
    private HDFSLocationFactory providesHDFSLocationFactory(CConfiguration cConf, Configuration hConf) {
      String hdfsUser = cConf.get(Constants.CFG_HDFS_USER);
      String namespace = cConf.get(Constants.CFG_HDFS_NAMESPACE);
      FileSystem fileSystem;

      try {
        if (hdfsUser == null) {
          fileSystem = FileSystem.get(FileSystem.getDefaultUri(hConf), hConf);
        } else {
          fileSystem = FileSystem.get(FileSystem.getDefaultUri(hConf), hConf, hdfsUser);
        }
        return new HDFSLocationFactory(fileSystem, namespace);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
