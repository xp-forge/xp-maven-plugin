/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

import net.xp_forge.maven.plugins.xp.io.IniFile;
import net.xp_forge.maven.plugins.xp.io.PthFile;
import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
import net.xp_forge.maven.plugins.xp.logging.LogLogger;
import net.xp_forge.maven.plugins.xp.filter.ExtensionFileFilter;

import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;

/**
 * Build project package artifact
 *
 */
public abstract class AbstractPackageMojo extends AbstractXpMojo {
  public static final String[] EXCLUDES= {"META-INF/manifest.ini"};

  private AbstractArchiver archiver;

  // [project.pth] entries (for [app] packaging)
  private PthFile pth;

  /**
   * Name of the generated XAR
   *
   * @parameter expression="${project.build.finalName}"
   * @required
   */
  protected String finalName;

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
   * Get packaging strategy
   *
   * @return java.lang.String
   */
  protected abstract String getStrategy();

  /**
   * Get packaging format
   *
   * @return java.lang.String
   */
  protected abstract String getFormat();

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
   * Get packVendorLibs
   *
   * @return boolean
   */
  protected abstract boolean getPackVendorLibs();

  /**
   * Get application directories (from ${outputdir})
   *
   * @return java.util.List<java.io.File>
   */
  protected abstract List<File> getAppDirectories();

  /**
   * Get application main class
   *
   * @return java.lang.String
   */
  protected abstract String getMainClass();

  /**
   * If true, skip compiling
   *
   * @return boolean
   */
  protected abstract boolean isSkip();

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void execute() throws MojoExecutionException {
    ArchiveUtils.enableLogging(new LogLogger(getLog()));
    FileUtils.setTempDirectory(new File(this.outputDirectory, "package.tmp"));

    String classifier        = this.getClassifier();
    File outputFile          = this.getOutputFile();
    String strategy          = this.getStrategy();
    String format            = this.getFormat();
    boolean packDependencies = this.getPackDependencies();
    boolean packRuntime      = this.getPackRuntime();
    boolean packVendorLibs   = this.getPackVendorLibs();

    getLog().info("Classes directory  [" + this.getClassesDirectory() + "]");
    getLog().info("Output file        [" + outputFile + "]");
    getLog().info("Classifier         [" + (null == classifier ? "n/a" : classifier) + "]");
    getLog().info("Packaging format   [" + format + "]");
    getLog().info("Packaging strategy [" + strategy + "]");
    getLog().info("Pack runtime       [" + (packRuntime ? "yes" : "no") + "]");
    getLog().info("Pack dependencies  [" + (packDependencies ? "yes" : "no") + "]");
    getLog().info("Pack vendor libs   [" + (packVendorLibs ? "yes" : "no") + "]");

    // Check ${maven.test.skip} is active and we're trying to package the "tests" artifact
    if (this.isSkip()) {
      getLog().info("Skipping packaging of tests (maven.test.skip)");
      return;
    }

    // Load archiver
    this.archiver= ArchiveUtils.getArchiver(outputFile);

    // Init [project.pth] entries
    this.pth= new PthFile();
    this.pth.useBang(false);

    // Package library
    if (strategy.equals("lib")) {
      this.packClasses(null);
      this.packLibraryResources();
      if (packDependencies) this.mergeDependencies();

    // Package application
    } else if (strategy.equals("app")) {
      if (packRuntime) this.includeRuntime();
      if (packDependencies) this.includeDependencies();
      if (packVendorLibs) this.includeVendorLibs();
      this.packClasses("classes/");
      this.packApplicationResources();
      this.packProjectPth();

    // Invalid packing strategy
    } else{
      throw new MojoExecutionException(
        "${xp.package.strategy} has an invalid value [" + strategy + "]"
      );
    }

    // Package manifest file [META-INF/manifest.ini]
    this.packManifest();

    // Save archive to output file
    try {
      getLog().debug(" - Creating archive [" + outputFile + "]");
      outputFile.delete();
      this.archiver.createArchive();
    } catch (Exception ex) {
      throw new MojoExecutionException(
        "Cannot create [" + format + "] to [" + outputFile + "]", ex
      );
    }

    // Attach/set generated archive as project artifact
    if (null != classifier) {
      this.projectHelper.attachArtifact(this.project, format, classifier, outputFile);
    } else {
      this.project.getArtifact().setFile(outputFile);

      // Select the correct artifact handler based of the artifact format (xar or zip)
      this.project.getArtifact().setArtifactHandler(new DefaultArtifactHandler(format));
    }
  }

