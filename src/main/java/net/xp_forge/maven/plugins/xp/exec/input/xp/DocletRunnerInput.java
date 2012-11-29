/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.input.xp;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;

/**
 * Command line doclet runner
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Usage:
 * ========================================================================
 *   doclet [options] class [doclet-options] name [name [name...]]
 * ========================================================================
 *
 * Class is the fully qualified class name of a doclet class.
 *
 * Options can be one or more of:
 *
 *   * -sp sourcepath: Sets sourcepath - paths in which the doclet
 *     implementation will search for classes. Multiple paths are
 *     separated by the path separator char.
 *   * -cp classpath: Adds classpath element in which the class
 *     loader will search for the doclet class.
 *
 * Doclet-Options depend on the doclet implementation.
 *
 * Names can be one or more of:
 *
 *   * {package.name}.*: All classes inside a given package
 *   * {package.name}.**: All classes inside a given package and all it subpackages
 *   * {qualified.class.Name}: A fully qualified class name
 *
 */
public class DocletRunnerInput extends AbstractClasspathRunnerInput {
  public String              docletClass;
  public Map<String, String> docletOptions;
  public List<File>          sourcepaths;
  public List<String>        names;

  /**
   * Constructor
   *
   * @param  java.lang.String docletClass
   */
  public DocletRunnerInput(String docletClass) {
    super();
    this.docletClass   = docletClass;
    this.docletOptions = new HashMap<String, String>();
    this.sourcepaths   = new ArrayList<File>();
    this.names         = new ArrayList<String>();
  }

  /**
   * Setter for docletOptions
   *
   * @param  java.lang.String key
   * @param  java.lang.String val
   * @return void
   */
  public void addDocletOption(String key, String val) {

    // Invalid key / val
    if (null == key || 0 == key.trim().length() || null == val || 0 == val.trim().length()) return;

    // Add to map
    this.docletOptions.put(key, val);
  }

  /**
   * Setter for sourcepaths
   *
   * @param  java.io.File sourcepath Sourcepath to add
   * @return void
   */
  public void addSourcepath(File sourcepath) {

    // Invalid path
    if (!sourcepath.exists()) return;

    // Check path not added twice
    String sourcepathPath= sourcepath.getAbsolutePath();
    for (File sp : this.sourcepaths) {
      if (sp.getAbsolutePath().equals(sourcepathPath)) return;
    }

    // Add to list
    this.sourcepaths.add(sourcepath);
  }

  /**
   * Setter for sourcepaths
   *
   * @param  java.util.Set<org.apache.maven.artifact.Artifact> artifacts
   * @return void
   */
  public void addSourcepath(Set<Artifact> artifacts) {
    for (Artifact artifact : artifacts) {

      // Skip non-xar artifacts
      if (!artifact.getType().equals("xar")) continue;

      // Add to classpath
      this.addSourcepath(artifact.getFile());
    }
  }

  /**
   * Setter for names
   *
   * @param  java.lang.String name Name to add
   * @return void
   */
  public void addName(String name) {

    // Invalid name
    if (null == name || 0 == name.trim().length()) return;

    // Check name not added twice
    for (String n : this.names) {
      if (n.equals(name)) return;
    }

    // Add to list
    this.names.add(name);
  }
}
