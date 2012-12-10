/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.input.xp;

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;

/**
 * Runner input with classpath configuration
 *
 */
public class AbstractClasspathRunnerInput {
  public List<String> classpaths;
  public boolean verbose;

  /**
   * Constructor
   *
   */
  public AbstractClasspathRunnerInput() {
    this.classpaths = new ArrayList<String>();
    this.verbose    = false;
  }

  /**
   * Setter for classpaths
   *
   * @param  java.util.Set<org.apache.maven.artifact.Artifact> artifacts
   * @return void
   */
  public void addClasspath(Set<Artifact> artifacts) {
    if (null == artifacts) return;

    for (Artifact artifact : artifacts) {
      this.addClasspath(artifact);
    }
  }

  /**
   * Setter for classpaths
   *
   * @param  org.apache.maven.artifact.Artifact artifact
   * @return void
   */
  public void addClasspath(Artifact artifact) {
    if (null == artifact) return;

    // Skip non-xar artifacts
    if (!artifact.getType().equals("xar")) return;

    // Add to classpath
    if (null != artifact.getClassifier() && artifact.getClassifier().equals("patch")) {
      this.addClasspath(artifact.getFile().getAbsolutePath(), true);
    } else {
      this.addClasspath(artifact.getFile(), false);
    }
  }

  /**
   * Setter for classpaths
   *
   * @param  java.util.List<java.lang.String> classpaths
   * @return void
   */
  public void addClasspath(List<String> classpaths) {
    if (null == classpaths) return;
    for (String classpath : classpaths) {
      this.addClasspath(classpath);
    }
  }

  /**
   * Setter for classpaths
   *
   * @param  java.lang.String classpath
   * @param  boolean isPatch
   * @return void
   */
  public void addClasspath(String classpath, boolean isPatch) {
    if (null == classpath) return;

    // Check classpath not added twice
    for (String cp : this.classpaths) {
      if (cp.equals(classpath)) return;
    }

    // Add to list
    if (isPatch) {
      this.classpaths.add(0, "!" + classpath);
    } else {
      this.classpaths.add(classpath);
    }
  }

  /**
   * Setter for classpaths
   *
   * @param  java.lang.String classpath
   * @return void
   */
  public void addClasspath(String classpath) {
    this.addClasspath(classpath, false);
  }

  /**
   * Setter for classpaths
   *
   * @param  java.io.File file Archive to add to classpath
   * @param  boolean isPatch
   * @return void
   */
  public void addClasspath(File file, boolean isPatch) {
    if (null == file || !file.exists()) return;

    // Add to list
    this.addClasspath(file.getAbsolutePath(), isPatch);
  }

  /**
   * Setter for classpaths
   *
   * @param  java.io.File file Archive to add to classpath
   * @param  boolean isPatch
   * @return void
   */
  public void addClasspath(File file) {
    this.addClasspath(file, false);
  }
}
