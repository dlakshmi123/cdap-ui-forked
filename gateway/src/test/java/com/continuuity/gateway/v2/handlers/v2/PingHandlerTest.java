package com.continuuity.gateway.v2.handlers.v2;

import com.continuuity.gateway.GatewayFastTestsSuite;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test ping handler.
 */
public class PingHandlerTest {
  @Test
  public void testPing() throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet("/ping");
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    Assert.assertEquals("OK.\n", EntityUtils.toString(response.getEntity()));
  }

  @Test
  public void testStatus() throws Exception {
    HttpResponse response = GatewayFastTestsSuite.doGet("/status");
    Assert.assertEquals(200, response.getStatusLine().getStatusCode());
    Assert.assertEquals("OK.\n", EntityUtils.toString(response.getEntity()));
  }
}
