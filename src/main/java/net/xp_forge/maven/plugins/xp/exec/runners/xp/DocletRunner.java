/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.runners.xp;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import net.xp_forge.maven.plugins.xp.exec.input.xp.DocletRunnerInput;

/**
 * Wrapper over XP-Framework "doclet" runner
 *
 */
public class DocletRunner extends AbstractClasspathRunner {
  private DocletRunnerInput input;

  /**
   * Constructor
   *
   * @param  java.io.File executable
   * @param  net.xp_forge.maven.plugins.xp.exec.input.xp.DocletRunnerInput input
   */
  public DocletRunner(File executable, DocletRunnerInput input) {
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
    this.setClasspath(this.input.classpaths, arguments);

    // Add sourcepath (-sp)
    if (!this.input.sourcepaths.isEmpty()) {
      arguments.add("-sp");
      arguments.add(StringUtils.join(this.input.sourcepaths, File.pathSeparator));
    }

    // Add doclet class
    arguments.add(this.input.docletClass);

    // Add doclet options
    for (Map.Entry<String, String> entry: this.input.docletOptions.entrySet()) {
      arguments.add("-" + entry.getKey());
      arguments.add(entry.getValue());
    }

    // Add names; should not be empty
    if (this.input.names.isEmpty()) {
      throw new RunnerException("At least one name must be provided");
    } else {
      for (String name: this.input.names) {
        arguments.add(name);
      }
    }

    // Execute command
    this.executeCommand(arguments);
  }
}
