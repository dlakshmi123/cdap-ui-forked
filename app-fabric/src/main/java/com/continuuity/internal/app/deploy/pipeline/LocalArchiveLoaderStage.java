/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.internal.app.deploy.pipeline;

import com.continuuity.api.ApplicationSpecification;
import com.continuuity.app.Id;
import com.continuuity.app.deploy.ConfigResponse;
import com.continuuity.internal.app.ApplicationSpecificationAdapter;
import com.continuuity.internal.app.deploy.InMemoryConfigurator;
import com.continuuity.internal.io.ReflectionSchemaGenerator;
import com.continuuity.pipeline.AbstractStage;
import com.continuuity.weave.filesystem.Location;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.TimeUnit;

/**
 * LocalArchiveLoaderStage gets a {@link com.continuuity.weave.filesystem.Location} and emits a {@link ApplicationSpecification}.
 * <p>
 * This stage is responsible for reading the JAR and generating an ApplicationSpecification
 * that is forwarded to the next stage of processing.
 * </p>
 */
public class LocalArchiveLoaderStage extends AbstractStage<com.continuuity.weave.filesystem.Location> {
  private final ApplicationSpecificationAdapter adapter;
  private final Id.Account id;

  /**
   * Constructor with hit for handling type.
   */
  public LocalArchiveLoaderStage(Id.Account id) {
    super(TypeToken.of(com.continuuity.weave.filesystem.Location.class));
    this.id = id;
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
    Id.Application appId = Id.Application.from(id, specification.getName());
    emit(new ApplicationSpecLocation(appId, specification, archive));
  }
}