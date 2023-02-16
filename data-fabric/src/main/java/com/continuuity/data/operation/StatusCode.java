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

package com.continuuity.data.operation;

/**
 * Defines internal status codes for use in operation results or execptions.
 */
public class StatusCode {

  public static final int OK = 0;

  public static final int WRITE_CONFLICT = 500;

  public static final int ENTRY_NOT_FOUND = 501;

  public static final int ENTRY_EXISTS = 502;

  public static final int TRANSACTION_CONFLICT = 503;

  public static final int ENTRY_DOES_NOT_MATCH = 504;

  public static final int KEY_NOT_FOUND = 404;

  public static final int COLUMN_NOT_FOUND = 405;

  public static final int ILLEGAL_INCREMENT = 2000;
  public static final int INCOMPATIBLE_TYPE = 2001;

  public static final int INTERNAL_ERROR = 5000;

  public static final int QUEUE_NOT_FOUND = 1000;
//  public static final int QUEUE_EMPTY = 1001;
  public static final int ILLEGAL_ACK = 1002;
//  public static final int TOO_MANY_RETRIES = 1003;
  public static final int ILLEGAL_GROUP_CONFIG_CHANGE = 1004;
  public static final int ILLEGAL_FINALIZE = 1005;
  public static final int ILLEGAL_UNACK = 1006;
  public static final int NOT_CONFIGURED = 1007;
  public static final int INVALID_STATE = 1008;

  public static final int INVALID_TRANSACTION = 1009;

  public static final int SQL_ERROR = 5001;
  public static final int HBASE_ERROR = 5002;
  public static final int THRIFT_ERROR = 5003;
}
