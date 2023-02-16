/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.test.internal.guice;

import com.continuuity.app.guice.AppFabricServiceRuntimeModule;
import com.continuuity.common.guice.LocationRuntimeModule;
import com.continuuity.app.guice.ProgramRunnerRuntimeModule;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.common.guice.IOModule;
import com.continuuity.data.runtime.DataFabricModules;
import com.continuuity.metrics.guice.MetricsClientRuntimeModule;
import com.continuuity.weave.filesystem.LocalLocationFactory;
import com.continuuity.weave.filesystem.LocationFactory;
import com.google.inject.AbstractModule;
import org.apache.hadoop.conf.Configuration;

/**
 *
 */
public final class AppFabricTestModule extends AbstractModule {

  private final CConfiguration cConf;
  private final Configuration hConf;

  public AppFabricTestModule(CConfiguration configuration) {
    this.cConf = configuration;
    hConf = new Configuration();
    hConf.addResource("mapred-site-local.xml");
    hConf.reloadConfiguration();
  }

  @Override
  protected void configure() {
    install(new DataFabricModules().getInMemoryModules());
    install(new ConfigModule(cConf, hConf));
    install(new IOModule());
    install(new DiscoveryRuntimeModule().getInMemoryModules());
    install(new AppFabricServiceRuntimeModule().getInMemoryModules());
    install(new ProgramRunnerRuntimeModule().getInMemoryModules());
    install(new MetricsClientRuntimeModule().getNoopModules());
    bind(LocationFactory.class).toInstance(new LocalLocationFactory());
  }
}
