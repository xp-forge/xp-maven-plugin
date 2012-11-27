/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.runners.xp.XpRunner;
import net.xp_forge.maven.plugins.xp.exec.input.xp.XpRunnerInput;

/**
 * Run XP code
 *
 * This goal functions the same as the "run" goal but does not fork
 * the build and is suitable for attaching to the build lifecycle
 *
 * @goal run-no-fork
 * @phase compile
 * @requiresDependencyResolution runtime
 * @since 3.2.0
 */
public class RunNoForkMojo extends AbstractXpMojo {

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the xp runner
   *
   * @parameter expression="${xp.run.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the xp runner
   *
   * @parameter
   */
  protected List<String> classpaths;

  /**
   * Define name of class to run
   *
   * @parameter expression="${xp.run.class}"
   */
  protected String className;

  /**
   * Define inline code to run
   *
   * @parameter expression="${xp.run.code}"
   */
  protected String code;

  /**
   * Get location of XP-Runners
   *
   * For a non-forked lifecycle, this variable is set in the "initialize" phase
   *
   * @return java.io.File
   */
  protected File getRunnersDirectory() {
    return this.runnersDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Debug info
    getLog().debug("Classes directory [" + this.classesDirectory + "]");
    getLog().debug("Classpaths        [" + (null == this.classpaths ? "NULL" : this.classpaths) + "]");
    getLog().debug("Class             [" + (null == this.className ? "NULL" : this.className) + "]");
    getLog().debug("Code              [" + (null == this.code ? "NULL" : this.code) + "]");

    // Prepare unittest input
    XpRunnerInput input= new XpRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(this.getArtifacts(false));

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add classesDirectory and testClassesDirectory to classpaths
    input.addClasspath(this.classesDirectory);
    input.addClasspath(this.testClassesDirectory);

    input.className= this.className;
    input.code= this.code;

    // Configure "xp" runner
    File executable= new File(this.getRunnersDirectory(), "xp");
    XpRunner runner= new XpRunner(executable, input);
    runner.setLog(getLog());

    // Set runner working directory to [/target]
    runner.setWorkingDirectory(this.outputDirectory);

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xp] runner failed", ex);
    }
  }
}
