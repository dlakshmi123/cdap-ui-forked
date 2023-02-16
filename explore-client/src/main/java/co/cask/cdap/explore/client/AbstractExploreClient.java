/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.explore.client;

import co.cask.cdap.explore.service.Explore;
import co.cask.cdap.explore.service.ExploreException;
import co.cask.cdap.explore.service.HandleNotFoundException;
import co.cask.cdap.explore.service.MetaDataInfo;
import co.cask.cdap.proto.ColumnDesc;
import co.cask.cdap.proto.QueryHandle;
import co.cask.cdap.proto.QueryResult;
import co.cask.cdap.proto.QueryStatus;
import com.google.common.base.Functions;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.twill.common.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * A base for an Explore Client that talks to a server implementing {@link Explore} over HTTP.
 */
public abstract class AbstractExploreClient extends ExploreHttpClient implements ExploreClient {
  private final ListeningScheduledExecutorService executor;

  protected AbstractExploreClient() {
    executor = MoreExecutors.listeningDecorator(
      Executors.newSingleThreadScheduledExecutor(Threads.createDaemonThreadFactory("explore-client-executor")));
  }

  @Override
  public void close() throws IOException {
    // This will cancel all the running tasks, with interruption - that means that all
    // queries submitted by this executor will be closed
    executor.shutdownNow();
  }

  @Override
  public boolean isServiceAvailable() {
    return isAvailable();
  }

