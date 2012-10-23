/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec;

/**
 * Runner exception
 *
 */
public class RunnerException extends java.lang.Exception {

  /**
   * Constructor
   *
   * @param  java.lang.String message
   */
  public RunnerException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param  java.lang.String message
   * @param  java.lang.Throwable cause
   */
  public RunnerException(String message, Throwable cause) {
    super(message, cause);
  }
}
