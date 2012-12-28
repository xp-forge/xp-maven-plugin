/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;

/**
 * Package classes and resources
 *
 * @goal test-package
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution test
 */
public class TestPackageMojo extends AbstractPackageMojo {

  /**
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  protected boolean skip;

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

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<File> getAppDirectories() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getMainClass() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean isSkip() {
    return this.skip;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<File> getVendorLibraries() {
    return null;
  }
}
