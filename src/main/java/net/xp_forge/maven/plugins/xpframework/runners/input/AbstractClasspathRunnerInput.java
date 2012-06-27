/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework.runners.input;

import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;

/**
 * Runner input with classpath configuration
 *
 */
public class AbstractClasspathRunnerInput {
  public List<File> classpaths;
  public boolean verbose;


  /**
   * Constructor
   *
   */
  public AbstractClasspathRunnerInput() {
    this.classpaths = new ArrayList<File>();
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

      // Ignore non-XAR artifacts
      if (!artifact.getType().equalsIgnoreCase("xar")) continue;

      // Ignore "net.xp_framework:core" and "net.xp_framework:tools"
      if (
        artifact.getGroupId().equals("net.xp-framework") &&
        (
          artifact.getArtifactId().equals("core") ||
          artifact.getArtifactId().equals("tools") ||
          artifact.getArtifactId().equals("language")
        )
      ) {
        continue;
      }

      // Add to classpath
      this.addClasspath(artifact.getFile());
    }
  }

  /**
   * Setter for classpaths
   *
   * @param  java.io.File file XAR to add to classpath
   * @return void
   */
  public void addClasspath(File file) {

    // Invalid path
    if (!file.exists()) {
      return;
    }

    // Check classpath not added twice
    String filepath= file.getAbsolutePath();
    for (File classpath : this.classpaths) {
      if (classpath.getAbsolutePath().equals(filepath)) {
        return;
      }
    }

    // Add to list
    this.classpaths.add(file);
  }
}
