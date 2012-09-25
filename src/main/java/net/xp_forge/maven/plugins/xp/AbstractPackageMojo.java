/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;

/**
 * Build project package artifact
 *
 */
public abstract class AbstractPackageMojo extends AbstractXpMojo {
  private Archiver archiver;

  // [project.pth] entries (for [app] packaging)
  private List<String> projectPthEntries;

  /**
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  protected boolean skip;

  /**
   * Name of the generated XAR
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  protected String finalName;

  /**
   * Specify what archiver to use. There are 2 options:
   * - zip
   * - xar
   *
   * @parameter expression="${project.packaging}" default-value="xar"
   * @required
   */
  protected String packaging;

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
   * Specify if dependencies will also be packed
   *
   * For "app" stragegy, dependencies will be packed to "lib"
   * For "lib" stragegy, dependencies will be merged to "/"
   *
   * @parameter expression="${xp.package.packDependencies}" default-value="false"
   * @required
   */
  protected boolean packDependencies;

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
  protected abstract File getClassesDirectory();

  /**
   * Get artifact classifier
   *
   * @return java.lang.String
   */
  protected abstract String getClassifier();

  /**
   * Get strategy
   *
   * @return java.lang.String
   */
  protected abstract String getStrategy();

  /**
   * Get packaging
   *
   * @return java.lang.String
   */
  protected abstract String getPackaging();

  /**
   * Get packDependencies
   *
   * @return boolean
   */
  protected abstract boolean getPackDependencies();

  /**
   * Get packRuntime
   *
   * @return boolean
   */
  protected abstract boolean getPackRuntime();

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {
    String classifier        = this.getClassifier();
    File outputFile          = this.getOutputFile();
    String strategy          = this.getStrategy();
    String packaging         = this.getPackaging();
    boolean packDependencies = this.getPackDependencies();
    boolean packRuntime      = this.getPackRuntime();

    getLog().info("Classes directory  [" + this.getClassesDirectory() + "]");
    getLog().info("Output file        [" + outputFile + "]");
    getLog().info("Classifier         [" + (null == classifier ? "n/a" : classifier) + "]");
    getLog().info("Packaging format   [" + packaging + "]");
    getLog().info("Packaging strategy [" + strategy + "]");
    getLog().info("Pack runtime       [" + (packRuntime ? "yes" : "no") + "]");
    getLog().info("Pack dependencies  [" + (packDependencies ? "yes" : "no") + "]");

    // Check ${maven.test.skip} is active and we're trying to package the "tests" artifact
    if (this.skip && null != classifier && classifier.equals("tests")) {
      getLog().info("Skipping packaging of tests (maven.test.skip)");
      return;
    }

    // Load archiver
    this.archiver= ArchiveUtils.getArchiver(outputFile);

    // Init [project.pth] entries
    this.projectPthEntries= new ArrayList<String>();
    this.projectPthEntries.add("classes");

    // Package library
    if (strategy.equals("lib")) {
      this.packClasses(null);
      if (packDependencies) this.mergeDependencies();

    // Package application
    } else if (strategy.equals("app")) {
      this.packClasses("classes/");
      this.packApplicationResources();
      if (packRuntime) this.includeRuntime();
      if (packDependencies) this.includeDependencies();
      this.packProjectPth();

    // Invalid packing strategy
    } else{
      throw new MojoExecutionException(
        "${xp.package.strategy} has an invalid value [" + strategy + "]"
      );
    }

    // Save archive to output file
    try {
      getLog().debug(" - Creating archive [" + outputFile + "]");
      this.archiver.createArchive();
    } catch (Exception ex) {
      throw new MojoExecutionException(
        "Cannot create [" + packaging + "] to [" + outputFile + "]", ex
      );
    }

    // Attach/set generated archive as project artifact
    if (null != classifier) {
      this.projectHelper.attachArtifact(this.project, packaging, classifier, outputFile);
    } else {
      this.project.getArtifact().setFile(outputFile);
    }
  }

  /**
   * Returns the output file, based on finalName, classifier and packaging
   *
   * @return java.io.File Location where to generate the output XAR file
   */
  private File getOutputFile() {
    String classifier = this.getClassifier();
    String packaging  = this.getPackaging();

    if (null == classifier || classifier.length() <= 0) {
      return new File(this.outputDirectory, this.finalName + "." + packaging);
    }
    return new File(
      this.outputDirectory,
      this.finalName +
      (classifier.startsWith("-") ? "" : "-") + classifier +
      "." + packaging
    );
  }

