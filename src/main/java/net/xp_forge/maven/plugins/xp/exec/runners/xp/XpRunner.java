/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.runners.xp;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.input.xp.XpRunnerInput;

/**
 * Wrapper over XP-Framework "xp" runner
 *
 */
public class XpRunner extends AbstractClasspathRunner {
  private XpRunnerInput input;

  /**
   * Constructor
   *
   * @param  java.io.File executable
   * @param  net.xp_forge.maven.plugins.xp.exec.input.xp.XpRunnerInput input
   */
  public XpRunner(File executable, XpRunnerInput input) {
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

    // Configure classpath (via project.pth)
    File pthFile= new File(this.getWorkingDirectory(), "project.pth");
    this.setClasspath(this.input.classpaths, pthFile);

    // Check what to execute
    if (null != this.input.className) {
        arguments.add(this.input.className);

    } else if (null != this.input.code) {
        arguments.add("-e");
        arguments.add(" " + this.input.code);

    } else {
        throw new RunnerException("Neither class nor code given");
    }

    // Execute command
    this.executeCommand(arguments);
  }
}
