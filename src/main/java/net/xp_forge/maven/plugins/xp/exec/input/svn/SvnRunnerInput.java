/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.input.svn;

import java.util.List;
import java.util.ArrayList;

/**
 * Runner input for svn runner
 *
 */
public class SvnRunnerInput {
  public String command;
  public List<String> arguments;

  /**
   * Constructor
   *
   */
  public SvnRunnerInput(String command) {
    this.command   = command;
    this.arguments = new ArrayList<String>();
  }
}
