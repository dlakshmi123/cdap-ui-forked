/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data2.transaction.queue;

/**
 * Constants for queue implementation in HBase.
 */
public final class QueueConstants {

  /**
   * Configuration keys for queues in HBase.
   */
  public static final class ConfigKeys {
    public static final String QUEUE_TABLE_COPROCESSOR_DIR = "data.queue.table.coprocessor.dir";
    public static final String QUEUE_TABLE_PRESPLITS = "data.queue.table.presplits";
  }

  public static final String DEFAULT_QUEUE_TABLE_COPROCESSOR_DIR = "/queue";
  public static final int DEFAULT_QUEUE_TABLE_PRESPLITS = 16;

  public static final byte[] COLUMN_FAMILY = new byte[] {'q'};
  public static final byte[] DATA_COLUMN = new byte[] {'d'};
  public static final byte[] META_COLUMN = new byte[] {'m'};
  public static final byte[] STATE_COLUMN_PREFIX = new byte[] {'s'};

  public static final long MAX_CREATE_TABLE_WAIT = 5000L;    // Maximum wait of 5 seconds for table creation.

  private QueueConstants() {
  }
}
