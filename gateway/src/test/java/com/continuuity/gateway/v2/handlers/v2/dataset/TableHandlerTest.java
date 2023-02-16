package com.continuuity.gateway.v2.handlers.v2.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.OperationResult;
import com.continuuity.api.data.dataset.table.Read;
import com.continuuity.api.data.dataset.table.Table;
import com.continuuity.api.data.dataset.table.Write;
import com.continuuity.data.operation.OperationContext;
import com.continuuity.data2.transaction.TransactionSystemClient;
import com.continuuity.gateway.GatewayFastTestsSuite;
import com.continuuity.gateway.TestUtil;
import com.continuuity.gateway.util.DataSetInstantiatorFromMetaData;
import com.continuuity.gateway.v2.txmanager.TxManager;
import com.continuuity.metadata.MetadataService;
import com.continuuity.metadata.thrift.Account;
import com.continuuity.metadata.thrift.Dataset;
import com.continuuity.metadata.thrift.MetadataServiceException;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

/**
 * Tests TableHandler.
 */
public class TableHandlerTest {
  private static final OperationContext context = TestUtil.DEFAULT_CONTEXT;

  @Test
  public void testTableReads() throws Exception {
    Table t = newTable("tTR_" + System.nanoTime());
    // write a row with 10 cols c0...c9 with values v0..v9
    String row = "tTR10";
    byte[] rowKey = row.getBytes();
    byte[][] cols = new byte[10][];
    byte[][] vals = new byte[10][];
    for (int i = 0; i < 10; ++i) {
      cols[i] = ("c" + i).getBytes();
      vals[i] = ("v" + i).getBytes();
    }

    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    TransactionSystemClient txClient = GatewayFastTestsSuite.getInjector().getInstance(TransactionSystemClient.class);
    TxManager txManager = new TxManager(txClient, instantiator.getInstantiator().getTransactionAware());

    txManager.start();
    t.write(new Write(rowKey, cols, vals));
    txManager.commit();

    // now read back in various ways
    String queryPrefix = "/v2/tables/" + t.getName() + "/row/" + row;
    assertRead(queryPrefix, 0, 9, ""); // all columns
    assertRead(queryPrefix, 5, 5, "?columns=c5"); // only c5
    assertRead(queryPrefix, 3, 5, "?columns=c5,c3,c4"); // only c3,c4, and c5
    assertRead(queryPrefix, 8, 9, "?columns=c8,c9,c10"); // only c8 and c9
    assertRead(queryPrefix, 0, 4, "?stop=c5"); // range up to exclusive 5
    assertRead(queryPrefix, 0, 2, "?stop=c5&limit=3"); // range up to exclusive 5, limit 3 -> c0..c2
    assertRead(queryPrefix, 0, 2, "?stop=c3&limit=5"); // range up to exclusive 3, limit 5 -> c0..c2
    assertRead(queryPrefix, 5, 9, "?start=c5"); // range starting at 5
    assertRead(queryPrefix, 5, 7, "?start=c5&limit=3"); // range starting at 5, limit 3 -> c5..c7
    assertRead(queryPrefix, 0, 4, "?limit=5"); // all limit 5 -> c0..c5
    assertRead(queryPrefix, 0, 9, "?limit=12"); // all limit 12 -> c0..c9
    assertRead(queryPrefix, 2, 5, "?start=c2&stop=c6"); // range from 2 to exclusive 6 -> 2,3,4,5
    assertRead(queryPrefix, 2, 4, "?start=c2&stop=c6&limit=3"); // range from 2 to exclusive 6 limited to 3 -> 2,3,4

    // now read some stuff that returns errors
    assertReadFails(queryPrefix, "/c1", HttpStatus.SC_NOT_FOUND); // path does not end with row
    assertReadFails(queryPrefix, "?columns=c1,c2&start=c5", HttpStatus.SC_BAD_REQUEST); // range and list of columns
    assertReadFails(queryPrefix, "?columns=c10&encoding=hex", HttpStatus.SC_BAD_REQUEST); // col invalid under encoding
    assertReadFails(queryPrefix, "?columns=c10&encoding=blah", HttpStatus.SC_BAD_REQUEST); // bad encoding
    assertReadFails(queryPrefix, "?columns=a", HttpStatus.SC_NO_CONTENT); // non-existing column
    assertReadFails("", "/v2/tables/" + t.getName() + "/row/abc", HttpStatus.SC_NO_CONTENT); // non-existing row
    assertReadFails("", "/v2/tables/abc/row/tTR10", HttpStatus.SC_NOT_FOUND); // non-existing table
  }

