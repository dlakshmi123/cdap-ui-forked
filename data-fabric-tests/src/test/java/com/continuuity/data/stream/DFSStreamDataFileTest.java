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
package com.continuuity.data.stream;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.guice.ConfigModule;
import com.continuuity.common.guice.DiscoveryRuntimeModule;
import com.continuuity.data.runtime.DataFabricDistributedModule;
import com.continuuity.data.runtime.TransactionMetricsModule;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.twill.filesystem.HDFSLocationFactory;
import org.apache.twill.filesystem.LocationFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 *
 */
public class DFSStreamDataFileTest extends StreamDataFileTestBase {

  private static LocationFactory locationFactory;
  private static StreamAdmin streamAdmin;
  private static MiniDFSCluster dfsCluster;


  @BeforeClass
  public static void init() throws IOException {
    Configuration hConf = new Configuration();
    hConf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, tmpFolder.newFolder().getAbsolutePath());
    dfsCluster = new MiniDFSCluster.Builder(hConf).numDataNodes(1).build();
    final FileSystem fileSystem = dfsCluster.getFileSystem();

    CConfiguration cConf = CConfiguration.create();

    Injector injector = Guice.createInjector(
      new ConfigModule(cConf, hConf),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(LocationFactory.class).toInstance(new HDFSLocationFactory(fileSystem));
        }
      },
      new TransactionMetricsModule(),
      new DiscoveryRuntimeModule().getInMemoryModules(),
      new DataFabricDistributedModule()
    );

    locationFactory = injector.getInstance(LocationFactory.class);
    streamAdmin = injector.getInstance(StreamAdmin.class);
  }

  @AfterClass
  public static void finish() {
    dfsCluster.shutdown();
  }

  @Override
  protected LocationFactory getLocationFactory() {
    return locationFactory;
  }

  @Override
  protected StreamAdmin getStreamAdmin() {
    return streamAdmin;
  }
}
