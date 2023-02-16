/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.internal.app.services;

import com.continuuity.app.services.AppFabricService;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.metrics.OverlordMetricsReporter;
import com.continuuity.weave.discovery.Discoverable;
import com.continuuity.weave.discovery.DiscoveryService;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AppFabric Server that implements {@link AbstractExecutionThreadService}.
 */
public class AppFabricServer extends AbstractExecutionThreadService {
  private static final int THREAD_COUNT = 2;

  private final CConfiguration conf;
  private final AppFabricService.Iface service;
  private final int port;
  private final DiscoveryService discoveryService;
  private final InetAddress hostname;

  private TThreadedSelectorServer server;
  private ExecutorService executor;

  /**
   * Construct the AppFabricServer with service factory and configuration coming from guice injection.
   */
  @Inject
  public AppFabricServer(AppFabricService.Iface service, CConfiguration configuration,
                         DiscoveryService discoveryService, @Named("config.hostname") InetAddress hostname) {
    this.conf = configuration;
    this.hostname = hostname;
    this.service = service;
    this.discoveryService = discoveryService;
    this.port = configuration.getInt(Constants.CFG_APP_FABRIC_SERVER_PORT, Constants.DEFAULT_APP_FABRIC_SERVER_PORT);
  }

  /**
   * Configures the AppFabricService pre-start.
   */
  @Override
  protected void startUp() throws Exception {

    executor = Executors.newFixedThreadPool(THREAD_COUNT);

    // Register with discovery service.
    InetSocketAddress socketAddress = new InetSocketAddress(hostname, port);
    InetAddress address = socketAddress.getAddress();
    if (address.isAnyLocalAddress()) {
      address = InetAddress.getLocalHost();
    }
    final InetSocketAddress finalSocketAddress = new InetSocketAddress(address, port);

    discoveryService.register(new Discoverable() {
      @Override
      public String getName() {
        return "app.fabric.service";
      }

      @Override
      public InetSocketAddress getSocketAddress() {
        return finalSocketAddress;
      }
    });

    TThreadedSelectorServer.Args options = new TThreadedSelectorServer.Args(new TNonblockingServerSocket(socketAddress))
      .executorService(executor)
      .processor(new AppFabricService.Processor<AppFabricService.Iface>(service))
      .workerThreads(THREAD_COUNT);
    options.maxReadBufferBytes = Constants.DEFAULT_MAX_READ_BUFFER;
    server = new TThreadedSelectorServer(options);

    // Start the flow metrics reporter.
    OverlordMetricsReporter.enable(1, TimeUnit.SECONDS, conf);
  }

  @Override
  protected void shutDown() throws Exception {
    OverlordMetricsReporter.disable();
    executor.shutdownNow();
  }

  /**
   * Runs the AppFabricServer.
   * <p>
   *   It's run on a different thread.
   * </p>
   */
  @Override
  protected void run() throws Exception {
    server.serve();
  }

  /**
   * Invoked during shutdown of the thread.
   */
  protected void triggerShutdown() {
    server.stop();
  }
}