  @Test
  public void testTableWritesAndDeletes() throws Exception {
    String urlPrefix = "/v2";
    Table t = newTable("tTW_" + System.nanoTime());
    String row = "abc";
    byte[] c1 = { 'c', '1' }, c2 = { 'c', '2' }, c3 = { 'c', '3' };
    byte[] v1 = { 'v', '1' }, mt = { }, v3 = { 'v', '3' };

    // write a row with 3 cols c1...c3 with values v1, "", v3
    String json = "{\"c1\":\"v1\",\"c2\":\"\",\"c3\":\"v3\"}";
    assertWrite(urlPrefix, HttpStatus.SC_OK, "/tables/" + t.getName() + "/row/" + row, json);

    // starting new tx so that we see what was committed
    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    TransactionSystemClient txClient = GatewayFastTestsSuite.getInjector().getInstance(TransactionSystemClient.class);
    TxManager txManager = new TxManager(txClient, instantiator.getInstantiator().getTransactionAware());

    // read back directly and verify
    txManager.start();
    OperationResult<Map<byte[], byte[]>> result = t.read(new Read(row.getBytes()));
    Assert.assertFalse(result.isEmpty());
    Assert.assertArrayEquals(v1, result.getValue().get(c1));
    byte[] r2 = result.getValue().get(c2);
    Assert.assertTrue(null == r2 || Arrays.equals(mt, r2));
    Assert.assertArrayEquals(v3, result.getValue().get(c3));

    // delete c1 and c2
    assertDelete(urlPrefix, HttpStatus.SC_OK, "/tables/" + t.getName() + "/row/" + row + "?columns=c1;columns=c2");

    // starting new tx so that we see what was committed
    txManager.commit();

    txManager.start();

    // read back directly and verify they're gone
    result = t.read(new Read(row.getBytes()));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(1, result.getValue().size());
    Assert.assertNull(result.getValue().get(c1));
    Assert.assertNull(result.getValue().get(c2));
    Assert.assertArrayEquals(v3, result.getValue().get(c3));

    // test some error cases
    assertWrite(urlPrefix, HttpStatus.SC_NOT_FOUND, "/tables/abc/row/" + row, json); // non-existent table
    assertWrite(urlPrefix, HttpStatus.SC_NOT_FOUND, "/tables/abc/row/a/x" + row, json); // path does not end with row
    assertWrite(urlPrefix, HttpStatus.SC_BAD_REQUEST, "/tables/" + t.getName() + "/row/" + row, ""); // no json
    assertWrite(urlPrefix, HttpStatus.SC_BAD_REQUEST, "/tables/" + t.getName() + "/row/" + row,
                "{\"\"}"); // wrong json

    // test errors for delete
    assertDelete(urlPrefix, HttpStatus.SC_BAD_REQUEST, "/tables/" + t.getName() + "/row/" + row); // no columns
    // specified
    assertDelete(urlPrefix, HttpStatus.SC_NOT_FOUND, "/tables/abc/row/" + row + "?columns=a"); // non-existent table
    assertDelete(urlPrefix, HttpStatus.SC_METHOD_NOT_ALLOWED, "/tables/" + t.getName()); // no/empty row key
    assertDelete(urlPrefix, HttpStatus.SC_METHOD_NOT_ALLOWED, "/tables//" + t.getName()); // no/empty row key
  }

