/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.archive;

import com.continuuity.common.lang.MultiClassLoader;
import com.continuuity.weave.filesystem.Location;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * JarClassLoader extends {@link com.continuuity.common.lang.MultiClassLoader}
 */
public class JarClassLoader extends MultiClassLoader {
  private final JarResources jarResources;

  /**
   * Creates a ClassLoader that load classes from the given jar file.
   * @param jarLocation Location of the jar file
   * @throws IOException If there is error loading the jar file
   * @see #JarClassLoader(JarResources)
   */
  public JarClassLoader(Location jarLocation) throws IOException {
    this(new JarResources(jarLocation));
  }

  /**
   * Creates a ClassLoader with provided archive resources and uses context classloader as parent if available.
   * Otherwise, the classloader of this class would be used as parent classloader.
   * @param jarResources instance of archive resources
   */
  public JarClassLoader(JarResources jarResources) {
    this(jarResources,
         Thread.currentThread().getContextClassLoader() == null ?
           JarClassLoader.class.getClassLoader() : Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a ClassLoader that load classes from the given jar file with the given ClassLoader as its parent.
   * @param jarLocation Location of the jar file.
   * @param parent Parent ClassLoader.
   * @throws IOException If there is error loading the jar file.
   */
  public JarClassLoader(Location jarLocation, ClassLoader parent) throws IOException {
    this(new JarResources(jarLocation), parent);
  }

  /**
   * Creates a ClassLoader with provided archive resources with the given ClassLoader as its parent.
   * @param jarResources instance of archive resources
   * @param parent Parent ClassLoader.
   */
  public JarClassLoader(JarResources jarResources, ClassLoader parent) {
    super(parent);
    this.jarResources = jarResources;
  }

  /**
   * Returns an input stream for reading the specified resource. If the resource is not found then it will try
   * finding it with its parent ClassLoader, if any.
   * @param s The resource name
   * @return An input stream for reading the resource, or null if the resource could not be found
   */
  @Override
  public InputStream getResourceAsStream(String s) {
    // Since entries in jarResources do not start with leading "/", remove it from s to query jarResources.
    String entry = s;
    if (s.startsWith("/")) {
      entry = entry.substring(1);
    }

    InputStream input;
    try {
      input = jarResources.getResourceAsStream(entry);
    } catch (IOException e) {
      input = null;
    }

    if (input == null) {
      ClassLoader parent = getParent();
      if (parent != null) {
        return parent.getResourceAsStream(s);
      }
    }
    return input;
  }

  /**
   * Loads the class bytes based on the name specified. Name
   * munging is used to identify the class to be loaded from
   * the archive.
   *
   * @param className Name of the class bytes to be loaded.
   * @return array of bytes for the class.
   */
  @Override
  @Nullable
  public byte[] loadClassBytes(String className) {
    return jarResources.getResource(formatClassName(className));
  }
}