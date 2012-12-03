/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.input.xp.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * FileFilter to show only *.ini files in directory listing
 *
 */
public class InifileFilter implements FileFilter {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public boolean accept(File file) {
    return file.getName().endsWith(".ini") && file.exists() && file.isFile();
  }
}