  @Test
  public void testIncrement() throws Exception {
    String urlPrefix = "/v2";
    Table t = newTable("tI_" + System.nanoTime());
    String row = "abc";
    // directly write a row with two columns, a long, b not
    final byte[] a = { 'a' }, b = { 'b' }, c = { 'c' };
    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    TransactionSystemClient txClient = GatewayFastTestsSuite.getInjector().getInstance(TransactionSystemClient.class);
    TxManager txManager = new TxManager(txClient, instantiator.getInstantiator().getTransactionAware());

    txManager.start();
    t.write(new Write(row.getBytes(), new byte[][] { a, b }, new byte[][] { Bytes.toBytes(7L), b }));
    txManager.commit();

    // submit increment for row with c1 and c3, should succeed
    String json = "{\"a\":35, \"c\":11}";
    Map<String, Long> map = assertIncrement(urlPrefix, 200, "/tables/" + t.getName() + "/row/" + row, json);

    // starting new tx so that we see what was committed
    txManager.start();
    // verify result is the incremented value
    Assert.assertNotNull(map);
    Assert.assertEquals(2L, map.size());
    Assert.assertEquals(new Long(42), map.get("a"));
    Assert.assertEquals(new Long(11), map.get("c"));

    // verify directly incremented has happened
    OperationResult<Map<byte[], byte[]>> result = t.read(new Read(row.getBytes()));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(3, result.getValue().size());
    Assert.assertArrayEquals(Bytes.toBytes(42L), result.getValue().get(a));
    Assert.assertArrayEquals(b, result.getValue().get(b));
    Assert.assertArrayEquals(Bytes.toBytes(11L), result.getValue().get(c));

    // submit an increment for a and b, must fail with not-a-number
    json = "{\"a\":1,\"b\":12}";
    assertIncrement(urlPrefix, 400, "/tables/" + t.getName() + "/row/" + row, json);

    // starting new tx so that we see what was committed
    txManager.commit();
    txManager.start();
    // verify directly that the row is unchanged
    result = t.read(new Read(row.getBytes()));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(3, result.getValue().size());
    Assert.assertArrayEquals(Bytes.toBytes(42L), result.getValue().get(a));
    Assert.assertArrayEquals(b, result.getValue().get(b));
    Assert.assertArrayEquals(Bytes.toBytes(11L), result.getValue().get(c));

    // submit an increment for non-existent row, should succeed
    json = "{\"a\":1,\"b\":-12}";
    map = assertIncrement(urlPrefix, 200, "/tables/" + t.getName() + "/row/xyz", json);

    // starting new tx so that we see what was committed
    txManager.commit();
    txManager.start();
    // verify return value is equal to increments
    Assert.assertNotNull(map);
    Assert.assertEquals(2L, map.size());
    Assert.assertEquals(new Long(1), map.get("a"));
    Assert.assertEquals(new Long(-12), map.get("b"));
    // verify directly that new values are there
    // verify directly that the row is unchanged
    result = t.read(new Read("xyz".getBytes()));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(2, result.getValue().size());
    Assert.assertArrayEquals(Bytes.toBytes(1L), result.getValue().get(a));
    Assert.assertArrayEquals(Bytes.toBytes(-12L), result.getValue().get(b));

    // test some bad cases
    assertIncrement(urlPrefix, 404, "/tables/" + t.getName() + "1/abc", json); // table does not exist
    assertIncrement(urlPrefix, 404, "/tables/" + t.getName() + "1/abc/x", json); // path does not end on row
    assertIncrement(urlPrefix, 400, "/tables/" + t.getName() + "/row/xyz", "{\"a\":\"b\"}"); // json invalid
    assertIncrement(urlPrefix, 400, "/tables/" + t.getName() + "/row/xyz", "{\"a\":1"); // json invalid
  }

  @Test
  public void testEncodingOfKeysAndValues() throws Exception {

    // first create the table
    String tableName = "tEOCAV_" + System.nanoTime();
    Table table = createTable(tableName);
    byte[] x = { 'x' }, y = { 'y' }, z = { 'z' }, a = { 'a' };

    // setup accessor
    String urlPrefix = "/v2";
    String tablePrefix = urlPrefix + "/tables/" + tableName + "/row/";

    // table is empty, write value z to column y of row x, use encoding "url"
    assertWrite(tablePrefix, HttpStatus.SC_OK, "%78" + "?encoding=url", "{\"%79\":\"%7A\"}");
    // read back directly and verify

    // starting new tx so that we see what was committed
    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    TransactionSystemClient txClient = GatewayFastTestsSuite.getInjector().getInstance(TransactionSystemClient.class);
    TxManager txManager = new TxManager(txClient, instantiator.getInstantiator().getTransactionAware());

    txManager.start();
    OperationResult<Map<byte[], byte[]>> result = table.read(new Read(x));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(1, result.getValue().size());
    Assert.assertArrayEquals(z, result.getValue().get(y));

    // read back with same encoding through REST - the response will not escape y or z
    assertRead(tablePrefix, "%78" + "?columns=%79" + "&encoding=url", "y", "z");
    // read back with hex encoding through REST - the response will escape y and z
    assertRead(tablePrefix, "78" + "?columns=79" + "&encoding=hex", "79", "7a");
    // read back with base64 encoding through REST - the response will escape y and z
    assertRead(tablePrefix, "eA" + "?columns=eQ" + "&encoding=base64", "eQ", "eg");

    // delete using hex encoding
    assertDelete(tablePrefix, 200, "78" + "?columns=79" + "&encoding=hex");

    // starting new tx so that we see what was committed
    txManager.commit();
    txManager.start();
    // and verify that it is really gone
    OperationResult<Map<byte[], byte[]>> read = table.read(new Read(x));
    Assert.assertTrue(read.isEmpty());

    // increment column using REST
    assertIncrement(tablePrefix, 200, "eA" + "?encoding=base64", "{\"YQ\":42}");
    // verify the value was written using the Table
    // starting new tx so that we see what was committed
    txManager.commit();
    txManager.start();
    result = table.read(new Read(x));
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(1, result.getValue().size());
    Assert.assertArrayEquals(Bytes.toBytes(42L), result.getValue().get(a));
    // read back via REST with hex encoding
    assertRead(tablePrefix, "eA" + "?column=YQ" + "&encoding=base64", "YQ",
               Base64.encodeBase64URLSafeString(Bytes.toBytes(42L)));
  }

