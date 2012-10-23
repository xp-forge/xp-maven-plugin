/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.runners.xp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.xp_forge.maven.plugins.xp.io.PthFile;
import net.xp_forge.maven.plugins.xp.exec.AbstractRunner;
import net.xp_forge.maven.plugins.xp.exec.RunnerException;
import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;

/**
 * Base class for all XP-Framework runners
 *
 */
public abstract class AbstractClasspathRunner extends AbstractRunner {

  /**
   * Constructor
   *
   * @param  java.io.File executable
   */
  public AbstractClasspathRunner(File executable) {
    super(executable);
  }

  /**
   * Set classpath via command line arguments
   *
   * @param  java.util.List<java.lang.String> classpaths
   * @param  java.util.List<java.lang.String> arguments
   * @return void
   */
  public void setClasspath(List<String> classpaths, List<String> arguments) {
    for (String classpath : classpaths) {
      arguments.add("-cp");
      arguments.add(classpath);
    }
  }

  /**
   * Set classpath via [project.pth] file
   *
   * @param  java.util.List<java.lang.String> classpaths
   * @param  java.io.File pthFile
   * @return void
   * @throws net.xp_forge.maven.plugins.xp.runners.RunnerException When cannot create project.pth file
   */
  public void setClasspath(List<String> classpaths, File pthFile) throws RunnerException {
    PthFile pth= new PthFile();
    pth.addEntries(classpaths);

    try {
      pth.setComment(CREATED_BY_NOTICE);
      pth.dump(pthFile);
    } catch (IOException ex) {
      throw new RunnerException("Cannot write [" + pthFile + "] file", ex);
    }
  }
}
