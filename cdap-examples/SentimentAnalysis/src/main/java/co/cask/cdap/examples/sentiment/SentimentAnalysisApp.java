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
package co.cask.cdap.examples.sentiment;

import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.data.stream.Stream;
import co.cask.cdap.api.dataset.lib.TimeseriesTable;
import co.cask.cdap.api.dataset.table.Table;

/**
 * Application that analyzes sentiment of sentences as positive, negative or neutral.
 */
public class SentimentAnalysisApp extends AbstractApplication {

  @Override
  public void configure() {
    setName("sentiment");
    setDescription("Sentiment Analysis");
    addStream(new Stream("sentence"));
    createDataset("sentiments", Table.class);
    createDataset("text-sentiments", TimeseriesTable.class);
    addFlow(new SentimentAnalysisFlow());
    addProcedure(new SentimentAnalysisProcedure());
  }
}
