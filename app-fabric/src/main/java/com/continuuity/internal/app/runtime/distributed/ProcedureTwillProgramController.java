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
package com.continuuity.internal.app.runtime.distributed;

import com.continuuity.internal.app.runtime.ProgramOptionConstants;
import org.apache.twill.api.TwillController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
*
*/
final class ProcedureTwillProgramController extends AbstractTwillProgramController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcedureTwillProgramController.class);

  ProcedureTwillProgramController(String programName, TwillController controller) {
    super(programName, controller);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doCommand(String name, Object value) throws Exception {

    if (!ProgramOptionConstants.INSTANCES.equals(name) || !(value instanceof Map)) {
      return;
    }

    Map<String, Integer> command = (Map<String, Integer>) value;
    try {
      for (Map.Entry<String, Integer> entry : command.entrySet()) {
        LOG.info("Change procedure instance count: {} new count is: {}",  entry.getKey(), entry.getValue());
        twillController.changeInstances(entry.getKey(), entry.getValue());
        LOG.info("Procedure instance count changed: {} new count is {}", entry.getKey(), entry.getValue());
      }
    } catch (Throwable t) {
      LOG.error(String.format("Fail to change instances: %s", command), t);
    }
  }
}
