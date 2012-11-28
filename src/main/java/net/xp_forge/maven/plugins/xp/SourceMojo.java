/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import net.xp_forge.maven.plugins.xp.SourceNoForkMojo;

/**
 * Pack project sources
 *
 * This goal forks the build lifecycle upto and including the "generate-sources" phase
 *
 * @goal source
 * @execute phase="generate-sources"
 * @requiresDirectInvocation
 * @since 3.2.0
 */
public class SourceMojo extends SourceNoForkMojo {

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
