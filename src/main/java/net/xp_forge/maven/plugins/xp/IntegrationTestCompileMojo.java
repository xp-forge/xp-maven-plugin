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

import org.apache.maven.artifact.Artifact;

/**
 * Run XP Framework XCC compiler (compile integration test .xp sources)
 *
 * @goal integration-test-compile
 * @requiresDependencyResolution compile
 */
public class IntegrationTestCompileMojo extends AbstractCompileMojo {

  /**
   * Set this to 'true' to bypass integration tests entirely
   * Its use is NOT RECOMMENDED, but quite convenient on occasion
   *
   * @parameter expression="${maven.it.skip}" default-value="false"
   */
  private boolean skip;

  /**
   * The source directories containing the raw PHP sources to be copied
   * Default value: [src/it/php]
   *
   * @parameter
   */
  private List<String> itPhpSourceRoots;

  /**
   * PHP sources include pattern
   * Default value: [** /*.class.php]
   *
   * @parameter expression="${xp.compile.itPhpIncludePattern}"
   */
  private String itPhpIncludePattern;

  /**
   * The source directories containing the sources to be compiled
   * Default value: [src/it/xp]
   *
   * @parameter
   */
  private List<String> itCompileSourceRoots;

  /**
   * @parameter default-value="${project.artifact}"
   * @required
   * @readonly
   */
  private Artifact artifact;

  /**
   * @parameter default-value="${project.attachedArtifacts}
   * @required
   * @readonly
   */
  private List<Artifact> attachedArtifacts;

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<String> getPhpSourceRoots() {
    if (null == this.itPhpSourceRoots || this.itPhpSourceRoots.isEmpty()) {
      this.itPhpSourceRoots= new ArrayList<String>();
      this.itPhpSourceRoots.add("src" + File.separator + "it" + File.separator + "php");
    }
    return this.itPhpSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getPhpIncludePattern() {
    return null == this.itPhpIncludePattern ? "**/*.class.php" : this.itPhpIncludePattern;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected List<String> getCompileSourceRoots() {
    if (null == this.itCompileSourceRoots) {
      this.itCompileSourceRoots= new ArrayList<String>();
      this.itCompileSourceRoots.add(this.project.getProperties().getProperty("project.itSourceDirectory"));
    }
    return this.itCompileSourceRoots;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getAdditionalClasspath() {

    // Add generated artifact to classpath when running integration tests
    Artifact artifact= this.getMainArtifact();
    if (null != artifact && null != artifact.getFile()) {
      return artifact.getFile().getAbsolutePath();
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected File getClassesDirectory() {
    return this.itClassesDirectory;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected boolean isSkip() {
    return this.skip;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected Map<String, String> getAppDirectoriesMap() {
    return null;
  }
}
