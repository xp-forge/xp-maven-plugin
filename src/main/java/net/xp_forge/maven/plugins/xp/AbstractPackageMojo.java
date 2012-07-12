/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.ArchiverException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;

/**
 * Build project package artifact
 *
 */
public abstract class AbstractPackageMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {
  Archiver archiver;

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
   * Packing strategy: specify what type of artifact to build. There are 2 options:
   * - lib
   * - app
   *
   * @parameter expression="${xp.package.strategy}" default-value="lib"
   * @required
   */
  protected String strategy;

  /**
   * Specify what archiver to use. There are 2 options:
   * - zip
   * - xar
   *
   * @parameter expression="${xp.package.format}" default-value="xar"
   * @required
   */
  protected String format;

  /**
   * Specify how to pack dependencies. There are 3 options:
   * - ignore  - dependencies are ignored when building the package artifact
   * - include - dependencies are included in the package artifact in "lib" directory
   * - merge   - dependencies are merged in the package artifact
   *
   * @parameter expression="${xp.package.packDependencies}" default-value="ignore"
   * @required
   */
  protected String packDependencies;

  /**
   * Specify if XP-artifacts (core & tools) and the XP-runners should also be packed
   *
   * Bootstrap will be packed inside /runtime/bootstrap
   * XP-artifacts will be packed inside /runtime/lib
   *
   * @parameter expression="${xp.package.packRuntime}" default-value="false"
   * @required
   */
  protected boolean packRuntime;

  /**
   * Get location of compiled files (.class.php) to include in the package
   *
   * @return java.io.File
   */
  protected abstract File getSourcesDirectory();

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {
    File outputFile= this.getOutputFile();

    getLog().info("Classes directory  [" + this.getSourcesDirectory() + "]");
    getLog().info("Output file        [" + outputFile + "]");
    getLog().info("Pack runtime       [" + (this.packRuntime ? "yes" : "no") + "]");
    getLog().info("Pack dependencies  [" + this.packDependencies + "]");
    getLog().info("Packaging strategy [" + this.strategy + "]");
    getLog().info("Artifact format    [" + this.format + "]");

    // Load archiver
    this.archiver= ArchiveUtils.getArchiver(outputFile);

    // Package library
    if (this.strategy.equals("lib")) {

      // Pack project classes
      this.packClasses(null);

    // Package application
    } else if (this.strategy.equals("app")) {

      // Pack project classes
      this.packClasses("classes/");

      // Pack XP-runtime
      this.packRuntime();

      // Pack project dependencies
      this.packDependencies();

      // Pack application resources
      this.packApplicationResources();

    // Invalid packing strategy
    } else{
      throw new MojoExecutionException(
        "${xp.package.strategy} has an invalid value [" + this.strategy + "]"
      );
    }

    // Save archive to output file
    try {
      getLog().debug(" - Creating archive [" + outputFile + "]");
      this.archiver.createArchive();
    } catch (Exception ex) {
      throw new MojoExecutionException(
        "Cannot create [" + this.format + "] to [" + outputFile + "]", ex
      );
    }

    // Attach generated archive as project artifact
    this.project.getArtifact().setFile(outputFile);
  }

  /**
   * Returns the output file, based on finalName, classifier and format
   *
   * @return java.io.File Location where to generate the output XAR file
   */
  private File getOutputFile() {
    if (null == this.classifier || this.classifier.length() <= 0) {
      return new File(this.outputDirectory, this.finalName + "." + this.format);
    }
    return new File(
      this.outputDirectory,
      this.finalName +
      (null != this.classifier && this.classifier.startsWith("-") ? "" : "-") + this.classifier +
      "." + this.format
    );
  }

  /**
   * Pack project sources into archive
   *
   * @param  java.lang.String prefix
   * @return void
   */
  private void packClasses(String prefix) {
    File sourcesDirectory= this.getSourcesDirectory();

    getLog().debug(" - Add directory [" + sourcesDirectory + "] to [" + (null == prefix ? "/" : prefix) + "]");
    if (null == prefix) {
      this.archiver.addDirectory(sourcesDirectory);
    } else {
      this.archiver.addDirectory(sourcesDirectory, prefix);
    }
  }

