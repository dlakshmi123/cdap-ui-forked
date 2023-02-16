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

import co.cask.cdap.api.ResourceSpecification;
import co.cask.cdap.api.annotation.Handle;
import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.lib.TimeseriesTable;
import co.cask.cdap.api.dataset.table.Get;
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.api.procedure.AbstractProcedure;
import co.cask.cdap.api.procedure.ProcedureRequest;
import co.cask.cdap.api.procedure.ProcedureResponder;
import co.cask.cdap.api.procedure.ProcedureResponse;
import co.cask.cdap.api.procedure.ProcedureSpecification;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Procedure that returns the aggregates timeseries sentiment data.
 */
public class SentimentAnalysisProcedure extends AbstractProcedure {
  private static final Logger LOG = LoggerFactory.getLogger(SentimentAnalysisProcedure.class);

  @UseDataSet("sentiments")
  private Table sentiments;

  @UseDataSet("text-sentiments")
  private TimeseriesTable textSentiments;

  @Handle("aggregates")
  public void sentimentAggregates(ProcedureRequest request, ProcedureResponder response) throws Exception {
    Row row = sentiments.get(new Get("aggregate"));
    Map<byte[], byte[]> result = row.getColumns();
    if (result == null) {
      response.error(ProcedureResponse.Code.FAILURE, "No sentiments processed.");
      return;
    }
    Map<String, Long> resp = Maps.newHashMap();
    for (Map.Entry<byte[], byte[]> entry : result.entrySet()) {
      resp.put(Bytes.toString(entry.getKey()), Bytes.toLong(entry.getValue()));
    }
    response.sendJson(ProcedureResponse.Code.SUCCESS, resp);
  }

  @Handle("sentiments")
  public void getSentiments(ProcedureRequest request, ProcedureResponder response) throws Exception {
    String sentiment = request.getArgument("sentiment");
    if (sentiment == null) {
      response.error(ProcedureResponse.Code.CLIENT_ERROR, "No sentiment sent");
      return;
    }

    long time = System.currentTimeMillis();
    List<TimeseriesTable.Entry> entries =
      textSentiments.read(sentiment.getBytes(Charsets.UTF_8),
                          time - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS),
                          time);

    Map<String, Long> textTimeMap = Maps.newHashMapWithExpectedSize(entries.size());
    for (TimeseriesTable.Entry entry : entries) {
      textTimeMap.put(Bytes.toString(entry.getValue()), entry.getTimestamp());
    }
    response.sendJson(ProcedureResponse.Code.SUCCESS, textTimeMap);
  }

  @Override
  public ProcedureSpecification configure() {
    return ProcedureSpecification.Builder.with()
      .setName("sentiment-query")
      .setDescription("Sentiments Procedure")
      .withResources(ResourceSpecification.BASIC)
      .build();
  }
}
