/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;

/**
 * Package classes and resources
 *
 * @goal package
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class PackageMojo extends AbstractPackageMojo {

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected File getClassesDirectory() {
    return this.classesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getClassifier() {
    return this.classifier;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getFormat() {
    if (null != this.format && !this.format.isEmpty()) {
      return this.format;
    }

    // Format is not set; use ${project.packaging}
    return this.project.getPackaging();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getStrategy() {
    return this.strategy;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean getPackDependencies() {
    return this.packDependencies;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean getPackRuntime() {
    return this.packRuntime;
  }
}
