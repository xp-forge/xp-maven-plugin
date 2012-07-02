/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.util;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.ArrayList;

import net.xp_forge.xar.XarEntry;
import net.xp_forge.xar.XarArchive;

/**
 * Utility class to handle XAR archives
 *
 */
public final class XarUtils {

  /**
   * Utility classes should not have a public or default constructor
   *
   */
  private XarUtils() {
  }

  /**
   * Add contents of a whole directory to a XAR archive
   *
   * @param  net.xp_forge.xar.XarArchive target
   * @param  java.io.File directory
   * @param  java.lang.String prefix
   * @return void
   */
  public static void addDirectory(XarArchive target, File directory, String prefix) {
    if(!directory.isDirectory()) return;

    // Traverse directory
    for(String filename : directory.list()) {
      File file= new File(directory, filename);

      // Build new prefix
      String newPrefix;
      if (null == prefix) {
        newPrefix= filename;
      } else {
        newPrefix= prefix + "/" + filename;
      }

      // Recurse sub-directory
      if (file.isDirectory()) {
        XarUtils.addDirectory(target, file, newPrefix);

      // Add to target
      } else {
        target.addEntry(new XarEntry(newPrefix, file));
      }
    }
  }

  /**
   * Add contents of a whole directory to a XAR archive
   *
   * @param  net.xp_forge.xar.XarArchive target
   * @param  net.xp_forge.xar.XarArchive archive
   * @param  java.lang.String prefix
   * @return void
   */
  public static void addArchive(XarArchive target, XarArchive archive, String prefix) {
    if(null == archive) return;

    // Traverse archive
    for(XarEntry entry : archive.getEntries()) {
      XarEntry newEntry= entry;

      // Add prefix; if the case
      if (null != prefix) {
        newEntry= new XarEntry(prefix + "/" + entry.getName(), entry.getPayload());
      }

      // Add to target
      try {
        target.addEntry(entry);

      // An entry with this name already exists; skip it
      } catch (IllegalArgumentException ignore) { }
    }
  }
}
