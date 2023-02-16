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

package com.continuuity.data2.util.hbase;

import com.continuuity.common.conf.CConfiguration;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.test.SlowTests;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests reading and writing {@link CConfiguration} instances to an HBase table.
 */
@Category(SlowTests.class)
public class ConfigurationTableTest {
  private static HBaseTestingUtility hbaseUtil;

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    hbaseUtil = new HBaseTestingUtility();
    hbaseUtil.startMiniCluster();
  }

  @AfterClass
  public static void teardownAfterClass() throws Exception {
    hbaseUtil.shutdownMiniCluster();
  }

  @Test
  public void testConfigurationSerialization() throws Exception {
    CConfiguration cconf = CConfiguration.create();
    String expectedNamespace = cconf.get(DataSetAccessor.CFG_TABLE_PREFIX, DataSetAccessor.DEFAULT_TABLE_PREFIX);

    ConfigurationTable configTable = new ConfigurationTable(hbaseUtil.getConfiguration());
    configTable.write(ConfigurationTable.Type.DEFAULT, cconf);

    CConfiguration cconf2 = configTable.read(ConfigurationTable.Type.DEFAULT, expectedNamespace);
    assertNotNull(cconf2);

    for (Map.Entry<String, String> e : cconf) {
      assertEquals("Configuration value mismatch (cconf -> cconf2) for key: " + e.getKey(),
                   e.getValue(), cconf2.get(e.getKey()));
    }
    for (Map.Entry<String, String> e : cconf2) {
      assertEquals("Configuration value mismatch (cconf2 -> cconf) for key: " + e.getKey(),
                   e.getValue(), cconf.get(e.getKey()));
    }
  }
}
