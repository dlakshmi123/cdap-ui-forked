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

package net.fake.test.app;

import com.continuuity.test.ApplicationManager;
import com.continuuity.test.FlowManager;
import com.continuuity.test.ProcedureClient;
import com.continuuity.test.ProcedureManager;
import com.continuuity.test.ReactorTestBase;
import com.continuuity.test.RuntimeMetrics;
import com.continuuity.test.RuntimeStats;
import com.continuuity.test.SlowTests;
import com.continuuity.test.StreamWriter;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

/**
 * Tests bundle jar feature, in which the application jar contains
 * its dependency jars inside the "/lib" folder within the application jar.
 */
@Category(SlowTests.class)
public class TestBundleJarApp extends ReactorTestBase {

  @Test
  public void testFlow() throws IOException, URISyntaxException, TimeoutException, InterruptedException {
    File helloWorldJar = new File(TestBundleJarApp.class.getClassLoader().getResource("helloworld.jar").toURI());
    ApplicationManager applicationManager = deployApplication(BundleJarApp.class, helloWorldJar);

    FlowManager flowManager = applicationManager.startFlow("SimpleFlow");
    StreamWriter streamWriter = applicationManager.getStreamWriter("simpleInputStream");
    for (int i = 0; i < 5; i++) {
      streamWriter.send("test" + i + ":" + i);
    }

    // Check the flowlet metrics
    RuntimeMetrics flowletMetrics = RuntimeStats.getFlowletMetrics("BundleJarApp", "SimpleFlow", "SimpleFlowlet");
    Thread.sleep(3000);

    // TODO: not working
    //flowletMetrics.waitForProcessed(100, 5, TimeUnit.SECONDS);
    // Assert.assertEquals(0L, flowletMetrics.getException());

    // Query the result
    ProcedureManager procedureManager = applicationManager.startProcedure("SimpleGetInput");
    ProcedureClient procedureClient = procedureManager.getClient();

    // Verify the query result
    String queryResult = procedureClient.query("get", ImmutableMap.of("key", "test1"));
    String expectedQueryResult = new Gson().toJson(
      ImmutableMap.of("test1", "1" + BundleJarApp.EXPECTED_LOAD_TEST_CLASSES_OUTPUT));
    Assert.assertEquals(expectedQueryResult, queryResult);
  }

  @Test
  public void testProcedure() throws IOException, URISyntaxException {
    File helloWorldJar = new File(TestBundleJarApp.class.getClassLoader().getResource("helloworld.jar").toURI());
    ApplicationManager appManager = deployApplication(BundleJarApp.class, helloWorldJar);
    ProcedureManager procedureManager = appManager.startProcedure("PrintProcedure");

    String helloWorldClassName = "hello.HelloWorld";
    String result = procedureManager.getClient().query("load", ImmutableMap.of("class", helloWorldClassName));

    String expected = new Gson().toJson(
      ImmutableMap.builder()
        .put("Class.forName", helloWorldClassName)
        .build());

    Assert.assertEquals(expected, result);
    procedureManager.stop();
  }

}
