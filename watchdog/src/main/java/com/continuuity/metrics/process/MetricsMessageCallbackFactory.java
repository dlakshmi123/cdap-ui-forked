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
package com.continuuity.metrics.process;

import com.continuuity.common.metrics.MetricsScope;
import com.continuuity.internal.io.DatumReader;
import com.continuuity.internal.io.DatumReaderFactory;
import com.continuuity.internal.io.Schema;
import com.continuuity.internal.io.SchemaGenerator;
import com.continuuity.internal.io.UnsupportedTypeException;
import com.continuuity.metrics.MetricsConstants;
import com.continuuity.metrics.transport.MetricsRecord;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.kafka.client.KafkaConsumer;

import java.util.Set;

/**
 * A {@link MessageCallbackFactory} that creates MessageCallback for processing MetricsRecord
 * with offset persists to {@link KafkaConsumerMetaTable}.
 */
public final class MetricsMessageCallbackFactory implements MessageCallbackFactory {

  private final DatumReader<MetricsRecord> datumReader;
  private final Schema recordSchema;
  private final Set<MetricsProcessor> processors;
  private final int persistThreshold;

  @Inject
  public MetricsMessageCallbackFactory(SchemaGenerator schemaGenerator, DatumReaderFactory readerFactory,
                                       Set<MetricsProcessor> processors,
                                       @Named(MetricsConstants.ConfigKeys.KAFKA_CONSUMER_PERSIST_THRESHOLD)
                                       int persistThreshold) {
    try {
      this.recordSchema = schemaGenerator.generate(MetricsRecord.class);
      this.datumReader = readerFactory.create(TypeToken.of(MetricsRecord.class), recordSchema);
      this.processors = processors;
      this.persistThreshold = persistThreshold;

    } catch (UnsupportedTypeException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public KafkaConsumer.MessageCallback create(KafkaConsumerMetaTable metaTable, MetricsScope scope) {
    return new PersistedMessageCallback(
      new MetricsMessageCallback(scope, processors, datumReader, recordSchema), metaTable, persistThreshold);
  }
}