  /**
   * Pack XP-runtime
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packRuntime() throws MojoExecutionException {
    if (!this.packRuntime) return;
    getLog().info("Packing XP-runtime");

    // Init [runtime.pth] entries
    List<String> pthEntries= new ArrayList<String>();

    // Locate "core" and "tools" artifacts
    Artifact coreArtifact= this.findArtifact("net.xp-framework", "core");
    if (null == coreArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
    }

    Artifact toolsArtifact= this.findArtifact("net.xp-framework", "tools");
    if (null == toolsArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
    }

    // Pack XP-artifacts
    getLog().debug(" - Add file [" + coreArtifact.getFile() + "] to [runtime/lib]");
    this.archiver.addFile(coreArtifact.getFile(), "runtime/lib/" + coreArtifact.getFile().getName());
    pthEntries.add("runtime/lib/" + coreArtifact.getFile().getName());

    getLog().debug(" - Add file [" + toolsArtifact.getFile() + "] to [runtime/lib]");
    this.archiver.addFile(toolsArtifact.getFile(), "runtime/lib/" + toolsArtifact.getFile().getName());
    pthEntries.add("runtime/lib/" + toolsArtifact.getFile().getName());

    // Pack bootstrap
    try {

      // Set temp directory
      FileUtils.setTempDirectory(new File(this.outputDirectory, "package.tmp"));

      Map<String, String> entries= new HashMap<String, String>();
      entries.put("lang.base.php", "runtime/bootstrap/lang.base.php");
      ArchiveUtils.copyArchiveEntries(coreArtifact, this.archiver, entries);

      entries.clear();
      entries.put("tools/class.php", "runtime/bootstrap/tools/class.php");
      entries.put("tools/web.php", "runtime/bootstrap/tools/web.php");
      entries.put("tools/xar.php", "runtime/bootstrap/tools/xar.php");
      ArchiveUtils.copyArchiveEntries(toolsArtifact, this.archiver, entries);

    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot pack XP-runtime", ex);
    }

    // On-the-fly generate a [runtime.pth] file and add it to archive
    getLog().info("Packing on-the-fly created [runtime.pth] to archive");
    File pthFile= new File(this.outputDirectory, "runtime.pth-package");
    try {
      FileUtils.setFileContents(pthFile, StringUtils.join(pthEntries, "\n"));
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]");
    }

    getLog().debug(" - Add file [" + pthFile + "] to [runtime.pth]");
    this.archiver.addFile(pthFile, "runtime.pth");
  }

  /**
   * Pach project dependencies into archive
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packDependencies() throws MojoExecutionException {

    // Ignore dependencies
    if (this.packDependencies.equals("ignore")) return;

    // Include dependencies into the "lib" folder
    if (this.packDependencies.equals("include")) {
      List<String> pthEntries= new ArrayList<String>();

      getLog().info("Including dependencies");
      for (Artifact artifact : (Iterable<Artifact>)this.getArtifacts(false)) {
        getLog().info(" - " + artifact.getType() + " [" + artifact.getFile() + "]");

        getLog().debug(" - Add file [" + artifact.getFile() + "] to [libs/]");
        this.archiver.addFile(artifact.getFile(), "libs/" + artifact.getFile().getName());
        pthEntries.add("libs/" + artifact.getFile().getName());
      }

      // Add libs to [project.pth]
      pthEntries.add("classes");
      pthEntries.add("lib");

      // On-the-fly generate a "project.pth" file and add it to archive
      getLog().info("Packing on-the-fly created [project.pth] to archive");
      File pthFile= new File(this.outputDirectory, "project.pth-package");
      try {
        FileUtils.setFileContents(pthFile, StringUtils.join(pthEntries, "\n"));
      } catch (IOException ex) {
        throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]");
      }

      getLog().debug(" - Add file [" + pthFile + "] to [project.pth]");
      this.archiver.addFile(pthFile, "project.pth");

      // Done
      return;
    }

    // Include dependencies into the [lib] folder
    if (this.packDependencies.equals("merge")) {
      getLog().info("Merging dependencies");

      File tmpDirectory= new File(new File(this.outputDirectory, "package.tmp"), "dependencies");
      for (Artifact artifact : (Iterable<Artifact>)this.getArtifacts(false)) {
        getLog().info(" - " + artifact.getType() + " [" + artifact.getFile() + "]");

        try {
          ArchiveUtils.dumpArtifact(artifact, tmpDirectory, artifact.getClassifier().equals("patch"));
        } catch (ArchiverException ex) {
          throw new MojoExecutionException("Cannot dump artifact [" + artifact.getFile() + "] into [" + tmpDirectory + "]");
        }
      }

      // Add dump dumpDirectory to archive
      getLog().debug(" - Add directory [" + tmpDirectory + "] to [classes/]");
      archiver.addDirectory(tmpDirectory, "classes/");

      // Done
      return;
    }

    // Invalid settings for ${xp.package.packDependencies}
    throw new MojoExecutionException(
      "${xp.package.packDependencies} has an invalid value [" + this.packDependencies + "]"
    );
  }

  /**
   * Pack application resources: "doc_root", "etc", "xsl"
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packApplicationResources() throws MojoExecutionException {

    // Add application directories
    getLog().info("Packing application resources");
    File mainDir= new File(this.project.getBuild().getSourceDirectory()).getParentFile();
    for (String appDirName : Arrays.asList("doc_root", "etc", "xsl")) {

      // If app dir does not exist; skip it
      File appDir= new File(mainDir, appDirName);
      if (!appDir.exists()) continue;

      // Add contents to archive
      getLog().debug(" - Add directory [" + appDir + "] to [" + appDirName + "/]");
      this.archiver.addDirectory(appDir, appDirName + "/");
    }
  }
}
