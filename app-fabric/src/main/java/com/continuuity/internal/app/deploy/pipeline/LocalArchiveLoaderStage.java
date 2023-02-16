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

package com.continuuity.internal.app.deploy.pipeline;

import com.continuuity.app.ApplicationSpecification;
import com.continuuity.app.deploy.ConfigResponse;
import com.continuuity.internal.app.ApplicationSpecificationAdapter;
import com.continuuity.internal.app.ForwardingApplicationSpecification;
import com.continuuity.internal.app.deploy.InMemoryConfigurator;
import com.continuuity.internal.io.ReflectionSchemaGenerator;
import com.continuuity.pipeline.AbstractStage;
import com.continuuity.proto.Id;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.twill.filesystem.Location;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * LocalArchiveLoaderStage gets a {@link Location} and emits a {@link ApplicationSpecification}.
 * <p>
 * This stage is responsible for reading the JAR and generating an ApplicationSpecification
 * that is forwarded to the next stage of processing.
 * </p>
 */
public class LocalArchiveLoaderStage extends AbstractStage<Location> {
  private final ApplicationSpecificationAdapter adapter;
  private final Id.Account id;
  private final String appId;

  /**
   * Constructor with hit for handling type.
   */
  public LocalArchiveLoaderStage(Id.Account id, @Nullable String appId) {
    super(TypeToken.of(Location.class));
    this.id = id;
    this.appId = appId;
    this.adapter = ApplicationSpecificationAdapter.create(new ReflectionSchemaGenerator());
  }

  /**
   * Creates a {@link com.continuuity.internal.app.deploy.InMemoryConfigurator} to run through
   * the process of generation of {@link ApplicationSpecification}
   *
   * @param archive Location of archive.
   */
  @Override
  public void process(Location archive) throws Exception {
    InMemoryConfigurator inMemoryConfigurator = new InMemoryConfigurator(id, archive);
    ListenableFuture<ConfigResponse> result = inMemoryConfigurator.config();
    //TODO: Check with Terence on how to handle this stuff.
    ConfigResponse response = result.get(120, TimeUnit.SECONDS);
    ApplicationSpecification specification = adapter.fromJson(response.get());
    if (appId != null) {
      specification = new ForwardingApplicationSpecification(specification) {
        @Override
        public String getName() {
          return appId;
        }
      };
    }
    Id.Application applicationId = Id.Application.from(id, specification.getName());
    emit(new ApplicationSpecLocation(applicationId, specification, archive));
  }
}
