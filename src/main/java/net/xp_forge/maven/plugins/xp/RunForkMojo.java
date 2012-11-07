/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Forks a "run" lifecycle
 *
 * @goal run-fork
 * @execute lifecycle="run" phase="process-classes"
 * @requiresDependencyResolution runtime
 */
public class RunForkMojo extends org.apache.maven.plugin.AbstractMojo {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void execute() throws MojoExecutionException {

    // Nothing to do. I'm here just to fork the "run" lifecycle
  }
}
