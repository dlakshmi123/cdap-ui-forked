package com.continuuity.data2.datafabric.dataset;

import com.continuuity.api.common.Bytes;
import com.continuuity.internal.data.dataset.lib.table.OrderedTable;
import com.continuuity.internal.data.dataset.lib.table.Row;
import com.continuuity.internal.data.dataset.lib.table.Scanner;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Provides handy methods to manage objects in {@link OrderedTable}
 */
// todo: review usage of OrderedTable after adding handy methods to it (operating on objects Get, Put, etc.)
public abstract class AbstractObjectsStore implements Closeable {
  private static final Gson GSON = new Gson();

  /**
   * All rows we store use single column of this name.
   */
  private static final byte[] COLUMN = Bytes.toBytes("c");

  private final OrderedTable table;

  public AbstractObjectsStore(OrderedTable table) {
    this.table = table;
  }

  @Override
  public void close() throws IOException {
    table.close();
  }

  protected final <T> T get(byte[] key, Class<T> classOfT) {
    try {
      byte[] value = table.get(key).get(COLUMN);
      if (value == null) {
        return null;
      }

      return GSON.fromJson(new String(value, Charsets.UTF_8), classOfT);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  // note will not have prefix in keys in the map in the output
  protected final <T> Map<String, T> scan(byte[] prefix, Class<T> classOfT) {
    byte[] stopKey = createStopKey(prefix);

    try {
      Map<String, T> map = Maps.newHashMap();
      Scanner scan = table.scan(prefix, stopKey);
      Row next;
      while ((next = scan.next()) != null) {
        byte[] columnValue = next.get(COLUMN);
        T value = GSON.fromJson(new String(columnValue, Charsets.UTF_8), classOfT);
        String key = new String(next.getRow(), prefix.length, next.getRow().length - prefix.length, Charsets.UTF_8);
        map.put(key, value);
      }
      return map;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  // NOTE: null means "read to the end"
  @Nullable
  static byte[] createStopKey(byte[] prefix) {
    for (int i = prefix.length - 1; i >= 0; i--) {
      int unsigned = prefix[i] & 0xff;
      if (unsigned < 0xff) {
        byte[] stopKey = Arrays.copyOf(prefix, i + 1);
        stopKey[stopKey.length - 1]++;
        return stopKey;
      }
    }

    // i.e. "read to the end"
    return null;
  }

  protected final <T> void put(byte[] key, T value) {
    try {
      table.put(key, COLUMN, GSON.toJson(value).getBytes(Charsets.UTF_8));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected final void delete(byte[] key) {
    try {
      table.delete(key, COLUMN);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
