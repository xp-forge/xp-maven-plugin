/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;

import org.apache.maven.project.MavenProject;

import net.xp_forge.maven.plugins.xp.RunNoForkMojo;

/**
 * Run XP code
 *
 * This goal forks the build lifecycle upto an including the "compile" phase
 *
 * @goal run
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 * @requiresDirectInvocation
 * @since 3.2.0
 */
public class RunMojo extends RunNoForkMojo {

  /**
   * The paralel Maven project that was forked before the "xp:run" goal was executed
   *
   * We need this to get the ${xp.runtime.runners} property set by the "xp:initialize" goal
   *
   * @parameter default-value="${executedProject}"
   * @required
   * @readonly
   */
  protected MavenProject executedProject;

  /**
   * Get location of XP-Runners
   *
   * For a forked lifecycle, get the property value from ${executedProject}
   *
   * @return java.io.File
   * @see    net.xp_forge.maven.plugins.xp.InitializeMojo::execute()
   */
  @Override
  protected File getRunnersDirectory() {
    return new File(this.executedProject.getProperties().getProperty("xp.runtime.runners"));
  }
}
