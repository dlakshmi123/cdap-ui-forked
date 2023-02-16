/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.operations.cdap;

import co.cask.cdap.AllProgramsApp;
import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.namespace.NamespaceAdmin;
import co.cask.cdap.internal.AppFabricTestHelper;
import co.cask.cdap.operations.OperationalStats;
import co.cask.cdap.proto.NamespaceMeta;
import co.cask.cdap.proto.id.NamespaceId;
import com.google.inject.Injector;
import org.apache.tephra.Transaction;
import org.apache.tephra.TransactionSystemClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

/**
 * Tests for {@link OperationalStats} for CDAP
 */
public class CDAPOperationalStatsTest {
  private static final NamespaceId NAMESPACE = new NamespaceId("operations");
  private static final long TEST_START_TIME = System.currentTimeMillis();

  private static Injector injector;
  private static NamespaceAdmin namespaceAdmin;

  @BeforeClass
  public static void setup() throws Exception {
    injector = AppFabricTestHelper.getInjector();
    namespaceAdmin = injector.getInstance(NamespaceAdmin.class);
    namespaceAdmin.create(new NamespaceMeta.Builder().setName(NAMESPACE).build());
    CConfiguration cConf = injector.getInstance(CConfiguration.class);
    AppFabricTestHelper.deployApplication(NAMESPACE.toId(), AllProgramsApp.class, null, cConf);
    TransactionSystemClient txClient = injector.getInstance(TransactionSystemClient.class);
    Transaction tx1 = txClient.startShort();
    txClient.canCommit(tx1, Collections.singleton(Bytes.toBytes("foo")));
    Transaction tx2 = txClient.startShort();
    txClient.commit(tx2);
    Transaction tx3 = txClient.startShort();
    txClient.invalidate(tx3.getTransactionId());
  }

  @AfterClass
  public static void teardown() throws Exception {
    namespaceAdmin.delete(NAMESPACE);
  }

  @Test
  public void test() throws Exception {
    CDAPInfo info = new CDAPInfo();
    Assert.assertEquals(AbstractCDAPStats.SERVICE_NAME, info.getServiceName());
    Assert.assertEquals("info", info.getStatType());
    Assert.assertTrue(info.getUptime() <= System.currentTimeMillis());
    CDAPEntities entities = new CDAPEntities();
    entities.initialize(injector);
    Assert.assertEquals(AbstractCDAPStats.SERVICE_NAME, entities.getServiceName());
    Assert.assertEquals("entities", entities.getStatType());
    entities.collect();
    Assert.assertEquals(1, entities.getNamespaces());
    Assert.assertEquals(1, entities.getArtifacts());
    Assert.assertEquals(1, entities.getApplications());
    Assert.assertEquals(7, entities.getPrograms());
    Assert.assertEquals(4, entities.getDatasets());
    Assert.assertEquals(1, entities.getStreams());
    Assert.assertEquals(0, entities.getStreamViews());
    CDAPTransactions transactions = new CDAPTransactions();
    transactions.initialize(injector);
    Assert.assertEquals(AbstractCDAPStats.SERVICE_NAME, transactions.getServiceName());
    Assert.assertEquals("transactions", transactions.getStatType());
    transactions.collect();
    Assert.assertEquals(1, transactions.getNumInProgressTransactions());
    Assert.assertEquals(1, transactions.getNumInvalidTransactions());
    Assert.assertEquals(1, transactions.getNumCommittingChangeSets());
    Assert.assertEquals(0, transactions.getNumCommittedChangeSets());
    Assert.assertTrue(transactions.getVisibilityUpperBound() > TEST_START_TIME);
    Assert.assertTrue(transactions.getSnapshotTime() > TEST_START_TIME);
    Assert.assertTrue(transactions.getReadPointer() > TEST_START_TIME);
    Assert.assertTrue(transactions.getWritePointer() > TEST_START_TIME);
    CDAPLoad requests = new CDAPLoad();
    requests.initialize(injector);
    Assert.assertEquals(AbstractCDAPStats.SERVICE_NAME, requests.getServiceName());
    Assert.assertEquals("lastHourLoad", requests.getStatType());
    requests.collect();
  }
}
