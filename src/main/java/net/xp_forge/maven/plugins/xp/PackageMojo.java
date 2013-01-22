/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Package classes and resources
 *
 * @goal package
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class PackageMojo extends AbstractPackageMojo {

  /**
   * Specify what archiver to use. There are 2 options:
   * - zip
   * - xar
   *
   * If not set, ${project.packaging} will be used
   *
   * @parameter expression="${xp.package.format}"
   */
  protected String format;

  /**
   * Packing strategy: specify what type of artifact to build. There are 2 options:
   * - lib
   * - app
   *
   * @parameter expression="${xp.package.strategy}" default-value="lib"
   * @required
   */
  protected String strategy;

  /**
   * Specify if dependencies will also be packed
   *
   * For "app" stragegy, dependencies will be packed to "lib"
   * For "lib" stragegy, dependencies will be merged to "/"
   *
   * @parameter expression="${xp.package.packDependencies}" default-value="false"
   * @required
   */
  protected boolean packDependencies;

  /**
   * Specify if XP-artifacts (core) and the XP-runners should also be packed
   *
   * Bootstrap will be packed inside /lib/bootstrap
   * XP-artifacts will be packed inside /lib/runtime
   *
   * @parameter expression="${xp.package.packRuntime}" default-value="false"
   * @required
   */
  protected boolean packRuntime;

  /**
   * Specify if vendor libraries (inside ${xp.vendorLibDirectory}) should also be packed
   *
   * @parameter expression="${xp.package.packVendorLibs}" default-value="true"
   * @required
   */
  protected boolean packVendorLibs;

  /**
   * Specify main class for this artifact. used when calling [xp -xar artifact.xar]
   *
   * @parameter expression="${xp.package.mainClass}"
   */
  protected String mainClass;

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

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean getPackVendorLibs() {
    return this.packVendorLibs;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<File> getAppDirectories() {
    List<File> retVal= new ArrayList<File>();

    // Iterate on APP_DIRECTORIES_MAP and return the list of unique values
    for (String appDirName: AbstractXpMojo.APP_DIRECTORIES_MAP.values()) {
      File appDir= new File(this.outputDirectory, appDirName);

      // Add to list; if not already added
      if (retVal.contains(appDir)) continue;
      retVal.add(appDir);
    }

    return retVal;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getMainClass() {
    return this.mainClass;
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
