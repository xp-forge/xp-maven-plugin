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
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.archiver.UnArchiver;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
import net.xp_forge.maven.plugins.xp.ini.IniProperties;

/**
 * Check for the presence of XP-Framework runners
 *
 * @goal validate
 * @requiresDependencyResolution compile
 */
public class ValidateMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {

    // Prepare XP-Framework runtime
    if (this.local) {
      this.setupUserRuntime();
    } else {
      this.setupLocalRuntime();
    }
    getLog().info("Using runners from [" + this.runnersDirectory.getAbsolutePath() + "]");
    this.project.getProperties().setProperty("xp.runtime.runners.directory", this.runnersDirectory.getAbsolutePath());

    // Alter default Maven settings
    this.alterSourceDirectories();
  }

  /**
   * Use XP-Framework runtime already installed on local machine
   * by searching for XP runners in PATH
   *
   * @return void
   * @throws import org.apache.maven.plugin.MojoExecutionException
   */
  private void setupUserRuntime() throws MojoExecutionException {
    getLog().debug("Looking for XP-Framework local runtime");
    try {
      this.runnersDirectory= ExecuteUtils.getExecutable("xp").getParentFile();
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot find XP Framework local runtime", ex);
    }
  }

  /**
   * Prepare our own XP-runtime in [target/.runtime]
   *
   * @return void
   * @throws import org.apache.maven.plugin.MojoExecutionException
   */
  private void setupLocalRuntime() throws MojoExecutionException {
    UnArchiver unArchiver;

    getLog().debug("Preparing XP-Framework runtime");
    File runtimeDirectory= new File(this.outputDirectory, ".runtime");

    File bootstrapDirectory= new File(runtimeDirectory, "bootstrap");
    this.runnersDirectory= new File(runtimeDirectory, "runners");

    // Init [boot.pth] entries
    List<String> pthEntries= new ArrayList<String>();
    pthEntries.add(bootstrapDirectory.getAbsolutePath());

    // Locate required XP-artifacts: core & tools
    Artifact coreArtifact= this.findArtifact("net.xp-framework", "core");
    if (null == coreArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
    }

    Artifact toolsArtifact = this.findArtifact("net.xp-framework", "tools");
    if (null == toolsArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
    }

    pthEntries.add(coreArtifact.getFile().getAbsolutePath());
    pthEntries.add(toolsArtifact.getFile().getAbsolutePath());

    // Locate optional XP-artifacts: language
    Artifact languageArtifact= this.findArtifact("net.xp-framework", "language");
    if (null != languageArtifact) {
      pthEntries.add(languageArtifact.getFile().getAbsolutePath());
    }

    // Unpack bootstrap
    unArchiver= ArchiveUtils.getUnArchiver(coreArtifact);
    unArchiver.extract("lang.base.php", bootstrapDirectory);

    File toolsDirectory= new File(bootstrapDirectory, "tools");
    unArchiver= ArchiveUtils.getUnArchiver(toolsArtifact);
    unArchiver.extract("tools/class.php", toolsDirectory);
    unArchiver.extract("tools/web.php", toolsDirectory);
    unArchiver.extract("tools/xar.php", toolsDirectory);

    // Create [target/bootstrap/boot.pth]
    File pthFile= new File(bootstrapDirectory, "boot.pth");
    try {
      FileUtils.setFileContents(pthFile, pthEntries, "#" + CREATED_BY_NOTICE);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot write [" + pthFile + "]", ex);
    }

    // Extract XP-runners
    try {
      getLog().debug(" - Extracting runners from resources");

      ExecuteUtils.saveRunner("xp", this.runnersDirectory);
      ExecuteUtils.saveRunner("xcc", this.runnersDirectory);
      ExecuteUtils.saveRunner("xar", this.runnersDirectory);
      ExecuteUtils.saveRunner("xpcli", this.runnersDirectory);
      ExecuteUtils.saveRunner("doclet", this.runnersDirectory);
      ExecuteUtils.saveRunner("unittest", this.runnersDirectory);

    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot extract XP-runners to [" + this.runnersDirectory + "]", ex);
    }

    // Set USE_XP
    IniProperties ini= new IniProperties();
    ini.setProperty("use", bootstrapDirectory.getAbsolutePath());

    // Set PHP executable and timezone
    this.setupPhp();
    ini.setProperty("runtime", "default", this.php.getAbsolutePath());

    this.setupTimezone();
    ini.setProperty("runtime", "date.timezone", this.timezone);

    // Dump ini file
    File iniFile= new File(this.runnersDirectory, "xp.ini");
    try {
      ini.setComment(CREATED_BY_NOTICE);
      ini.dump(iniFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot write [" + iniFile + "]", ex);
    }
  }

  /**
   * Locate PHP executable
   *
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void setupPhp() throws MojoExecutionException {
    getLog().debug("Identifying PHP executable");

    // ${xp.runtime.php.executable} is already set
    if (null != this.php) {
      getLog().debug(" - Provided by caller");

    // Check for PHP executable in PATH
    } else {
      try {
        getLog().debug(" - Inspecting PATH");
        this.php= ExecuteUtils.getExecutable("php");

      // Extract runners from resources
      } catch (FileNotFoundException ex) {
        throw new MojoExecutionException("Cannot find PHP executable. You can use -Dxp.runtime.php=...", ex);
      }
    }

    // Update ${xp.runtime.php} property
    getLog().debug(" - Using PHP from [" + this.php.getAbsolutePath() + "]");
    this.project.getProperties().setProperty("xp.runtime.php", this.php.getAbsolutePath());
  }

  /**
   * Determine timezone
   *
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void setupTimezone() throws MojoExecutionException {
    getLog().debug("Identifying timezone");

    // ${xp.runtime.timezone} is already set
    if (null != this.timezone) {
      getLog().debug(" - Provided by caller");

    // Use system default
    } else {
      getLog().debug(" - Using system timezone");
      this.timezone= Calendar.getInstance().getTimeZone().getID();
    }

    // Update ${xp.runtime.timezone} property
    getLog().debug(" - Using timezone [" + this.timezone + "]");
    this.project.getProperties().setProperty("xp.runtime.timezone", this.timezone);
  }

  /**
   * This will alter ${project.sourceDirectory} and ${project.testSourceDirectory} only if they have
   * the default values set up in the Maven Super POM:
   * - src/main/java
   * - src/test/java
   *
   * to the following values:
   * - src/main/xp
   * - src/test/xp
   *
   * @return void
   */
  private void alterSourceDirectories() {

    // Check ${project.sourceDirectory} ends with "src/main/java"
    String oldDirectory = this.project.getBuild().getSourceDirectory();
    String xpDirectory  = this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "xp";
    if (oldDirectory.endsWith("src" + File.separator + "main" + File.separator + "java")) {

      // Alter ${project.sourceDirectory}
      this.project.getBuild().setSourceDirectory(xpDirectory);
      getLog().debug("Set ${project.sourceDirectory} to [" + xpDirectory + "]");

      // Maven2 limitation: changing ${project.sourceDirectory} doesn't change ${project.compileSourceRoots}
      List<String> newRoots= new ArrayList<String>();
      for (String oldRoot : (List<String>)this.project.getCompileSourceRoots()) {
        if (oldRoot.equals(oldDirectory)) {
          newRoots.add(xpDirectory);
        } else {
          newRoots.add(oldRoot);
        }
      }

      // Replace ${project.compileSourceRoots} with new list
      this.project.getCompileSourceRoots().clear();
      for (String newRoot : newRoots) {
        this.project.addCompileSourceRoot(newRoot);
      }
    }

    // Check ${project.testSourceDirectory} ends with "src/test/java"
    oldDirectory= this.project.getBuild().getTestSourceDirectory();
    xpDirectory= this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "xp";
    if (oldDirectory.endsWith("src" + File.separator + "test" + File.separator + "java")) {

      // Alter ${project.testSourceDirectory}
      this.project.getBuild().setTestSourceDirectory(xpDirectory);
      getLog().debug("Set ${project.testSourceDirectory} to [" + xpDirectory + "]");

      // Maven2 limitation: changing ${project.testSourceDirectory} doesn't change ${project.testCompileSourceRoots}
      List<String> newRoots= new ArrayList<String>();
      for (String oldRoot : (List<String>)this.project.getTestCompileSourceRoots()) {
        if (oldRoot.equals(oldDirectory)) {
          newRoots.add(xpDirectory);
        } else {
          newRoots.add(oldRoot);
        }
      }

      // Replace ${project.testCompileSourceRoots} with new list
      this.project.getTestCompileSourceRoots().clear();
      for (String newRoot : newRoots) {
        this.project.addTestCompileSourceRoot(newRoot);
      }
    }
  }
}
