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
package co.cask.cdap.data.runtime;

import co.cask.cdap.api.flow.flowlet.StreamEvent;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.io.Locations;
import co.cask.cdap.data.file.FileWriter;
import co.cask.cdap.data.stream.StreamFileWriterFactory;
import co.cask.cdap.data.stream.StreamUtils;
import co.cask.cdap.data.stream.TimePartitionedStreamFileWriter;
import co.cask.cdap.data2.transaction.stream.StreamAdmin;
import co.cask.cdap.data2.transaction.stream.StreamConfig;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import org.apache.twill.filesystem.Location;

import java.io.IOException;

/**
 * A {@link StreamFileWriterFactory} that provides {@link FileWriter} which writes to file location.
 * Use for both local and distributed mode.
 */
public final class LocationStreamFileWriterFactory implements StreamFileWriterFactory {

  private final StreamAdmin streamAdmin;
  private final String filePrefix;

  @Inject
  LocationStreamFileWriterFactory(CConfiguration cConf, StreamAdmin streamAdmin) {
    this.streamAdmin = streamAdmin;
    this.filePrefix = String.format("%s.%d",
                                    cConf.get(Constants.Stream.FILE_PREFIX),
                                    cConf.getInt(Constants.Stream.CONTAINER_INSTANCE_ID, 0));
  }

  @Override
  public FileWriter<StreamEvent> create(StreamConfig config, int generation) throws IOException {
    try {
      Preconditions.checkNotNull(config.getLocation(), "Location for stream {} is unknown.", config.getName());

      Location baseLocation = StreamUtils.createGenerationLocation(config.getLocation(), generation);
      Locations.mkdirsIfNotExists(baseLocation);

      return new TimePartitionedStreamFileWriter(baseLocation, config.getPartitionDuration(),
                                                 filePrefix, config.getIndexInterval());

    } catch (Exception e) {
      Throwables.propagateIfPossible(e, IOException.class);
      throw new IOException(e);
    }
  }
}
