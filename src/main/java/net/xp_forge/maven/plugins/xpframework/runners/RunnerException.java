/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework.runners;

/**
 * Runner exeception
 *
 */
public class RunnerException extends java.lang.Exception {

  /**
   * Constructor
   *
   */
  public RunnerException(java.lang.String message) {
    super(message);
  }

  /**
   * Constructor
   *
   */
  public RunnerException(java.lang.String message, java.lang.Throwable cause) {
    super(message, cause);
  }
}
