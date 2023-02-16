/*
 * Copyright 2014 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.data.runtime;

import com.continuuity.api.flow.flowlet.StreamEvent;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data.file.FileWriter;
import com.continuuity.data.stream.StreamFileWriterFactory;
import com.continuuity.data.stream.TimePartitionedStreamFileWriter;
import com.continuuity.data2.transaction.stream.StreamAdmin;
import com.continuuity.data2.transaction.stream.StreamConfig;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

import java.io.IOException;

/**
 * A {@link StreamFileWriterFactory} that provides {@link FileWriter} which writes to file location.
 * Use for both local and distributed mode.
 */
final class LocationStreamFileWriterFactory implements StreamFileWriterFactory {

  private final StreamAdmin streamAdmin;
  private final String filePrefix;

  @Inject
  LocationStreamFileWriterFactory(CConfiguration cConf, StreamAdmin streamAdmin) {
    this.streamAdmin = streamAdmin;
    this.filePrefix = cConf.get(Constants.Stream.FILE_PREFIX);
  }

  @Override
  public FileWriter<StreamEvent> create(String streamName) throws IOException {
    try {
      StreamConfig config = streamAdmin.getConfig(streamName);
      Preconditions.checkNotNull(config.getLocation(), "Location for stream {} is unknown.", streamName);
      return new TimePartitionedStreamFileWriter(config, filePrefix);

    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw new IOException(e);
    }
  }
}
