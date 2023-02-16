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

package com.continuuity.common.queue;

import org.junit.Assert;
import org.junit.Test;

/**
 * tests that queue name works properly.
 */
public class QueueNameTest {

  @Test
  public void testQueueNameForFlowlet() {
    // create a queue name
    QueueName queueName = QueueName.fromFlowlet("app", "flow", "flowlet", "out");

    // verify all parts are correct
    Assert.assertTrue(queueName.isQueue());
    Assert.assertFalse(queueName.isStream());
    Assert.assertEquals("app", queueName.getFirstComponent());
    Assert.assertEquals("flow", queueName.getSecondComponent());
    Assert.assertEquals("flowlet", queueName.getThirdComponent());
    Assert.assertEquals("out", queueName.getSimpleName());

    // convert to URI and back, verify again
    queueName = QueueName.from(queueName.toURI());
    Assert.assertTrue(queueName.isQueue());
    Assert.assertFalse(queueName.isStream());
    Assert.assertEquals("app", queueName.getFirstComponent());
    Assert.assertEquals("flow", queueName.getSecondComponent());
    Assert.assertEquals("flowlet", queueName.getThirdComponent());
    Assert.assertEquals("out", queueName.getSimpleName());

    // convert to bytes and back, verify again
    queueName = QueueName.from(queueName.toBytes());
    Assert.assertTrue(queueName.isQueue());
    Assert.assertFalse(queueName.isStream());
    Assert.assertEquals("app", queueName.getFirstComponent());
    Assert.assertEquals("flow", queueName.getSecondComponent());
    Assert.assertEquals("flowlet", queueName.getThirdComponent());
    Assert.assertEquals("out", queueName.getSimpleName());
  }

  @Test
  public void testQueueNameForStream() {
    // create a queue name
    QueueName queueName = QueueName.fromStream("mystream");
    verifyStreamName(queueName, "mystream");

    queueName = QueueName.fromStream("audi_test_stream");
    verifyStreamName(queueName, "audi_test_stream");

    queueName = QueueName.fromStream("audi_-test_stream");
    verifyStreamName(queueName, "audi_-test_stream");
  }

  private void verifyStreamName(QueueName queueName, String streamName) {
    // verify all parts are correct
    Assert.assertFalse(queueName.isQueue());
    Assert.assertTrue(queueName.isStream());
    Assert.assertEquals(streamName, queueName.getFirstComponent());
    Assert.assertNull(queueName.getSecondComponent());
    Assert.assertNull(queueName.getThirdComponent());
    Assert.assertEquals(streamName, queueName.getSimpleName());

    // convert to URI and back, verify again
    queueName = QueueName.from(queueName.toURI());
    Assert.assertFalse(queueName.isQueue());
    Assert.assertTrue(queueName.isStream());
    Assert.assertEquals(streamName, queueName.getFirstComponent());
    Assert.assertNull(queueName.getSecondComponent());
    Assert.assertNull(queueName.getThirdComponent());
    Assert.assertEquals(streamName, queueName.getSimpleName());

    // convert to bytes and back, verify again
    queueName = QueueName.from(queueName.toBytes());
    Assert.assertFalse(queueName.isQueue());
    Assert.assertTrue(queueName.isStream());
    Assert.assertEquals(streamName, queueName.getFirstComponent());
    Assert.assertNull(queueName.getSecondComponent());
    Assert.assertNull(queueName.getThirdComponent());
    Assert.assertEquals(streamName, queueName.getSimpleName());
  }
}
