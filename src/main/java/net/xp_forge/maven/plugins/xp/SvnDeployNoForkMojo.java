/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
import net.xp_forge.maven.plugins.xp.logging.LogLogger;
import net.xp_forge.maven.plugins.xp.exec.RunnerOutput;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.runners.svn.SvnRunner;
import net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput;

/**
 * Deploy the unpacked artifact to the following SVN locations:
 *
 * - ${repositoryUrl}/${baseTagName}/${volatileTagName}
 * - ${repositoryUrl}/${baseTagName}/${project.version}
 *
 * This goal functions the same as the "svn-deploy" goal but does not fork
 * the build and is suitable for attaching to the build lifecycle
 *
 * @goal svn-deploy-no-fork
 * @phase deploy
 * @since 3.2.0
 */
public class SvnDeployNoForkMojo extends AbstractXpMojo {

  /**
   * Skip deploy to SVN
   *
   * @parameter expression="${xp.svn-deploy.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * Location of the deploy repository
   *
   * E.g.: http://svn.company.org/deploy
   *
   * @parameter expression="${xp.svn-deploy.repositoryUrl}"
   * @required
   */
  protected String repositoryUrl;

  /**
   * Tag base
   *
   * @parameter expression="${xp.svn-deploy.baseTagName}" default-value="${project.artifactId}"
   * @required
   */
  protected String baseTagName;

  /**
   * Volatile tag name
   *
   * @parameter expression="${xp.svn-deploy.volatileTagName}" default-value="LATEST"
   * @required
   */
  protected String volatileTagName;

  /**
   * Prepend uppercase(${project.artifactId}) to volatile tag name
   *
   * E.g.: LIB-COMMON_LATEST
   *
   * @parameter expression="${xp.svn-deploy.legacyVolatileTagName}" default-value="false"
   * @since 3.2.5
   */
  protected boolean legacyVolatileTagName;
  /**
   * SVN username
   *
   * @parameter expression="${xp.svn-deploy.username}"
   */
  protected String username;

  /**
   * SVN password
   *
   * @parameter expression="${xp.svn-deploy.password}"
   */
  protected String password;

  /**
   * SVN commit message prefix
   *
   * @parameter expression="${xp.svn-deploy.messagePrefix}" default-value="[xp-maven-plugin]"
   */
  protected String messagePrefix;

  /**
   * Location of the "svn" executable. If not specified, will look for in into PATH env variable.
   *
   * @parameter expression="${xp.svn-deploy.svnExecutable}"
   */
  protected File svnExecutable;

