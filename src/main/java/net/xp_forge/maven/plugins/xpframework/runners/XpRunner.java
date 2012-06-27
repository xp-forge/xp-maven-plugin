/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework.runners;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

import net.xp_forge.maven.plugins.xpframework.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xpframework.runners.input.XpRunnerInput;

/**
 * Wrapper over XP-Framework "xp" runner
 *
 */
public class XpRunner extends AbstractRunner {
  XpRunnerInput input;

  /**
   * Constructor
   *
   * @param  net.xp_forge.maven.plugins.xpframework.runners.input.XpRunnerInput input
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

    // Configure classpath
    this.addClasspathsTo(arguments, this.input.classpaths);

    // Check what to execute...
    if (null != this.input.className) {
        arguments.add(this.input.className);
    } else if (null != this.input.code) {
        arguments.add("-e");
        arguments.add(" " + this.input.code);
    } else {
        throw new RunnerException("Neither className nor code given");
    }

    // Execute command
    this.executeCommand(arguments);
  }
}
