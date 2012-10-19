/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.runners.input;

import java.util.List;
import java.util.ArrayList;

/**
 * XP runtime
 * ==========
 *
 * Usage
 * -----
 * Display version and classloader information:
 * $ xp [options] -v
 *
 * Evaluate code:
 * $ xp [options] -e {code}
 *
 * Evaluate code and write result:
 * $ xp [options] -w {code}
 *
 * Evaluate code and dump result:
 * $ xp [options] -d {code}
 *
 * Reflect a class:
 * $ xp [options] -r {qualified.class.Name}
 *
 * Running classes:
 * $ xp [options] {qualified.class.Name} [arg [arg [...]]]
 *
 * Running XARs:
 * $ xp [options] -xar {app.xar} [arg [arg [...]]]
 *
 * Options
 * -------
 * -cp {path}: Add {path} to classpath
 *
 */
public class XpRunnerInput extends AbstractRunnerInput {
  public String className;
  public String code;
  public List<String> arguments;

  /**
   * Constructor
   *
   */
  public XpRunnerInput() {
    super();
    this.arguments= new ArrayList<String>();
  }
}
