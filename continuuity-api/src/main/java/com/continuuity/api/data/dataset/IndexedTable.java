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

package com.continuuity.api.data.dataset;

import com.continuuity.api.annotation.Property;
import com.continuuity.api.common.Bytes;
import com.continuuity.api.data.DataSet;
import com.continuuity.api.data.DataSetContext;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.dataset.table.Delete;
import com.continuuity.api.data.dataset.table.EmptyRow;
import com.continuuity.api.data.dataset.table.Get;
import com.continuuity.api.data.dataset.table.Increment;
import com.continuuity.api.data.dataset.table.Put;
import com.continuuity.api.data.dataset.table.Row;
import com.continuuity.api.data.dataset.table.Table;
import com.google.common.base.Charsets;

import java.util.Arrays;

/**
 * This data set implements a table that can be accessed via secondary key.
 * The secondary key need not be unique, but a read by secondary key will
 * only return the value with the least primary key.
 *
 * This data set uses two tables - the actual data table,
 * and a second table for the index. All operations are performed
 * asynchronously, as part of the enclosing transaction (some operations
 * require multiple writes, and we want to make sure they are all committed
 * together).
 *
 * @deprecated As of Reactor 2.3.0, replaced by {@link com.continuuity.api.dataset.lib.IndexedTable}
 */
@Deprecated
public class IndexedTable extends DataSet {

  // the two underlying tables
  private Table table, index;
  // the secondary index column
  private byte[] column;

  // String representation of the column, mainly for storing into spec.
  @Property
  private String columnName;

  /**
   * Configuration time constructor.
   * @param name the name of the table
   * @param columnToIndex the name of the secondary index column
   */
  public IndexedTable(String name, byte[] columnToIndex) {
    this(name, columnToIndex, -1);
  }

  /**
   * Configuration time constructor.
   * @param name the name of the table
   * @param columnToIndex the name of the secondary index column
   * @param ttl time to live for the data in ms, negative means unlimited.
   */
  public IndexedTable(String name, byte[] columnToIndex, int ttl) {
    super(name);
    this.columnName = new String(columnToIndex, Charsets.UTF_8);
    this.table = new Table("d", ttl);
    this.index = new Table("i", ttl);
  }

  @Override
  public void initialize(DataSetSpecification spec, DataSetContext context) {
    super.initialize(spec, context);
    this.column = columnName.getBytes(Charsets.UTF_8);
  }

  // the value in the index. the index will have a row for every secondary
  // key that exists. That row has a column with the column key of the row
  // key of every row with that secondary key. The column must have a value,
  // we use the value 'x', but iy could be any value.
  static final byte[] EXISTS = { 'x' };

  /**
   * Read by primary key.
   * @param get the read operation, as if it were on a non-indexed table
   * @return the result of the read on the underlying primary table
   */
  public Row get(Get get) {
    return table.get(get);
  }

  /**
   * Read by secondary key.
   * @param get The read operation, as if it were on the non-indexed table,
   *             but with the secondary key as the row key of the Read.
   * @return an empty result if no row has that secondary key. If there is a
   * matching row, the result is the same as a read with the row key of the
   * first row that has this secondary key. The columns are the same as if
   * the read was performed on a non-indexed table.
   */
  public Row readBy(Get get) {
    // read the entire row of the index for the given key
    Row row = index.get(get.getRow());

    // if the index has no match, return nothing
    if (row.isEmpty()) {
      return row;
    }
    // iterate over all columns in the index result:
    // each of them represents a row key in the main table
    for (byte[] column : row.getColumns().keySet()) {
      if (Arrays.equals(EXISTS, row.get(column))) {
        // construct a new read with this column as the row key
        Get tableGet = new Get(column, get.getColumns());
        // issue that read against the main table
        Row tableResult =
            table.get(tableGet);
        // if this yields something, return it
        if (!tableResult.isEmpty()) {
          return tableResult;
        }
      }
    }
    // nothing found - return empty result
    return EmptyRow.of(get.getRow());
  }

