/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.shell.command;

import co.cask.cdap.client.DatasetTypeClient;
import co.cask.cdap.proto.DatasetModuleMeta;
import co.cask.cdap.proto.DatasetTypeMeta;
import co.cask.cdap.shell.AbstractCommand;
import co.cask.cdap.shell.ElementType;
import co.cask.cdap.shell.util.AsciiTable;
import co.cask.cdap.shell.util.RowMaker;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.PrintStream;
import java.util.List;
import javax.inject.Inject;

/**
 * Lists dataset types.
 */
public class ListDatasetTypesCommand extends AbstractCommand {

  private final DatasetTypeClient datasetTypeClient;

  @Inject
  public ListDatasetTypesCommand(DatasetTypeClient datasetTypeClient) {
    super("types", null, "Lists " + ElementType.DATASET_TYPE.getPluralPrettyName());
    this.datasetTypeClient = datasetTypeClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    List<DatasetTypeMeta> datasetTypeMetas = datasetTypeClient.list();

    new AsciiTable<DatasetTypeMeta>(
      new String[]{ "name", "modules" }, datasetTypeMetas,
      new RowMaker<DatasetTypeMeta>() {
        @Override
        public Object[] makeRow(DatasetTypeMeta object) {
          List<String> modulesStrings = Lists.newArrayList();
          for (DatasetModuleMeta module : object.getModules()) {
            modulesStrings.add(module.getName());
          }
          return new Object[] { object.getName(), Joiner.on(", ").join(modulesStrings) };
        }
      }).print(output);
  }
}
