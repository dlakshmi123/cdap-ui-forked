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

package com.continuuity.internal.app.runtime.service;

import com.continuuity.api.common.RuntimeArguments;
import com.continuuity.api.service.ServiceSpecification;
import com.continuuity.app.ApplicationSpecification;
import com.continuuity.app.metrics.ServiceRunnableMetrics;
import com.continuuity.app.program.Program;
import com.continuuity.app.runtime.ProgramController;
import com.continuuity.app.runtime.ProgramOptions;
import com.continuuity.app.runtime.ProgramRunner;
import com.continuuity.common.election.InMemoryElectionRegistry;
import com.continuuity.common.lang.InstantiatorFactory;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.internal.app.runtime.MetricsFieldSetter;
import com.continuuity.internal.app.runtime.ProgramOptionConstants;
import com.continuuity.internal.app.runtime.ProgramServiceDiscovery;
import com.continuuity.internal.lang.Reflections;
import com.continuuity.logging.context.UserServiceLoggingContext;
import com.continuuity.proto.ProgramType;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.apache.twill.api.RunId;
import org.apache.twill.api.RuntimeSpecification;
import org.apache.twill.api.TwillRunnable;
import org.apache.twill.common.Cancellable;
import org.apache.twill.discovery.Discoverable;
import org.apache.twill.discovery.DiscoveryService;
import org.apache.twill.discovery.DiscoveryServiceClient;
import org.apache.twill.discovery.ServiceDiscovered;
import org.apache.twill.internal.RunIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Runs runnable-service in single-node
 */
public class InMemoryRunnableRunner implements ProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryRunnableRunner.class);

  private final MetricsCollectionService metricsCollectionService;
  private final ProgramServiceDiscovery serviceDiscovery;
  private final DiscoveryService dsService;
  private final InMemoryElectionRegistry electionRegistry;

  @Inject
  public InMemoryRunnableRunner(MetricsCollectionService metricsCollectionService,
                                ProgramServiceDiscovery serviceDiscovery,
                                DiscoveryService dsService, InMemoryElectionRegistry electionRegistry) {
    this.metricsCollectionService = metricsCollectionService;
    this.serviceDiscovery = serviceDiscovery;
    this.dsService = dsService;
    this.electionRegistry = electionRegistry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ProgramController run(final Program program, ProgramOptions options) {
    InMemoryTwillContext twillContext = null;
    try {
      // Extract and verify parameters
      String runnableName = options.getName();
      Preconditions.checkNotNull(runnableName, "Missing runnable name.");

      int instanceId = Integer.parseInt(options.getArguments().getOption(ProgramOptionConstants.INSTANCE_ID, "-1"));
      Preconditions.checkArgument(instanceId >= 0, "Missing instance Id");

      int instanceCount = Integer.parseInt(options.getArguments().getOption(ProgramOptionConstants.INSTANCES, "0"));
      Preconditions.checkArgument(instanceCount > 0, "Invalid or missing instance count");

      String runIdOption = options.getArguments().getOption(ProgramOptionConstants.RUN_ID);
      Preconditions.checkNotNull(runIdOption, "Missing runId");
      RunId runId = RunIds.fromString(runIdOption);

      ApplicationSpecification appSpec = program.getSpecification();
      Preconditions.checkNotNull(appSpec, "Missing application specification.");

      ProgramType processorType = program.getType();
      Preconditions.checkNotNull(processorType, "Missing processor type.");
      Preconditions.checkArgument(processorType == ProgramType.SERVICE, "Only Service process type is supported.");

      String processorName = program.getName();
      Preconditions.checkNotNull(processorName, "Missing processor name.");

      ServiceSpecification serviceSpec = appSpec.getServices().get(processorName);
      RuntimeSpecification runnableSpec = serviceSpec.getRunnables().get(runnableName);
      Preconditions.checkNotNull(runnableSpec, "RuntimeSpecification missing for Runnable \"%s\"", runnableName);

      Class<?> clz = null;
      clz = Class.forName(runnableSpec.getRunnableSpecification().getClassName(),
                          true, program.getClassLoader());

      Preconditions.checkArgument(TwillRunnable.class.isAssignableFrom(clz), "%s is not a TwillRunnable.", clz);

      Class<? extends TwillRunnable> runnableClass = (Class<? extends TwillRunnable>) clz;
      RunId twillRunId = RunIds.generate();
      final String[] argArray = RuntimeArguments.toPosixArray(options.getUserArguments());

      DiscoveryService dService = new DiscoveryService() {
        @Override
        public Cancellable register(final Discoverable discoverable) {
          return dsService.register(new Discoverable() {
            @Override
            public String getName() {
              return String.format("service.%s.%s.%s.%s", program.getAccountId(),
                                   program.getApplicationId(), program.getName(), discoverable.getName());
            }

            @Override
            public InetSocketAddress getSocketAddress() {
              return discoverable.getSocketAddress();
            }
          });
        }
      };

      DiscoveryServiceClient dClient = new DiscoveryServiceClient() {
        @Override
        public ServiceDiscovered discover(String s) {
          return serviceDiscovery.discover(program.getAccountId(), program.getApplicationId(),
                                           program.getName(), s);
        }
      };

      twillContext = new InMemoryTwillContext(twillRunId, runId, InetAddress.getLocalHost(), new String[0], argArray,
                                              runnableSpec.getRunnableSpecification(), instanceId,
                                              runnableSpec.getResourceSpecification().getVirtualCores(),
                                              runnableSpec.getResourceSpecification().getMemorySize(),
                                              dClient, dService, instanceCount, electionRegistry);

      TypeToken<? extends  TwillRunnable> runnableType = TypeToken.of(runnableClass);
      TwillRunnable runnable = new InstantiatorFactory(false).get(runnableType).create();
      InMemoryRunnableDriver driver = new
        InMemoryRunnableDriver(runnable, twillContext, new UserServiceLoggingContext(program.getAccountId(),
                                                                                     program.getApplicationId(),
                                                                                     processorName,
                                                                                     runnableName));

      //Injecting Metrics
      Reflections.visit(runnable, runnableType,
                        new MetricsFieldSetter(new ServiceRunnableMetrics(metricsCollectionService,
                                                                          program.getApplicationId(),
                                                                          serviceSpec.getName(), runnableName)));

      ProgramController controller = new InMemoryRunnableProgramController(program.getName(), runnableName,
                                                                           twillContext, driver);

      LOG.info("Starting Runnable: {}", runnableName);
      driver.start();
      LOG.info("Runnable started: {}", runnableName);

      return controller;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
