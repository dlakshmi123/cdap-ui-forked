/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.passport.core.exceptions;

/**
 *
 */
public class StaleNonceException extends Exception {
  public StaleNonceException(String message) {
    super(message);
  }
}
