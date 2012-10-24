/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

/**
 * Runner output
 *
 */
public class RunnerOutput {
  private List<String> lines;

  /**
   * Constructor
   *
   * @param  java.lang.String message
   */
  public RunnerOutput() {
    this.lines= new ArrayList<String>();
  }

  /**
   * Add a line to this output
   *
   * @param  java.lang.String line
   * @return void
   */
  public void addLine(String line) {
    if (null == line) return;
    this.lines.add(line);
  }

  /**
   * Clear all output lines
   *
   * @return void
   */
  public void clear() {
    this.lines.clear();
  }

  /**
   * Return all lines of this output
   *
   * @return java.util.List<java.lang.String>
   */
  public List<String> getLines() {
    return this.lines;
  }

  /**
   * Search all lines of this output for the specified needle
   *
   * @param  java.lang.String needle
   * @return boolean
   */
  public boolean contains(String needle) {
    for (String line : this.lines) {
      if (line.contains(needle)) return true;
    }
    return false;
  }

  /**
   * Return output lines as a string
   *
   * @return java.lang.String
   */
  public String asString() {
    return StringUtils.join(this.lines, "\n");
  }
}
