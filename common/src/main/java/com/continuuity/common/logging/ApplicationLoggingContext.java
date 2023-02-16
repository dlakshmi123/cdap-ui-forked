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

package com.continuuity.common.logging;

/**
 * Application logging context.
 */
public abstract class ApplicationLoggingContext extends AccountLoggingContext {
  public static final String TAG_APPLICATION_ID = ".applicationId";

  /**
   * Constructs ApplicationLoggingContext.
   * @param accountId account id
   * @param applicationId application id
   */
  public ApplicationLoggingContext(final String accountId, final String applicationId) {
    super(accountId);
    setSystemTag(TAG_APPLICATION_ID, applicationId);
  }

  @Override
  public String getLogPartition() {
    return super.getLogPartition() + String.format(":%s", getSystemTag(TAG_APPLICATION_ID));
  }

  @Override
  public String getLogPathFragment() {
    return String.format("%s/%s", super.getLogPathFragment(), getSystemTag(TAG_APPLICATION_ID));
  }
}
