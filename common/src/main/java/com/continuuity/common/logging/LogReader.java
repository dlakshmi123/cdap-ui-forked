package com.continuuity.common.logging;

import java.io.IOException;
import java.util.List;

/**
 * Represents classes that can read log.
 */
public interface LogReader {

  /**
   * Configures this reader.
   * @param config specifies what log to read
   */
  public void configure(LogConfiguration config) throws IOException;

  /**
   * retrieves the tail of the log, up to size bytes, line by line.
   * @param sizeToRead limits the number of bytes to read
   * @param writePos position of the current writer. This is a hint to the
   *                 reader as to how far it can seek into the latest log.
   *                 This is necessary because we are reading into the latest
   *                 file while it is still open for write, and hence the
   *                 file system status will not give us a precise file length.
   */
  public List<String> tail(int sizeToRead, long writePos) throws IOException;
}