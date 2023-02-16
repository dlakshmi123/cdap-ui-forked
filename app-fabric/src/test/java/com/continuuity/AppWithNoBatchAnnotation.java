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

package com.continuuity;

import com.continuuity.api.Application;
import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.annotation.ProcessInput;
import com.continuuity.api.data.stream.Stream;
import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import com.continuuity.api.flow.flowlet.StreamEvent;
import com.google.common.base.Charsets;

import java.util.Iterator;

/**
 * App that is not deployable. Since it has no Batch annotation in flowlet.
 * Designed for testing.
 */
public class AppWithNoBatchAnnotation implements Application {

  @Override
  public ApplicationSpecification configure() {
    return ApplicationSpecification.Builder.with()
      .setName("AppWithNoBatchAnnotation")
      .setDescription("Application with no batch annotation that cannot be deployed")
      .withStreams().add(new Stream("input"))
      .noDataSet()
      .withFlows().add(new FlowWithDeployError())
      .noProcedure()
      .noMapReduce()
      .noWorkflow()
      .build();
  }

  /**
   * Flow that has a deployment error due to missing Batch annotation.
   */
  public static final class FlowWithDeployError implements Flow {
    @Override
    public FlowSpecification configure() {
      return FlowSpecification.Builder.with()
        .setName("ToyFlow")
        .setDescription("Complex Toy Flow")
        .withFlowlets()
        .add(new ParserFlowlet())
        .add(new BatchExecutionFlowlet())
        .connect()
        .fromStream("input").to("ParserFlowlet")
        .from("ParserFlowlet").to("BatchExecutionFlowlet")
        .build();
    }
  }

  /**
   * Simple Flowlet.
   */
  public static final class ParserFlowlet extends AbstractFlowlet {
    private OutputEmitter<String> out;
    @ProcessInput
    public void process(StreamEvent event) {
      out.emit(Charsets.UTF_8.decode(event.getBody()).toString());
    }
  }

  /**
   * Batch execution flowlet with error.
   */
  public static final class BatchExecutionFlowlet extends AbstractFlowlet {
    @ProcessInput
    public void process(Iterator<String> datum) {
      while (datum.hasNext()) {
        datum.next();
        //no-op we are just testing the Batch execution mechanics here.
      }
    }
  }

}
