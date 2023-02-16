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

package com.continuuity.logging.kafka;

import com.continuuity.common.logging.LoggingContext;
import com.continuuity.logging.write.LogWriteEvent;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.avro.generic.GenericRecord;

/**
 * Represents a log event fetched from Kafka.
 */
public final class KafkaLogEvent extends LogWriteEvent {
  private final int partition;
  private final long nextOffset;

  public KafkaLogEvent(GenericRecord genericRecord, ILoggingEvent logEvent, LoggingContext loggingContext,
                       int partition, long nextOffset) {
    super(genericRecord, logEvent, loggingContext);
    this.partition = partition;
    this.nextOffset = nextOffset;
  }

  public int getPartition() {
    return partition;
  }

  public long getNextOffset() {
    return nextOffset;
  }

}