  /**
   * Returns the output file, based on finalName, classifier and format
   *
   * @return java.io.File Location where to generate the output XAR file
   */
  private File getOutputFile() {
    String classifier = this.getClassifier();
    String format     = this.getFormat();

    if (null == classifier || classifier.length() <= 0) {
      return new File(this.outputDirectory, this.finalName + "." + format);
    }
    return new File(
      this.outputDirectory,
      this.finalName +
      (classifier.startsWith("-") ? "" : "-") + classifier +
      "." + format
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
    DefaultFileSet fileSet= new DefaultFileSet();
    fileSet.setDirectory(classesDirectory);
    fileSet.setExcludes(AbstractPackageMojo.EXCLUDES);
    if (null != prefix) {
      fileSet.setPrefix(prefix);
    }
    this.archiver.addFileSet(fileSet);
  }

  /**
   * Include XP-runtime
   *
   * - Include bootstrap files into "lib/bootstrap"
   * - Include XP-artifacts into "lib/runtime"
   *
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void includeRuntime() throws MojoExecutionException {
    getLog().info("Including XP-runtime");

    // Add bootstrap to pth entries
    this.pth.addEntry("lib/bootstrap");

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
    getLog().debug(" - Add file [" + coreArtifact.getFile() + "] to [lib/runtime]");
    this.archiver.addFile(coreArtifact.getFile(), "lib/runtime/" + coreArtifact.getFile().getName());
    this.pth.addEntry("lib/runtime/" + coreArtifact.getFile().getName());

    getLog().debug(" - Add file [" + toolsArtifact.getFile() + "] to [lib/runtime]");
    this.archiver.addFile(toolsArtifact.getFile(), "lib/runtime/" + toolsArtifact.getFile().getName());
    this.pth.addEntry("lib/runtime/" + toolsArtifact.getFile().getName());

    // Pack bootstrap
    try {
      Map<String, String> entries= new HashMap<String, String>();
      entries.put("lang.base.php", "lib/bootstrap/lang.base.php");
      ArchiveUtils.copyArchiveEntries(coreArtifact, this.archiver, entries);

      entries.clear();
      entries.put("tools/class.php", "lib/bootstrap/tools/class.php");
      entries.put("tools/web.php", "lib/bootstrap/tools/web.php");
      entries.put("tools/xar.php", "lib/bootstrap/tools/xar.php");
      ArchiveUtils.copyArchiveEntries(toolsArtifact, this.archiver, entries);

    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot pack XP-runtime", ex);
    }
  }

  /**
   * Include project dependencies into "lib"
   *
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void includeDependencies() throws MojoExecutionException {
    getLog().info("Including dependencies");
    for (Artifact artifact : (Iterable<Artifact>)this.getArtifacts(false)) {

      // Ignore non-xar artifacts
      if (!artifact.getType().equals("xar")) {
        getLog().info(" - Ignore non-xar dependency [" + artifact.getFile() + "]");
        continue;
      }

      // Include patch
      if (null != artifact.getClassifier() && artifact.getClassifier().equals("patch")) {
        getLog().info(" + Add patch [" + artifact.getFile() + "] to [lib/patch]");
        this.archiver.addFile(artifact.getFile(), "lib/patch/" + artifact.getFile().getName());
        this.pth.addEntry("lib/patch/" + artifact.getFile().getName(), true);

      // Include dependency
      } else {
        getLog().info(" + Add dependency [" + artifact.getFile() + "] to [lib/]");
        this.archiver.addFile(artifact.getFile(), "lib/" + artifact.getFile().getName());
        this.pth.addEntry("lib/" + artifact.getFile().getName(), false);
      }
    }
  }

  /**
   * Include vendor libraries into "lib/vendor"
   *
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void includeVendorLibs() throws MojoExecutionException {
    getLog().info("Including vendor libraries");

    // Add vendor libs
    File[] files= this.vendorLibDir.listFiles(new ExtensionFileFilter("xar"));
    if (null != files) {
      for (File file : Arrays.asList(files)) {
        getLog().info(" + Add vendor library [" + file + "] to [lib/vendor/]");
        this.archiver.addFile(file, "lib/vendor/" + file.getName());
        this.pth.addEntry("lib/vendor/" + file.getName(), false);
      }
    }

    // Add patch vendor libs
    files= new File(this.vendorLibDir, "patch").listFiles(new ExtensionFileFilter("xar"));
    if (null != files) {
      for (File file : Arrays.asList(files)) {
        getLog().info(" + Add patch vendor library [" + file + "] to [lib/vendor/patch/]");
        this.archiver.addFile(file, "lib/vendor/patch/" + file.getName());
        this.pth.addEntry("lib/vendor/patch/" + file.getName(), true);
      }
    }
  }

  /**
   * Merge project dependencies into archive root
   *
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
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

    // Add tmpDirectory to archive
    getLog().debug(" - Add directory [" + tmpDirectory + "] to [/]");
    DefaultFileSet fileSet= new DefaultFileSet();
    fileSet.setDirectory(tmpDirectory);
    fileSet.setExcludes(AbstractPackageMojo.EXCLUDES);
    this.archiver.addFileSet(fileSet);
  }

  /**
   * Pack library resources ("xsl") into archive root
   *
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void packLibraryResources() throws MojoExecutionException {
    getLog().info("Including library resources");

    File xslDir= new File(this.outputDirectory, "xsl");
    if (xslDir.exists()) {
      getLog().debug(" - Add directory [" + xslDir + "] to [/]");
      DefaultFileSet fileSet= new DefaultFileSet();
      fileSet.setDirectory(xslDir);
      fileSet.setExcludes(AbstractPackageMojo.EXCLUDES);
      this.archiver.addFileSet(fileSet);
    }
  }

  /**
   * Pack application resources ("doc_root", "etc", "xsl") into archive root
   *
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void packApplicationResources() throws MojoExecutionException {
    getLog().info("Including application resources");

    // Get list of application directories
    List<File> appDirs= this.getAppDirectories();
    if (null == appDirs) return;

    for (File appDir : appDirs) {
      if (!appDir.exists()) continue;

      // If directory exists but contains no files; it won't be added to artifact
      // see https://github.com/xp-forge/xp-maven-plugin/issues/9
      try {
        if (false == FileUtils.containsAtLeastOneFile(appDir)) {
          throw new MojoExecutionException(
            "Application resource directory [" + appDir + "] exists but contains no files"
          );
        }
      } catch (IOException ex) {
        throw new MojoExecutionException("Cannot pack application resource directory [" + appDir + "]", ex);
      }

      // Add app directory contents to archive
      getLog().debug(" - Add directory [" + appDir + "] to [" + appDir.getName() + "/]");
      DefaultFileSet fileSet= new DefaultFileSet();
      fileSet.setDirectory(appDir);
      fileSet.setExcludes(AbstractPackageMojo.EXCLUDES);
      fileSet.setPrefix(appDir.getName() + "/");
      this.archiver.addFileSet(fileSet);
    }
  }

  /**
   * Pack on-the-fly created [project.pth]
   *
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void packProjectPth() throws MojoExecutionException {
    getLog().info("Packing on-the-fly created [project.pth] to archive");

    // Add project sources as last pth entry
    this.pth.addEntry("classes");

    File pthFile= new File(this.outputDirectory, "project.pth-package");
    try {
      this.pth.setComment(CREATED_BY_NOTICE);
      this.pth.dump(pthFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]", ex);
    }

    getLog().debug(" - Add file [" + pthFile + "] to [project.pth]");
    this.archiver.addFile(pthFile, "project.pth");
  }

  /**
   * Pack on-thle-fly created [META-INF/manifest.ini]
   *
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void packManifest() throws MojoExecutionException {
    getLog().info("Packing on-the-fly created [manifest.ini] to archive");

    // Init [manifest.ini]
    IniFile ini= new IniFile();

    // Set project properties
    ini.setProperty("project", "group-id", this.project.getGroupId());
    ini.setProperty("project", "artifact-id", this.project.getArtifactId());
    ini.setProperty("project", "version", this.project.getVersion());
    ini.setProperty("project", "name", this.project.getName());

    if (null != this.getClassifier()) {
      ini.setProperty("project", "classifier", this.getClassifier());
    }

    // Set archive properties
    ini.setProperty("archive", "generator", "xp-maven-plugin");
    ini.setProperty("archive", "created-by", this.getMachineInfo());
    ini.setProperty("archive", "created-on", this.getCurrentTimestamp());
    ini.setProperty("archive", "format", this.getFormat());
    ini.setProperty("archive", "strategy", this.getStrategy());

    // Add main-class; if the case
    if (null != this.getMainClass()) {
      ini.setProperty("archive", "main-class", this.getMainClass());
    }

    // Dump ini file
    File iniFile= new File(this.outputDirectory, "manifest.ini-package");
    try {
      ini.dump(iniFile);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot write [" + iniFile + "]", ex);
    }

    getLog().debug(" - Add file [" + iniFile + "] to [META-INF/manifest.ini]");
    this.archiver.addFile(iniFile, "META-INF/manifest.ini");
  }

  /**
   * Get formatted current date
   *
   * @return java.lang.String
   */
  private String getCurrentTimestamp() {
    SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(new Date());
  }

  /**
   * Get username@hostname
   *
   * @return java.lang.String
   */
  private String getMachineInfo() {
    String retVal= System.getProperty("user.name") + "@";
    try {
      retVal+= InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      retVal+= "unknown";
    }
    return retVal;
  }
}
