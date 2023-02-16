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

package com.continuuity.metrics.data;

import com.continuuity.api.common.Bytes;
import com.continuuity.common.utils.ImmutablePair;
import com.continuuity.data.table.Scanner;
import com.continuuity.metrics.MetricsConstants;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public final class MetricsScanner implements Iterator<MetricsScanResult> {

  private final MetricsScanQuery query;
  private final Scanner scanner;
  private final MetricsEntityCodec entityCodec;
  private final int resolution;

  // Track the number of row scanned through the iterator. It's for reporting and debugging purpose.
  private int rowScanned;

  // Use an internal iterator to avoid leaking AbstractIterator methods to outside.
  private final Iterator<MetricsScanResult> internalIterator;

  /**
   * Construct a MetricScanner. Should only be called by MetricTable.
   * @param query The query used to create this scanner.
   * @param scanner The table scanner of a query.
   */
  MetricsScanner(MetricsScanQuery query, Scanner scanner, MetricsEntityCodec entityCodec, int resolution) {
    this.query = query;
    this.scanner = scanner;
    this.entityCodec = entityCodec;
    this.resolution = resolution;
    this.internalIterator = createIterator();
  }

  public void close() {
    scanner.close();
  }

  public int getRowScanned() {
    return rowScanned;
  }

  @Override
  public boolean hasNext() {
    return internalIterator.hasNext();
  }

  @Override
  public MetricsScanResult next() {
    return internalIterator.next();
  }

  @Override
  public void remove() {
    internalIterator.remove();
  }

  private Iterator<MetricsScanResult> createIterator() {
    return new AbstractIterator<MetricsScanResult>() {
      @Override
      protected MetricsScanResult computeNext() {
        ImmutablePair<byte[], Map<byte[], byte[]>> rowResult;
        while ((rowResult = scanner.next()) != null) {
          rowScanned++;
          byte[] rowKey = rowResult.getFirst();
          Map<byte[], byte[]> columnValue = rowResult.getSecond();

          // Decode context and metric from key
          int offset = 0;
          String context = entityCodec.decode(MetricsEntityType.CONTEXT, rowKey, offset);
          if (query.getContextPrefix() != null && !context.startsWith(query.getContextPrefix())) {
            continue;
          }

          offset += entityCodec.getEncodedSize(MetricsEntityType.CONTEXT);
          String metric = entityCodec.decode(MetricsEntityType.METRIC, rowKey, offset);
          if (!metric.startsWith(query.getMetricPrefix())) {
            continue;
          }

          offset += entityCodec.getEncodedSize(MetricsEntityType.METRIC);

          String tag = entityCodec.decode(MetricsEntityType.TAG, rowKey, offset);
          // If there is no tag in the key, query shouldn't have tag
          if (tag.equals(MetricsConstants.EMPTY_TAG) && query.getTagPrefix() != null) {
            continue;
          }
          // If there is tag in the query, it must match with the row key.
          if (query.getTagPrefix() != null && !tag.startsWith(query.getTagPrefix())) {
            continue;
          }

          // Next 4 bytes is timebase.
          offset += entityCodec.getEncodedSize(MetricsEntityType.TAG);
          int timeBase = Bytes.toInt(rowKey, offset, 4);

          // Then it's the runId
          offset += 4;
          String runId = entityCodec.decode(MetricsEntityType.RUN, rowKey, offset);

          // If there is runId in the query, it must match with the row key.
          if (query.getRunId() != null && !query.getRunId().equals(runId)) {
            continue;
          }

          return new MetricsScanResult(context, runId, metric, tag.equals(MetricsConstants.EMPTY_TAG) ? null : tag,
                                      new TimeValueIterable(timeBase, resolution,
                                                            query.getStartTime(), query.getEndTime(),
                                                            columnValue.entrySet()));
        }

        scanner.close();
        return endOfData();
      }
    };
  }
}
