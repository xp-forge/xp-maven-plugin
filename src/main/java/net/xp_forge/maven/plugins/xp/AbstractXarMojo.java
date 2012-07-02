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
import java.util.Set;
import java.util.Iterator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.xar.XarArchive;
import net.xp_forge.maven.plugins.xp.util.XarUtils;

/**
 * Build XAR files using java-xarlib
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

    // Create empty archive
    XarArchive archive= new XarArchive();

    // Add all files in classesDirectory to archive root
    XarUtils.addDirectory(archive, classesDirectory, null);

    // Save archive to output file
    try {
      archive.save(xarFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot save XAR archive to [" + xarFile + "]", ex);
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
   * @param  java.io.File xarFile Original XAR file without dependencies
   * @param  java.io.File uberXarFile Output Uber-XAR file location
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xar runner failed
   */
  protected void executeUberXar(File xarFile, File uberXarFile) throws MojoExecutionException {
    Iterator i;

    // Debug info
    getLog().info("Uber-XAR output file [" + uberXarFile + "]");

    // Create empty archive
    XarArchive archive= new XarArchive();

    // Add contents of xarFile
    try {
      XarUtils.addArchive(archive, new XarArchive(xarFile), null);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot find archive [" + xarFile + "]", ex);
    }

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
      try {
        XarUtils.addArchive(archive, new XarArchive(artifact.getFile()), null);
      } catch (IOException ex) {
        throw new MojoExecutionException("Cannot find archive [" + artifact.getFile() + "]", ex);
      }
    }

    // Save archive to output file
    try {
      archive.save(uberXarFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot save XAR archive to [" + uberXarFile + "]", ex);
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
