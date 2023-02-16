/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.FlowletDefinition;
import com.continuuity.api.flow.flowlet.FlowletSpecification;
import com.continuuity.app.program.Program;
import com.continuuity.app.program.Type;
import com.continuuity.weave.api.EventHandler;
import com.continuuity.weave.api.ResourceSpecification;
import com.continuuity.weave.api.WeaveApplication;
import com.continuuity.weave.api.WeaveSpecification;
import com.continuuity.weave.filesystem.Location;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Map;

/**
 *
 */
public final class FlowWeaveApplication implements WeaveApplication {

  private final FlowSpecification spec;
  private final Program program;
  private final File hConfig;
  private final File cConfig;
  private final boolean disableTransaction;
  private final EventHandler eventHandler;

  public FlowWeaveApplication(Program program, FlowSpecification spec,
                              File hConfig, File cConfig, boolean disableTransaction,
                              EventHandler eventHandler) {
    this.spec = spec;
    this.program = program;
    this.hConfig = hConfig;
    this.cConfig = cConfig;
    this.disableTransaction = disableTransaction;
    this.eventHandler = eventHandler;
  }

  @Override
  public WeaveSpecification configure() {
    WeaveSpecification.Builder.MoreRunnable moreRunnable = WeaveSpecification.Builder.with()
      .setName(String.format("%s.%s.%s.%s",
                             Type.FLOW.name().toLowerCase(),
                             program.getAccountId(), program.getApplicationId(), spec.getName()))
      .withRunnable();

    Location programLocation = program.getJarLocation();
    String programName = programLocation.getName();
    WeaveSpecification.Builder.RunnableSetter runnableSetter = null;
    for (Map.Entry<String, FlowletDefinition> entry  : spec.getFlowlets().entrySet()) {
      FlowletDefinition flowletDefinition = entry.getValue();
      FlowletSpecification flowletSpec = flowletDefinition.getFlowletSpec();
      ResourceSpecification resourceSpec = ResourceSpecification.Builder.with()
        .setVirtualCores(flowletSpec.getResources().getVirtualCores())
        .setMemory(flowletSpec.getResources().getMemoryMB(), ResourceSpecification.SizeUnit.MEGA)
        .setInstances(flowletDefinition.getInstances())
        .build();

      String flowletName = entry.getKey();
      runnableSetter = moreRunnable
        .add(flowletName,
             new FlowletWeaveRunnable(flowletName, "hConf.xml", "cConf.xml", disableTransaction), resourceSpec)
        .withLocalFiles().add(programName, programLocation.toURI())
                         .add("hConf.xml", hConfig.toURI())
                         .add("cConf.xml", cConfig.toURI()).apply();
    }

    Preconditions.checkState(runnableSetter != null, "No flowlet for the flow.");
    return runnableSetter.anyOrder().withEventHandler(eventHandler).build();
  }
}
