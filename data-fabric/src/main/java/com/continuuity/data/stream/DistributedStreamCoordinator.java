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
package com.continuuity.data.stream;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.conf.PropertyStore;
import com.continuuity.common.io.Codec;
import com.continuuity.common.zookeeper.store.ZKPropertyStore;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.twill.zookeeper.ZKClient;

/**
 * A {@link StreamCoordinator} uses ZooKeeper to implementation coordination needed for stream.
 */
@Singleton
public final class DistributedStreamCoordinator extends AbstractStreamCoordinator {

  private ZKClient zkClient;

  @Inject
  protected DistributedStreamCoordinator(StreamAdmin streamAdmin) {
    super(streamAdmin);
  }

  @Inject(optional = true)
  void setZkClient(ZKClient zkClient) {
    // Use optional injection for zk client to make testing easier in case this class is not used.
    this.zkClient = zkClient;
  }

  @Override
  protected <T> PropertyStore<T> createPropertyStore(Codec<T> codec) {
    Preconditions.checkState(zkClient != null, "Missing ZKClient. Check Guice binding.");
    return ZKPropertyStore.create(zkClient, "/" + Constants.Service.STREAMS + "/properties", codec);
  }
}
