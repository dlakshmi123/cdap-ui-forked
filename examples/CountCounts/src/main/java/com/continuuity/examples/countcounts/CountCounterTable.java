/*
 * Copyright (c) 2013, Continuuity Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are not permitted
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.continuuity.examples.countcounts;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.OperationResult;
import com.continuuity.api.data.dataset.table.Increment;
import com.continuuity.api.data.dataset.table.Read;
import com.continuuity.api.data.dataset.table.Table;
import com.continuuity.api.metrics.Metrics;


import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class CountCounterTable extends DataSet {

  private Table table;
  private Metrics metric;

  private static final byte[] KEY_ONLY_COLUMN = new byte[]{'c'};

  public CountCounterTable(String name) {
    super(name);
    this.table = new Table("cct_" + getName());
  }

  public CountCounterTable(DataSetSpecification spec) {
    super(spec);
    this.table = new Table(spec.getSpecificationFor("cct_" + getName()));
  }

  @Override
  public DataSetSpecification configure() {
    return new DataSetSpecification.Builder(this).dataset(this.table.configure()).create();
  }

  // Word count methods

  private static final byte[] WORD_COUNT_KEY = Bytes.toBytes("word_count");
  private static final byte[] WORD_COUNT_COUNTS_KEY = Bytes.toBytes("count_counts");

  public void incrementWordCount(long count) throws OperationException {
    // Increment the total word count
    increment(WORD_COUNT_KEY, count);
    // Increment the counts count
    increment(WORD_COUNT_COUNTS_KEY, Bytes.toBytes(count), 1L);
    metric.count("increment.word.count", 1);
  }

  public long getTotalWordCount() throws OperationException {
    metric.count("get.word.count", 1);
    return get(WORD_COUNT_KEY);
  }

  public Map<Long, Long> getWordCountCounts() throws OperationException {
    metric.count("get.word.counts", 1);
    OperationResult<Map<byte[], byte[]>> result = this.table.read(new Read(WORD_COUNT_COUNTS_KEY, null, null));
    Map<Long, Long> counts = new TreeMap<Long, Long>();

    if (result.isEmpty()) {
      return counts;
    }

    for (Map.Entry<byte[], byte[]> entry : result.getValue().entrySet()) {
      counts.put(Bytes.toLong(entry.getKey()), Bytes.toLong(entry.getValue()));
    }
    return counts;
  }

  // Line count methods
  private static final byte[] LINE_COUNT_KEY = Bytes.toBytes("line_count");

  public void incrementLineCount() throws OperationException {
    metric.count("increment.count", 1);
    increment(LINE_COUNT_KEY, 1L);
  }

  public long getLineCount() throws OperationException {
    return get(LINE_COUNT_KEY);
  }

  // Line length methods

  private static final byte[] LINE_LENGTH_KEY = Bytes.toBytes("line_length");

  public void incrementLineLength(long length) throws OperationException {
    metric.count("increment.line.length", 1);
    increment(LINE_LENGTH_KEY, length);
  }

  public long getLineLength() throws OperationException {
    return get(LINE_LENGTH_KEY);
  }

  // Private helpers

  private void increment(byte[] key, long count) throws OperationException {
    increment(key, KEY_ONLY_COLUMN, count);
  }

  private void increment(byte[] key, byte[] column, long count) throws OperationException {
    this.table.write(new Increment(key, column, count));
  }

  private long get(byte[] key) throws OperationException {
    OperationResult<Map<byte[], byte[]>> result = this.table.read(new Read(key, KEY_ONLY_COLUMN));

    if (result.isEmpty()) {
      return 0L;
    }

    byte[] value = result.getValue().get(KEY_ONLY_COLUMN);

    if (value == null) {
      return 0L;
    }

    if (value.length != Bytes.SIZEOF_LONG) {
      return -1L;
    }

    return Bytes.toLong(value);
  }
}
