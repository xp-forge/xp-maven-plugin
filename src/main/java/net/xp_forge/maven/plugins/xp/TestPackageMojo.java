/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;

/**
 * Package classes and resources
 *
 * @goal test-package
 * @requiresProject
 * @requiresDependencyResolution test
 */
public class TestPackageMojo extends AbstractPackageMojo {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected File getClassesDirectory() {
    return this.testClassesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getClassifier() {
    return "tests";
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getFormat() {
    return "xar";
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getStrategy() {
    return "lib";
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean getPackDependencies() {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean getPackRuntime() {
    return false;
  }
}
