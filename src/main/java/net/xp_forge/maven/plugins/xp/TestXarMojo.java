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
 * Package test classes and resources into a XAR package
 *
 * @goal test-package
 * @requiresDependencyResolution runtime
 */
public class TestXarMojo extends AbstractXarMojo {

  /**
   * Set this to 'true' to bypass unit tests entirely
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  private boolean skip;

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
    getLog().info("BUILD TEST XAR PACKAGE");
    getLog().info(LINE_SEPARATOR);

    // Skip tests alltogether?
    if (this.skip) {
      getLog().info("Not packing test classes (maven.test.skip)");
      return;
    }

    // Assemble test XAR archive
    File testXarFile= AbstractXarMojo.getXarFile(this.outputDirectory, this.finalName + "-test", this.classifier);
    this.executeXar(this.testClassesDirectory, testXarFile);
    getLog().info(LINE_SEPARATOR);

    // Merge dependencies into an uber-XAR?
    if (!this.merge) return;

    getLog().info("BUILD TEST UBER-XAR PACKAGE");
    getLog().info(LINE_SEPARATOR);

    // Assemble uber-XAR archive
    File testUberXarFile= AbstractXarMojo.getUberXarFile(this.outputDirectory, this.finalName + "-test", this.classifier);
    this.executeUberXar(testXarFile, testUberXarFile);

    getLog().info(LINE_SEPARATOR);
  }
}
