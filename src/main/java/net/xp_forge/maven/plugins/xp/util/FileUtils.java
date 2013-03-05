/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.util;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringBufferInputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class
 *
 */
public final class FileUtils {
  private static File tempDirectory;

  /**
   * Utility classes should not have a public or default constructor
   *
   */
  private FileUtils() {
  }

  /**
   * Set STATIC temp directory
   *
   * @param  java.io.File directory
   * @return void
   */
  public static void setTempDirectory(File directory) {
    if (!directory.exists()) {
      directory.mkdirs();
    }
    FileUtils.tempDirectory= directory;
  }

  /**
   * Build an absolute path from the specified relative path
   *
   * @param  java.lang.String str
   * @param  java.io.File workingDirectory
   * @return java.lang.String
   */
  public static String getAbsolutePath(String str, File workingDirectory) {
    if (null == str) return null;

    // Make it absolute
    File f= new File(str);
    if (!f.exists()) {
      f= new File(workingDirectory.getAbsolutePath() + File.separator + str);
      if (!f.exists()) return null;
    }

    // Return absolute path
    return f.getAbsolutePath();
  }

  /**
   * Filter a list of directories and remove empty ones
   *
   * @param  java.util.List<String> directories Source directories
   * @return java.util.List<String> Filtered directories
   */
  public static List<String> filterEmptyDirectories(List<String> directories) {
    List<String> retVal= new ArrayList<String>();

    // Sanity check
    if (null == directories || directories.isEmpty()) return retVal;

    // Copy as I may be modifying it
    for (String directory : directories) {
      File f= new File(directory);

      // Check directory exists
      if (retVal.contains(directory) || !f.exists() || !f.isDirectory()) continue;

      // Check directory is not empty
      String[] files = f.list();
      if (0 == files.length) continue;

      // Add to return value
      retVal.add(f.getAbsolutePath());
    }

    return retVal;
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.io.InputStream is
   * @throws java.io.IOException when I/O errors occur
   */
  public static void setFileContents(File file, InputStream is) throws IOException {

    // Sanity check
    if (null == file) {
      throw new IllegalArgumentException("File cannot be [null]");
    }

    if (null == is) {
      throw new IllegalArgumentException("Input stream cannot be [null]");
    }

    // Make dirs
    File parent= file.getParentFile();
    if (null != parent && !parent.exists()) {
      parent.mkdirs();
    }

    // Save InputStream contents to file
    FileOutputStream os= null;
    try {
      os= new FileOutputStream(file);
      byte[] buffer= new byte[2048];
      int bytesRead;
      while (-1 != (bytesRead= is.read(buffer, 0, 2048))) {
        os.write(buffer, 0, bytesRead);
      }
      is.close();
      os.flush();
      os.close();
    } finally {
      if (null != os) {
        os.close();
      }
    }
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.lang.String text
   * @throws java.io.IOException when I/O errors occur
   */
  public static void setFileContents(File file, String text) throws IOException {
    FileUtils.setFileContents(file, new StringBufferInputStream(text));
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.lang.String text
   * @param  java.lang.String header
   * @throws java.io.IOException when I/O errors occur
   */
  public static void setFileContents(File file, String text, String header) throws IOException {
    FileUtils.setFileContents(file, new StringBufferInputStream(
      String.format("%s%n%s", header, text)
    ));
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.util.List<java.lang.String> lines
   * @throws java.io.IOException when I/O errors occur
   */
  public static void setFileContents(File file, List<String> lines) throws IOException {
    FileUtils.setFileContents(file, new StringBufferInputStream(
      StringUtils.join(lines, String.format("%n"))
    ));
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.util.List<java.lang.String> lines
   * @param  java.lang.String header
   * @throws java.io.IOException when I/O errors occur
   */
  public static void setFileContents(File file, List<String> lines, String header) throws IOException {
    FileUtils.setFileContents(file, StringUtils.join(lines, String.format("%n")), header);
  }

  /**
   * Create a temporary directory
   *
   * @return java.io.File
   * @throws java.io.IOException
   */
  public static File getTempDirectory() throws IOException {

    // Create a temporary file to get a unique name
    File tmpFile= File.createTempFile("mxp-", null, FileUtils.tempDirectory);
    tmpFile.deleteOnExit();

    // Create temporary directory
    File tmpDirectory= new File(tmpFile.getPath() + ".directory");
    tmpDirectory.mkdirs();
    tmpDirectory.deleteOnExit();
    return tmpDirectory;
  }

  /**
   * Copies a file to a new location
   *
   * @param  java.io.File srcFile
   * @param  java.io.File destFile
   * @return void
   * @throws java.io.IOException
   */
  public static void copyFile(File srcFile, File destFile) throws IOException {

    if (!destFile.exists()) {

      // Create directory; if the case
      File destDirectory= destFile.getParentFile();
      if (null != destDirectory && !destDirectory.exists()) {
        destDirectory.mkdirs();
      }

      // Create destination file
      destFile.createNewFile();
    }

    FileInputStream fIn   = null;
    FileOutputStream fOut = null;
    FileChannel srcChan   = null;
    FileChannel destChan  = null;

    try {
      fIn      = new FileInputStream(srcFile);
      srcChan  = fIn.getChannel();
      fOut     =  new FileOutputStream(destFile);
      destChan = fOut.getChannel();

      long transfered = 0;
      long bytes      = srcChan.size();

      while (transfered < bytes) {
        transfered += destChan.transferFrom(srcChan, 0, srcChan.size());
        destChan.position(transfered);
      }
    } finally {
      if (null != srcChan) {
        srcChan.close();
      } else if (null != fIn) {
        fIn.close();
      }

      if (null != destChan) {
        destChan.close();
      } else if (null != fOut) {
        fOut.close();
      }
    }
  }

  /**
   * Deletes a non-empty directory
   *
   * @param  java.io.File directory
   * @return void
   * @throws java.io.IOException
   */
  public static void deleteDirectory(File directory) throws IOException {
    if (null == directory || !directory.exists()) return;

    // Not a directory
    if (!directory.isDirectory()) {
      throw new IOException("[" + directory + "] is not a directory");
    }

    // List directory contents
    File[] entries= directory.listFiles();
    if (null == entries) {
      throw new IOException("Failed to list contents of directory [" + directory + "]");
    }

    for (File entry : entries) {

      // Delete directories
      if (entry.isDirectory()) {
        FileUtils.deleteDirectory(entry);
        continue;
      }

      // Delete files
      if (false == entry.delete()) {
        throw new IOException("Unable to delete file [" + entry + "]");
      }
    }

    // Delete empty directory
    if (false == directory.delete()) {
      throw new IOException("Unable to delete directory [" + directory + "]");
    }
  }

  /**
   * Checks (recursively) whether the specified directory contains at least one file
   *
   * @param  java.io.File directory
   * @return boolean
   * @throws java.io.IOException
   */
  public static boolean containsAtLeastOneFile(File directory) throws IOException {
    if (null == directory || !directory.exists() || !directory.isDirectory()) return false;

    // List directory contents
    File[] entries= directory.listFiles();
    if (null == entries) {
      throw new IOException("Failed to list contents of directory [" + directory + "]");
    }

    for (File entry : entries) {

      // Found one file
      if (entry.isFile()) return true;

      // Recurse
      if (true == FileUtils.containsAtLeastOneFile(entry)) return true;
    }

    // No files found
    return false;
  }
}
