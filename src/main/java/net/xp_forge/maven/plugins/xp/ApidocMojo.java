/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;

import org.apache.maven.project.MavenProject;

import net.xp_forge.maven.plugins.xp.SourceNoForkMojo;

/**
 * Generate and pack project API documentation
 *
 * This goal forks the build lifecycle upto and including the "generate-sources" phase
 *
 * @goal apidoc
 * @execute phase="generate-sources"
 * @requiresDirectInvocation
 * @since 3.2.0
 */
public class ApidocMojo extends ApidocNoForkMojo {

  /**
   * The paralel Maven project that was forked before the "xp:run" goal was executed
   *
   * We need this to get the ${xp.runtime.runners.directory} property set by the "xp:initialize" goal
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
  protected File getRunnersDirectory() {
    return new File(this.executedProject.getProperties().getProperty("xp.runtime.runners.directory"));
  }

  /**
   * Get skip setting
   *
   * For a forked lifecycle, this is always false
   *
   * @return boolean
   */
  protected boolean isSkip() {
    return false;
  }

  /**
   * Get attach setting
   *
   * For a forked lifecycle, this is always false
   *
   * @return boolean
   */
  protected boolean isAttach() {
    return false;
  }
}
