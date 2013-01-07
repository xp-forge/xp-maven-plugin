/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;

/**
 * Run integration tests
 *
 * @goal integration-test
 * @requiresDependencyResolution test
 */
public class IntegrationTestMojo extends AbstractTestMojo {

  /**
   * Skip integration tests
   *
   * @parameter expression="${maven.it.skip}" default-value="false"
   */
  protected boolean itSkip;

  /**
   * Directory to scan for [*.ini] files
   *
   * @parameter expression="${xp.it.iniDirectory}" default-value="${basedir}/src/it/config/unittest"
   */
  protected File iniDirectory;

  /**
   * Additional directories to scan for [*.ini] files
   *
   * @parameter
   */
  protected List<File> itAdditionalIniDirectories;

  /**
   * Whether to run all integration tests using a single unittest runner instance. If false, a new unittest runner
   * instance will be spawned for every [*.ini] file (default).
   *
   * @parameter expression="${xp.it.singleInstance}" default-value="false"
   */
  protected boolean itSingleInstance;

  /**
   * {@inheritDoc}
   *
   */
  protected boolean isSkip() {

    // Can't test "pom" projects
    if (this.packaging.equals("pom")) {
      getLog().info("Not running integration tests for [pom] projects");
      return true;
    }

    // Get main artifact that is to be tested
    Artifact artifact= this.getMainArtifact();
    if (null == artifact) {
      getLog().warn("Not running integration tests as no main artifact was found");
      return true;
    }

    return this.itSkip;
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
    return this.itAdditionalIniDirectories;
  }

  /**
   * {@inheritDoc}
   *
   * Note: for integration tests, return main artifact instead of classes directory
   */
  protected File getClassesDirectory() {

    // Get main artifact that is to be tested
    Artifact artifact= this.getMainArtifact();
    if (null == artifact) {
      getLog().warn("Not running integration tests as no main artifact was found");
      return null;
    }

    return artifact.getFile();
  }

  /**
   * {@inheritDoc}
   *
   */
  protected File getTestClassesDirectory() {
    return this.itClassesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected boolean isSingleInstance() {
    return this.itSingleInstance;
  }
}
