/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.AbstractCompileMojo;

/**
 * Run XP Framework XCC compiler (compile .xp sources)
 *
 * @goal compile
 * @requiresDependencyResolution compile
 */
public class CompileMojo extends AbstractCompileMojo {

  /**
   * The source directories containing the raw PHP sources to be copied
   * Default value: [src/main/php]
   *
   * @parameter
   */
  private List<String> phpSourceRoots;

  /**
   * The source directories containing the sources to be compiled
   *
   * @parameter default-value="${project.compileSourceRoots}"
   * @required
   * @readonly
   */
  private List<String> compileSourceRoots;

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<String> getPhpSourceRoots() {
    return this.phpSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<String> getCompileSourceRoots() {
    return this.compileSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getAdditionalClasspath() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean isSkip() {
    return false;
  }
}
