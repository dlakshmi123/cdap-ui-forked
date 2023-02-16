/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.metrics.query;

import com.continuuity.common.metrics.MetricsScope;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 *
 */
public class MetricsRequestParserTest {

  @Test
  public void testQueryArgs() {
    MetricsRequest request = MetricsRequestParser.parse(URI.create("/reactor/apps/app1/reads?count=60"));
    Assert.assertEquals(MetricsRequest.Type.TIME_SERIES, request.getType());
    Assert.assertEquals(60, request.getCount());

    request = MetricsRequestParser.parse(URI.create("/reactor/apps/app1/reads?summary=true"));
    Assert.assertEquals(MetricsRequest.Type.SUMMARY, request.getType());

    request = MetricsRequestParser.parse(URI.create("/reactor/apps/app1/reads?count=60&start=1&end=61"));
    Assert.assertEquals(1, request.getStartTime());
    Assert.assertEquals(61, request.getEndTime());
    Assert.assertEquals(MetricsRequest.Type.TIME_SERIES, request.getType());
  }

  @Test
  public void testScope() {
    MetricsRequest request = MetricsRequestParser.parse(URI.create("/reactor/apps/app1/reads?summary=true"));
    Assert.assertEquals(MetricsScope.REACTOR, request.getScope());

    request = MetricsRequestParser.parse(URI.create("/user/apps/app1/reads?summary=true"));
    Assert.assertEquals(MetricsScope.USER, request.getScope());
  }

  @Test
  public void testOverview() {
    MetricsRequest request = MetricsRequestParser.parse(URI.create("/reactor/reads?aggregate=true"));
    Assert.assertNull(request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());
  }

  @Test
  public void testApps() {
    MetricsRequest request = MetricsRequestParser.parse(URI.create("/reactor/apps/app1/reads?aggregate=true"));
    Assert.assertEquals("app1", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());
  }

