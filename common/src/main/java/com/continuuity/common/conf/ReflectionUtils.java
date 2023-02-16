/*
 * Copyright (c) 2012-2013 Continuuity Inc. All rights reserved.
 *
 * The original Apache 2.0 licensed code was changed by Continuuity Inc.
 */

package com.continuuity.common.conf;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * General reflection utils.
 */
public class ReflectionUtils {

  private static final Class<?>[] EMPTY_ARRAY = new Class[]{};

  /**
   * Cache of constructors for each class. Pins the classes so they
   * can't be garbage collected until ReflectionUtils can be collected.
   */
  private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE =
    new ConcurrentHashMap<Class<?>, Constructor<?>>();

  /**
   * Check and set 'configuration' if necessary.
   *
   * @param theObject object for which to set configuration
   * @param conf Configuration
   */
  private static void setConf(Object theObject, Configuration conf) {
    if (conf != null) {
      if (theObject instanceof Configurable) {
        ((Configurable) theObject).setConf(conf);
      }
    }
  }

  /** Create an object for the given class and initialize it from conf.
   *
   * @param theClass class of which an object is created
   * @param conf Configuration
   * @return a new object
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> theClass, Configuration conf) {
    T result;
    try {
      Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
      if (meth == null) {
        meth = theClass.getDeclaredConstructor(EMPTY_ARRAY);
        meth.setAccessible(true);
        CONSTRUCTOR_CACHE.put(theClass, meth);
      }
      result = meth.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    setConf(result, conf);
    return result;
  }

  /**
   * Return the correctly-typed {@link Class} of the given object.
   *
   * @param o object whose correctly-typed <code>Class</code> is to be obtained
   * @return the correctly typed <code>Class</code> of the given object.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClass(T o) {
    return (Class<T>) o.getClass();
  }

  // methods to support testing
  static void clearCache() {
    CONSTRUCTOR_CACHE.clear();
  }

  static int getCacheSize() {
    return CONSTRUCTOR_CACHE.size();
  }
}

