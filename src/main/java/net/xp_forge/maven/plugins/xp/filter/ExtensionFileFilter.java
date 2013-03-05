/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * An implementation of FileFilter that filters using a specified set of extensions
 *
 */
public class ExtensionFileFilter implements FileFilter {
  private String extension;

  /**
   * Constructor
   *
   */
  public ExtensionFileFilter(String extension) {
    this.extension= "." + extension;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public boolean accept(File file) {
    return file.getName().endsWith(this.extension) && file.exists() && file.isFile();
  }
}
