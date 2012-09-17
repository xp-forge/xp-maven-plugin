/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;

/**
 * Package classes and resources
 *
 * @goal package
 * @requiresDependencyResolution runtime
 */
public class PackageMojo extends AbstractPackageMojo {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected File getClassesDirectory() {
    return this.classesDirectory;
  }
}
