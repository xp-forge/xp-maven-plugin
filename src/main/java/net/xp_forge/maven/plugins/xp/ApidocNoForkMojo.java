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
 * Important: the [doclet] runner cannot (yet) generate API documentation
 * from *.xp sources
 *
 * This goal functions the same as the "apidoc-no-fork" goal but does not fork
 * the build and is suitable for attaching to the build lifecycle
 *
 * @goal apidoc-no-fork
 * @phase package
 * @requiresDependencyResolution compile
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
   * Get skip setting
   *
   * @return boolean
   */
  protected boolean isSkip() {
    return this.skip;
  }

  /**
   * Get attach setting
   *
   * @return boolean
   */
  protected boolean isAttach() {
    return this.attach;
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
    if (this.isSkip()) {
      getLog().info("Not generating apidoc (xp.apidoc.skip)");
      return;
    }

    // Pom artifacts dont have API docs
    if (this.project.getPackaging().equals("pom")) {
      getLog().info("Cannot generate apidoc for [pom] projects; silently skipping");
      return;
    }

    // Get output file
    File outputFile= this.getOutputFile();

    // Debug info
    getLog().info("Output file [" + outputFile + "]");
    getLog().info("Format      [" + this.format + "]");
    getLog().info("Sourcepaths [" + (null == this.sourcepaths ? "n/a" : this.sourcepaths) + "]");
    getLog().info("Names       [" + (null == this.names ? "n/a" : this.names) + "]");
    getLog().info("Attach      [" + (this.isAttach() ? "yes" : "no") + "]");

    // Extract doclet.xar from resources
    File docletFile= new File(this.outputDirectory, ".runtime" + File.separator + "doclet" + File.separator + "doclet.xar");
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

    // Add default sourcepaths (src/main/php)
    input.addSourcepath(new File(this.basedir, "src" + File.separator + "main" + File.separator + "php"));

    // ATM, the [doclet] runner cannot handle *.xp files ;skipping
    //input.addSourcepath(new File(this.basedir, "src" + File.separator + "main" + File.separator + "xp"));

    // Add user-defined sourcepaths
    if (null != this.sourcepaths) {
      for (File sourcepath : this.sourcepaths) {
        input.addSourcepath(sourcepath);
      }
    }

    // Check no sourcepaths found (E.g.: XSL-only projects); early return
    if (input.sourcepaths.isEmpty()) {
      getLog().warn("No sourcepaths found this project; silently skipping");
      return;
    }

    // No names configured, try to calculate them
    if (null == this.names || this.names.isEmpty()) {
      try {
        this.names= this.calculateNamesFromDirectoryStructure(input.sourcepaths);
      } catch (IOException ex) {
        throw new MojoExecutionException("Cannot calculate names from directory structure", ex);
      }

      // Check no names found
      if (this.names.isEmpty()) {
        throw new MojoExecutionException(
          "No names found from directory structure; you may want to specify them yourself via ${names} configuration"
        );
      }
      getLog().info("Calculated names [" + this.names + "]");
    }

    for (String name : this.names) {
      input.addName(name);
    }

    // Special case: self-bootstrap for loading XP-Framework itself
    // A bit hackish atm
    if (
        this.project.getGroupId().equals(XP_FRAMEWORK_GROUP_ID) &&
        (
          this.project.getArtifactId().equals(CORE_ARTIFACT_ID) ||
          this.project.getArtifactId().equals(TOOLS_ARTIFACT_ID)
        )
      ) {

      // Locate parent project
      File parentProjectDirectory= this.basedir.getParentFile();

      // Add classpaths
      input.addClasspath(new File(parentProjectDirectory, "core/src/main/php"));
      input.addSourcepath(new File(parentProjectDirectory, "core/src/main/php"));
    }

    // Add project dependecies as sourcepaths
    input.addSourcepath(this.getArtifacts(true));

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
    if (this.isAttach()) {
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

  /**
   * Try to detect all exposed names (namespaces) from the directory layout
   *
   *
   * @param  java.util.List<java.io.File> directories
   * @return java.util.List<java.lang.String>
   * @throws java.io.IOException when cannot read the contents of the directories
   */
  private List<String> calculateNamesFromDirectoryStructure(List<File> directories) throws IOException {
    List<String> retVal= new ArrayList<String>();
    for (File directory : directories) {
      retVal.addAll(this.calculateNamesFromDirectoryStructure(directory));
    }
    return retVal;
  }

  /**
   * Try to detect all exposed names (namespaces) from the directory layout
   *
   *
   * @param  java.io.File directory E.g. /path/to/src/main/php
   * @return java.util.List<java.lang.String>
   * @throws java.io.IOException when cannot read the contents of the directory
   */
  private List<String> calculateNamesFromDirectoryStructure(File directory) throws IOException {
    return this.calculateNamesFromDirectoryStructure(directory, "");
  }

  /**
   * Try to detect all exposed names (namespaces) from the directory layout
   *
   *
   * @param  java.io.File directory E.g. /path/to/src/main/php
   * @param  java.lang.String prefix E.g. "org.company"
   * @return java.util.List<java.lang.String>
   * @throws java.io.IOException when cannot read the contents of the directory
   */
  private List<String> calculateNamesFromDirectoryStructure(File directory, String prefix) throws IOException {

    // Init list of found names
    List<String> retVal= new ArrayList<String>();

    // Sanity check
    if (null == directory || !directory.exists()) return retVal;

    // Not a directory
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("[" + directory + "] is not a directory");
    }

    // List directory contents
    File[] entries= directory.listFiles();
    if (null == entries) {
      throw new IOException("Failed to list contents of directory [" + directory + "]");
    }

    // Analyze directory contents
    for (File entry : entries) {
      String entryName= entry.getName();

      // At least one *class* file found; don't go any deeper
      if (!entry.isDirectory() && (entryName.endsWith(".class.php") || entryName.endsWith(".xp"))) {
        retVal.clear();
        retVal.add(prefix + "**");
        return retVal;
      }

      // Delve deeper
      if (entry.isDirectory()) {
        retVal.addAll(this.calculateNamesFromDirectoryStructure(entry, prefix + entryName + "."));
      }
    }

    // Return all collected names
    return retVal;
  }
}
