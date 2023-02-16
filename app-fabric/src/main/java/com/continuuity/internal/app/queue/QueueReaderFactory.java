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

package com.continuuity.internal.app.queue;

import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.app.queue.QueueReader;
import com.continuuity.data2.queue.QueueConsumer;
import com.continuuity.data2.transaction.stream.StreamConsumer;
import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.nio.ByteBuffer;

/**
 *
 */
public final class QueueReaderFactory {

  public <T> QueueReader<T> createQueueReader(Supplier<QueueConsumer> consumerSupplier,
                                              int batchSize, Function<ByteBuffer, T> decoder) {
    return new SingleQueue2Reader<T>(consumerSupplier, batchSize, decoder);
  }

  public <T> QueueReader<T> createStreamReader(Supplier<StreamConsumer> consumerSupplier,
                                               int batchSize, Function<StreamEvent, T> transformer) {
    return new StreamQueueReader<T>(consumerSupplier, batchSize, transformer);
  }
}
