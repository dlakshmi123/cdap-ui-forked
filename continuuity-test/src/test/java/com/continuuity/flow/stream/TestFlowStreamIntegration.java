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

package com.continuuity.flow.stream;

import com.continuuity.test.ApplicationManager;
import com.continuuity.test.ReactorTestBase;
import com.continuuity.test.RuntimeMetrics;
import com.continuuity.test.RuntimeStats;
import com.continuuity.test.SlowTests;
import com.continuuity.test.StreamWriter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@Category(SlowTests.class)
public class TestFlowStreamIntegration extends ReactorTestBase {
  @Test
  public void testStreamBatch() throws Exception {
    ApplicationManager applicationManager = deployApplication(TestFlowStreamIntegrationApp.class);
    try {
      StreamWriter s1 = applicationManager.getStreamWriter("s1");
      for (int i = 0; i < 50; i++) {
        s1.send(String.valueOf(i));
      }

      applicationManager.startFlow("StreamTestFlow");
      RuntimeMetrics flowletMetrics1 = RuntimeStats.getFlowletMetrics("TestFlowStreamIntegrationApp",
                                                                      "StreamTestFlow", "StreamReader");
      flowletMetrics1.waitForProcessed(1, 10, TimeUnit.SECONDS);
      if (flowletMetrics1.getException() > 0) {
        Assert.fail("StreamReader test failed");
      }
    } finally {
      applicationManager.stopAll();
      TimeUnit.SECONDS.sleep(1);
      clear();
    }
  }
}
