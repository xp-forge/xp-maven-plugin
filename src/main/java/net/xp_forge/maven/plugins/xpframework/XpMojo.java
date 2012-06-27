/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xpframework.runners.RunnerException;
import net.xp_forge.maven.plugins.xpframework.runners.XpRunner;
import net.xp_forge.maven.plugins.xpframework.runners.input.XpRunnerInput;

/**
 * Run XP classes
 *
 * @goal xp
 * @requiresDependencyResolution runtime
 */
public class XpMojo extends AbstractXpFrameworkMojo {

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the xp runner
   *
   * @parameter expression="${xpframework.xp.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the xp runner
   *
   * @parameter expression="${xpframework.xp.classpaths}"
   */
  protected ArrayList<String> classpaths;

  /**
   * Define name of class to run
   *
   * @parameter expression="${xpframework.xp.classname}"
   */
  protected String className;

  /**
   * Define inline code to run
   *
   * @parameter expression="${xpframework.xp.code}"
   */
  protected String code;

  /**
   * {@inheritDoc}
   *
   */
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {
    Iterator i;

    // Run tests
    getLog().info(LINE_SEPARATOR);
    getLog().info("RUN - XP CLASS");
    getLog().info(LINE_SEPARATOR);

    // Debug info
    getLog().debug("Classes directory      [" + this.classesDirectory + "]");
    getLog().debug("Classpaths             [" + (null == this.classpaths ? "NULL" : this.classpaths.toString()) + "]");
    getLog().debug("ClassName              [" + this.className + "]");
    getLog().debug("Code                   [" + this.code + "]");

    // Prepare unittest input
    XpRunnerInput input= new XpRunnerInput();
    input.verbose= this.verbose;

    // Add testClassesDirectory and classesDirectory to classpaths
    input.addClasspath(this.classesDirectory);

    // Add pom-defined classpaths
    if (null != this.classpaths) {
      for (String classpath : this.classpaths) {
        input.addClasspath(new File(classpath));
      }
    }

    input.className= this.className;
    input.code= this.code;

    // Configure "xp" runner
    File executable= new File(this.runnersDirectory, "xp");
    XpRunner runner= new XpRunner(executable, input);
    runner.setTrace(getLog());

    // Set runner working directory
    try {
      runner.setWorkingDirectory(this.basedir);
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot set [xp] runner working directory", ex);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xp] runner failed", ex);
    }

    getLog().info(LINE_SEPARATOR);
  }
}
