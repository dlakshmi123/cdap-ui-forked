package com.continuuity.gateway.v2.tools;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data2.OperationException;
import com.continuuity.gateway.GatewayFastTestsSuite;
import com.continuuity.gateway.tools.DataSetClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Data set client.
 */
public class DataSetClientTest {
  private static String hostname = "127.0.0.1";
  private static String API_KEY = GatewayFastTestsSuite.getAuthHeader().getValue();

  @Test
  public void testValueAsCounter() throws OperationException {
    CConfiguration configuration = CConfiguration.create();
    String port = Integer.toString(GatewayFastTestsSuite.getPort());

    final String table = "tVAC";
    final String row = "myRow";
    final String column = "myCounter";
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[] {
      "create", "--table", table, "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[] {
      "write", "--table", table, "--row", row, "--column", column, "--value", "41", "--counter",
      "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));
    Assert.assertEquals("42", new DataSetClient().disallowSSL().execute(new String[] {
      "increment", "--table", table, "--row", row, "--column", column, "--value", "1",
      "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));

    configuration.set(Constants.Router.ADDRESS, hostname);
    configuration.set(Constants.Router.FORWARD, port + ":" + Constants.Service.GATEWAY + ",20000:$HOST");
    Assert.assertEquals("42", new DataSetClient().disallowSSL().execute(new String[]{
      "read", "--table", table, "--row", row, "--column", column, "--counter", "--apikey", API_KEY}, configuration));
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[]{
      "delete", "--table", table, "--row", row, "--column", column, "--value", "41", "--apikey", API_KEY},
                                                           configuration));
    Assert.assertNull(new DataSetClient().disallowSSL().execute(new String[]{
      "read", "--table", table, "--row", row, "--column", column, "--counter", "--apikey", API_KEY}, configuration));
  }

  @Test
  public void testClearDataset() throws OperationException {
    CConfiguration configuration = CConfiguration.create();
    String port = Integer.toString(GatewayFastTestsSuite.getPort());

    final String table = "tVACLr";
    final String row = "myRow";
    final String column = "myCounter";
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[] {
      "create", "--table", table, "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[] {
      "write", "--table", table, "--row", row, "--column", column, "--value", "41", "--counter",
      "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));
    Assert.assertEquals("42", new DataSetClient().disallowSSL().execute(new String[] {
      "increment", "--table", table, "--row", row, "--column", column, "--value", "1",
      "--host", hostname, "--port", port, "--apikey", API_KEY }, configuration));

    configuration.set(Constants.Router.ADDRESS, hostname);
    configuration.set(Constants.Router.FORWARD, port + ":" + Constants.Service.GATEWAY);
    Assert.assertEquals("OK.", new DataSetClient().disallowSSL().execute(new String[]{
      "clear", "--table", table, "--apikey", API_KEY}, configuration));
    Assert.assertNull(new DataSetClient().disallowSSL().execute(new String[]{
      "read", "--table", table, "--row", row, "--column", column, "--counter", "--apikey", API_KEY}, configuration));
  }
}
