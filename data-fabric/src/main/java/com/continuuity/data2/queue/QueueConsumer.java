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
package com.continuuity.data2.queue;

import com.continuuity.common.queue.QueueName;

import java.io.IOException;

/**
 * Interface for queue consumer.
 */
public interface QueueConsumer {

  /**
   * Returns the queue name that this consumer is working on.
   */
  QueueName getQueueName();

  /**
   * Returns the configuration of this consumer.
   * @return
   */
  ConsumerConfig getConfig();

  /**
   * Dequeue an entry from the queue.
   * @return A {@link DequeueResult}.
   */
  DequeueResult<byte[]> dequeue() throws IOException;

  /**
   * Dequeue multiple entries from the queue. The dequeue result may have less entries than the given
   * maxBatchSize, depending on how many entries in the queue.
   * @param maxBatchSize Maximum number of entries to queue.
   * @return A {@link DequeueResult}.
   */
  DequeueResult<byte[]> dequeue(int maxBatchSize) throws IOException;
}
