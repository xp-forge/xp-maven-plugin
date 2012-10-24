/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Arrays;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
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
 * @goal svntag
 */
public class SvntagMojo extends AbstractXpMojo {

  /**
   * Location of the deploy repository
   *
   * @parameter expression="${xp.svntag.respositoryUrl}"
   * @required
   */
  protected String repositoryUrl;

  /**
   * Tag base
   *
   * @parameter expression="${xp.svntag.baseTagName}" default-value="${project.artifactId}"
   * @required
   */
  protected String baseTagName;

  /**
   * Volatile tag name
   *
   * @parameter expression="${xp.svntag.volatileTagName}" default-value="LATEST"
   * @required
   */
  protected String volatileTagName;

  /**
   * SVN username
   *
   * @parameter expression="${xp.svntag.username}"
   */
  protected String username;

  /**
   * SVN password
   *
   * @parameter expression="${xp.svntag.password}"
   */
  protected String password;

  /**
   * Location of the "svn" executable. If not specified, will look for in into PATH env variable.
   *
   * @parameter expression="${xp.svntag.svnExecutable}"
   */
  protected File svnExecutable;

  /**
   * @parameter default-value="${project.packaging}"
   * @required
   * @readonly
   */
  private String packaging;

  /**
   * @parameter default-value="${project.artifact}"
   * @required
   * @readonly
   */
  private Artifact artifact;

  /**
   * @parameter default-value="${project.attachedArtifacts}
   * @required
   * @readonly
   */
  private List<Artifact> attachedArtifacts;

  /**
   * {@inheritDoc}
   *
   */
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {

    // Pom artifacts cannot be deployed to svn
    if (this.packaging.equals("pom")) {
      getLog().warn("Cannot deploy [pom] artifacts to SVN repository; silently skipping");
      return;
    }

    // Get artifact to deploy
    File artifactFile= this.artifact.getFile();
    if (null == artifactFile || !artifactFile.isFile()) {

      // Primary artifact is mising and cannot find other attached artifact
      if (this.attachedArtifacts.isEmpty()) {
        throw new MojoExecutionException("The packaging for this project did not assign a file to the build artifact");
      }

      // Use attached artifact
      getLog().warn("No primary artifact to deploy, deploying *first* attached artifact instead");
      artifactFile= this.attachedArtifacts.get(0).getFile();
    }
    getLog().info("Artifact to deploy [" + artifactFile + "]");

    // If not specified, try to guess $svnExecutable
    if (null == this.svnExecutable) {
      try {
        this.svnExecutable= ExecuteUtils.getExecutable("svn");

      } catch (FileNotFoundException ex) {
        throw new MojoExecutionException("Cannot find [svn] executable; specify it via ${xp.tag.svnExecutable}");
      }
    }

    // Check tagBase exists; if not, try to create it
    String baseTagUrl= this.repositoryUrl + "/" + this.baseTagName;
    getLog().debug("Ensure SVN base tag exists [" + baseTagUrl + "]");
    this.ensureTag(baseTagUrl);

    // Check volatile tag exists; if not, try to create it
    String volatileTagUrl= baseTagUrl + "/" + this.volatileTagName;
    getLog().debug("Ensure SVN volatile tag exists [" + volatileTagUrl + "]");
    this.ensureTag(volatileTagUrl);

    // Checkout volatile tag into "${outputDirectory}/.svntag"
    getLog().info("Checkout volatile tag [" + volatileTagUrl + "] into [" + localDirectory + "]");
    File localDirectory= new File(this.outputDirectory, ".svntag");
    this.checkoutTag(volatileTagUrl, localDirectory);

    // Cleanup checkout directory (but keep ".svn" files)
    getLog().debug("Cleanup SVN checkout directory [" + localDirectory + "]");
    this.cleanCheckoutDirectory(localDirectory);

    // Dump artifact to checkout directory
    getLog().debug("Dump artifact [" + artifactFile + "] to [" + localDirectory + "]");
    ArchiveUtils.dumpArtifact(artifactFile, localDirectory, true);
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
   * @return net.xp_forge.maven.plugins.xp.exec.RunnerOutput
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private RunnerOutput executeSvn(SvnRunnerInput input) throws MojoExecutionException {
    SvnRunner runner= new SvnRunner(this.svnExecutable, input);
    runner.setLog(getLog());

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
   * Make sure tagBase exists on the remote SVN server
   *
   * @param  java.lang.String remoteUrl
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void ensureTag(String remoteUrl) throws MojoExecutionException {

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

      // If output contains 'non-existent in that revision'; tagBase does not exist
      if (runner.getOutput().contains("non-existent in that revision")) {
        this.createTag(remoteUrl);
        return;
      }

      // Some other error
      throw new MojoExecutionException("Execution of [svn] runner failed: " + runner.getOutput().asString(), ex);
    }
  }

  /**
   * Create a tag on the remote SVN server
   *
   * @param  java.lang.String remoteUrl
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException
   */
  private void createTag(String remoteUrl) throws MojoExecutionException {
    getLog().info("Create SVN tag [" + remoteUrl + "]");

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("mkdir");
    input.remoteUrl = remoteUrl;
    input.message   = "Create empty tag";

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
    localDirectory.delete();
    localDirectory.mkdirs();

    // Setup runner input
    SvnRunnerInput input= this.conjureSvnRunnerInput("checkout");
    input.remoteUrl      = remoteUrl;
    input.localDirectory = localDirectory;

    // Execute runner
    this.executeSvn(input);
  }

  /**
   * Recursively clean the specified directory of all files and folders, but kepe the ".svn" directories
   *
   * @param  java.io.File directory
   * @return void
   * @throws java.io.IOException
   */
  private void cleanCheckoutDirectory(File directory) {
    if (null == directory || !directory.exists() || !directory.isDirectory()) return;

    // Cleanup this directory
    for (File file : directory.listFiles()) {

      // Delete files
      if (file.isFile()) {
        file.delete();
        continue;
      }

      // Skip ".svn" directories
      if (file.getName().equals(".svn")) continue;

      // Recursively delete directories
      this.cleanCheckoutDirectory(file);
    }
  }
}
