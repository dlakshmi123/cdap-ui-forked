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

package com.continuuity.examples.wordcount;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.OperationException;
import com.continuuity.api.data.OperationResult;
import com.continuuity.api.data.dataset.table.Increment;
import com.continuuity.api.data.dataset.table.Read;
import com.continuuity.api.data.dataset.table.Table;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 */
public class AssociationTable extends DataSet {

  private Table table;

  public AssociationTable(String name) {
    super(name);
    this.table = new Table("word_assoc_" + name);
  }

  public AssociationTable(DataSetSpecification spec) {
    super(spec);
    this.table = new Table(
      spec.getSpecificationFor("word_assoc_" + this.getName()));
  }

  @Override
  public DataSetSpecification configure() {
    return new DataSetSpecification.Builder(this)
      .dataset(this.table.configure())
      .create();
  }

  /**
   * Stores associations between the specified set of words. That is, for every
   * word in the set, an association will be stored for each of the other words
   * in the set.
   * @param words words to store associations between
   * @throws OperationException
   */
  public void writeWordAssocs(Set<String> words) throws OperationException {

    // For sets of less than 2 words, there are no associations
    int n = words.size();

    if (n < 2) {
      return;
    }

    // Every word will get (n-1) increments (one for each of the other words)
    long[] values = new long[n - 1];
    Arrays.fill(values, 1);

    // Convert all words to bytes
    byte[][] wordBytes = new byte[n][];
    int i = 0;
    for (String word : words) {
      wordBytes[i++] = Bytes.toBytes(word);
    }

    // Generate an increment for each word
    for (int j = 0; j < n; j++) {
      byte[] row =  wordBytes[j];
      byte[][] columns = new byte[n - 1][];
      System.arraycopy(wordBytes, 0, columns, 0, j);
      System.arraycopy(wordBytes, j + 1, columns, j, n - j - 1);
      this.table.write(new Increment(row, columns, values));
    }
  }

  /**
   * Returns the top words associated with the specified word and the number
   * of times the words have appeared together.
   * @param word the word of interest
   * @param limit the number of associations to return, at most
   * @return a map of the top associated words to their co-occurrence count
   * @throws OperationException
   */
  public Map<String, Long> readWordAssocs(String word, int limit)
    throws OperationException {

    // Retrieve all columns of the word’s row
    OperationResult<Map<byte[], byte[]>> result =
      this.table.read(new Read(Bytes.toBytes(word), null, null));
    TopKCollector collector = new TopKCollector(limit);
    if (!result.isEmpty()) {
      // Iterate over all columns
      for (Map.Entry<byte[], byte[]> entry : result.getValue().entrySet()) {
        collector.add(Bytes.toLong(entry.getValue()),
                      Bytes.toString(entry.getKey()));
      }
    }
    return collector.getTopK();
  }

  /**
   * Returns how many times two words occured together.
   * @param word1 the first word
   * @param word2 the other word
   * @return how many times word1 and word2 occurred together
   * @throws OperationException 
   */
  public long getAssoc(String word1, String word2) throws OperationException {
    OperationResult<Map<byte[], byte[]>> result =
      this.table.read(new Read(Bytes.toBytes(word1), Bytes.toBytes(word2)));
    if (result.isEmpty()) {
      return 0;
    }
    byte[] count = result.getValue().get(Bytes.toBytes(word2));
    if (count == null || count.length != Bytes.SIZEOF_LONG) {
      return 0;
    }
    return Bytes.toLong(count);
  }
}

class TopKCollector {

  class Entry implements Comparable<Entry> {
    final long count;
    final String word;

    Entry(long count, String word) {
      this.count = count;
      this.word = word;
    }

    @Override
    public int compareTo(Entry other) {
      if (count == other.count) {
        return word.compareTo(other.word);
      }
      return Long.signum(count - other.count);
    }
  }

  final int limit;
  TreeSet<Entry> entries = new TreeSet<Entry>();

  TopKCollector(int limit) {
    this.limit = limit;
  }

  void add(long count, String word) {
    if (entries.size() < limit) {
      entries.add(new Entry(count, word));
    } else {
      if (entries.first().count < count) {
        entries.pollFirst();
        entries.add(new Entry(count, word));
      }
    }
  }

  Map<String, Long> getTopK() {
    TreeMap<String, Long> topK = new TreeMap<String, Long>();
    for (int i = 0; i < limit; i++) {
      Entry entry = entries.pollLast();
      if (entry == null) {
        break;
      } else {
        topK.put(entry.word, entry.count);
      }
    }
    return topK;
  }
}
