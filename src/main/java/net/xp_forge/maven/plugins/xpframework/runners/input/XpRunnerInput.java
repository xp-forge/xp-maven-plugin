/**
 * This file is part of the XP-Framework
 *
 * Maven XP-Framework plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xpframework.runners.input;

import java.util.ArrayList;

/**
 * Runner input
 *
 */
public class XpRunnerInput extends AbstractClassPathRunnerInput {
  public String className;
  public String code;
  public ArrayList<String> arguments;

  public XpRunnerInput() {
    super();
    this.className= null;
    this.code= null;
    this.arguments= new ArrayList<String>();
  }
}
