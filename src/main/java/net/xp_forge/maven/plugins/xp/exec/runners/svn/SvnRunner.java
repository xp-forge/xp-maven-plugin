/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.runners.svn;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xp_forge.maven.plugins.xp.exec.AbstractRunner;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput;

/**
 * Wrapper over "svn" executable
 *
 */
public class SvnRunner extends AbstractRunner {
  private SvnRunnerInput input;
  private List<String> output;

  /**
   * Constructor
   *
   * @param  java.io.File executable
   * @param  net.xp_forge.maven.plugins.xp.exec.input.svn.SvnRunnerInput input
   */
  public SvnRunner(File executable, SvnRunnerInput input) {
    super(executable);
    this.input= input;
  }

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws RunnerException {

    // Build arguments
    List<String> arguments= new ArrayList<String>();

    // Add SVN command
    arguments.add(this.input.command);

    // Add command arguments
    for (String argument : this.input.arguments) {
      arguments.add(argument);
    }

    // Execute command and capture output
    this.output= this.executeCommand(arguments, true);
  }

  /**
   * Get command output
   *
   * @return List<String>
   */
  public List<String> getOutput() {
    return this.output;
  }
}