  /**
   * A put to an indexed table. This is the same as on an indexed table,
   * except that additional work is done to maintain the index.
   * @param put The put operation
   */
  public void put(Put put) {
    // first read the existing row to find its current value of the index col
    byte[] oldSecondaryKey = table.get(put.getRow(), this.column);

    // find out whether the write contains a new value for the index column
    byte[] newSecondaryKey = put.getValues().get(this.column);

    boolean keyMatches = Arrays.equals(oldSecondaryKey, newSecondaryKey);
    // if there is an existing row with a value for the index column,
    // and that value is different from the new value to be written,
    // then we must remove that the row key from the index for that value;
    Delete idxDelete = null;
    if (oldSecondaryKey != null && !keyMatches) {
      idxDelete = new Delete(oldSecondaryKey, put.getRow());
    }

    // and we only need to write to index if the row is new or gets a new
    // value for the index column;
    Put idxPut = null;
    if (newSecondaryKey != null && !keyMatches) {
      idxPut = new Put(newSecondaryKey, put.getRow(), EXISTS);
    }

    // apply all operations to both tables
    table.put(put);
    if (idxDelete != null) {
      index.delete(idxDelete);
    }
    if (idxPut != null) {
      index.put(idxPut);
    }
  }

  /**
   * Perform a delete by primary key.
   * @param delete The delete operation, as if it were on a non-indexed table
   */
  public void delete(Delete delete) {
    // first read the existing row to find its current value of the index col
    byte[] oldSecondaryKey = table.get(delete.getRow(), this.column);

    // if there is an existing row with a value for the index column,
    // then we must remove that the row key from the index for that value;
    Delete idxDelete = null;
    if (oldSecondaryKey != null) {
      idxDelete = new Delete(oldSecondaryKey, delete.getRow());
    }

    // apply all operations to both tables
    table.delete(delete);
    if (idxDelete != null) {
      index.delete(idxDelete);
    }
  }

  /**
   * Perform a swap operation by primary key.
   * Parameters are as if they were on a non-indexed table.
   * Note that if the swap is on the secondary key column,
   * then the index must be updated; otherwise this is a
   * pass-through to the underlying table.
   */
  public boolean compareAndSwap(byte[] row, byte[] column, byte[] expected, byte[] newValue) {
    // if the swap is on a column other than the column key, then
    // the index is not affected - just execute the swap.
    // also, if the swap is on the index column, but the old value
    // is the same as the new value, then the index is not affected either.
    if (!Arrays.equals(this.column, column) ||
        Arrays.equals(expected, newValue)) {
      return table.compareAndSwap(row, column, expected, newValue);
    }

    // the swap is on the index column. it will only succeed if the current
    // value matches the expected value of the swap. if that value is not null,
    // then we must remove the row key from the index for that value.
    Delete idxDelete = null;
    if (expected != null) {
      idxDelete = new Delete(expected, row);
    }

    // if the new value is not null, then we must add the rowkey to the index
    // for that value.
    Put idxPut = null;
    if (newValue != null) {
      idxPut = new Put(newValue, row, EXISTS);
    }

    // apply all operations to both tables
    boolean success = table.compareAndSwap(row, column, expected, newValue);
    if (!success) {
      // do nothing: no changes
      return false;
    }
    if (idxDelete != null) {
      index.delete(idxDelete);
    }
    if (idxPut != null) {
      index.put(idxPut);
    }

    return true;
  }

  /**
   * Perform an increment operation by primary key.
   * @param increment The increment operation, as if it were on a non-indexed table.
   *             Note that if the increment is on the secondary key column,
   *             then the index must be updated; otherwise this is a
   *             pass-through to the underlying table.
   */
  public void increment(Increment increment) {
    // if the increment is on columns other than the index, just pass
    // it through to the table - the index is not affected
    Long indexIncrement = increment.getValues().get(this.column);
    if (indexIncrement == null) {
      // note this only adds the increment to the current xaction, it may be deferred
      table.increment(increment);
      return;
    }

    // index column is affected. Perform the increment synchronously
    Long newIndexValue = table.increment(increment).getLong(this.column);
    if (newIndexValue == null) {
      // should never happen (we checked that it was in the increment columns)
      // but if it does, we are done;
      return;
    }

    // delete the old secondary key from the index
    byte[] oldSecondaryKey = Bytes.toBytes(newIndexValue - indexIncrement);
    table.delete(oldSecondaryKey, increment.getRow());

    // add the new secondary key to the index
    byte[] newSecondaryKey = Bytes.toBytes(newIndexValue);
    table.put(newSecondaryKey, increment.getRow(), EXISTS);
  }

}
