/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.exec.input.svn;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Runner input for svn runner
 *
 */
public class SvnRunnerInput {
  public String command;
  public String remoteUrl;
  public File localDirectory;
  public String username;
  public String password;
  public String message;
  public boolean force          = false;
  public boolean nonInteractive = false;
  public List<String> arguments = new ArrayList<String>();

  /**
   * Constructor
   *
   * @param  java.lang.String command
   */
  public SvnRunnerInput(String command) {
    this.command= command;
  }

  /**
   * Add an argument
   *
   * @param  java.lang.String argument
   * @return void
   */
  public void addArgument(String argument) {
    this.arguments.add(argument);
  }

  /**
   * Add a list of arguments
   *
   * @param  java.util.List<java.lang.String> arguments
   * @return void
   */
  public void addArguments(List<String> arguments) {
    this.arguments.addAll(arguments);
  }

  /**
   * Add an array of arguments
   *
   * @param  java.lang.String[] arguments
   * @return void
   */
  public void addArguments(String[] arguments) {
    this.addArguments(Arrays.asList(arguments));
  }
}
