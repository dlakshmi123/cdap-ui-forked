package com.continuuity.gateway.v2.handlers.v2.log;


import com.continuuity.gateway.GatewayFastTestsSuite;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test LogHandler.
 */
public class LogHandlerTest {
  public static String account = "developer";

  @Test
  public void testFlowNext() throws Exception {
    testNext("testApp1", "flow", "testFlow1");
    testNextNoMax("testApp1", "flow", "testFlow1");
    testNextFilter("testApp1", "flow", "testFlow1");
    testNextNoFrom("testApp1", "flow", "testFlow1");
  }

  @Test
  public void testProcedureNext() throws Exception {
    testNext("testApp2", "procedure", "testProcedure1");
    testNextNoMax("testApp2", "procedure", "testProcedure1");
    testNextFilter("testApp2", "procedure", "testProcedure1");
    testNextNoFrom("testApp2", "procedure", "testProcedure1");
  }

  @Test
  public void testMapReduceNext() throws Exception {
    testNext("testApp3", "mapreduce", "testMapReduce1");
    testNextNoMax("testApp3", "mapreduce", "testMapReduce1");
    testNextFilter("testApp3", "mapreduce", "testMapReduce1");
    testNextNoFrom("testApp3", "mapreduce", "testMapReduce1");
  }

  @Test
  public void testFlowPrev() throws Exception {
    testPrev("testApp1", "flow", "testFlow1");
    testPrevNoMax("testApp1", "flow", "testFlow1");
    testPrevFilter("testApp1", "flow", "testFlow1");
    testPrevNoFrom("testApp1", "flow", "testFlow1");
  }

  @Test
  public void testProcedurePrev() throws Exception {
    testPrev("testApp2", "procedure", "testProcedure1");
    testPrevNoMax("testApp2", "procedure", "testProcedure1");
    testPrevFilter("testApp2", "procedure", "testProcedure1");
    testPrevNoFrom("testApp2", "procedure", "testProcedure1");
  }

  @Test
  public void testMapReducePrev() throws Exception {
    testPrev("testApp3", "mapreduce", "testMapReduce1");
    testPrevNoMax("testApp3", "mapreduce", "testMapReduce1");
    testPrevFilter("testApp3", "mapreduce", "testMapReduce1");
    testPrevNoFrom("testApp3", "mapreduce", "testMapReduce1");
  }

  @Test
  public void testFlowLogs() throws Exception {
    testLogs("testApp1", "flow", "testFlow1");
    testLogsFilter("testApp1", "flow", "testFlow1");
  }

  @Test
  public void testProcedureLogs() throws Exception {
    testLogs("testApp2", "procedure", "testProcedure1");
    testLogsFilter("testApp2", "procedure", "testProcedure1");
  }

  @Test
  public void testMapReduceLogs() throws Exception {
    testLogs("testApp3", "mapreduce", "testMapReduce1");
    testLogsFilter("testApp3", "mapreduce", "testMapReduce1");
  }

  private void testNext(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response =
      GatewayFastTestsSuite.doGet(
        String.format("/v2/apps/%s/%s/%s/logs/next?fromOffset=5&max=10", appId, entityType, entityId)
      );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(10, logLines.size());
    int expected = 5;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testNextNoMax(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/next?fromOffset=10", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(50, logLines.size());
    int expected = 10;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testNextFilter(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/next?fromOffset=12&max=16&filter=loglevel=EVEN",
                    appId, entityType, entityId));
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(8, logLines.size());
    int expected = 12;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected += 2;
    }
  }

  private void testNextNoFrom(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/next", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(50, logLines.size());
    int expected = 30;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testPrev(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response =
      GatewayFastTestsSuite.doGet(
        String.format("/v2/apps/%s/%s/%s/logs/prev?fromOffset=25&max=10", appId, entityType, entityId)
      );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(10, logLines.size());
    int expected = 15;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testPrevNoMax(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/prev?fromOffset=70", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(50, logLines.size());
    int expected = 20;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testPrevFilter(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/prev?fromOffset=41&max=16&filter=loglevel=EVEN",
                    appId, entityType, entityId));
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(8, logLines.size());
    int expected = 26;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected += 2;
    }
  }

  private void testPrevNoFrom(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs/prev", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<LogLine> logLines = new Gson().fromJson(out, new TypeToken<List<LogLine>>() {}.getType());
    Assert.assertEquals(50, logLines.size());
    int expected = 30;
    for (LogLine logLine : logLines) {
      Assert.assertEquals(expected, logLine.getOffset());
      String expectedStr = entityId + "-" + expected + "\n";
      String log = logLine.getLog();
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testLogs(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs?fromTime=20&toTime=35", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<String> logLines = Lists.newArrayList(Splitter.on("\n").split(out));
    logLines.remove(logLines.size() - 1);  // Remove last element that is empty
    Assert.assertEquals(15, logLines.size());
    int expected = 20;
    for (String log : logLines) {
      String expectedStr = entityId + "-" + expected;
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected++;
    }
  }

  private void testLogsFilter(String appId, String entityType, String entityId) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet(
      String.format("/v2/apps/%s/%s/%s/logs?fromTime=20&toTime=35&filter=loglevel=EVEN", appId, entityType, entityId)
    );
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), response.getStatusLine().getStatusCode());
    String out = EntityUtils.toString(response.getEntity());
    List<String> logLines = Lists.newArrayList(Splitter.on("\n").split(out));
    logLines.remove(logLines.size() - 1);  // Remove last element that is empty
    Assert.assertEquals(8, logLines.size());
    int expected = 20;
    for (String log : logLines) {
      String expectedStr = entityId + "-" + expected;
      Assert.assertEquals(expectedStr, log.substring(log.length() - expectedStr.length()));
      expected += 2;
    }
  }
}
