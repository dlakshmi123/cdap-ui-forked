package com.continuuity.common.utils;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

/**
 * Copied from Google Guava as these methods are now deprecated
 */
public final class DirUtils {

  /**
   * Utility classes should have a public constructor or a default constructor
   * hence made it private.
   */
  private DirUtils(){}

  /**
   * Deletes files within a directory recursively.
   * @param file
   * @throws IOException
   */
  public static void deleteRecursively(File file) throws IOException {
    if (file.isDirectory()) {
      deleteDirectoryContents(file);
    }
    if (!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }

  /**
   * Wipes out all the a directory starting from a given directory
   * @param directory to be cleaned
   * @throws IOException
   */
  public static void deleteDirectoryContents(File directory)
        throws IOException {
    Preconditions.checkArgument(directory.isDirectory(),
          "Not a directory: %s", directory);
    File[] files = directory.listFiles();
    if (files == null) {
      throw new IOException("Error listing files for " + directory);
    }
    for (File file : files) {
      deleteRecursively(file);
    }
  }
}
