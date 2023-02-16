package com.continuuity.internal.data.dataset.lib.table;

import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.batch.Split;
import com.google.common.base.Objects;

/**
 * Table splits are simply a start and stop key.
 */
public class TableSplit extends Split {
  private final byte[] start, stop;

  public TableSplit(byte[] start, byte[] stop) {
    this.start = start;
    this.stop = stop;
  }

  byte[] getStart() {
    return start;
  }

  byte[] getStop() {
    return stop;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("start", Bytes.toString(start))
      .add("stop", Bytes.toString(stop))
      .toString();
  }
}
