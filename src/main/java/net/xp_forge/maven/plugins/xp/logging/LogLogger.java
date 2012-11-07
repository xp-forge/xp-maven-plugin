/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.logging;

import org.apache.maven.plugin.logging.Log;

import org.codehaus.plexus.logging.Logger;

/**
 * Logger forwarding everything to the specified [org.apache.maven.plugin.logging.Log] instance
 *
 */
public final class LogLogger implements Logger {
  private int threshold;
  private Log log;

  /**
   * Constructor
   *
   * @param  org.apache.maven.plugin.logging.Log l
   * @throw  lang.IllegalArgumentException when an null Log instance is passed
   */
  public LogLogger(Log logger) {

    // Sanity check
    if (null == logger) {
      throw new IllegalArgumentException("Log cannot be null");
    }

    this.threshold = Logger.LEVEL_DEBUG;
    this.log       = logger;
  }

  /**
   * {@inheritDoc}
   *
   */
  public boolean isDebugEnabled() {
    return this.log.isDebugEnabled();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void debug(String message, Throwable error) {
    this.log.debug(message, error);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void debug(String message) {
    this.log.debug(message);
  }

  /**
   * {@inheritDoc}
   *
   */
  public boolean isInfoEnabled() {
    return this.log.isInfoEnabled();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void info(String message, Throwable error) {
    this.log.info(message, error);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void info(String message) {
    this.log.info(message);
  }

  /**
   * {@inheritDoc}
   *
   */
  public boolean isWarnEnabled() {
    return this.log.isWarnEnabled();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void warn(String message, Throwable error) {
    this.log.warn(message, error);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void warn(String message) {
    this.log.warn(message);
  }

  /**
   * {@inheritDoc}
   *
   */
  public boolean isErrorEnabled() {
    return this.log.isErrorEnabled();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void error(String message, Throwable error) {
    this.log.error(message, error);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void error(String message) {
    this.log.error(message);
  }

  /**
   * {@inheritDoc}
   *
   */
  public boolean isFatalErrorEnabled() {
    return this.log.isErrorEnabled();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void fatalError(String message, Throwable error) {
    this.log.error(message, error);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void fatalError(String message) {
    this.log.error(message);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Logger getChildLogger(String name) {
    return this;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public String getName() {
    return "LogLogger";
  }

  /**
   * {@inheritDoc}
   *
   */
  public void setThreshold(int threshold) {
    this.threshold= threshold;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public int getThreshold() {
    return this.threshold;
  }
}