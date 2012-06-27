/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework;

import java.io.File;

import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

/**
 * Base class for all MOJO's
 *
 */
public abstract class AbstractXpFrameworkMojo extends AbstractMojo {
  public static final String LINE_SEPARATOR= "------------------------------------------------------------------------";

  /**
   * The Maven project
   *
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * The Maven session
   *
   * @parameter default-value="${session}"
   * @readonly
   * @required
   */
  protected MavenSession session;

  /**
   * Maven project helper
   *
   * @component
   */
  protected MavenProjectHelper projectHelper;

  /**
   * Maven resource filtering
   *
   * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
   * @required
   */
  protected MavenResourcesFiltering mavenResourcesFiltering;

  /**
   * Project base directory
   *
   * @parameter expression="${basedir}" default-value="${basedir}"
   * @required
   * @readonly
   */
  protected File basedir;

  /**
   * Directory containing the generated XAR
   *
   * @parameter expression="${project.build.directory}"
   * @required
   */
  protected File outputDirectory;

  /**
   * The directory containing generated classes of the project being tested
   * This will be included after the test classes in the test classpath
   *
   * @parameter default-value="${project.build.outputDirectory}"
   */
  protected File classesDirectory;

  /**
   * The directory containing generated test classes of the project being tested
   * This will be included at the beginning of the test classpath
   *
   * @parameter default-value="${project.build.testOutputDirectory}"
   */
  protected File testClassesDirectory;

  /**
   * Whether to use local XP-Framework install or to use bootstrap in [/target]. Default [false].
   *
   * @parameter default-value="${xp.runtime.local}"
   */
  protected boolean local;

  /**
   * Directory where XP runners are located. If not set, runners will be
   * extracted from resources to "${project.build.directory}/bootstrap/runners"
   *
   * @parameter default-value="${xp.runtime.runners.directory}"
   */
  protected File runnersDirectory;

  /**
   * Bootstrap timezone. If not set will use system default
   *
   * @parameter default-value="${xp.runtime.timezone}"
   */
  protected String timezone;

  /**
   * Location of the PHP executable. If not set will search for it in PATH
   *
   * @parameter default-value="${xp.runtime.php}"
   */
  protected File php;

  /**
   * Helper function to find a project dependency
   *
   *
   * @param  java.lang.String groupId
   * @param  java.lang.String artifactId
   * @return org.apache.maven.model.Dependency null if the specified dependency cannot be found
   */
  @SuppressWarnings("unchecked")
  protected Dependency findDependency(String groupId, String artifactId) {
    for (Dependency dependency : (Iterable<Dependency>)this.project.getDependencies()) {
      if (dependency.getGroupId().equals(groupId) && dependency.getArtifactId().equals(artifactId)) {
        return dependency;
      }
    }

    // Specified dependency not found
    return null;
  }

  /**
   * Helper function to find a project artifact
   *
   *
   * @param  java.lang.String groupId
   * @param  java.lang.String artifactId
   * @return org.apache.maven.artifact.Artifact null if the specified artifact cannot be found
   */
  @SuppressWarnings("unchecked")
  protected Artifact findArtifact(String groupId, String artifactId) {
    for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
      if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
        return artifact;
      }
    }

    // Specified artifact not found
    return null;
  }
}
