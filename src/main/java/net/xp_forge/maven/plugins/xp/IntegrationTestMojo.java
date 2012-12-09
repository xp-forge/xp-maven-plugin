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
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.runners.xp.UnittestRunner;
import net.xp_forge.maven.plugins.xp.exec.input.xp.UnittestRunnerInput;

/**
 * Run integration tests
 *
 * @goal integration-test
 * @requiresDependencyResolution test
 */
public class IntegrationTestMojo extends AbstractXpMojo {

  /**
   * Skip integration tests
   *
   * @parameter expression="${maven.it.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the unittest runner
   *
   * @parameter expression="${xp.test.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the unittest runner
   *
   * @parameter
   */
  protected List<String> classpaths;

  /**
   * Define argument to pass to tests
   *
   * The -a argument for the unittest runner
   *
   * @parameter
   */
  protected List<String> testArguments;

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
  protected List<File> iniDirectories;

  /**
   * {@inheritDoc}
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Skip tests alltogether?
    if (this.skip) {
      getLog().info("Not running integration tests (maven.it.skip)");
      return;
    }

    // Can't test "pom" projects
    if (this.packaging.equals("pom")) {
      getLog().info("Not running integration tests for [pom] projects");
      return;
    }

    // Get main artifact that is to be tested
    Artifact artifact= this.getMainArtifact();
    if (null == artifact) {
      getLog().warn("Not running integration tests as no main artifact was found");
      return;
    }

    // Debug info
    getLog().info("Running tests from [" + this.iniDirectory + "]");
    getLog().debug("Additional directories [" + (null == this.iniDirectories ? "NULL" : this.iniDirectories) + "]");
    getLog().debug("Tested artifact        [" + artifact.getFile() + "]");
    getLog().debug("Test classes directory [" + this.testClassesDirectory + "]");
    getLog().debug("Classpaths             [" + (null == this.classpaths ? "NULL" : this.classpaths) + "]");
    getLog().debug("Test arguments         [" + (null == this.testArguments ? "NULL" : this.testArguments) + "]");

    // Prepare [unittest] input
    UnittestRunnerInput input= new UnittestRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(this.getArtifacts(false));

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add artifact to classpath
    input.addClasspath(artifact);

    // Add itClassesDirectory to classpath
    input.addClasspath(this.itClassesDirectory);

    // Add arguments
    if (null != this.testArguments) {
      for (String testArgument : this.testArguments) {
        input.addArgument(testArgument);
      }
    }

    // Inifiles
    input.addInifileDirectory(this.iniDirectory);
    if (null != this.iniDirectories) {
      for (File dir : this.iniDirectories) {
        input.addInifileDirectory(dir);
      }
    }

    // Check no tests to run
    if (0 == input.inifiles.size()) {
      getLog().info("There are no tests to run");
      getLog().info(LINE_SEPARATOR);
      return;
    }

    // Configure [unittest] runner
    File executable= new File(this.runnersDirectory, "unittest");
    UnittestRunner runner= new UnittestRunner(executable, input);
    runner.setLog(getLog());

    // Set runner working directory to [/target]
    runner.setWorkingDirectory(this.outputDirectory);

    // Set USE_XP environment variable
    if (null != this.use_xp) {
      runner.setEnvironmentVariable("USE_XP", this.use_xp);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [unittest] runner failed", ex);
    }
  }
}
