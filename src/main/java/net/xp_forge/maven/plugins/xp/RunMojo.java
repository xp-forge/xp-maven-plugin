/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.runners.XpRunner;
import net.xp_forge.maven.plugins.xp.runners.RunnerException;
import net.xp_forge.maven.plugins.xp.runners.input.XpRunnerInput;

/**
 * Run XP classes
 *
 * @goal run
 * @requiresDependencyResolution runtime
 */
public class RunMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {

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
   * @parameter expression="${xp.run.classpaths}"
   */
  protected List<String> classpaths;

  /**
   * Define name of class to run
   *
   * @parameter expression="${xp.run.classname}"
   */
  protected String className;

  /**
   * Define inline code to run
   *
   * @parameter expression="${xp.run.code}"
   */
  protected String code;

  /**
   * {@inheritDoc}
   *
   */
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Debug info
    getLog().debug("Classes directory [" + this.classesDirectory + "]");
    getLog().debug("Classpaths        [" + (null == this.classpaths ? "NULL" : this.classpaths) + "]");
    getLog().debug("ClassName         [" + (null == this.className ? "NULL" : this.className) + "]");
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
    File executable= new File(this.runnersDirectory, "xp");
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
