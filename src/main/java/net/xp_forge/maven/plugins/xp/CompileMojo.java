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
   * PHP sources include pattern
   * Default value: [** /*.class.php]
   *
   * @parameter expression="${xp.compile.phpIncludePattern}"
   */
  private String phpIncludePattern;

  /**
   * The source directories containing the sources to be compiled
   * Default value: [src/main/xp]
   *
   * @parameter expression="${project.compileSourceRoots}"
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
    if (null == this.phpSourceRoots || this.phpSourceRoots.isEmpty()) {
      this.phpSourceRoots= new ArrayList<String>();
      this.phpSourceRoots.add("src" + File.separator + "main" + File.separator + "php");
    }
    return this.phpSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getPhpIncludePattern() {
    return null == this.phpIncludePattern ? "**/*.class.php" : this.phpIncludePattern;
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
  protected File getClassesDirectory() {
    return this.classesDirectory;
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
