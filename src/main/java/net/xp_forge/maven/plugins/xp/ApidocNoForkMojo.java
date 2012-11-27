/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
import net.xp_forge.maven.plugins.xp.logging.LogLogger;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.runners.xp.DocletRunner;
import net.xp_forge.maven.plugins.xp.exec.input.xp.DocletRunnerInput;

/**
 * Generate and pack project API documentation
 *
 * This goal functions the same as the "apidoc-no-fork" goal but does not fork
 * the build and is suitable for attaching to the build lifecycle
 *
 * @goal apidoc-no-fork
 * @phase package
 * @since 3.2.0
 */
public class ApidocNoForkMojo extends AbstractXpMojo {

  /**
   * Skip this goal by configuration
   *
   * @parameter expression="${xp.apidoc.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * Specify what archiver to use. There are 2 options:
   * - zip
   * - xar
   *
   * @parameter expression="${xp.source.format}" default-value="xar"
   * @required
   */
  protected String format;

  /**
   * Adds path to source path
   *
   * The -sp argument for the doclet runner
   *
   * @parameter
   */
  protected List<File> sourcepaths;

  /**
   * List of names to include
   *
   * @parameter
   */
  protected List<String> names;

  /**
   * Specifies whether or not to attach the artifact to the project
   *
   * @parameter expression="${xp.apidoc.attach}" default-value="true"
   * @required
   */
  protected boolean attach;

  /**
   * Name of the generated XAR
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  protected String finalName;

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
    ArchiveUtils.enableLogging(new LogLogger(getLog()));

    // Skip apidoc
    if (this.skip) {
      getLog().info("Not generating apidoc (xp.apidoc.skip)");
      return;
    }

    // Debug info
    getLog().info("Format      [" + this.format + "]");
    getLog().info("Sourcepaths [" + (null == this.sourcepaths ? "n/a" : this.sourcepaths) + "]");
    getLog().info("Names       [" + (null == this.names ? "n/a" : this.names) + "]");
    getLog().info("Attach      [" + (this.attach ? "yes" : "no") + "]");

    // Get output file
    File outputFile= this.getOutputFile();
    getLog().debug("Output file [" + outputFile + "]");

    // Extract doclet.xar from resources
    File docletFile= new File(this.outputDirectory, ".doclet" + File.separator + "doclet.xar");
    try {
      getLog().debug(" - Extracting doclet.xar from resources");
      ExecuteUtils.saveResource("/net/xp_forge/doclet/doclet.xar", docletFile);

    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot extract doclet.xar to [" + docletFile + "]", ex);
    }

    // Prepare [doclet] input
    DocletRunnerInput input= new DocletRunnerInput("net.xp_forge.apidoc.Doclet");

    // Add classpaths
    input.addClasspath(this.getArtifacts(false));
    input.addClasspath(docletFile);

    // Add default sourcepaths (src/main/php, src/main/xp)
    input.addSourcepath(new File(this.basedir, "src" + File.separator + "main" + File.separator + "php"));
    input.addSourcepath(new File(this.basedir, "src" + File.separator + "main" + File.separator + "xp"));

    // Add user-defined sourcepaths
    if (null != this.sourcepaths) {
      for (File sourcepath : this.sourcepaths) {
        input.addSourcepath(sourcepath);
      }
    }

    // Add names
    if (null == this.names) {
      input.addName(this.project.getGroupId() + ".**");
    } else {
      for (String name : this.names) {
        input.addName(name);
      }
    }

    // Prepare output directory
    File apidocDirectory= new File(this.outputDirectory, "apidoc");
    apidocDirectory.mkdirs();

    // Add options for the Doclet class
    input.addDocletOption("output", apidocDirectory.getAbsolutePath());
    input.addDocletOption("api",    this.project.getGroupId() + ":" + this.project.getArtifactId() + "#" + this.project.getVersion());
    input.addDocletOption("css",    "res://css/default.css");
    input.addDocletOption("gen",    "Version " + this.project.getVersion());

    // Configure [unittest] runner
    File executable= new File(this.getRunnersDirectory(), "doclet");
    DocletRunner runner= new DocletRunner(executable, input);
    runner.setLog(getLog());

    // Set runner working directory
    runner.setWorkingDirectory(this.basedir);

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [doclet] runner failed", ex);
    }

    // Load archiver
    AbstractArchiver archiver= ArchiveUtils.getArchiver(outputFile);

    // Init archive contents
    DefaultFileSet fileSet= new DefaultFileSet();
    fileSet.setDirectory(apidocDirectory);
    fileSet.setPrefix(this.finalName + "-apidoc/");

    // Add filtered resources to archive
    archiver.addFileSet(fileSet);

    // Save archive to output file
    try {
      getLog().debug(" - Creating apidoc archive [" + outputFile + "]");
      outputFile.delete();
      archiver.createArchive();
    } catch (Exception ex) {
      throw new MojoExecutionException(
        "Cannot create [" + this.format + "] to [" + outputFile + "]", ex
      );
    }

    // Attach generated archive as project artifact
    if (this.attach) {
      this.projectHelper.attachArtifact(this.project, this.format, "apidoc", outputFile);
    }
  }

  /**
   * Returns the output file, based on finalName, classifier and format
   *
   * @return java.io.File Location where to generate the output XAR file
   */
  private File getOutputFile() {
    return new File(
      this.outputDirectory,
      this.finalName + "-apidoc." + this.format
    );
  }
}
