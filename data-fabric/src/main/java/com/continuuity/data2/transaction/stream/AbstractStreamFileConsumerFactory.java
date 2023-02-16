/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data2.transaction.stream;

import com.continuuity.common.queue.QueueName;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data.file.FileReader;
import com.continuuity.data.stream.MultiLiveStreamFileReader;
import com.continuuity.data.stream.StreamEventOffset;
import com.continuuity.data.stream.StreamFileOffset;
import com.continuuity.data.stream.StreamUtils;
import com.continuuity.data2.queue.ConsumerConfig;
import com.continuuity.data2.queue.QueueClientFactory;
import com.continuuity.data2.transaction.queue.QueueConstants;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.twill.filesystem.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base class for implementing {@link StreamConsumerFactory} using
 * {@link MultiLiveStreamFileReader}.
 */
public abstract class AbstractStreamFileConsumerFactory implements StreamConsumerFactory {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamFileConsumerFactory.class);

  private final StreamAdmin streamAdmin;
  private final StreamConsumerStateStoreFactory stateStoreFactory;
  private final String tablePrefix;

  // This is just for compatibility upgrade from pre 2.2.0 to 2.2.0.
  // TODO: Remove usage of this when no longer needed
  private final QueueClientFactory queueClientFactory;
  private final StreamAdmin oldStreamAdmin;

  protected AbstractStreamFileConsumerFactory(DataSetAccessor dataSetAccessor, StreamAdmin streamAdmin,
                                              StreamConsumerStateStoreFactory stateStoreFactory,
                                              QueueClientFactory queueClientFactory, StreamAdmin oldStreamAdmin) {
    this.streamAdmin = streamAdmin;
    this.stateStoreFactory = stateStoreFactory;
    this.tablePrefix = dataSetAccessor.namespace(QueueConstants.STREAM_TABLE_PREFIX, DataSetAccessor.Namespace.SYSTEM);
    this.queueClientFactory = queueClientFactory;
    this.oldStreamAdmin = oldStreamAdmin;
  }

  /**
   * Creates a {@link StreamConsumer}.
   *
   * @param tableName name of the table for storing process states
   * @param streamConfig configuration of the stream to consume from
   * @param consumerConfig configuration of the consumer
   * @param stateStore The {@link StreamConsumerStateStore} for recording consumer state
   * @param reader The {@link FileReader} to read stream events from
   * @return A new instance of {@link StreamConsumer}
   */
  protected abstract StreamConsumer create(
    String tableName, StreamConfig streamConfig, ConsumerConfig consumerConfig,
    StreamConsumerStateStore stateStore, StreamConsumerState beginConsumerState,
    FileReader<StreamEventOffset, Iterable<StreamFileOffset>> reader) throws IOException;

  /**
   * Gathers stream file offsets.
   *
   * @param partitionLocation Location of the partition directory
   * @param fileOffsets for collecting stream file offsets
   */
  protected abstract void getFileOffsets(Location partitionLocation,
                                         Collection<? super StreamFileOffset> fileOffsets) throws IOException;

  @Override
  public final StreamConsumer create(QueueName streamName, String namespace,
                                     ConsumerConfig consumerConfig) throws IOException {
    StreamConfig streamConfig = StreamUtils.ensureExists(streamAdmin, streamName.getSimpleName());

    String tableName = getTableName(streamName, namespace);
    StreamConsumerStateStore stateStore = stateStoreFactory.create(streamConfig);
    StreamConsumerState consumerState = stateStore.get(consumerConfig.getGroupId(), consumerConfig.getInstanceId());

    StreamConsumer newConsumer = create(tableName, streamConfig, consumerConfig,
                                        stateStore, consumerState, createReader(streamConfig, consumerState));

    try {
      // The old stream admin uses full URI of queue name as the name for checking existence
      if (!oldStreamAdmin.exists(streamName.toURI().toString())) {
        return newConsumer;
      }

      // For old stream consumer, the group size doesn't matter in queue based stream.
      StreamConsumer oldConsumer = new QueueToStreamConsumer(streamName, consumerConfig,
                                                             queueClientFactory.createConsumer(streamName,
                                                                                               consumerConfig, -1)
      );
      return new CombineStreamConsumer(oldConsumer, newConsumer);
    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw new IOException(e);
    }
  }

  private String getTableName(QueueName streamName, String namespace) {
    return String.format("%s.%s.%s", tablePrefix, streamName.getSimpleName(), namespace);
  }

  private MultiLiveStreamFileReader createReader(StreamConfig streamConfig,
                                                 StreamConsumerState consumerState) throws IOException {
    Location streamLocation = streamConfig.getLocation();
    Preconditions.checkNotNull(streamLocation, "Stream location is null for %s", streamConfig.getName());

    if (!Iterables.isEmpty(consumerState.getState())) {
      LOG.info("Create file reader with consumer state: {}", consumerState);
      // Has existing offsets, just resume from there.
      MultiLiveStreamFileReader reader = new MultiLiveStreamFileReader(streamConfig, consumerState.getState());
      reader.initialize();
      return reader;
    }

    // TODO: Support starting from some time rather then from beginning.
    // Otherwise, search for files with the smallest partition start time
    // If no partition exists for the stream, start with one partition earlier than current time to make sure
    // no event will be lost if events start flowing in about the same time.
    long startTime = StreamUtils.getPartitionStartTime(System.currentTimeMillis() - streamConfig.getPartitionDuration(),
                                                       streamConfig.getPartitionDuration());

    for (Location partitionLocation : streamLocation.list()) {
      if (!partitionLocation.isDirectory()) {
        // Partition should be a directory
        continue;
      }

      long partitionStartTime = StreamUtils.getPartitionStartTime(partitionLocation.getName());
      if (partitionStartTime < startTime) {
        startTime = partitionStartTime;
      }
    }

    // Create file offsets
    // TODO: Be able to support dynamic name of stream writer instances.
    // Maybe it's done through MultiLiveStreamHandler to alter list of file offsets dynamically
    Location partitionLocation = StreamUtils.createPartitionLocation(streamLocation,
                                                                     startTime, streamConfig.getPartitionDuration());
    List<StreamFileOffset> fileOffsets = Lists.newArrayList();
    getFileOffsets(partitionLocation, fileOffsets);

    LOG.info("Empty consumer state. Create file reader with file offsets: groupId={}, instanceId={} states={}",
             consumerState.getGroupId(), consumerState.getInstanceId(), fileOffsets);

    MultiLiveStreamFileReader reader = new MultiLiveStreamFileReader(streamConfig, fileOffsets);
    reader.initialize();
    return reader;
  }
}
