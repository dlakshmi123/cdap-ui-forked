/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.shell.command.stop;

import co.cask.cdap.client.ProgramClient;
import co.cask.cdap.shell.ElementType;
import co.cask.cdap.shell.ProgramIdCompleterFactory;
import co.cask.cdap.shell.command.AbstractCommand;
import co.cask.cdap.shell.completer.Completable;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;

/**
 * Stops a program.
 */
public class StopProgramCommand extends AbstractCommand implements Completable {

  private final ProgramClient programClient;
  private final ProgramIdCompleterFactory completerFactory;
  private final ElementType elementType;

  public StopProgramCommand(ElementType elementType, ProgramIdCompleterFactory completerFactory,
                            ProgramClient programClient) {
    super(elementType.getName(), "<program-id>", "Stops a " + elementType.getPrettyName());
    this.elementType = elementType;
    this.completerFactory = completerFactory;
    this.programClient = programClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String[] programIdParts = args[0].split("\\.");
    String appId = programIdParts[0];
    String programId = programIdParts[1];

    programClient.stop(appId, elementType.getProgramType(), programId);
    output.printf("Successfully stopped %s '%s' of application '%s'\n", elementType.getPrettyName(), programId, appId);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completerFactory.getProgramIdCompleter(elementType)));
  }
}