  @Test
  public void testFlow() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/flowlets/flowlet1/process.bytes?count=60&start=1&end=61"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("process.bytes", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/some.metric?summary=true"));
    Assert.assertEquals("app1.f.flow1", request.getContextPrefix());
    Assert.assertEquals("some.metric", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/loads?aggregate=true"));
    Assert.assertEquals("app1.f", request.getContextPrefix());
    Assert.assertEquals("loads", request.getMetricPrefix());
  }

  @Test
  public void testQueues() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/flowlets/flowlet1/queues/queue1/process.bytes.in?count=60"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("process.bytes.in", request.getMetricPrefix());
    Assert.assertEquals("queue1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/flowlets/flowlet1/queues/queue1/process.bytes.out?count=60"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("process.bytes.out", request.getMetricPrefix());
    Assert.assertEquals("queue1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/flowlets/flowlet1/queues/queue1/process.events.in?count=60"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("process.events.in", request.getMetricPrefix());
    Assert.assertEquals("queue1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/flows/flow1/flowlets/flowlet1/queues/queue1/process.events.out?count=60"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("process.events.out", request.getMetricPrefix());
    Assert.assertEquals("queue1", request.getTagPrefix());
  }

  @Test
  public void testMapReduce() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/mapreduce/mapred1/mappers/reads?summary=true"));
    Assert.assertEquals("app1.b.mapred1.m", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/mapreduce/mapred1/reducers/reads?summary=true"));
    Assert.assertEquals("app1.b.mapred1.r", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/mapreduce/mapred1/reads?summary=true"));
    Assert.assertEquals("app1.b.mapred1", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/mapreduce/reads?summary=true"));
    Assert.assertEquals("app1.b", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());
  }

  @Test
  public void testProcedure() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/procedures/proc1/reads?summary=true"));
    Assert.assertEquals("app1.p.proc1", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/apps/app1/procedures/reads?summary=true"));
    Assert.assertEquals("app1.p", request.getContextPrefix());
    Assert.assertEquals("reads", request.getMetricPrefix());
  }

  @Test
  public void testDataset() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/datasets/dataset1/apps/app1/flows/flow1/flowlets/flowlet1/store.reads?summary=true"));
    Assert.assertEquals("app1.f.flow1.flowlet1", request.getContextPrefix());
    Assert.assertEquals("store.reads", request.getMetricPrefix());
    Assert.assertEquals("dataset1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/datasets/dataset1/apps/app1/flows/flow1/store.reads?summary=true"));
    Assert.assertEquals("app1.f.flow1", request.getContextPrefix());
    Assert.assertEquals("store.reads", request.getMetricPrefix());
    Assert.assertEquals("dataset1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/datasets/dataset1/apps/app1/flows/store.reads?summary=true"));
    Assert.assertEquals("app1.f", request.getContextPrefix());
    Assert.assertEquals("store.reads", request.getMetricPrefix());
    Assert.assertEquals("dataset1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/datasets/dataset1/apps/app1/store.reads?summary=true"));
    Assert.assertEquals("app1", request.getContextPrefix());
    Assert.assertEquals("store.reads", request.getMetricPrefix());
    Assert.assertEquals("dataset1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/datasets/dataset1/store.reads?summary=true"));
    Assert.assertNull(request.getContextPrefix());
    Assert.assertEquals("store.reads", request.getMetricPrefix());
    Assert.assertEquals("dataset1", request.getTagPrefix());
  }

  @Test
  public void testStream() {
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/reactor/streams/stream1/apps/app1/flows/flow1/collect.events?summary=true"));
    Assert.assertEquals("app1.f.flow1", request.getContextPrefix());
    Assert.assertEquals("collect.events", request.getMetricPrefix());
    Assert.assertEquals("stream1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/streams/stream1/apps/app1/flows/collect.events?summary=true"));
    Assert.assertEquals("app1.f", request.getContextPrefix());
    Assert.assertEquals("collect.events", request.getMetricPrefix());
    Assert.assertEquals("stream1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/streams/stream1/apps/app1/collect.events?summary=true"));
    Assert.assertEquals("app1", request.getContextPrefix());
    Assert.assertEquals("collect.events", request.getMetricPrefix());
    Assert.assertEquals("stream1", request.getTagPrefix());

    request = MetricsRequestParser.parse(
      URI.create("/reactor/streams/stream1/collect.events?summary=true"));
    Assert.assertNull(request.getContextPrefix());
    Assert.assertEquals("collect.events", request.getMetricPrefix());
    Assert.assertEquals("stream1", request.getTagPrefix());
  }

  @Test
  public void testMetricURIDecoding() throws UnsupportedEncodingException {
    String weirdMetric = "/weird?me+tr ic#$name////";
    // encoded version or weirdMetric
    String encodedWeirdMetric = "%2Fweird%3Fme%2Btr%20ic%23%24name%2F%2F%2F%2F";
    MetricsRequest request = MetricsRequestParser.parse(
      URI.create("/user/apps/app1/flows/" + encodedWeirdMetric + "?aggregate=true"));
    Assert.assertEquals("app1.f", request.getContextPrefix());
    Assert.assertEquals(weirdMetric, request.getMetricPrefix());
    Assert.assertEquals(MetricsScope.USER, request.getScope());

    request = MetricsRequestParser.parse(
      URI.create("/user/apps/app1/" + encodedWeirdMetric + "?aggregate=true"));
    Assert.assertEquals("app1", request.getContextPrefix());
    Assert.assertEquals(weirdMetric, request.getMetricPrefix());
    Assert.assertEquals(MetricsScope.USER, request.getScope());
  }


  @Test(expected = IllegalArgumentException.class)
  public void testUserMetricBadURIThrowsException() {
    String badEncoding = "/%2";
    MetricsRequestParser.parse(URI.create("/user/apps/app1/flows" + badEncoding + "?aggregate=true"));
  }
}