/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.runners.XarRunner;
import net.xp_forge.maven.plugins.xp.runners.RunnerException;
import net.xp_forge.maven.plugins.xp.runners.input.XarRunnerInput;

/**
 * Wrapper around the XP-Framework "xar" runner
 *
 */
public abstract class AbstractXarMojo extends AbstractXpFrameworkMojo {

  /**
   * Name of the generated XAR
   *
   * @parameter default-value="${project.build.finalName}"
   * @required
   */
  protected String finalName;

  /**
   * Classifier to add to the generated artifact
   *
   * @parameter
   */
  protected String classifier;

  /**
   * Assemble XAR archive
   *
   * @param  java.io.File classesDirectory Directory where the compile files are
   * @param  java.io.File xarFile Output XAR file location
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xar runner failed
   */
  protected void executeXar(File classesDirectory, File xarFile) throws MojoExecutionException {

    // Debug info
    getLog().debug("Classes directory [" + classesDirectory + "]");
    getLog().info("XAR output file [" + xarFile + "]");

    // Prepare xar input
    XarRunnerInput input= new XarRunnerInput();
    input.operation= XarRunnerInput.operations.CREATE;

    // Set ouput file
    input.outputFile= xarFile;

    // Add sources
    input.addSource(classesDirectory);

    // Configure [xar] runner
    File executable= new File(this.runnersDirectory, "xar");
    XarRunner runner= new XarRunner(executable, input);
    runner.setTrace(getLog());

    // Set runner working directory to [/target/classes]
    try {
      runner.setWorkingDirectory(classesDirectory);
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot set [xar] runner working directory", ex);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xar] runner failed", ex);
    }

    // Check XAR file was assembled
    if (!xarFile.exists()) {
      throw new MojoExecutionException("Cannot find assembled XAR file [" + xarFile.getAbsolutePath() + "]");
    }

    // Attach/set generated xar as project artifact
    if (null != this.classifier) {
      this.projectHelper.attachArtifact(this.project, "xar", this.classifier, xarFile);
    } else {
      this.project.getArtifact().setFile(xarFile);
    }
  }

  /**
   * Assemble Uber-XAR archive
   *
   * @param  java.io.File xarFile Original XAR file withour dependencies
   * @param  java.io.File uberXarFile Output Uber-XAR file location
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xar runner failed
   */
  protected void executeUberXar(File xarFile, File uberXarFile) throws MojoExecutionException {
    Iterator i;

    // Debug info
    getLog().info("Uber-XAR output file [" + uberXarFile + "]");

    // Prepare xar input
    XarRunnerInput input= new XarRunnerInput();
    input.operation= XarRunnerInput.operations.MERGE;
    input.outputFile= uberXarFile;
    input.addSource(xarFile);

    // Add dependencies
    getLog().info("Inspecting dependencies");
    for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
      getLog().info(" - " + artifact.getType() + " [" + artifact.getFile().getAbsolutePath() + "]");

      // Ignore non-XAR and XP-Framework artifacts
      if (
        artifact.getGroupId().equals("net.xp-framework") &&
        (
          artifact.getType().equalsIgnoreCase("xar") ||
          artifact.getArtifactId().equals("core")  ||
          artifact.getArtifactId().equals("tools") ||
          artifact.getArtifactId().equals("language")
        )
      ) {
        getLog().info("   -> won't be merged");
        continue;
      }

      // Merge this dependency
      input.addSource(artifact.getFile());
    }

    // Check no XAR dependencies
    if (1 == input.sources.size()) {
      getLog().warn("No dependencies found so no Uber-XAR will be assembled");
      return;
    }

    // Configure [xar] runner
    File executable= new File(this.runnersDirectory, "xar");
    XarRunner runner= new XarRunner(executable, input);
    runner.setTrace(getLog());

    // Set runner working directory
    try {
      runner.setWorkingDirectory(xarFile.getParentFile());
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot set [xar] runner working directory", ex);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xar] runner failed", ex);
    }

    // Check Uber-XAR file was assembled
    if (!uberXarFile.exists()) {
      throw new MojoExecutionException("Cannot find assembled Uber-XAR [" + uberXarFile.getAbsolutePath() + "]");
    }
  }

  /**
   * Returns the XAR file to generate, based on an optional classifier
   *
   * @param java.io.File basedir Project target directory
   * @param java.io.File finalName The name of the XAR file
   * @param java.lang.String classifier An optional classifier
   * @return java.io.File Location where to generate the output XAR file
   */
  protected static File getXarFile(File basedir, String finalName, String classifier) {
    if (null == classifier || classifier.length() <= 0) {
      return new File(basedir, finalName + ".xar");
    }
    return new File(basedir, finalName + (classifier.startsWith("-") ? "" : "-") + classifier + ".xar");
  }

  /**
   * Returns the Uber-XAR file to generate, based on an optional classifier
   *
   * @param java.io.File basedir Project target directory
   * @param java.io.File finalName The name of the XAR file
   * @param java.lang.String classifier An optional classifier
   * @return java.io.File Location where to generate the output Uber-XAR file
   */
  protected static File getUberXarFile(File basedir, String finalName, String classifier) {
    if (null == classifier || classifier.length() <= 0) {
      return new File(basedir, finalName + "-uber.xar");
    }
    return new File(basedir, finalName + (classifier.startsWith("-") ? "" : "-") + classifier + "-uber.xar");
  }
}
