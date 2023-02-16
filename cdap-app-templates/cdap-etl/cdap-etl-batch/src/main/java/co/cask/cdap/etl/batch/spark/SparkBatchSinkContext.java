/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.etl.batch.spark;

import co.cask.cdap.api.data.batch.OutputFormatProvider;
import co.cask.cdap.api.spark.SparkContext;
import co.cask.cdap.etl.api.LookupProvider;
import co.cask.cdap.etl.api.batch.BatchRuntimeContext;
import co.cask.cdap.etl.api.batch.BatchSinkContext;

import java.util.Collections;
import java.util.Map;

/**
 * Default implementation of {@link BatchSinkContext} for spark contexts.
 */
public class SparkBatchSinkContext extends AbstractSparkBatchContext implements BatchSinkContext {

  private final SparkBatchSinkFactory sinkFactory;

  public SparkBatchSinkContext(SparkBatchSinkFactory sinkFactory, SparkContext sparkContext,
                               LookupProvider lookupProvider, String stageId) {
    super(sparkContext, lookupProvider, stageId);
    this.sinkFactory = sinkFactory;
  }

  @Override
  public void addOutput(String datasetName) {
    addOutput(datasetName, Collections.<String, String>emptyMap());
  }

  @Override
  public void addOutput(String datasetName, Map<String, String> arguments) {
    sinkFactory.addOutput(getStageName(), datasetName, arguments);
  }

  @Override
  public void addOutput(String outputName, OutputFormatProvider outputFormatProvider) {
    sinkFactory.addOutput(getStageName(), outputName, outputFormatProvider);
  }
}
