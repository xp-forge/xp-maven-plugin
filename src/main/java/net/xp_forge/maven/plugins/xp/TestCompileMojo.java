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

import org.apache.maven.plugin.MojoExecutionException;
import net.xp_forge.maven.plugins.xp.util.FileUtils;

/**
 * Run XP Framework XCC compiler (compile test .xp sources)
 *
 * @goal test-compile
 * @requiresDependencyResolution
 */
public class TestCompileMojo extends AbstractCompileMojo {

  /**
   * Set this to 'true' to bypass unit tests entirely
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * The source directories containing the raw PHP sources to be copied
   * Default value: [src/test/php]
   *
   * @parameter
   */
  private List<String> testPhpSourceRoots;

  /**
   * The source directories containing the sources to be compiled
   *
   * @parameter default-value="${project.testCompileSourceRoots}"
   * @required
   * @readonly
   */
  private List<String> testCompileSourceRoots;

  /**
   * {@inheritDoc}
   *
   */
  protected List<String> getPhpSourceRoots() {
    return this.testPhpSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected List<String> getCompileSourceRoots() {
    return this.testCompileSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  protected String getAdditionalClasspath() {
    return this.classesDirectory.getAbsolutePath();
  }

  /**
   * {@inheritDoc}
   *
   */
  protected boolean isSkip() {
    return this.skip;
  }
}