  static Table createTable(String name) throws Exception {
    Table table = newTable(name);
    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    TransactionSystemClient txClient = GatewayFastTestsSuite.getInjector().getInstance(TransactionSystemClient.class);
    TxManager txManager = new TxManager(txClient, instantiator.getInstantiator().getTransactionAware());

    txManager.start();
    table.write(new Write(new byte[] {'a'}, new byte[] {'b'}, new byte[] {'c'}));
    txManager.commit();
    return table;
  }

  static Table newTable(String name) throws TException, MetadataServiceException {
    DataSetSpecification spec = new Table(name).configure();
    Dataset ds = new Dataset(spec.getName());
    ds.setName(spec.getName());
    ds.setType(spec.getType());
    ds.setSpecification(new Gson().toJson(spec));

    MetadataService mds = GatewayFastTestsSuite.getInjector().getInstance(MetadataService.class);
    mds.assertDataset(new Account(context.getAccount()), ds);

    DataSetInstantiatorFromMetaData instantiator =
      GatewayFastTestsSuite.getInjector().getInstance(DataSetInstantiatorFromMetaData.class);
    Table table = instantiator.getDataSet(name, context);
    table.getName();
    return table;
  }

  static void assertRead(String prefix, int start, int end, String query) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.GET(prefix + query);
    Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

    Reader reader = new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8);
    Type stringMapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> map = new Gson().fromJson(reader, stringMapType);
    Assert.assertEquals(end - start + 1, map.size());
    for (int i = start; i < end; i++) {
      Assert.assertEquals("v" + i, map.get("c" + i));
    }
  }

  static void assertRead(String prefix, String query, String col, String val) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.GET(prefix + query);
    Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    Reader reader = new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8);
    Type stringMapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> map = new Gson().fromJson(reader, stringMapType);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(val, map.get(col));
  }

  static void assertReadFails(String prefix, String query, int expected) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.GET(prefix + query);
      Assert.assertEquals(expected, response.getStatusLine().getStatusCode());
  }

  static void assertWrite(String prefix, int expected, String query, String json) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.PUT(prefix + query, json);
    Assert.assertEquals(expected, response.getStatusLine().getStatusCode());
  }

  static void assertDelete(String prefix, int expected, String query) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.DELETE(prefix + query);
    Assert.assertEquals(expected, response.getStatusLine().getStatusCode());
  }

  static void assertCreate(String prefix, int expected, String query) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.PUT(prefix + query);
    Assert.assertEquals(expected, response.getStatusLine().getStatusCode());
  }

  static Map<String, Long> assertIncrement(String prefix, int expected, String query, String json) throws Exception {
    HttpResponse response = GatewayFastTestsSuite.POST(prefix + query, json);
    Assert.assertEquals(expected, response.getStatusLine().getStatusCode());
    if (expected != HttpStatus.SC_OK) {
      return null;
    }

    Reader reader = new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8);
    // JSon always returns string maps, no matter what the type, must be due to type erasure
    Type valueMapType = new TypeToken<Map<String, String>>(){}.getType();
    Map<String, String> map = new Gson().fromJson(reader, valueMapType);
    // convert to map(string->long)
    Map<String, Long> longMap = Maps.newHashMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      longMap.put(entry.getKey(), Long.parseLong(entry.getValue()));
    }
    return longMap;
  }
}
