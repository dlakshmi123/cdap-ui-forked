/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.logging.save;

import com.continuuity.common.logging.LoggingContext;
import com.continuuity.logging.kafka.KafkaLogEvent;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that manages writing of KafkaLogEvent to Avro files. The events are written into appropriate files
 * based on the LoggingContext of the event. The files are also rotated based on size. This class is not thread-safe.
 */
public final class AvroFileWriter implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(AvroFileWriter.class);

  private final CheckpointManager checkpointManager;
  private final FileMetaDataManager fileMetaDataManager;
  private final FileSystem fileSystem;
  private final Schema schema;
  private final int syncIntervalBytes;
  private final Path pathRoot;
  private final Map<String, AvroFile> fileMap;
  private final long maxFileSize;
  private final long checkpointIntervalMs;
  private final long inactiveIntervalMs;

  private long lastCheckpointTime = System.currentTimeMillis();

  /**
   * Constructs an AvroFileWriter object.
   * @param checkpointManager used to store checkpoint meta data.
   * @param fileMetaDataManager used to store file meta data.
   * @param fileSystem fileSystem where the Avro files are to be created.
   * @param pathRoot Root path for the files to be created.
   * @param schema schema of the Avro data to be written.
   * @param maxFileSize Avro files greater than maxFileSize will get rotated.
   * @param syncIntervalBytes the approximate number of uncompressed bytes to write in each block.
   * @param checkpointIntervalMs interval to save checkpoint.
   * @param inactiveIntervalMs files that have no data written for more than inactiveIntervalMs will be closed.
   */
  public AvroFileWriter(CheckpointManager checkpointManager, FileMetaDataManager fileMetaDataManager,
                        FileSystem fileSystem, Path pathRoot, Schema schema,
                        long maxFileSize, int syncIntervalBytes, long checkpointIntervalMs, long inactiveIntervalMs) {
    this.checkpointManager = checkpointManager;
    this.fileMetaDataManager = fileMetaDataManager;
    this.fileSystem = fileSystem;
    this.schema = schema;
    this.syncIntervalBytes = syncIntervalBytes;
    this.pathRoot = pathRoot;
    this.fileMap = Maps.newHashMap();
    this.maxFileSize = maxFileSize;
    this.checkpointIntervalMs = checkpointIntervalMs;
    this.inactiveIntervalMs = inactiveIntervalMs;
  }

  /**
   * Appends a log event to an appropriate Avro file based on LoggingContext. If the log event does not contain
   * LoggingContext then the event will be dropped.
   * @param events Log event
   * @throws IOException
   */
  public void append(List<KafkaLogEvent> events) throws Exception {
    if (events.isEmpty()) {
      LOG.debug("Empty append list.");
      return;
    }

    KafkaLogEvent event = events.get(0);
    LoggingContext loggingContext = event.getLoggingContext();
    LOG.debug("Appending {} messages for logging context {} and partition {}",
              events.size(), loggingContext.getLogPathFragment(), event.getPartition());

    long timestamp = event.getLogEvent().getTimeStamp();
    AvroFile avroFile = getAvroFile(loggingContext, timestamp, event.getPartition());
    avroFile = rotateFile(avroFile, loggingContext, timestamp, event.getPartition());

    for (KafkaLogEvent e : events) {
      avroFile.append(e);
    }
    avroFile.flush();

    checkPoint(false);
  }

  @Override
  public void close() throws IOException {
    // First checkpoint state
    try {
      checkPoint(true);
    } catch (Exception e) {
      LOG.error("Caught exception while checkpointing", e);
    }

    // Close all files
    LOG.info("Closing all files");
    for (Map.Entry<String, AvroFile> entry : fileMap.entrySet()) {
      try {
        entry.getValue().close();
      } catch (Throwable e) {
        LOG.error(String.format("Caught exception while closing file %s", entry.getValue().getPath()), e);
      }
    }
    fileMap.clear();

    fileSystem.close();
  }

  private AvroFile getAvroFile(LoggingContext loggingContext, long timestamp, int partition) throws Exception {
    AvroFile avroFile = fileMap.get(loggingContext.getLogPathFragment());
    if (avroFile == null) {
      avroFile = createAvroFile(loggingContext, timestamp, partition);
    }
    return avroFile;
  }

  private AvroFile createAvroFile(LoggingContext loggingContext, long timestamp, int partition) throws Exception {
    long currentTs = System.currentTimeMillis();
    Path path = createPath(loggingContext.getLogPathFragment(), currentTs);
    LOG.info(String.format("Creating Avro file %s", path.toUri()));
    AvroFile avroFile = new AvroFile(path, partition);
    try {
      avroFile.open();
    } catch (IOException e) {
      avroFile.close();
      throw e;
    }
    fileMap.put(loggingContext.getLogPathFragment(), avroFile);
    fileMetaDataManager.writeMetaData(loggingContext, timestamp, path);
    return avroFile;
  }

  private Path createPath(String pathFragment, long timestamp) {
    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    return new Path(pathRoot, String.format("%s/%s/%s.avro", pathFragment, date, timestamp));
  }

  private AvroFile rotateFile(AvroFile avroFile, LoggingContext loggingContext, long timestamp,
                              int partition) throws Exception {
    if (avroFile.getPos() > maxFileSize) {
      LOG.info(String.format("Rotating file %s", avroFile.getPath()));
      checkPoint(true);
      avroFile.close();
      return createAvroFile(loggingContext, timestamp, partition);
    }
    return avroFile;
  }

  public void checkPoint(boolean force) throws Exception {
    long currentTs = System.currentTimeMillis();
    if (!force && currentTs - lastCheckpointTime < checkpointIntervalMs) {
      return;
    }

    // Get the max checkpoint seen for each partition
    Map<Integer, Long> partitionOffsetMap =  Maps.newHashMap();
    Set<String> files = Sets.newHashSetWithExpectedSize(fileMap.size());
    for (Iterator<Map.Entry<String, AvroFile>> it = fileMap.entrySet().iterator(); it.hasNext();) {
      AvroFile avroFile = it.next().getValue();
      avroFile.sync();

      files.add(avroFile.getPath().toUri().toString());
      Long fileMax = partitionOffsetMap.get(avroFile.getPartition());
      if (fileMax == null || avroFile.getMaxNextOffset() > fileMax) {
        partitionOffsetMap.put(avroFile.getPartition(), avroFile.getMaxNextOffset());
      }

      // Close inactive files
      if (currentTs - avroFile.getLastModifiedTs() > inactiveIntervalMs) {
        avroFile.close();
        it.remove();
      }
    }

    for (Map.Entry<Integer, Long> entry : partitionOffsetMap.entrySet()) {
      LOG.debug("Saving checkpoint offset {} for partition {}", entry.getValue(), entry.getKey());
      checkpointManager.saveCheckpoint(entry.getKey(), entry.getValue());
    }
    lastCheckpointTime = currentTs;
  }

  /**
   * Represents an Avro file.
   */
  public class AvroFile implements Closeable {
    private final Path path;
    private final int partition;
    private FSDataOutputStream outputStream;
    private DataFileWriter<GenericRecord> dataFileWriter;
    private long maxNextOffset = -1;
    private long lastModifiedTs;
    private boolean isOpen = false;

    public AvroFile(Path path, int partition) {
      this.path = path;
      this.partition = partition;
    }

    /**
     * Opens the underlying file for writing. If open throws an exception, then @{link #close()} needs to be called to
     * free resources.
     * @throws IOException
     */
    void open() throws IOException {
      this.outputStream = fileSystem.create(path, false);
      this.dataFileWriter = new DataFileWriter<GenericRecord>(new GenericDatumWriter<GenericRecord>(schema));
      this.dataFileWriter.create(schema, this.outputStream);
      this.dataFileWriter.setSyncInterval(syncIntervalBytes);
      this.lastModifiedTs = System.currentTimeMillis();
      this.isOpen = true;
    }

    public Path getPath() {
      return path;
    }

    public int getPartition() {
      return partition;
    }

    public void append(KafkaLogEvent event) throws IOException {
      dataFileWriter.append(event.getGenericRecord());
      if (event.getNextOffset() > maxNextOffset) {
        maxNextOffset = event.getNextOffset();
      }
      lastModifiedTs = System.currentTimeMillis();
    }

    public long getPos() throws IOException {
      return outputStream.getPos();
    }

    public long getMaxNextOffset() {
      return maxNextOffset;
    }

    public long getLastModifiedTs() {
      return lastModifiedTs;
    }

    public void flush() throws IOException {
      dataFileWriter.flush();
      outputStream.hflush();
    }

    public void sync() throws IOException {
      dataFileWriter.flush();
      outputStream.hsync();
    }

    @Override
    public void close() throws IOException {
      if (!isOpen) {
        return;
      }

      try {
        if (dataFileWriter != null) {
          dataFileWriter.close();
        }
      } finally {
        if (outputStream != null) {
          outputStream.close();
        }
      }

      isOpen = false;
    }
  }
}