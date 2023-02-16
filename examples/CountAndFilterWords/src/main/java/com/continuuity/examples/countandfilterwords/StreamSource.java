/*
 * Copyright 2014 Continuuity, Inc.
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
package com.continuuity.examples.countandfilterwords;

import com.continuuity.api.annotation.ProcessInput;
import com.continuuity.api.common.Bytes;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.api.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream Source Flowlet.
 */
public class StreamSource extends AbstractFlowlet {
  private static final Logger LOG = LoggerFactory.getLogger(StreamSource
                                                              .class);
  private OutputEmitter<String> output;
  private Metrics metric;

  @ProcessInput
  public void process(StreamEvent event) {
    LOG.info(this.getContext().getName() + ": Received event " + event);

    byte[] body = Bytes.toBytes(event.getBody());
    String line = Bytes.toString(body);

    LOG.info(this.getContext().getName() + ": Emitting " + line);

    metric.count("input.events", 1);
    output.emit(line);
  }
}

