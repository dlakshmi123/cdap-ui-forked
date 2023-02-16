/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.metrics.query;

import com.continuuity.common.conf.Constants;
import com.continuuity.common.http.core.HttpResponder;
import com.continuuity.common.service.ServerException;
import com.continuuity.data2.OperationException;
import com.continuuity.gateway.auth.GatewayAuthenticator;
import com.continuuity.metrics.data.MetricsTableFactory;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class for handling requests for a single metric in a context.
 */
@Path(Constants.Gateway.GATEWAY_VERSION + "/metrics")
public class MetricsQueryHandler extends BaseMetricsHandler {

  private final MetricsRequestExecutor requestExecutor;

  @Inject
  public MetricsQueryHandler(GatewayAuthenticator authenticator, final MetricsTableFactory metricsTableFactory) {
    super(authenticator);
    this.requestExecutor = new MetricsRequestExecutor(metricsTableFactory);
  }

  @GET
  @Path("/{scope}/{metric}")
  public void handleOverview(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/apps/appX/process.events.processed
  @GET
  @Path("/{scope}/{type}/{type-id}/{metric}")
  public void handleTopLevel(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/apps/appX/flows/process.events.processed
  @GET
  @Path("/{scope}/{type}/{type-id}/{program-type}/{metric}")
  public void handleProgramType(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/apps/appX/flows/flowY/process.events.processed
  @GET
  @Path("/{scope}/{type}/{type-id}/{program-type}/{program-id}/{metric}")
  public void handleProgram(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/apps/appX/mapreduce/jobId/mappers/process.entries.in
  @GET
  @Path("/{scope}/{type}/{type-id}/{program-type}/{program-id}/{component-type}/{metric}")
  public void handleComponentType(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/apps/appX/flows/flowY/flowlets/flowletZ/process.events.processed
  @GET
  @Path("/{scope}/{type}/{type-id}/{program-type}/{program-id}/{component-type}/{component-id}/{metric}")
  public void handleComponent(HttpRequest request, HttpResponder responder) throws IOException {
    handleRequest(request, responder);
  }

  // ex: /reactor/datasets/tickTimeseries/apps/Ticker/flows/TickerTimeseriesFlow/flowlets/saver/store.bytes
  @GET
  @Path("/reactor/datasets/{dataset-id}/apps/{app-id}/flows/{flow-id}/flowlets/{flowlet-id}/{metric}")
  public void handleFlowletDatasetMetrics(HttpRequest request, HttpResponder responder)
    throws IOException, OperationException {
    handleRequest(request, responder);
  }

  private void handleRequest(HttpRequest request, HttpResponder responder) throws IOException {
    try {
      URI uri = new URI(MetricsRequestParser.stripVersionAndMetricsFromPath(request.getUri()));
      MetricsRequest metricsRequest = parseAndValidate(request, uri);
      responder.sendJson(HttpResponseStatus.OK, requestExecutor.executeQuery(metricsRequest));
    } catch (URISyntaxException e) {
      responder.sendError(HttpResponseStatus.BAD_REQUEST, e.getMessage());
    } catch (MetricsPathException e) {
      responder.sendError(HttpResponseStatus.NOT_FOUND, e.getMessage());
    } catch (OperationException e) {
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal error while querying metrics");
    } catch (ServerException e) {
      responder.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal error while querying metrics");
    }
  }
}