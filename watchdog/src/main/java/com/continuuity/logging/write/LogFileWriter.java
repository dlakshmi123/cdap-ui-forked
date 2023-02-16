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

package com.continuuity.logging.write;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;

/**
 * Interface to write log files.
 *
 * @param <T> type of log event.
 */
public interface LogFileWriter<T extends LogWriteEvent> extends Closeable, Flushable {

  /**
   * Appends a log event to an appropriate Avro file based on LoggingContext. If the log event does not contain
   * LoggingContext then the event will be dropped.
   * @param events Log event
   * @throws java.io.IOException
   */
  void append(List<T> events) throws Exception;
}
