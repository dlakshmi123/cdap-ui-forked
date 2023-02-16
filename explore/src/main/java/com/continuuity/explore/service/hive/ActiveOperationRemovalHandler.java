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

package com.continuuity.explore.service.hive;

import com.continuuity.proto.QueryHandle;
import com.continuuity.proto.QueryStatus;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static com.continuuity.explore.service.hive.BaseHiveExploreService.OperationInfo;

/**
 * Takes care of closing operations after they are removed from the cache.
 */
public class ActiveOperationRemovalHandler implements RemovalListener<QueryHandle, OperationInfo> {
  private static final Logger LOG = LoggerFactory.getLogger(ActiveOperationRemovalHandler.class);

  private final BaseHiveExploreService exploreService;
  private final ExecutorService executorService;

  public ActiveOperationRemovalHandler(BaseHiveExploreService exploreService, ExecutorService executorService) {
    this.exploreService = exploreService;
    this.executorService = executorService;
  }

  @Override
  public void onRemoval(RemovalNotification<QueryHandle, OperationInfo> notification) {
    LOG.trace("Got removal notification for handle {} with cause {}", notification.getKey(), notification.getCause());
    executorService.submit(new ResourceCleanup(notification.getKey(), notification.getValue()));
  }

  private class ResourceCleanup implements Runnable {
    private final QueryHandle handle;
    private final OperationInfo opInfo;

    private ResourceCleanup(QueryHandle handle, OperationInfo opInfo) {
      this.handle = handle;
      this.opInfo = opInfo;
    }

    @Override
    public void run() {
      try {
        QueryStatus status = exploreService.fetchStatus(opInfo.getOperationHandle());

        // If operation is still not complete, cancel it.
        if (status.getStatus() != QueryStatus.OpStatus.FINISHED && status.getStatus() != QueryStatus.OpStatus.CLOSED &&
          status.getStatus() != QueryStatus.OpStatus.CANCELED && status.getStatus() != QueryStatus.OpStatus.ERROR) {
          LOG.info("Cancelling handle {} with status {} due to timeout",
                   handle.getHandle(), status.getStatus());
          exploreService.cancel(handle);
        }

      } catch (Throwable e) {
        LOG.error("Could not cancel handle {} due to exception", handle.getHandle(), e);
      } finally {
        LOG.debug("Timing out handle {}", handle);
        try {
          // Finally close the operation
          exploreService.closeInternal(handle, opInfo);
        } catch (Throwable e) {
          LOG.error("Exception while closing handle {}", handle, e);
        }
      }
    }
  }
}
