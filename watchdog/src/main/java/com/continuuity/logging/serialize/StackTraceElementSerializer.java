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

package com.continuuity.logging.serialize;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import static com.continuuity.logging.serialize.Util.stringOrNull;

/**
 * Serializer for StackTraceElement.
 */
public class StackTraceElementSerializer {
  private StackTraceElementSerializer() {}

  public static GenericRecord encode(Schema schema, StackTraceElement stackTraceElement) {
    if (stackTraceElement != null) {
      Schema steSchema = schema.getTypes().get(1);
      GenericRecord datum = new GenericData.Record(steSchema);
      datum.put("declaringClass", stackTraceElement.getClassName());
      datum.put("methodName", stackTraceElement.getMethodName());
      datum.put("fileName", stackTraceElement.getFileName());
      datum.put("lineNumber", stackTraceElement.getLineNumber());
      return datum;
    }
    return null;
  }

  public static StackTraceElement decode(GenericRecord datum) {
    if (datum != null) {
      String declaringClass =  stringOrNull(datum.get("declaringClass"));
      String methodName = stringOrNull(datum.get("methodName"));
      String fileName = stringOrNull(datum.get("fileName"));
      int lineNumber = (Integer) datum.get("lineNumber");
      return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
    }
    return null;
  }
}
