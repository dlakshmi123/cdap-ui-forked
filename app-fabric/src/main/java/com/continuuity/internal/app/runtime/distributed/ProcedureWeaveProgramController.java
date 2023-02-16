/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.weave.api.WeaveController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
*/
final class ProcedureWeaveProgramController extends AbstractWeaveProgramController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcedureWeaveProgramController.class);

  ProcedureWeaveProgramController(String programName, WeaveController controller) {
    super(programName, controller);
  }

  @Override
  protected void doCommand(String name, Object value) throws Exception {
    // Procedure doesn't have any command for now.
    LOG.info("Command ignored for procedure controller: {}, {}", name, value);
  }
}