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
package com.continuuity.data2.queue;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 *
 */
public final class ConsumerConfig {

  private final long groupId;
  private final int instanceId;
  private final int groupSize;
  private final DequeueStrategy dequeueStrategy;
  private final String hashKey;

  public ConsumerConfig(long groupId, int instanceId, int groupSize, DequeueStrategy dequeueStrategy, String hashKey) {
    Preconditions.checkArgument(instanceId >= 0, "Instance ID must be >= 0.");
    Preconditions.checkArgument(instanceId < groupSize, "Instance ID must be < groupSize");
    this.groupId = groupId;
    this.instanceId = instanceId;
    this.groupSize = groupSize;
    this.dequeueStrategy = dequeueStrategy;
    this.hashKey = dequeueStrategy == DequeueStrategy.HASH ? hashKey : null;
  }

  public long getGroupId() {
    return groupId;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public int getGroupSize() {
    return groupSize;
  }

  public DequeueStrategy getDequeueStrategy() {
    return dequeueStrategy;
  }

  public String getHashKey() {
    return hashKey;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
                  .add("groupId", groupId)
                  .add("instanceId", instanceId)
                  .add("groupSize", groupSize)
                  .add("dequeueStrategy", dequeueStrategy)
                  .add("hashKey", hashKey)
                  .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConsumerConfig other = (ConsumerConfig) o;

    return groupId == other.groupId
      && groupSize == other.groupSize
      && instanceId == other.instanceId
      && dequeueStrategy == other.dequeueStrategy
      && Objects.equal(hashKey, other.hashKey);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(groupId, groupSize, instanceId, dequeueStrategy, hashKey);
  }
}
