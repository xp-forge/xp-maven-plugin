/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.maven.plugin.logging.Log;
import org.apache.commons.exec.LogOutputStream;

import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xp.exec.RunnerOutput;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;

/**
 * Base class for all runners
 *
 */
public abstract class AbstractRunner {
  private Log log;
  private File executable;
  private File workingDirectory;
  private Map<String, String> environmentVariables= new HashMap<String, String>();
  private RunnerOutput output;

  /**
   * Constructor
   *
   * @param  java.io.File executable
   */
  public AbstractRunner(File executable) {
    this.executable = executable;
    this.output     = new RunnerOutput();
  }

  /**
   * Execute this runner
   *
   * @throws net.xp_forge.maven.plugins.xp.runners.RunnerException When runner execution failed
   */
  public abstract void execute() throws RunnerException;

  /**
   * Set logging trace
   *
   * @param  org.apache.maven.plugin.logging.Log log
   * @return void
   */
  public void setLog(Log log) {
    this.log= log;
  }

  /**
   * Get logging trace
   *
   * @return org.apache.maven.plugin.logging.Log
   */
  public Log getLog() {
    return this.log;
  }

  /**
   * Get executable
   *
   * @return java.io.File
   */
  public File getExecutable() {
    return this.executable;
  }

  /**
   * Set runner working directory
   *
   * @param  java.io.File workingDirectory
   * @return void
   */
  public void setWorkingDirectory(File workingDirectory) {

    // Check directory exists
    if (!workingDirectory.exists()) {
      workingDirectory.mkdirs();
    }

    // Set working directory
    this.workingDirectory= workingDirectory;
  }

  /**
   * Get working directory; default to current directory
   *
   * @return java.io.File
   */
  public File getWorkingDirectory() {
    if (null == this.workingDirectory || !this.workingDirectory.exists()) {
      this.workingDirectory= new File(System.getProperty("user.dir"));
    }
    return this.workingDirectory;
  }

  /**
   * Set runner environment variable
   *
   * @param  java.lang.String name
   * @param  java.lang.String value
   * @return void
   */
  public void setEnvironmentVariable(String name, String value) {
    this.environmentVariables.put(name, value);
  }

  /**
   * Get runner environment variable
   *
   * @param  java.lang.String name
   * @return java.lang.String
   */
  public String getEnvironmentVariable(String name) {
    return this.environmentVariables.get(name);
  }

  /**
   * Get all runner environment variables
   *
   * @return java.util.Map<java.lang.String>
   */
  public Map<String, String> getEnvironmentVariables() {
    return this.environmentVariables;
  }

  /**
   * Execute command using the specified arguments and returns the command output
   *
   * @param  java.util.List<String> arguments Executable arguments
   * @param  boolean captureOutput
   * @return void
   * @throws net.xp_forge.maven.plugins.xp.runners.RunnerException When execution failed
   */
  protected void executeCommand(List<String> arguments, boolean captureOutput) throws RunnerException {

    // If captureOutput is disabled, send output to $cat and return null
    if (false == captureOutput) {
      try {
        ExecuteUtils.executeCommand(
          this.getExecutable(),
          arguments,
          this.getWorkingDirectory(),
          this.getEnvironmentVariables(),
          this.log
        );
      } catch (ExecutionException ex) {
        throw new RunnerException("Execution failed", ex);
      }

      return;
    }

    // Execute command and capture output inside $outputLines
    this.output.clear();
    try {
      ExecuteUtils.executeCommand(
        this.getExecutable(),
        arguments,
        this.getWorkingDirectory(),
        this.getEnvironmentVariables(),
        this.log,
        new LogOutputStream() {
          @Override
          protected void processLine(String line, @SuppressWarnings("unused") int level) {
            output.addLine(line);
          }
        }
      );

    } catch (ExecutionException ex) {
      throw new RunnerException("Execution failed", ex);
    }
  }

  /**
   * Execute command using the specified arguments
   *
   * @param  java.util.List<String> arguments Executable arguments
   * @return void
   * @throws net.xp_forge.maven.plugins.xp.runners.RunnerException When execution failed
   */
  protected void executeCommand(List<String> arguments) throws RunnerException {
    this.executeCommand(arguments, false);
  }

  /**
   * Get runner output
   *
   * @return net.xp_forge.maven.plugins.xp.exec.RunnerOutput
   */
  public RunnerOutput getOutput() {
    return this.output;
  }
}
