/*
 * Copyright © 2014-2015 Cask Data, Inc.
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

package co.cask.cdap.notifications.feeds.service;

import co.cask.cdap.notifications.feeds.NotificationFeed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Implementation of the {@link NotificationFeedStore} that keeps the feeds in memory.
 */
public class InMemoryNotificationFeedStore implements NotificationFeedStore {

  private final Map<String, NotificationFeed> feeds = Maps.newHashMap();

  @Nullable
  @Override
  public synchronized NotificationFeed createNotificationFeed(NotificationFeed feed) {
    NotificationFeed existingFeed = feeds.get(feed.getId());
    if (existingFeed != null) {
      return existingFeed;
    }
    feeds.put(feed.getId(), feed);
    return null;
  }

  @Nullable
  @Override
  public synchronized NotificationFeed getNotificationFeed(String feedId) {
    return feeds.get(feedId);
  }

  @Nullable
  @Override
  public synchronized NotificationFeed deleteNotificationFeed(String feedId) {
    return feeds.remove(feedId);
  }

  @Override
  public synchronized List<NotificationFeed> listNotificationFeeds() {
    return Lists.newArrayList(feeds.values());
  }
}
