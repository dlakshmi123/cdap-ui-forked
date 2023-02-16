/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data.stream.service;

import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.metrics.MetricsCollector;
import com.continuuity.common.metrics.MetricsScope;
import com.continuuity.common.queue.QueueName;
import com.continuuity.data.stream.StreamFileWriterFactory;
import com.continuuity.data2.queue.ConsumerConfig;
import com.continuuity.data2.queue.DequeueResult;
import com.continuuity.data2.queue.DequeueStrategy;
import com.continuuity.data2.transaction.TransactionAware;
import com.continuuity.data2.transaction.TransactionExecutor;
import com.continuuity.data2.transaction.TransactionExecutorFactory;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.continuuity.data2.transaction.stream.StreamConsumer;
import com.continuuity.data2.transaction.stream.StreamConsumerFactory;
import com.continuuity.gateway.auth.Authenticator;
import com.continuuity.gateway.handlers.AuthenticatedHttpHandler;
import com.continuuity.http.HandlerContext;
import com.continuuity.http.HttpHandler;
import com.continuuity.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.hash.Hashing;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * The {@link HttpHandler} for handling REST call to stream endpoints.
 *
 * TODO: Currently stream "dataset" is implementing old dataset API, hence not supporting multi-tenancy.
 */
@Path(Constants.Gateway.GATEWAY_VERSION + "/streams")
public final class StreamHandler extends AuthenticatedHttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(StreamHandler.class);

  private final StreamAdmin streamAdmin;
  private final ConcurrentStreamWriter streamWriter;

  // TODO: Need to make the decision of whether this should be inside StreamAdmin or not.
  // Currently is here to align with the existing Reactor organization that dataset admin is not aware of MDS
  private final StreamMetaStore streamMetaStore;

  // A timed cache from consumer id (group id) to stream consumer.
  private final LoadingCache<ConsumerCacheKey, StreamDequeuer> dequeuerCache;

  @Inject
  public StreamHandler(CConfiguration cConf, Authenticator authenticator,
                       StreamAdmin streamAdmin, StreamMetaStore streamMetaStore,
                       StreamConsumerFactory streamConsumerFactory, StreamFileWriterFactory writerFactory,
                       TransactionExecutorFactory executorFactory, MetricsCollectionService metricsCollectionService) {
    super(authenticator);
    this.streamAdmin = streamAdmin;
    this.streamMetaStore = streamMetaStore;
    this.dequeuerCache = createDequeuerCache(streamConsumerFactory, executorFactory);

    MetricsCollector collector = metricsCollectionService.getCollector(MetricsScope.REACTOR,
                                                                       Constants.Gateway.METRICS_CONTEXT, "0");
    this.streamWriter = new ConcurrentStreamWriter(streamAdmin, streamMetaStore, writerFactory,
                                                   cConf.getInt(Constants.Stream.WORKER_THREADS, 10), collector);
  }

  @Override
  public void destroy(HandlerContext context) {
    Closeables.closeQuietly(streamWriter);
  }

  @PUT
  @Path("/{stream}")
  public void create(HttpRequest request, HttpResponder responder,
                     @PathParam("stream") String stream) throws Exception {

    String accountID = getAuthenticatedAccountId(request);

    // TODO: Modify the REST API to support custom configurations.
    streamAdmin.create(stream);
    streamMetaStore.addStream(accountID, stream);

    // TODO: For create successful, 201 Created should be returned instead of 200.
    responder.sendStatus(HttpResponseStatus.OK);
  }

  @POST
  @Path("/{stream}")
  public void enqueue(HttpRequest request, HttpResponder responder,
                      @PathParam("stream") String stream) throws Exception {

    String accountId = getAuthenticatedAccountId(request);

    try {
      if (streamWriter.enqueue(accountId, stream, getHeaders(request, stream), request.getContent().toByteBuffer())) {
        responder.sendStatus(HttpResponseStatus.OK);
      } else {
        responder.sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      }
    } catch (IllegalArgumentException e) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Stream not exists");
    }
  }

  @POST
  @Path("/{stream}/dequeue")
  public void dequeue(HttpRequest request, HttpResponder responder,
                      @PathParam("stream") String stream) throws Exception {
    getAuthenticatedAccountId(request);

    // Get the consumer Id
    String consumerId = request.getHeader(Constants.Stream.Headers.CONSUMER_ID);
    long groupId;
    try {
      groupId = Long.parseLong(consumerId);
    } catch (Exception e) {
      LOG.trace("Invalid consumerId: {}", consumerId, e);
      responder.sendString(HttpResponseStatus.BAD_REQUEST, "Invalid or missing consumer id");
      return;
    }

    // See if the consumer id is valid
    StreamDequeuer dequeuer;
    try {
      dequeuer = dequeuerCache.get(new ConsumerCacheKey(stream, groupId));
    } catch (Exception e) {
      responder.sendError(HttpResponseStatus.BAD_REQUEST, "Consumer not exists.");
      return;
    }

    // Dequeue event
    StreamEvent event = dequeuer.fetch();
    if (event == null) {
      responder.sendStatus(HttpResponseStatus.NO_CONTENT);
      return;
    }

    // Construct response headers
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
    for (Map.Entry<String, String> entry : event.getHeaders().entrySet()) {
      builder.put(stream + "." + entry.getKey(), entry.getValue());
    }

    responder.sendBytes(HttpResponseStatus.OK, event.getBody(), builder.build());
  }

  @POST
  @Path("/{stream}/consumer-id")
  public void newConsumer(HttpRequest request, HttpResponder responder,
                          @PathParam("stream") String stream) throws Exception {
    String accountId = getAuthenticatedAccountId(request);

    if (!streamMetaStore.streamExists(accountId, stream)) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Stream not exists");
      return;
    }

    long groupId = Hashing.md5().newHasher()
      .putString(stream)
      .putString(accountId)
      .putLong(System.nanoTime())
      .hash().asLong();

    streamAdmin.configureInstances(QueueName.fromStream(stream), groupId, 1);

    String consumerId = Long.toString(groupId);
    responder.sendByteArray(HttpResponseStatus.OK, consumerId.getBytes(Charsets.UTF_8),
                            ImmutableMultimap.of(Constants.Stream.Headers.CONSUMER_ID, consumerId));
  }

  @POST
  @Path("/{stream}/truncate")
  public void truncate(HttpRequest request, HttpResponder responder,
                       @PathParam("stream") String stream) throws Exception {
    String accountId = getAuthenticatedAccountId(request);

    streamMetaStore.removeStream(accountId, stream);

    // TODO: Implement file removal logic
    responder.sendStatus(HttpResponseStatus.OK);
  }

  /**
   * Creates a loading cache for stream consumers. Used by the dequeue REST API.
   */
  private LoadingCache<ConsumerCacheKey, StreamDequeuer> createDequeuerCache(
    final StreamConsumerFactory consumerFactory, final TransactionExecutorFactory executorFactory) {
    return CacheBuilder
      .newBuilder()
      .expireAfterAccess(Constants.Stream.CONSUMER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
      .removalListener(new RemovalListener<ConsumerCacheKey, StreamDequeuer>() {
        @Override
        public void onRemoval(RemovalNotification<ConsumerCacheKey, StreamDequeuer> notification) {
          try {
            StreamDequeuer dequeuer = notification.getValue();
            if (dequeuer != null) {
              dequeuer.close();
            }
          } catch (IOException e) {
            LOG.error("Failed to close stream consumer for {}", notification.getKey(), e);
          }
        }
      }).build(new CacheLoader<ConsumerCacheKey, StreamDequeuer>() {
        @Override
        public StreamDequeuer load(ConsumerCacheKey key) throws Exception {
          ConsumerConfig config = new ConsumerConfig(key.getGroupId(), 0, 1, DequeueStrategy.FIFO, null);
          StreamConsumer consumer = consumerFactory.create(QueueName.fromStream(key.getStreamName()),
                                                           Constants.Stream.HANDLER_CONSUMER_NS, config);
          return new StreamDequeuer(consumer, executorFactory);
        }
      });
  }

  private Map<String, String> getHeaders(HttpRequest request, String stream) {
    // build a new event from the request, start with the headers
    ImmutableMap.Builder<String, String> headers = ImmutableMap.builder();
    // set some built-in headers
    headers.put(Constants.Gateway.HEADER_FROM_COLLECTOR, Constants.Gateway.STREAM_HANDLER_NAME);
    headers.put(Constants.Gateway.HEADER_DESTINATION_STREAM, stream);
    // and transfer all other headers that are to be preserved
    String prefix = stream + ".";
    for (Map.Entry<String, String> header : request.getHeaders()) {
      if (header.getKey().startsWith(prefix)) {
        headers.put(header);
      }
    }
    return headers.build();
  }

  private static final class ConsumerCacheKey {
    private final String streamName;
    private final long groupId;

    private ConsumerCacheKey(String streamName, long groupId) {
      this.streamName = streamName;
      this.groupId = groupId;
    }

    public String getStreamName() {
      return streamName;
    }

    public long getGroupId() {
      return groupId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ConsumerCacheKey that = (ConsumerCacheKey) o;
      return Objects.equal(streamName, that.streamName) && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(streamName, groupId);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(this)
        .add("stream", streamName)
        .add("groupId", groupId)
        .toString();
    }
  }

  /**
   * A Stream consumer that will commit automatically right after dequeue.
   */
  private static final class StreamDequeuer implements Closeable {

    private final StreamConsumer consumer;
    private final TransactionExecutor txExecutor;

    private StreamDequeuer(StreamConsumer consumer, TransactionExecutorFactory executorFactory) {
      this.consumer = consumer;
      this.txExecutor = executorFactory.createExecutor(ImmutableList.<TransactionAware>of(consumer));
    }

    /**
     * @return an event fetched from the stream or {@code null} if no event.
     */
    StreamEvent fetch() throws Exception {
      return txExecutor.execute(new Callable<StreamEvent>() {

        @Override
        public StreamEvent call() throws Exception {
          DequeueResult<StreamEvent> poll = consumer.poll(1, 0, TimeUnit.SECONDS);
          return poll.isEmpty() ? null : poll.iterator().next();
        }
      });
    }

    @Override
    public void close() throws IOException {
      consumer.close();
    }
  }
}
