/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.api.mapreduce;

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
   * @return The specification used to configure this {@link MapReduce} job nstance.
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
   *
   * @param dataset Input dataset.
   * @param splits Data selection splits.
   */
  @Deprecated
  void setInput(BatchReadable dataset, List<Split> splits);

  /**
   * Overrides the input configuration of this MapReduce job to use
   * the specified dataset by its name and data selection splits.
   *
   * @param datasetName Name of the input dataset.
   * @param splits Data selection splits.
   */
  void setInput(String datasetName, List<Split> splits);

  /**
   * Overrides the output configuration of this MapReduce job to write to the specified dataset.
   *
   * @param dataset Output dataset.
   */
  @Deprecated
  void setOutput(BatchWritable dataset);

  /**
   * Overrides the output configuration of this MapReduce job to write to the specified dataset by its name.
   *
   * @param datasetName Name of the output dataset.
   */
  void setOutput(String datasetName);
}
