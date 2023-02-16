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

package com.continuuity.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation tag required for {@link com.continuuity.api.flow.flowlet.Flowlet} process methods.
 * 
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * <code>
 * OutputEmitter{@literal <}Long> output;
 *
 * {@literal @}ProcessInput
 * public void round(Double number) {
 *   output.emit(Math.round(number));
 * }
 * </code>
 * </pre>
 *
 * <p>
 * See the <i><a href="http://continuuity.com/docs/reactor/current/en/">Continuuity Reactor Developer Guides</a></i>
 * and the Reactor example applications for more information.
 * </p>
 *
 * @see com.continuuity.api.flow.flowlet.Flowlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessInput {

  static final int DEFAULT_MAX_RETRIES = 20;

  /**
   * Optionally tags the name of inputs to the process method.
   */
  String[] value() default {};

  /**
   * Optionally specifies the maximum number of retries of failure inputs before giving up.
   * Defaults to {@link #DEFAULT_MAX_RETRIES}.
   */
  int maxRetries() default DEFAULT_MAX_RETRIES;
}