  @Override
  public ListenableFuture<Void> disableExplore(final String datasetInstance) {
    ListenableFuture<ExploreExecutionResult> futureResults = getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return doDisableExplore(datasetInstance);
      }
    });

    // Exceptions will be thrown in case of an error in the futureHandle
    return Futures.transform(futureResults, Functions.<Void>constant(null));
  }

  @Override
  public ListenableFuture<Void> enableExplore(final String datasetInstance) {
    ListenableFuture<ExploreExecutionResult> futureResults = getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return doEnableExplore(datasetInstance);
      }
    });

    // Exceptions will be thrown in case of an error in the futureHandle
    return Futures.transform(futureResults, Functions.<Void>constant(null));
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> submit(final String statement) {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return execute(statement);
      }
    });
  }

  @Override
  public Map<String, String> datasetSchema(String datasetName) throws ExploreException {
    return getDatasetSchema(datasetName);
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> columns(@Nullable final String catalog,
                                                          @Nullable final String schemaPattern,
                                                          final String tableNamePattern,
                                                          final String columnNamePattern) {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> catalogs() {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getCatalogs();
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> schemas(@Nullable final String catalog,
                                                          @Nullable final String schemaPattern) {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getSchemas(catalog, schemaPattern);
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> functions(@Nullable final String catalog,
                                                            @Nullable final String schemaPattern,
                                                            final String functionNamePattern) {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getFunctions(catalog, schemaPattern, functionNamePattern);
      }
    });
  }

  @Override
  public ListenableFuture<MetaDataInfo> info(final MetaDataInfo.InfoType infoType) {
    return executor.submit(new Callable<MetaDataInfo>() {
      @Override
      public MetaDataInfo call() throws Exception {
        return getInfo(infoType);
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> tables(@Nullable final String catalog,
                                                         @Nullable final String schemaPattern,
                                                         final String tableNamePattern,
                                                         @Nullable final List<String> tableTypes) {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getTables(catalog, schemaPattern, tableNamePattern, tableTypes);
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> tableTypes() {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getTableTypes();
      }
    });
  }

  @Override
  public ListenableFuture<ExploreExecutionResult> dataTypes() {
    return getResultsFuture(new HandleProducer() {
      @Override
      public QueryHandle getHandle() throws ExploreException, SQLException {
        return getTypeInfo();
      }
    });
  }

  private ListenableFuture<ExploreExecutionResult> getResultsFuture(final HandleProducer handleProducer) {
    // NOTE: here we have two levels of Future because we want to return the future that actually
    // finishes the execution of the operation - it is not enough that the future handle
    // be available
    ListenableFuture<QueryHandle> futureHandle = executor.submit(new Callable<QueryHandle>() {
      @Override
      public QueryHandle call() throws Exception {
        return handleProducer.getHandle();
      }
    });
    return getFutureResultsFromHandle(futureHandle);
  }

  /**
   * Create a {@link ListenableFuture} object by polling the Explore service using the
   * {@link ListenableFuture} containing a {@link QueryHandle}.
   */
  private ListenableFuture<ExploreExecutionResult> getFutureResultsFromHandle(
    final ListenableFuture<QueryHandle> futureHandle) {
    final StatementExecutionFuture resultFuture = new StatementExecutionFuture(this, futureHandle);
    Futures.addCallback(futureHandle, new FutureCallback<QueryHandle>() {
      @Override
      public void onSuccess(final QueryHandle handle) {
        try {
          QueryStatus status = getStatus(handle);
          if (!status.getStatus().isDone()) {
            executor.schedule(new Runnable() {
              @Override
              public void run() {
                onSuccess(handle);
              }
            }, 300, TimeUnit.MILLISECONDS);
          } else {
            if (!status.hasResults()) {
              close(handle);
            }
            if (!resultFuture.set(new ClientExploreExecutionResult(AbstractExploreClient.this,
                                                                   handle, status.hasResults()))) {
              close(handle);
            }
          }
        } catch (Exception e) {
          throw Throwables.propagate(e);
        }
      }

      @Override
      public void onFailure(Throwable t) {
        resultFuture.setException(t);
      }
    }, executor);
    return resultFuture;
  }

  /**
   * Interface that produces a handle.
   */
  private static interface HandleProducer {
    QueryHandle getHandle() throws ExploreException, SQLException;
  }

  /**
   * Result iterator which polls Explore service using HTTP to get next results.
   */
  private static final class ClientExploreExecutionResult extends AbstractIterator<QueryResult>
    implements ExploreExecutionResult {
    private static final Logger LOG = LoggerFactory.getLogger(ClientExploreExecutionResult.class);
    private static final int DEFAUL_FETCH_SIZE = 100;

    private int fetchSize = DEFAUL_FETCH_SIZE;
    private Iterator<QueryResult> delegate;

    private final ExploreHttpClient exploreClient;
    private final QueryHandle handle;
    private final boolean hasResults;

    public ClientExploreExecutionResult(ExploreHttpClient exploreClient, QueryHandle handle, boolean hasResults) {
      this.exploreClient = exploreClient;
      this.handle = handle;
      this.hasResults = hasResults;
    }

    @Override
    protected QueryResult computeNext() {
      if (!hasResults) {
        return endOfData();
      }

      if (delegate != null && delegate.hasNext()) {
        return delegate.next();
      }
      try {
        // call the endpoint 'next' to get more results and set delegate
        List<QueryResult> nextResults = exploreClient.nextResults(handle, fetchSize);
        delegate = nextResults.iterator();

        // At this point, if delegate has no result, there are no more results at all
        if (!delegate.hasNext()) {
          return endOfData();
        }
        return delegate.next();
      } catch (ExploreException e) {
        LOG.error("Exception while iterating through the results of query {}", handle.getHandle(), e);
        throw Throwables.propagate(e);
      } catch (HandleNotFoundException e) {
        // Handle may have timed out, or the handle given is just unknown
        LOG.debug("Received exception", e);
        return endOfData();
      }
    }

    @Override
    public void close() throws IOException {
      try {
        exploreClient.close(handle);
      } catch (HandleNotFoundException e) {
        // Don't need to throw an exception in that case - if the handle is not found, the query is already closed
        LOG.warn("Caught exception when closing the results", e);
      } catch (ExploreException e) {
        LOG.error("Caught exception during close operation", e);
        throw Throwables.propagate(e);
      }
    }

    @Override
    public int getFetchSize() {
      return fetchSize;
    }

    @Override
    public void setFetchSize(int fetchSize) {
      this.fetchSize = (fetchSize <= 0) ? DEFAUL_FETCH_SIZE : fetchSize;
    }

    @Override
    public List<ColumnDesc> getResultSchema() throws ExploreException {
      try {
        return exploreClient.getResultSchema(handle);
      } catch (HandleNotFoundException e) {
        LOG.error("Caught exception when retrieving results schema", e);
        throw new ExploreException(e);
      }
    }
  }

  /**
   * Implementation of a listenable future for {@link ExploreExecutionResult} with an overridden
   * {@link com.google.common.util.concurrent.ListenableFuture#cancel(boolean)} method.
   */
  private static final class StatementExecutionFuture extends AbstractFuture<ExploreExecutionResult> {
    private static final Logger LOG = LoggerFactory.getLogger(StatementExecutionFuture.class);

    private final Explore exploreClient;
    private final ListenableFuture<QueryHandle> futureHandle;

    StatementExecutionFuture(Explore exploreClient, ListenableFuture<QueryHandle> futureHandle) {
      this.exploreClient = exploreClient;
      this.futureHandle = futureHandle;
    }

    @Override
    public boolean set(@Nullable ExploreExecutionResult value) {
      return super.set(value);
    }

    @Override
    public boolean setException(Throwable throwable) {
      return super.setException(throwable);
    }

    @Override
    protected void interruptTask() {
      // Cancelling the future object means cancelling the query, as well as closing it
      // Since closing the query also cancels it, we only need to close
      Futures.addCallback(futureHandle, new FutureCallback<QueryHandle>() {
        @Override
        public void onSuccess(QueryHandle handle) {
          try {
            exploreClient.close(handle);
          } catch (ExploreException e) {
            LOG.error("Caught exception during cancel operation", e);
            throw Throwables.propagate(e);
          } catch (HandleNotFoundException e) {
            // Don't need to throw an exception in that case -
            // if the handle is not found, the query is already closed
            LOG.warn("Caught exception when closing execution", e);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          LOG.error("Caught exception", t);
          setException(t);
        }
      });
    }
  }
}
