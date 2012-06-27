/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework.runners;

import net.xp_forge.maven.plugins.xpframework.util.ExecuteUtils;
import net.xp_forge.maven.plugins.xpframework.runners.input.UnittestRunnerInput;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;


/**
 * Wrapper over XP-Framework "unittest" runner
 *
 */
public class UnittestRunner extends AbstractRunner {
  UnittestRunnerInput input;

  /**
   * Constructor
   *
   * @param  net.xp_forge.maven.plugins.xpframework.runners.input.UnittestRunnerInput input
   */
  public UnittestRunner(File executable, UnittestRunnerInput input) {
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

    // Add verbose (-v)
    if (this.input.verbose) arguments.add("-v");

    this.addClasspathsTo(arguments, this.input.classpaths);

    // Add arguments (-a)
    for (String arg : this.input.arguments) {
      arguments.add("-a");
      arguments.add(arg);
    }

    // Add inifiles
    for (File ini : this.input.inifiles) {
      arguments.add(ini.getAbsolutePath());
    }

    // Execute command
    this.executeCommand(arguments);
  }
}
