/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.app.deploy;

/**
 *
 */
public interface ManagerFactory {
  <U, V> Manager<U, V> create();
}