package com.continuuity.internal.pipeline;

import com.continuuity.pipeline.Pipeline;
import com.continuuity.pipeline.PipelineFactory;

/**
 * A factory for providing synchronous pipeline.
 */
public class SynchronousPipelineFactory<T> implements PipelineFactory<T> {

  /**
   * @return A synchronous pipeline.
   */
  @Override
  public Pipeline<T> getPipeline() {
    return new SynchronousPipeline();
  }
}