  /**
   * Get skip setting
   *
   * @return boolean
   */
  protected boolean isSkip() {
    return this.skip;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {
    ArchiveUtils.enableLogging(new LogLogger(getLog()));

    // Skip svn deploy
    if (this.isSkip()) {
      getLog().info("Not deploying to SVN (xp.svn-deploy.skip)");
      return;
    }

    // Pom artifacts cannot be deployed to svn
    if (this.packaging.equals("pom")) {
      getLog().info("Cannot deploy [pom] artifacts to SVN repository; silently skipping");
      return;
    }

    String baseTagUrl      = (this.repositoryUrl.replaceAll("/+$", "") + "/" + this.baseTagName).replaceAll("/+$", "");
    String volatileTagName = this.getVolatileTagName();
    String volatileTagUrl  = baseTagUrl + "/" + volatileTagName;
    String permanentTagUrl = baseTagUrl + "/" + this.project.getVersion();

    getLog().info("Repository URL            [" + this.repositoryUrl + "]");
    getLog().info("Base tag name             [" + this.baseTagName + "]");
    getLog().info("Base tag URL              [" + baseTagUrl + "]");
    getLog().info("Volatile tag name         [" + volatileTagName + "]");
    getLog().info("Legacy volatile tag name  [" + (this.legacyVolatileTagName ? "Yes" : "No") + "]");
    getLog().info("Volatile tag URL          [" + volatileTagUrl + "]");
    getLog().info("Permanent tag URL         [" + permanentTagUrl + "]");
    getLog().info("Commit message prefix     [" + this.messagePrefix + "]");

    // Get artifact to deploy on SVN
    Artifact artifact= this.getMainArtifact();
    if (null == artifact) {
      throw new MojoExecutionException("Found no artifact to deploy");
    } 
    getLog().debug("Artifact to deploy [" + artifact.getFile() + "]");

    // If not specified, try to guess $svnExecutable
    if (null == this.svnExecutable) {
      try {
        this.svnExecutable= ExecuteUtils.getExecutable("svn");

      } catch (FileNotFoundException ex) {
        throw new MojoExecutionException("Cannot find [svn] executable; specify it via ${xp.tag.svnExecutable}");
      }
    }

    // Check base tag exists; if not, try to create it
    if (!this.tagExists(baseTagUrl)) {
      this.createTag(baseTagUrl);
    }

    // Check volatile tag exists; if not, try to create it
    if (!this.tagExists(volatileTagUrl)) {
      this.createTag(volatileTagUrl);
    }

    // Checkout volatile tag into "${outputDirectory}/.svndeploy"
    File svndeployDirectory= new File(this.outputDirectory, ".svndeploy");
    getLog().info("Checkout volatile tag [" + volatileTagUrl + "] into [" + svndeployDirectory + "]");
    this.checkoutTag(volatileTagUrl, svndeployDirectory);

    // Empty directory after checkout (but keep ".svn" files)
    try {
      this.emptyCheckoutDirectory(svndeployDirectory);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot empty checkout directory [" + svndeployDirectory + "]", ex);
    }

    // Dump artifact to checkout directory
    File dumpDirectory= new File(svndeployDirectory, this.project.getArtifactId());
    getLog().info("Dump artifact [" + artifact.getFile() + "] to [" + dumpDirectory + "]");
    ArchiveUtils.dumpArtifact(artifact.getFile(), dumpDirectory, true);

    // Delete empty sub-directories from checkout directory
    try {
      this.deleteEmptyDirectories(svndeployDirectory);
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot clean checkout directory [" + svndeployDirectory + "]", ex);
    }

    // Create empty file using version as filename
    File versionFile= new File(svndeployDirectory, this.project.getVersion());
    try {
      FileUtils.setFileContents(versionFile, "");
    } catch (IOException ex) {
      throw new MojoExecutionException("Cannot touch file [" + versionFile + "]", ex);
    }

    // Update volatile tag with local changes
    getLog().info("Update volatile tag [" + volatileTagUrl + "]");
    this.updateTag(volatileTagUrl, svndeployDirectory);

    // Check permanent tag exists; if yes, remove it
    getLog().info("Create permanent tag [" + permanentTagUrl + "]");
    if (this.tagExists(permanentTagUrl)) {
      this.deleteTag(permanentTagUrl);
    }

    // Copy volatile tag to permanent tag
    this.cloneTag(volatileTagUrl, permanentTagUrl);
  }

  /**
   * Add the configured prefix to the specified message
   *
   * @param  java.lang.String message
   * @return java.lang.String
   */
  private String prefixMessage(String message) {
    return String.format(
      "%s [%s:%s:%s] %s",
      this.messagePrefix.trim(),
      this.project.getGroupId(),
      this.project.getArtifactId(),
      this.project.getVersion(),
      message
    );
  }

  /**
   * Create and setup a SVN runner input
   *
   * @param java.lang.String svnCommand
   * @return net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput
   */
  private SvnRunnerInput conjureSvnRunnerInput(String svnCommand) {
    SvnRunnerInput retVal= new SvnRunnerInput(svnCommand);

    // Setup username & password
    retVal.username= this.username;
    retVal.password= this.password;

    return retVal;
  }

  /**
   * Execute the SVN runner with the specified input
   *
   * @param  net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput
   * @param  java.io.File workingDirectory
   * @return net.xp_forge.maven.plugins.xp.exec.RunnerOutput
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private RunnerOutput executeSvn(SvnRunnerInput input, File workingDirectory) throws MojoExecutionException {
    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

    // Set runner working directory
    if (null != workingDirectory) {
      runner.setWorkingDirectory(workingDirectory);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [svn] runner failed: " + runner.getOutput().asString(), ex);
    }

    // Return runner outout
    return runner.getOutput();
  }

  /**
   * Execute the SVN runner with the specified input
   *
   * @param  net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput
   * @return net.xp_forge.maven.plugins.xp.exec.RunnerOutput
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private RunnerOutput executeSvn(SvnRunnerInput input) throws MojoExecutionException {
    return this.executeSvn(input, null);
  }

  /**
   * Check if the specified remoteUrl exists on the SVN server
   *
   * @param  java.lang.String remoteUrl
   * @return boolean
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private boolean tagExists(String remoteUrl) throws MojoExecutionException {
    getLog().debug("Check SVN tag exists [" + remoteUrl + "]");

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("list");
    input.remoteUrl= remoteUrl;

    // Setup runner
    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {

      // Execution of "svn list" failed, assume it is because remoteUrl does not exist
      getLog().debug(runner.getOutput().asString());
      return false;
    }

    // Tag exists
    return true;
  }

  /**
   * Create a tag on the remote SVN server
   *
   * @param  java.lang.String remoteUrl
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void createTag(String remoteUrl) throws MojoExecutionException {
    getLog().debug("Create SVN tag [" + remoteUrl + "]");

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("mkdir");
    input.remoteUrl = remoteUrl;
    input.message   = this.prefixMessage("Create empty tag");

    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

    // Execute runner
    this.executeSvn(input);
  }

  /**
   * Delete a tag on the remote SVN server
   *
   * @param  java.lang.String remoteUrl
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void deleteTag(String remoteUrl) throws MojoExecutionException {
    getLog().debug("Delete SVN tag [" + remoteUrl + "]");

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("delete");
    input.remoteUrl = remoteUrl;
    input.message   = this.prefixMessage("Delete tag");

    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

    // Execute runner
    this.executeSvn(input);
  }

  /**
   * Copy a tag to a new tag
   *
   * @param  java.lang.String srcUrl
   * @param  java.lang.String dstUrl
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void cloneTag(String srcUrl, String dstUrl) throws MojoExecutionException {
    getLog().debug("Clone SVN tag [" + srcUrl + "] to [" + dstUrl + "]");

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("copy");
    input.remoteUrl = srcUrl;
    input.addArgument(dstUrl);
    input.message   = this.prefixMessage("Clone tag");

    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

    // Execute runner
    this.executeSvn(input);
  }

  /**
   * Checkout the specified SVN tag to the specified directory
   *
   * @param  java.lang.String remoteUrl
   * @param  java.io.File localDirectory
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void checkoutTag(String remoteUrl, File localDirectory) throws MojoExecutionException {

    // Cleanup localDirectory
    try {
      FileUtils.deleteDirectory(localDirectory);
    } catch (IOException ex) {
      // Do nothing
    }
    localDirectory.mkdirs();

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("checkout");
    input.remoteUrl      = remoteUrl;
    input.localDirectory = localDirectory;

    // Execute runner
    this.executeSvn(input);
  }

  /**
   * Recursively empty the specified directory of all files and folders, but keep the ".svn" directories
   *
   * @param  java.io.File directory
   * @return void
   * @throws java.io.IOException
   */
  private void emptyCheckoutDirectory(File directory) throws IOException {
    if (null == directory || !directory.isDirectory() || directory.getName().equals(".svn")) return;

    // List directory contents
    File[] entries= directory.listFiles();
    if (null == entries) {
      throw new IOException("Failed to list contents of directory [" + directory + "]");
    }

    for (File entry : entries) {

      // Skip ".svn" directories
      if (entry.getName().equals(".svn")) continue;

      // Delete files
      if (entry.isFile()) {
        if (!entry.delete()) {
          throw new IOException("Unable to delete file [" + entry + "]");
        }
        continue;
      }

      // Recursively delete directories
      if (entry.isDirectory()) {
        this.emptyCheckoutDirectory(entry);
      }
    }
  }

  /**
   * Recursively delete empty directories
   *
   * Note: A directory with just a single ".svn" sub-directory is considered empty
   *
   * @param  java.io.File directory
   * @return void
   * @throws java.io.IOException
   */
  private void deleteEmptyDirectories(File directory) throws IOException {
    if (null == directory || !directory.isDirectory() || directory.getName().equals(".svn")) return;

    // List directory contents
    File[] entries= directory.listFiles();
    if (null == entries) {
      throw new IOException("Failed to list contents of directory [" + directory + "]");
    }

    boolean isDirectoryEmpty= true;
    for (File entry : entries) {

      // Skip ".svn" directories
      if (entry.getName().equals(".svn")) continue;
      isDirectoryEmpty= false;

      // Recurse
      if (entry.isDirectory()) {
        this.deleteEmptyDirectories(entry);
      }
    }

    // If directory is empty; remove it
    if (isDirectoryEmpty) {
      FileUtils.deleteDirectory(directory);
    }
  }

  /**
   * Update the specified tag with local changes:
   * - Delete missing files from local checkout
   * - Add new files from local checkout
   * - Commit modified files
   *
   * @param  java.lang.String remoteUrl
   * @param  java.io.File localDirectory
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void updateTag(String remoteUrl, File localDirectory) throws MojoExecutionException {

    // Run a "svn st" to see changes
    getLog().debug("Get list of SVN changes");
    SvnRunnerInput input= this.conjureSvnRunnerInput("status");
    RunnerOutput output= this.executeSvn(input, localDirectory);

    // Parse output
    List<String> addedEntries   = new ArrayList<String>();
    List<String> deletedEntries = new ArrayList<String>();
    boolean hasChanges= false;
    for (String line : output.getLines()) {
      hasChanges= true;

      // Entry was added
      if (line.startsWith("?")) {
        addedEntries.add(line.substring(3).trim());
        continue;
      }

      // Entry was deleted
      if (line.startsWith("!")) {
        deletedEntries.add(line.substring(3).trim());
        continue;
      }
    }

    // Early return
    if (false == hasChanges) {
      getLog().debug("Tag is already up-to-date");
      return;
    }

    // Add new entries
    if (!addedEntries.isEmpty()) {
      getLog().debug("Add new entries to SVN");
      input= this.conjureSvnRunnerInput("add");
      input.addArguments(addedEntries);
      this.executeSvn(input, localDirectory);
    }

    // Delete old entries
    if (!deletedEntries.isEmpty()) {
      getLog().debug("Delete old entries to SVN");
      input= this.conjureSvnRunnerInput("delete");
      input.addArguments(deletedEntries);
      this.executeSvn(input, localDirectory);
    }

    // Commit changes
    getLog().debug("Commit changes to SVN");
    input= this.conjureSvnRunnerInput("commit");
    input.message= this.prefixMessage("Update tag with latest changes");
    this.executeSvn(input, localDirectory);
  }

  /**
   * Get volatile tag name
   *
   * @return java.lang.String
   */
  private String getVolatileTagName() {
    if (!this.legacyVolatileTagName) {
      return this.volatileTagName;
    }

    // Legacy volatile tag name (E.g.: LIB-COMMON_RELEASE)
    return this.project.getArtifactId().toUpperCase() + "_" + this.volatileTagName;
  }
}
