/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to tag a {@link com.continuuity.api.flow.flowlet.Flowlet} process method.
 *
 * @see com.continuuity.api.flow.flowlet.Flowlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessInput {

  static final int DEFAULT_MAX_RETRIES = 20;

  /**
   * Optionally tag the name of inputs to the process method.
   */
  String[] value() default {};

  /**
   * Optionally declares the name of the partition key for data partitioning to the process methods
   * across multiple instances of {@link com.continuuity.api.flow.flowlet.Flowlet}.
   */
  @Deprecated
  String partition() default "";

  /**
   * Optionally specifies the maximum number of retries of failure inputs before giving up on it.
   * Defaults to {@link #DEFAULT_MAX_RETRIES}.
   */
  int maxRetries() default DEFAULT_MAX_RETRIES;
}
