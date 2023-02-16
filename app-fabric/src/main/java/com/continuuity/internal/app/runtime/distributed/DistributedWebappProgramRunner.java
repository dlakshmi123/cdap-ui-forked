/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.api.ApplicationSpecification;
import com.continuuity.app.program.Program;
import com.continuuity.app.program.Type;
import com.continuuity.app.runtime.ProgramController;
import com.continuuity.app.runtime.ProgramOptions;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.weave.api.WeaveController;
import com.continuuity.weave.api.WeaveRunner;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Distributed program runner for webapp.
 */
public final class DistributedWebappProgramRunner extends AbstractDistributedProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DistributedWebappProgramRunner.class);

  @Inject
  public DistributedWebappProgramRunner(WeaveRunner weaveRunner, Configuration hConf, CConfiguration cConf) {
    super(weaveRunner, hConf, cConf);
  }

  @Override
  protected ProgramController launch(Program program, ProgramOptions options,
                                     File hConfFile, File cConfFile, ApplicationLauncher launcher) {
    // Extract and verify parameters
    ApplicationSpecification appSpec = program.getSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    Type processorType = program.getType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == Type.WEBAPP, "Only WEBAPP process type is supported.");

    LOG.info("Launching distributed webapp: " + program.getName());
    WeaveController controller = launcher.launch(new WebappWeaveApplication(program, hConfFile,
                                                                            cConfFile, eventHandler));
    return new WebappWeaveProgramController(program.getName(), controller).startListen();
  }
}
