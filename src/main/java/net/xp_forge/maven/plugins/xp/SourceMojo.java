/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.SourceNoForkMojo;

/**
 * Pack project sources
 *
 * This goal forks the build lifecycle
 *
 * @goal source
 * @phase package
 * @execute lifecycle="xar" phase="generate-sources"
 * @requiresDirectInvocation
 * @since 3.1.9
 */
public class SourceMojo extends SourceNoForkMojo {
  // Nothing to do: I'm here just to fork the build lifecycle
}
