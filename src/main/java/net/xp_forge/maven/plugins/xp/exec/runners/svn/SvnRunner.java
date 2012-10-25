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

    // Add username parameter
    if (null != this.input.username && !this.input.username.isEmpty()) {
      arguments.add("--username");
      arguments.add(this.input.username);
    }

    // Add --password parameter
    if (null != this.input.password && !this.input.password.isEmpty()) {
      arguments.add("--password");
      arguments.add(this.input.password);
    }

    // Add --message parameter
    if (null != this.input.message && !this.input.message.isEmpty()) {
      arguments.add("--message");
      arguments.add(this.input.message.trim());
    }

    // Add --force parameter
    if (true == this.input.force) {
      arguments.add("--force");
    }

    // Add --non-interactive parameter
    if (true == this.input.nonInteractive) {
      arguments.add("--non-interactive");
    }

    // Add remoteUrl
    if (null != this.input.remoteUrl && !this.input.remoteUrl.isEmpty()) {
      arguments.add(this.input.remoteUrl);
    }

    // Add localDirectory
    if (null != this.input.localDirectory) {
      arguments.add(this.input.localDirectory.getAbsolutePath());
    }

    // Add other arguments
    for (String argument : this.input.arguments) {
      arguments.add(argument);
    }

    // Execute command and capture output
    this.executeCommand(arguments, true);
  }
}
