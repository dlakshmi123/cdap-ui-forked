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

package com.continuuity.gateway.apps;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.annotation.Handle;
import com.continuuity.api.annotation.ProcessInput;
import com.continuuity.api.annotation.UseDataSet;
import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.dataset.KeyValueTable;
import com.continuuity.api.data.stream.Stream;
import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.FlowletContext;
import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.api.procedure.AbstractProcedure;
import com.continuuity.api.procedure.ProcedureRequest;
import com.continuuity.api.procedure.ProcedureResponder;
import com.continuuity.api.procedure.ProcedureResponse;

import java.io.IOException;
import java.util.Map;

/**
 * App that filters based on a threshold.
 * To test runtimeArgs.
 */
public class HighPassFilterApp implements Application {
  private static final byte[] highPass = Bytes.toBytes("h");

  @Override
  public ApplicationSpecification configure() {
    return ApplicationSpecification.Builder.with()
      .setName("HighPassFilterApp")
      .setDescription("Application for filtering numbers. Test runtimeargs.")
      .withStreams().add(new Stream("inputvalue"))
      .withDataSets().add(new KeyValueTable("counter"))
      .withFlows().add(new FilterFlow())
      .withProcedures().add(new Count())
      .noMapReduce()
      .noWorkflow()
      .build();
  }

  /**
   * Flow to implement highpass filter.
   */
  public static class FilterFlow implements Flow {
    @Override
    public FlowSpecification configure() {
      return FlowSpecification.Builder.with()
        .setName("FilterFlow")
        .setDescription("Flow for counting words")
        .withFlowlets()
        .add("filter", new Filter())
        .connect().fromStream("inputvalue").to("filter")
        .build();
    }
  }

  /**
   * Flowlet that has filtering logic.
   */
  public static class Filter extends AbstractFlowlet {

    @UseDataSet("counter")
    private KeyValueTable counters;
    private long threshold = 0L;

    @ProcessInput
    public void process (StreamEvent event) {
      //highpass filter.
      String value = Bytes.toString(event.getBody().array());
      if (Long.parseLong(value) > threshold) {
        counters.increment(highPass, 1L);
      }
    }

    @Override
    public void initialize(FlowletContext context) throws Exception {
      Map<String, String> args = context.getRuntimeArguments();
      if (args.containsKey("threshold")) {
        this.threshold = Long.parseLong(args.get("threshold"));
      }
      super.initialize(context);
    }
  }

  /**
   * return counts.
   */
  public static class Count extends AbstractProcedure {
    @UseDataSet("counter")
    private KeyValueTable counters;

    @Handle("result")
    public void handle(ProcedureRequest request, ProcedureResponder responder) throws IOException {
      byte[]  result = counters.read(highPass);
      if (result == null) {
        responder.sendJson(ProcedureResponse.Code.NOT_FOUND, "No result");
      } else {
        responder.sendJson(ProcedureResponse.Code.SUCCESS, Bytes.toLong(result));
      }
    }
  }
}
