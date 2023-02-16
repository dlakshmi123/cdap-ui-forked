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
package com.continuuity.examples.counttokens;

import com.continuuity.api.annotation.ProcessInput;
import com.continuuity.api.flow.flowlet.AbstractFlowlet;
import com.continuuity.api.flow.flowlet.OutputEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tokenizer Flowlet.
 */
public class Tokenizer extends AbstractFlowlet {
  private static final Logger LOG = LoggerFactory.getLogger(Tokenizer
                                                                 .class);

  private OutputEmitter<String> output;

  @ProcessInput
  public void process(String line) {
    LOG.info("Received line: " + line);
    if (line == null || line.isEmpty()) {
      LOG.info("Received empty line");
      return;
    }

    String[] tokens = tokenize(line);

    for (String token : tokens) {
      LOG.info("Emitting token: " + token);
      output.emit(token);
    }
  }

  private String[] tokenize(String line) {
    String delimiters = "[ .-]";
    return line.split(delimiters);
  }
}
