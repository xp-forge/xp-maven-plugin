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
 * Run unit tests
 *
 * @goal test
 * @requiresDependencyResolution test
 */
public class TestMojo extends AbstractTestMojo {

  /**
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * Directory to scan for [*.ini] files
   *
   * @parameter expression="${xp.test.iniDirectory}" default-value="${basedir}/src/test/config/unittest"
   */
  protected File iniDirectory;

  /**
   * Additional directories to scan for [*.ini] files
   *
   * @parameter
   */
  protected List<File> testAdditionalIniDirectories;

  /**
   * Whether to run all unit tests using a single unittest runner instance. If false, a new unittest runner
   * instance will be spawned for every [*.ini] file (default).
   *
   * @parameter expression="${xp.test.singleInstance}" default-value="false"
   */
  protected boolean testSingleInstance;

  /**
   * {@inheritDoc}
   *
   */
  protected boolean isSkip() {
    return this.skip;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected File getIniDirectory() {
    return this.iniDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected List<File> getAdditionalIniDirectories() {
    return this.testAdditionalIniDirectories;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected File getClassesDirectory() {
    return this.classesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected File getTestClassesDirectory() {
    return this.testClassesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected boolean isSingleInstance() {
    return this.testSingleInstance;
  }
}
