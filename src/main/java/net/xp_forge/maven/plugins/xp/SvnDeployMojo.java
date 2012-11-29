/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import net.xp_forge.maven.plugins.xp.SvnDeployNoForkMojo;

/**
 * Deploy the unpacked artifact to SVN
 *
 * This goal forks the build lifecycle upto and including the "package" phase
 *
 * @goal svn-deploy
 * @execute phase="package"
 * @requiresDirectInvocation
 * @since 3.2.0
 */
public class SvnDeployMojo extends SvnDeployNoForkMojo {

  /**
   * The paralel Maven project that was forked before the "xp:svn-deploy" goal was executed
   *
   * We need this to get ${project.artifact} and ${project.attachedArtifacts}
   *
   * @parameter default-value="${executedProject}"
   * @required
   * @readonly
   */
  protected MavenProject executedProject;

  /**
   * Get skip setting
   *
   * For a forked lifecycle, this is always false
   *
   * @return boolean
   */
  @Override
  protected boolean isSkip() {
    return false;
  }

  /**
   * Get project main artifact
   *
   * For a forked lifecycle, get the artifact from ${executedProject}
   *
   */
  @Override
  protected Artifact getProjectArtifact() {
    return this.executedProject.getArtifact();
  }

  /**
   * Get project attached artifacts
   *
   * For a forked lifecycle, get the attached artifacts from ${executedProject}
   *
   */
  @Override
  protected List<Artifact> getProjectAttachedArtifacts() {
    return this.executedProject.getAttachedArtifacts();
  }
}
