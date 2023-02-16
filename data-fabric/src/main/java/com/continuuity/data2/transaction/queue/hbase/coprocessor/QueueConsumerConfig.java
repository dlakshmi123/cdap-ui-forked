/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data2.transaction.queue.hbase.coprocessor;

import com.continuuity.api.common.Bytes;

import java.util.Map;

/**
 * Holder class for queue consumer configuration information.
 */
final class QueueConsumerConfig {
  // Map from consumer instance to
  private final Map<ConsumerInstance, byte[]> startRows;
  private final int numGroups;
  private final byte[] smallest;

  QueueConsumerConfig(Map<ConsumerInstance, byte[]> startRows, int numGroups) {
    this.startRows = startRows;
    this.numGroups = numGroups;

    byte[] smallest = null;
    for (byte[] row : startRows.values()) {
      if (smallest == null || Bytes.compareTo(row, smallest) < 0) {
        smallest = row;
      }
    }
    this.smallest = smallest;
  }

  byte[] getStartRow(ConsumerInstance consumerInstance) {
    return startRows.get(consumerInstance);
  }

  int getNumGroups() {
    return numGroups;
  }

  /**
   * Returns the smallest start row among all consumers.
   */
  byte[] getSmallestStartRow() {
    return smallest;
  }
}
