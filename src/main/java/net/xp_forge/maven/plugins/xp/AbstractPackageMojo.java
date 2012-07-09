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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.archiver.ArchiverException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.archiver.xar.XarArchiver;
import net.xp_forge.maven.plugins.xp.archiver.xar.XarUnArchiver;

/**
 * Build project package artifact
 *
 */
public abstract class AbstractPackageMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {
  Archiver archiver;
  List<String> pthEntries;

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
   * Specify what archiver to use. There are 2 options:
   * - zip
   * - xar
   *
   * @parameter expression="${xp.package.format}" default-value="xar"
   * @required
   */
  protected String format;

  /**
   * Specify what type of artifact to build. There are 2 options:
   * - lib
   * - app
   *
   * @parameter expression="${xp.package.type}" default-value="lib"
   * @required
   */
  protected String type;

  /**
   * Get location of classes files to include in the package
   *
   */
  protected abstract File getSourcesDirectory();

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {
    getLog().info("Classes directory [" + this.getSourcesDirectory() + "]");
    getLog().info("Output file       [" + this.getOutputFile() + "]");
    getLog().info("Pack dependencies [" + this.packDependencies + "]");
    getLog().info("Package type      [" + this.type + "]");
    getLog().info("Artifact format   [" + this.format + "]");

    // Init entries for the on-the-fly generated [project.pth] to be included in the archive
    this.pthEntries= new ArrayList<String>();

    // Load archiver
    this.archiver= this.getArchiver();

    // Pack project sources
    this.packSources();

    // Pack project dependencies
    this.packDependencies();

    // Pack application resources
    this.packApplicationResources();

    // Save archive to output file
    try {
      getLog().debug(" - Creating archive to [" + this.getOutputFile() + "]");
      this.archiver.setDestFile(this.getOutputFile());
      this.archiver.createArchive();
    } catch (Exception ex) {
      throw new MojoExecutionException(
        "Cannot create [" + this.format + "] archive to [" + this.getOutputFile() + "]", ex
      );
    }
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
   * Get archiver based on specified format
   *
   * @return org.codehaus.plexus.archiver.Archiver
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private Archiver getArchiver() throws MojoExecutionException {
    if (this.format.equals("xar")) {
      return new XarArchiver();
    }

    if (format.equals("zip")) {
      return new ZipArchiver();
    }

    // Invalid package type
    throw new MojoExecutionException(
      "${xp.package.format} has an invalid value [" + this.format + "]"
    );
  }

  /**
   * Get unarchiver for the specified Artifact
   *
   * @param  org.apache.maven.artifact.Artifact artifact
   * @return org.codehaus.plexus.archiver.UnArchiver
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private UnArchiver getUnArchiver(Artifact artifact) throws MojoExecutionException {
    String format= artifact.getType();

    if (format.equals("xar")) {
      return new XarUnArchiver();
    }

    if (equals("zip")) {
      return new ZipUnArchiver();
    }

    // Invalid artifact type
    throw new MojoExecutionException(
      "Artifact [" + artifact.getFile() + "] has an invalid type [" + format + "]"
    );
  }

  /**
   * Dump artifact contents in the specified directory
   *
   * @param  org.apache.maven.artifact.Artifact artifact
   * @param  java.io.File destDirectory
   * @throw  org.codehaus.plexus.archiver.ArchiverException
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void dumpArtifact(Artifact artifact, File destDirectory) throws ArchiverException, MojoExecutionException {

    // Create destination directory if not exists
    if (!destDirectory.exists()) {
      destDirectory.mkdirs();
    }

    // Dump artifact contents
    UnArchiver unarchiver= this.getUnArchiver(artifact);
    unarchiver.setSourceFile(artifact.getFile());
    unarchiver.setDestDirectory(destDirectory);
    unarchiver.setOverwrite(null != artifact.getClassifier() && artifact.getClassifier().equals("patch"));
    unarchiver.extract();
  }

  /**
   * Pach project sources into archive
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packSources() throws MojoExecutionException {
    File sourcesDirectory= this.getSourcesDirectory();

    // Pack sources to /
    if (this.type.equals("lib")) {
      getLog().debug(" - Add directory [" + sourcesDirectory + "] to [/]");
      this.archiver.addDirectory(sourcesDirectory);
      this.pthEntries.add(".");
      return;
    }

    // Pack sources to /classes/
    if (this.type.equals("app")) {
      getLog().debug(" - Add directory [" + sourcesDirectory + "] to [/classes/]");
      this.archiver.addDirectory(sourcesDirectory, "classes/");
      this.pthEntries.add("classes");
      return;

    }

    // Invalid package type
    throw new MojoExecutionException(
      "${xp.package.type} has an invalid value [" + this.type + "]"
    );
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
      getLog().info("Including dependencies");
      for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
        getLog().info(" - " + artifact.getType() + " [" + artifact.getFile() + "]");

        getLog().debug(" - Add file [" + artifact.getFile() + "] to [/libs/]");
        this.archiver.addFile(artifact.getFile(), "libs/" + artifact.getFile().getName());
        pthEntries.add("libs/" + artifact.getFile().getName());
      }

      // Done
      return;
    }

    // Include dependencies into the [lib] folder
    if (this.packDependencies.equals("merge")) {
      getLog().info("Merging dependencies");

      File dumpDirectory= new File(this.outputDirectory, "dependencies.tmp");
      for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
        getLog().info(" - " + artifact.getType() + " [" + artifact.getFile() + "]");

        try {
          this.dumpArtifact(artifact, dumpDirectory);
        } catch (ArchiverException ex) {
          throw new MojoExecutionException("Cannot dump artifact [" + artifact.getFile() + "] into [" + dumpDirectory + "]");
        }
      }

      // Add dump dumpDirectory to archive
      if (this.type.equals("lib")) {
        getLog().debug(" - Add directory [" + dumpDirectory + "] to [/]");
        archiver.addDirectory(dumpDirectory);
        pthEntries.add(".");

      } else {
        getLog().debug(" - Add directory [" + dumpDirectory + "] to [/classes/]");
        archiver.addDirectory(dumpDirectory, "classes/");
        pthEntries.add("classes");
      }

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
    if (!this.type.equals("app")) return;

    // Add application directories
    getLog().info("Adding application resources");
    File mainDir= new File(this.project.getBuild().getSourceDirectory()).getParentFile();
    for (String appDirName : Arrays.asList("doc_root", "etc", "xsl")) {

      // If app dir does not exist; skip it
      File appDir= new File(mainDir, appDirName);
      if (!appDir.exists()) continue;

      // Add contents to archive
      getLog().debug(" - Add directory [" + appDir + "] to [/" + appDirName + "/]");
      this.archiver.addDirectory(appDir, appDirName + "/");
    }

    // On-the-fly generate a "project.pth" file and add it to archive
    getLog().info("Adding on-the-fly created [project.pth] to archive");
    File pthFile= new File(this.outputDirectory, "project.pth-package");
    try {
      FileUtils.setFileContents(pthFile, StringUtils.join(this.pthEntries, "\n"));
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]");
    }

    getLog().debug(" - Add file [" + pthFile + "] to [/]");
    this.archiver.addFile(pthFile, "project.pth");
  }
}
