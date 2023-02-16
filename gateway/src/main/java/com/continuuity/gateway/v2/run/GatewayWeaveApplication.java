package com.continuuity.gateway.v2.run;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.weave.api.ResourceSpecification;
import com.continuuity.weave.api.WeaveApplication;
import com.continuuity.weave.api.WeaveSpecification;

import java.io.File;

/**
 * WeaveApplication wrapper for Gateway.
 */
@SuppressWarnings("UnusedDeclaration")
public class GatewayWeaveApplication implements WeaveApplication {
  private static final String name = "GatewayWeaveApplication";

  private final CConfiguration cConf;
  private final File cConfFile;

  private final File hConfFile;

  public GatewayWeaveApplication(CConfiguration cConf, File cConfFile, File hConfFile) {
    this.cConf = cConf;
    this.cConfFile = cConfFile;
    this.hConfFile = hConfFile;
  }

  @Override
  public WeaveSpecification configure() {
    int numCores = cConf.getInt(Constants.Gateway.NUM_CORES, Constants.Gateway.DEFAULT_NUM_CORES);
    int memoryMb = cConf.getInt(Constants.Gateway.MEMORY_MB, Constants.Gateway.DEFAULT_MEMORY_MB);
    int instances = cConf.getInt(Constants.Gateway.NUM_INSTANCES, Constants.Gateway.DEFAULT_NUM_INSTANCES);

    ResourceSpecification spec = ResourceSpecification.Builder
      .with()
      .setVirtualCores(numCores)
      .setMemory(memoryMb, ResourceSpecification.SizeUnit.MEGA)
      .setInstances(instances)
      .build();

    return WeaveSpecification.Builder.with()
      .setName(name)
      .withRunnable()
      .add(new GatewayWeaveRunnable("GatewayWeaveRunnable", "cConf.xml", "hConf.xml"), spec)
      .withLocalFiles()
      .add("cConf.xml", cConfFile.toURI())
      .add("hConf.xml", hConfFile.toURI())
      .apply().anyOrder().build();
  }
}
