/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Package classes and resources into a XAR package
 *
 * @goal package
 * @requiresDependencyResolution runtime
 */
public class XarMojo extends AbstractXarMojo {

  /**
   * Include XAR dependencies into the final uber-XAR
   *
   * @parameter expression="${xp.xar.merge}" default-value="false"
   * @required
   */
  protected boolean merge;

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {
    getLog().info(LINE_SEPARATOR);
    getLog().info("BUILD XAR PACKAGE");
    getLog().info(LINE_SEPARATOR);

    // Assemble XAR archive
    File xarFile= AbstractXarMojo.getXarFile(this.outputDirectory, this.finalName, this.classifier);
    this.executeXar(this.classesDirectory, xarFile);
    getLog().info(LINE_SEPARATOR);

    // Merge dependencies into an uber-XAR?
    if (!this.merge) return;

    getLog().info("BUILD UBER-XAR PACKAGE");
    getLog().info(LINE_SEPARATOR);

    // Assemble uber-XAR archive
    File uberXarFile= AbstractXarMojo.getUberXarFile(this.outputDirectory, this.finalName, this.classifier);
    this.executeUberXar(xarFile, uberXarFile);

    getLog().info(LINE_SEPARATOR);
  }
}
