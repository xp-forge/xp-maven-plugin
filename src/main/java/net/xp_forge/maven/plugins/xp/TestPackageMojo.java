/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Package classes and resources
 *
 * @goal package-test
 * @requiresDependencyResolution runtime
 */
public class TestPackageMojo extends AbstractPackageMojo {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected File getSourcesDirectory() {
    return this.testClassesDirectory;
  }
}