  /**
   * Pack project classes into archive
   *
   * @param  java.lang.String prefix
   * @return void
   */
  private void packClasses(String prefix) {
    File classesDirectory= this.getClassesDirectory();

    // Check classes directory is empty
    if (!classesDirectory.exists()) {
      getLog().warn(" - Classes directory [" + classesDirectory + "] does not exist");
      return;
    }

    getLog().debug(" - Add classes directory [" + classesDirectory + "] to [" + (null == prefix ? "/" : prefix) + "]");
    if (null == prefix) {
      this.archiver.addDirectory(classesDirectory);
    } else {
      this.archiver.addDirectory(classesDirectory, prefix);
    }
  }

  /**
   * Include XP-runtime
   *
   * - Include bootstrap files into "runtime/bootstrap"
   * - Include XP-artifacts into "runtime/lib"
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void includeRuntime() throws MojoExecutionException {
    getLog().info("Including XP-runtime");

    // Locate CORE_ARTIFACT_ID and TOOLS_ARTIFACT_ID artifacts
    Artifact coreArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, CORE_ARTIFACT_ID);
    if (null == coreArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
    }

    Artifact toolsArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, TOOLS_ARTIFACT_ID);
    if (null == toolsArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
    }

    // Pack XP-artifacts
    getLog().debug(" - Add file [" + coreArtifact.getFile() + "] to [runtime/lib]");
    this.archiver.addFile(coreArtifact.getFile(), "runtime/lib/" + coreArtifact.getFile().getName());
    this.projectPthEntries.add("runtime/lib/" + coreArtifact.getFile().getName());

    getLog().debug(" - Add file [" + toolsArtifact.getFile() + "] to [runtime/lib]");
    this.archiver.addFile(toolsArtifact.getFile(), "runtime/lib/" + toolsArtifact.getFile().getName());
    this.projectPthEntries.add("runtime/lib/" + toolsArtifact.getFile().getName());

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
  }

  /**
   * Include project dependencies into "lib"
   *
   * @return void
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void includeDependencies() throws MojoExecutionException {
    getLog().info("Including dependencies");
    for (Artifact artifact : (Iterable<Artifact>)this.getArtifacts(false)) {

      // Ignore non-xar artifacts
      if (!artifact.getType().equals("xar")) {
        getLog().info(" - Ignore non-xar dependency [" + artifact.getFile() + "]");
        continue;

      } else {
        getLog().info(" + Add dependency [" + artifact.getFile() + "]");
      }

      getLog().debug(" - Add file [" + artifact.getFile() + "] to [libs/]");
      this.archiver.addFile(artifact.getFile(), "libs/" + artifact.getFile().getName());

      if (null != artifact.getClassifier() && artifact.getClassifier().equals("patch")) {
        this.projectPthEntries.add("!libs/" + artifact.getFile().getName());
      } else {
        this.projectPthEntries.add("libs/" + artifact.getFile().getName());
      }
    }
  }

  /**
   * Merge project dependencies into archive root
   *
   * @return void
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void mergeDependencies() throws MojoExecutionException {
    getLog().info("Merging dependencies");

    File tmpDirectory= new File(new File(this.outputDirectory, "package.tmp"), "dependencies");
    for (Artifact artifact : (Iterable<Artifact>)this.getArtifacts(false)) {

      // Ignore non-xar artifacts
      if (!artifact.getType().equals("xar")) {
        getLog().info(" - Ignore non-xar dependency [" + artifact.getFile() + "]");
        continue;

      } else {
        getLog().info(" + Merge dependency [" + artifact.getFile() + "]");
      }

      try {
        boolean isPatch= null != artifact.getClassifier() && artifact.getClassifier().equals("patch");
        ArchiveUtils.dumpArtifact(artifact, tmpDirectory, isPatch);
      } catch (ArchiverException ex) {
        throw new MojoExecutionException(
          "Cannot dump artifact [" + artifact.getFile() + "] into [" + tmpDirectory + "]", ex
        );
      }
    }

    // Add dump dumpDirectory to archive
    getLog().debug(" - Add directory [" + tmpDirectory + "] to [/]");
    archiver.addDirectory(tmpDirectory);
  }

  /**
   * Pack application resources: "doc_root", "etc", "xsl" into archive root
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packApplicationResources() throws MojoExecutionException {
    getLog().info("Including application resources");

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

  /**
   * Pack on-the-fly created [project.pth] with entries from this.projectPthEntries
   *
   * @throw  org.apache.maven.plugin.MojoExecutionException
   */
  private void packProjectPth() throws MojoExecutionException {
    getLog().info("Packing on-the-fly created [project.pth] to archive");
    File pthFile= new File(this.outputDirectory, "project.pth-package");
    try {
      FileUtils.setFileContents(pthFile, this.projectPthEntries);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]", ex);
    }

    getLog().debug(" - Add file [" + pthFile + "] to [project.pth]");
    this.archiver.addFile(pthFile, "project.pth");
  }
}
