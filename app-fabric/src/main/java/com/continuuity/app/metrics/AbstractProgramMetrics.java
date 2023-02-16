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

package com.continuuity.app.metrics;

import com.continuuity.api.metrics.Metrics;
import com.continuuity.common.metrics.MetricsCollectionService;
import com.continuuity.common.metrics.MetricsCollector;

/**
 * Base class for defining implementation of {@link Metrics} for different type of runtime context.
 * Metrics will be emitted through {@link MetricsCollectionService}.
 */
public abstract class AbstractProgramMetrics implements Metrics {

  private final MetricsCollector metricsCollector;

  protected AbstractProgramMetrics(MetricsCollector metricsCollector) {
    this.metricsCollector = metricsCollector;
  }

  @Override
  public void count(String counterName, int delta) {
    metricsCollector.gauge(counterName, delta);
  }
}
