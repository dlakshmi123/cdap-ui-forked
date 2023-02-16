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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class HBaseTableUtilTest {
  @Test
  public void testGetSplitKeys() {
    Assert.assertEquals(15, HBaseTableUtil.getSplitKeys(12).length);
    // it should return one key less than required splits count, because HBase will take care of the first automatically
    Assert.assertEquals(15, HBaseTableUtil.getSplitKeys(16).length);
    // at least #buckets - 1, but no less than user asked
    Assert.assertEquals(7, HBaseTableUtil.getSplitKeys(6).length);
    Assert.assertEquals(7, HBaseTableUtil.getSplitKeys(2).length);
    // "1" can be used for queue tables that we know are not "hot", so we do not pre-split in this case
    Assert.assertEquals(0, HBaseTableUtil.getSplitKeys(1).length);
    // allows up to 255 * 8 - 1 splits
    Assert.assertEquals(255 * 8 - 1, HBaseTableUtil.getSplitKeys(255 * 8).length);
    try {
      HBaseTableUtil.getSplitKeys(256 * 8);
      Assert.fail("getSplitKeys(256) should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      HBaseTableUtil.getSplitKeys(0);
      Assert.fail("getSplitKeys(0) should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
