package com.continuuity.internal.app.deploy.pipeline;

import com.continuuity.api.ProgramSpecification;
import com.continuuity.app.Id;
import com.continuuity.app.program.Type;
import com.continuuity.app.store.Store;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.discovery.RandomEndpointStrategy;
import com.continuuity.common.discovery.TimeLimitEndpointStrategy;
import com.continuuity.common.metrics.MetricsScope;
import com.continuuity.data2.transaction.queue.QueueAdmin;
import com.continuuity.internal.app.deploy.ProgramTerminator;
import com.continuuity.pipeline.AbstractStage;
import com.continuuity.weave.discovery.Discoverable;
import com.continuuity.weave.discovery.DiscoveryServiceClient;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.ning.http.client.SimpleAsyncHttpClient;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Deleted program handler stage. Figures out which programs are deleted and handles callback.
 */
public class DeletedProgramHandlerStage extends AbstractStage<ApplicationSpecLocation> {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeletedProgramHandlerStage.class);

  /**
   * Number of seconds for timing out a service endpoint discovery.
   */
  private static final long DISCOVERY_TIMEOUT_SECONDS = 3;

  /**
   * Timeout to get response from metrics system.
   */
  private static final long METRICS_SERVER_RESPONSE_TIMEOUT = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);

  private final Store store;
  private final ProgramTerminator programTerminator;
  private final QueueAdmin queueAdmin;
  private final DiscoveryServiceClient discoveryServiceClient;

  public DeletedProgramHandlerStage(Store store, ProgramTerminator programTerminator, QueueAdmin queueAdmin,
                                    DiscoveryServiceClient discoveryServiceClient) {
    super(TypeToken.of(ApplicationSpecLocation.class));
    this.store = store;
    this.programTerminator = programTerminator;
    this.queueAdmin = queueAdmin;
    this.discoveryServiceClient = discoveryServiceClient;
  }

  @Override
  public void process(ApplicationSpecLocation appSpec) throws Exception {
    List<ProgramSpecification> deletedSpecs = store.getDeletedProgramSpecifications(appSpec.getApplicationId(),
                                                                                    appSpec.getSpecification());

    List<String> deletedFlows = Lists.newArrayList();
    for (ProgramSpecification spec : deletedSpecs){
      //call the deleted spec
      Type type = Type.typeOfSpecification(spec);
      Id.Program programId = Id.Program.from(appSpec.getApplicationId(), spec.getName());
      programTerminator.stop(Id.Account.from(appSpec.getApplicationId().getAccountId()),
                                   programId, type);
      // drop all queues of a deleted flow
      if (Type.FLOW.equals(type)) {
        queueAdmin.dropAllForFlow(programId.getApplicationId(), programId.getId());
        deletedFlows.add(programId.getId());
      }
    }
    deleteMetrics(appSpec.getApplicationId().getAccountId(), appSpec.getApplicationId().getId(), deletedFlows);
    emit(appSpec);
  }

  private void deleteMetrics(String account, String application, Iterable<String> flows) throws IOException {
    Iterable<Discoverable> discoverables = this.discoveryServiceClient.discover(Constants.Service.GATEWAY);
    Discoverable discoverable = new TimeLimitEndpointStrategy(new RandomEndpointStrategy(discoverables),
                                                              DISCOVERY_TIMEOUT_SECONDS, TimeUnit.SECONDS).pick();

    if (discoverable == null) {
      LOG.error("Fail to get any metrics endpoint for deleting metrics.");
      return;
    }

    LOG.debug("Deleting metrics for application {}", application);
    for (MetricsScope scope : MetricsScope.values()) {
      for (String flow : flows) {
        String url = String.format("http://%s:%d%s/metrics/%s/apps/%s/flows/%s",
                                   discoverable.getSocketAddress().getHostName(),
                                   discoverable.getSocketAddress().getPort(),
                                   Constants.Gateway.GATEWAY_VERSION,
                                   scope.name().toLowerCase(),
                                   application, flow);

        SimpleAsyncHttpClient client = new SimpleAsyncHttpClient.Builder()
          .setUrl(url)
          .setRequestTimeoutInMs((int) METRICS_SERVER_RESPONSE_TIMEOUT)
          .build();

        try {
          client.delete().get(METRICS_SERVER_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          LOG.error("exception making metrics delete call", e);
          Throwables.propagate(e);
        } finally {
          client.close();
        }
      }
    }
  }

}
