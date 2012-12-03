/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.runners.php;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xp_forge.maven.plugins.xp.exec.AbstractRunner;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.input.php.PhpRunnerInput;

/**
 * Wrapper over "php" executable
 *
 * Note: only execute code is implemented
 *
 */
public class PhpRunner extends AbstractRunner {
  private PhpRunnerInput input;

  /**
   * Constructor
   *
   * @param  java.io.File executable
   * @param  net.xp_forge.maven.plugins.xp.exec.input.php.PhpRunnerInput input
   */
  public PhpRunner(File executable, PhpRunnerInput input) {
    super(executable);
    this.input= input;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void execute() throws RunnerException {

    // Build arguments
    List<String> arguments= new ArrayList<String>();

    // Add code
    if (null != this.input.code && !this.input.code.isEmpty()) {
      arguments.add("-r");
      arguments.add(this.input.code + ";");
    }

    // Check no arguments
    if (arguments.isEmpty()) {
      throw new RunnerException("No arguments specified");
    }

    // Execute command and capture output
    this.executeCommand(arguments, true);
  }
}
