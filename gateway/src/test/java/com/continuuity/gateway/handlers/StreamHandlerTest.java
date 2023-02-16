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

package com.continuuity.gateway.handlers;

import com.continuuity.common.conf.Constants;
import com.continuuity.gateway.GatewayTestBase;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Test stream handler. This is not part of GatewayFastTestsSuite because it needs to start the gateway multiple times.
 */
public class StreamHandlerTest extends GatewayTestBase {
  private static final String API_KEY = GatewayTestBase.getAuthHeader().getValue();
  private static final String hostname = "127.0.0.1";

  private HttpURLConnection openURL(String location, HttpMethod method) throws IOException {
    URL url = new URL(location);
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    urlConn.setRequestMethod(method.getName());
    urlConn.setRequestProperty(Constants.Gateway.CONTINUUITY_API_KEY, API_KEY);

    return urlConn;
  }

  @Test
  public void testStreamCreate() throws Exception {
    int port = GatewayTestBase.getPort();

    // Try to get info on a non-existant stream
    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1/info", hostname, port),
                                        HttpMethod.GET);

    Assert.assertEquals(HttpResponseStatus.NOT_FOUND.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // Now, create the new stream.
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1", hostname, port), HttpMethod.PUT);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // getInfo should now return 200
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1/info", hostname, port),
                                        HttpMethod.GET);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testSimpleStreamEnqueue() throws Exception {
    int port = GatewayTestBase.getPort();

    // Create new stream.
    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue", hostname, port),
                                        HttpMethod.PUT);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // Enqueue 10 entries
    for (int i = 0; i < 10; ++i) {
      urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue", hostname, port), HttpMethod.POST);
      urlConn.setDoOutput(true);
      urlConn.addRequestProperty("test_stream_enqueue.header1", Integer.toString(i));
      urlConn.getOutputStream().write(Integer.toString(i).getBytes(Charsets.UTF_8));
      Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
      urlConn.disconnect();
    }

    String groupId = getStreamConsumer("test_stream_enqueue");

    // Dequeue 10 entries
    for (int i = 0; i < 10; ++i) {
      urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue/dequeue",
                                                        hostname, port), HttpMethod.POST);
      urlConn.setRequestProperty(Constants.Stream.Headers.CONSUMER_ID, groupId);
      Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
      int actual = Integer.parseInt(new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8));
      Assert.assertEquals(i, actual);

      Assert.assertEquals(Integer.toString(i), urlConn.getHeaderField("test_stream_enqueue.header1"));
      urlConn.disconnect();
    }

    // Dequeue-ing again should give NO_CONTENT
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue/dequeue",
                                    hostname, port), HttpMethod.POST);
    urlConn.setRequestProperty(Constants.Stream.Headers.CONSUMER_ID, groupId);
    Assert.assertEquals(HttpResponseStatus.NO_CONTENT.getCode(), urlConn.getResponseCode());
  }

  private String getStreamConsumer(String streamName) throws IOException {
    int port = GatewayTestBase.getPort();

    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/%s/consumer-id",
                                                      hostname, port, streamName), HttpMethod.POST);
    try {
      Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
      return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    } finally {
      urlConn.disconnect();
    }
  }
}
