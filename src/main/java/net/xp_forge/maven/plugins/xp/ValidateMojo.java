/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.HashMap;
import java.util.Calendar;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.xar.XarArchive;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.util.IniProperties;

/**
 * Check for the presence of XP-Framework runners
 *
 * @goal validate
 * @requiresDependencyResolution compile
 */
public class ValidateMojo extends AbstractXpFrameworkMojo {

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {
    getLog().info(LINE_SEPARATOR);
    getLog().info("VALIDATE XP-FRAMEWORK INSTALL");
    getLog().info(LINE_SEPARATOR);

    // Prepare XP-Framework bootstrap
    if (this.local) {
      this.setupUserBootstrap();
    } else {
      this.setupLocalBootstrap();
    }
    getLog().debug(" - Using runners from [" + this.runnersDirectory.getAbsolutePath() + "]");
    this.project.getProperties().setProperty("xp.runtime.runners.directory", this.runnersDirectory.getAbsolutePath());

    // Alter default Maven settings
    this.alterSourceDirectories();
    getLog().info(LINE_SEPARATOR);
  }

  /**
   * Use XP-Framework installed on local machine by searching for XP runners in PATH
   *
   * @return void
   * @throws import org.apache.maven.plugin.MojoExecutionException
   */
  private void setupUserBootstrap() throws MojoExecutionException {
    getLog().debug("Looking for XP-Framework local install");
    try {
      this.runnersDirectory= ExecuteUtils.getExecutable("xp").getParentFile();

    // Extract runners from resources
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot find XP Framework local install", ex);
    }
  }

  /**
   * Prepare our own bootstrap in [/target/bootstrap]
   *
   * @return void
   * @throws import org.apache.maven.plugin.MojoExecutionException
   */
  private void setupLocalBootstrap() throws MojoExecutionException {
    getLog().debug("Preparing XP-Framework bootstrap");
    File bootstrapDirectory= new File(this.outputDirectory, "bootstrap");
    this.runnersDirectory= new File(bootstrapDirectory, "runners");

    // Init USE_XP
    List<File> use= new ArrayList<File>();


    // ===== Get dependencies for [net.xp-framework:core]
    Artifact coreArtifact= this.findArtifact("net.xp-framework", "core");
    if (null == coreArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
    }

    // Extract [lang.base.php] from [net.xp-framework:core]
    // Target: [/target/bootstrap/core]
    File coreDirectory= new File(bootstrapDirectory, "core");
    use.add(coreDirectory);

    File langFile= new File(coreDirectory, "lang.base.php");
    this.extract("lang.base.php", coreArtifact, langFile);

    // Create [/target/bootstrap/core/boot.pth]
    try {
      FileUtils.setFileContents(
        new File(coreDirectory, "boot.pth"),
        ".\n" + coreArtifact.getFile().getAbsolutePath()
      );
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot save [/target/bootstrap/core/boot.pth] file");
    }


    // ===== Get dependencies for [net.xp-framework:core]
    Artifact toolsArtifact = this.findArtifact("net.xp-framework", "tools");
    if (null == toolsArtifact) {
      throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
    }

    // Extract [tools/class.php], [tools/web.php] and [tools/xar.php] from [net.xp-framework:tools]
    // Target: [/target/bootstrap/tools/tools]
    File toolsDirectory= new File(bootstrapDirectory, "tools");
    use.add(toolsDirectory);

    File classFile= new File(new File(toolsDirectory, "tools"), "class.php");
    this.extract("tools/class.php", toolsArtifact, classFile);

    File webFile= new File(new File(toolsDirectory, "tools"), "web.php");
    this.extract("tools/web.php", toolsArtifact, webFile);

    File xarFile= new File(new File(toolsDirectory, "tools"), "xar.php");
    this.extract("tools/xar.php", toolsArtifact, xarFile);

    // Create [/target/bootstrap/tools/boot.pth]
    try {
      FileUtils.setFileContents(
        new File(toolsDirectory, "boot.pth"),
        toolsArtifact.getFile().getAbsolutePath()
      );
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot save [/target/bootstrap/tools/boot.pth] file");
    }


    // ===== Get dependencies for [net.xp-framework:language] - NOT required
    Artifact languageArtifact= this.findArtifact("net.xp-framework", "language");
    if (null != languageArtifact) {
      File languageDirectory= new File(bootstrapDirectory, "language");
      use.add(languageDirectory);

      // Create [/target/bootstrap/language/boot.pth]
      try {
        FileUtils.setFileContents(
          new File(languageDirectory, "boot.pth"),
          languageArtifact.getFile().getAbsolutePath()
        );
      } catch (IOException ex) {
        throw new MojoExecutionException("Cannot save [/target/bootstrap/language/boot.pth] file");
      }
    }


    // ===== Extract runners
    try {
      getLog().debug(" - Extracting runners from resources");

      ExecuteUtils.saveRunner("xp", this.runnersDirectory);
      ExecuteUtils.saveRunner("xcc", this.runnersDirectory);
      ExecuteUtils.saveRunner("xar", this.runnersDirectory);
      ExecuteUtils.saveRunner("xpcli", this.runnersDirectory);
      ExecuteUtils.saveRunner("doclet", this.runnersDirectory);
      ExecuteUtils.saveRunner("unittest", this.runnersDirectory);

    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot extract runners", ex);
    }

    IniProperties ini= new IniProperties();
    ini.setProperty("use", StringUtils.join(use.toArray(), File.pathSeparator));

    // Set PHP executable and timezone
    this.setupPhp();
    ini.setProperty("runtime", "default", this.php.getAbsolutePath());

    this.setupTimezone();
    ini.setProperty("runtime", "date.timezone", this.timezone);

    // Dump ini file
    try {
      File iniFile= new File(this.runnersDirectory, "xp.ini");
      ini.dump(new PrintStream(iniFile));
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot save [xp.ini] file", ex);
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
   * Extract the specified xar entry to the specified destination
   *
   * @param  java.lang.String entry
   * @param  org.apache.maven.artifact.Artifact artifact
   * @param  java.io.File out
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void extract(String entry, Artifact artifact, File out) throws MojoExecutionException {
    try {
      FileUtils.setFileContents(out, new XarArchive(artifact.getFile()).getEntry(entry).getInputStream());
    } catch (IOException ex) {
      throw new MojoExecutionException(
        "Cannot extract [" + entry + "] from [" + artifact.getFile() + "] to [" + out + "]",
        ex
      );
    }
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
