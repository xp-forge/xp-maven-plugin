/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import net.xp_forge.maven.plugins.xp.SvnDeployNoForkMojo;

/**
 * Deploy the unpacked artifact to SVN
 *
 * This goal forks the build lifecycle upto and including the "package" phase
 *
 * @goal svn-deploy
 * @execute phase="package"
 * @requiresDirectInvocation
 * @since 3.2.0
 */
public class SvnDeployMojo extends SvnDeployNoForkMojo {

  /**
   * Get skip setting
   *
   * For a forked lifecycle, this is always false
   *
   * @return boolean
   */
  @Override
  protected boolean isSkip() {
    return false;
  }
}
