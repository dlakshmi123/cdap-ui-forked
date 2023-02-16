package com.continuuity.api.batch;

import com.continuuity.api.RuntimeContext;
import com.continuuity.api.data.batch.BatchReadable;
import com.continuuity.api.data.batch.BatchWritable;
import com.continuuity.api.data.batch.Split;

import java.util.List;

/**
 * MapReduce job execution context.
 */
public interface MapReduceContext extends RuntimeContext {
  /**
   * @return The specification used to configure this {@link MapReduce} instance.
   */
  MapReduceSpecification getSpecification();

  /**
   * Returns the logical start time of this MapReduce job. Logical start time is the time when this MapReduce
   * job is supposed to start if this job is started by the scheduler. Otherwise it would be the current time when the
   * job runs.
   *
   * @return Time in milliseconds since epoch time (00:00:00 January 1, 1970 UTC).
   */
  long getLogicalStartTime();

  /**
   */
  <T> T getHadoopJob();

  /**
   * Overrides the input configuration of this MapReduce job to use the specified dataset and data selection splits.
   * @param dataset Input dataset
   * @param splits Data selection splits
   */
  void setInput(BatchReadable dataset, List<Split> splits);

  /**
   * Overrides the output configuration of this MapReduce job to write to the specified dataset.
   * @param dataset Output dataset
   */
  void setOutput(BatchWritable dataset);
}
