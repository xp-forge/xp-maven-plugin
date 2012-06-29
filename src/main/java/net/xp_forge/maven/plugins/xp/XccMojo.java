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

import net.xp_forge.maven.plugins.xp.AbstractXccMojo;
import net.xp_forge.maven.plugins.xp.util.FileUtils;

/**
 * Run XP Framework XCC compiler (compile .xp sources)
 *
 * @goal compile
 * @requiresDependencyResolution compile
 */
public class XccMojo extends AbstractXccMojo {

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
  public void execute() throws MojoExecutionException {
    getLog().info(LINE_SEPARATOR);
    getLog().info("COPY PHP SOURCES");
    getLog().info(LINE_SEPARATOR);

    // Copy hard-coded-path raw PHP files
    if (null == this.phpSourceRoots || this.phpSourceRoots.isEmpty()) {
      this.phpSourceRoots= new ArrayList<String>();
      this.phpSourceRoots.add("src" + File.separator + "main" + File.separator + "php");
    }
    this.copyPhpSources(this.phpSourceRoots, this.classesDirectory);

    getLog().info(LINE_SEPARATOR);
    getLog().info("COMPILE XP SOURCES");
    getLog().info(LINE_SEPARATOR);

    // Cleanup source roots
    this.compileSourceRoots= FileUtils.filterEmptyDirectories(this.compileSourceRoots);
    if (this.compileSourceRoots.isEmpty()) {
      getLog().info("There are no sources to compile");
      return;
    }

    // Let [xcc] know where to get sources from
    for (String compileSourceRoot : this.compileSourceRoots) {
      this.addSourcepath(compileSourceRoot);
    }

    // Also add the PHP sources to classpath
    for (String phpSourceRoot : this.phpSourceRoots) {
      this.addClasspath(phpSourceRoot);
    }

    // Execute [xcc]
    this.executeXcc(this.compileSourceRoots, this.classesDirectory);
    getLog().info(LINE_SEPARATOR);
  }
}
