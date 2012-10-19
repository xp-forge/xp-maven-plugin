/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.runners.input;

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;

/**
 * Runner input with classpath configuration
 *
 */
public class AbstractRunnerInput {
  public List<String> classpaths;
  public boolean verbose;

  /**
   * Constructor
   *
   */
  public AbstractRunnerInput() {
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
    for (Artifact artifact : artifacts) {

      // Skip non-xar artifacts
      if (!artifact.getType().equals("xar")) continue;

      // Add to classpath
      if (null != artifact.getClassifier() && artifact.getClassifier().equals("patch")) {
        this.addClasspath("!" + artifact.getFile().getAbsolutePath());
      } else {
        this.addClasspath(artifact.getFile());
      }
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
   * @return void
   */
  public void addClasspath(String classpath) {

    // Check classpath not added twice
    for (String cp : this.classpaths) {
      if (cp.equals(classpath)) {
        return;
      }
    }

    // Add to list
    this.classpaths.add(classpath);
  }

  /**
   * Setter for classpaths
   *
   * @param  java.io.File file Archive to add to classpath
   * @return void
   */
  public void addClasspath(File file) {
    if (!file.exists()) return;

    // Add to list
    this.addClasspath(file.getAbsolutePath());
  }
}
