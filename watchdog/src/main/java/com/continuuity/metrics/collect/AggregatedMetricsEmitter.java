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
package com.continuuity.metrics.collect;

import com.continuuity.metrics.transport.MetricsRecord;
import com.continuuity.metrics.transport.TagMetric;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link com.continuuity.common.metrics.MetricsCollector} and {@link MetricsEmitter} that aggregates metric values
 * during collection and emit the aggregated values when emit.
 */
final class AggregatedMetricsEmitter implements MetricsEmitter {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AggregatedMetricsEmitter.class);
  private static final long CACHE_EXPIRE_MINUTES = 1;

  private final String context;
  private final String runId;
  private final String name;
  private final AtomicInteger value;
  private final LoadingCache<String, AtomicInteger> tagValues;

  AggregatedMetricsEmitter(String context, String runId, String name) {
    this.context = context;
    this.runId = runId;
    this.name = name;
    this.value = new AtomicInteger();
    this.tagValues = CacheBuilder.newBuilder()
                                 .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                                 .build(new CacheLoader<String, AtomicInteger>() {
                                   @Override
                                   public AtomicInteger load(String key) throws Exception {
                                     return new AtomicInteger();
                                   }
                                 });
    if (name == null || name.isEmpty()) {
      LOG.warn("Creating emmitter with " + (name == null ? "null" : "empty") + " name, " +
        "for context " + context + " and runId " + runId);
    }
  }

  void gauge(int value, String... tags) {
    this.value.addAndGet(value);
    for (String tag : tags) {
      tagValues.getUnchecked(tag).addAndGet(value);
    }
  }

  @Override
  public MetricsRecord emit(long timestamp) {
    ImmutableList.Builder<TagMetric> builder = ImmutableList.builder();
    int value = this.value.getAndSet(0);
    for (Map.Entry<String, AtomicInteger> entry : tagValues.asMap().entrySet()) {
      builder.add(new TagMetric(entry.getKey(), entry.getValue().getAndSet(0)));
    }
    return new MetricsRecord(context, runId, name, builder.build(), timestamp, value);
  }
}
