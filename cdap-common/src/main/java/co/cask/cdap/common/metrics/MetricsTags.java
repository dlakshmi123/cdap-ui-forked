/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.common.metrics;

import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.proto.Id;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides ways to obtain the tags for certain metrics.
 */
public final class MetricsTags {

  private MetricsTags() {
  }

  public static Map<String, String> contextMap(String... tags) {
    Preconditions.checkArgument(tags.length % 2 == 0);
    Map<String, String> tagMap = new HashMap<>();
    for (int i = 0; i < tags.length; i += 2) {
      tagMap.put(tags[i], tags[i + 1]);
    }
    return tagMap;
  }
  // TODO: Use Id.Flow.Flowlet
  public static Map<String, String> flowlet(Id.Program flowId, String flowletId) {
    return ImmutableMap.of(
      Constants.Metrics.Tag.NAMESPACE, flowId.getNamespaceId(),
      Constants.Metrics.Tag.APP, flowId.getApplicationId(),
      Constants.Metrics.Tag.FLOW, flowId.getId(),
      Constants.Metrics.Tag.FLOWLET, flowletId);
  }

  public static Map<String, String> service(Id.Program id) {
    return ImmutableMap.of(
      Constants.Metrics.Tag.NAMESPACE, id.getNamespaceId(),
      Constants.Metrics.Tag.APP, id.getApplicationId(),
      Constants.Metrics.Tag.SERVICE, id.getId());
  }

  public static Map<String, String> serviceHandler(Id.Program id, String handlerId) {
    return ImmutableMap.of(
      Constants.Metrics.Tag.NAMESPACE, id.getNamespaceId(),
      Constants.Metrics.Tag.APP, id.getApplicationId(),
      Constants.Metrics.Tag.SERVICE, id.getId(),
      Constants.Metrics.Tag.HANDLER, handlerId);
  }
}
