/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.maven.model.Resource;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import net.xp_forge.maven.plugins.xp.runners.XccRunner;
import net.xp_forge.maven.plugins.xp.runners.RunnerException;
import net.xp_forge.maven.plugins.xp.runners.input.XccRunnerInput;
import net.xp_forge.maven.plugins.xp.util.FileUtils;
import net.xp_forge.maven.plugins.xp.util.MavenResourceUtils;

/**
 * Wrapper around the XP-Framework "XccRunner;" runner
 *
 */
public abstract class AbstractCompileMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {

  /**
   * Display verbose diagnostics
   *
   * The -v argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.verbose}" default-value="false"
   */
  protected boolean verbose;

  /**
   * Add path to classpath
   *
   * The -cp argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.classpaths}"
   */
  protected ArrayList<String> classpaths;

  /**
   * Adds path to source path (source path will equal classpath initially)
   *
   * The -sp argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.sourcepaths}"
   */
  protected ArrayList<String> sourcepaths;

  /**
   * Use emitter, defaults to "source"
   *
   * The -e argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.emitter}"
   */
  protected String emitter;

  /**
   * Use compiler profiles (defaults to ["default"]) - xp/compiler/{profile}.xcp.ini
   *
   * The -p argument for the xcc compiler
   *
   * @parameter expression="${xp.compile.profiles}"
   */
  protected ArrayList<String> profiles;

  /**
   * Get PHP sources
   *
   */
  protected abstract List<String> getPhpSourceRoots();

  /**
   * Get XP sources
   *
   */
  protected abstract List<String> getCompileSourceRoots();

  /**
   * Get additional classpath
   *
   */
  protected abstract String getAdditionalClasspath();

  /**
   * If true, skip compiling
   *
   */
  protected abstract boolean isSkip();

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws MojoExecutionException {

    // Skip tests alltogether?
    if (this.isSkip()) {
      getLog().info("Not compiling sources (maven.test.skip)");
      return;
    }

    // Copy hard-coded-path raw PHP files
    List<String> phpSourceRoots= this.getPhpSourceRoots();
    if (null == phpSourceRoots || phpSourceRoots.isEmpty()) {
      phpSourceRoots= new ArrayList<String>();
      phpSourceRoots.add("src" + File.separator + "main" + File.separator + "php");
    }
    this.copyPhpSources(phpSourceRoots, this.classesDirectory);

    // Cleanup source roots
    List<String> compileSourceRoots= FileUtils.filterEmptyDirectories(this.getCompileSourceRoots());
    if (compileSourceRoots.isEmpty()) {
      getLog().info("There are no sources to compile");
      return;
    }

    // Let [xcc] know where to get sources from
    for (String compileSourceRoot : compileSourceRoots) {
      this.addSourcepath(compileSourceRoot);
    }

    // Also add the PHP sources to classpath
    for (String phpSourceRoot : phpSourceRoots) {
      this.addClasspath(phpSourceRoot);
    }

    // Add additional classpath
    this.addClasspath(this.getAdditionalClasspath());

    // Execute [xcc]
    this.executeXcc(compileSourceRoots, this.classesDirectory);
  }

  /**
   * Execute XP-Framework XCC compiler
   *
   * @param  java.util.List<String> sourceDirectories Source where .xp file are
   * @param  java.io.File classesDirectory Destination where to output compiled classes
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xcc runner failed
   */
  public void executeXcc(List<String> sourceDirectories, File classesDirectory) throws MojoExecutionException {

    // Compile each source root
    Iterator i= sourceDirectories.iterator();
    while (i.hasNext()) {
      this.executeXcc((String)i.next(), classesDirectory);
    }
  }

  /**
   * Execute XP-Framework XCC compiler
   *
   * @param  java.lang.String sourceDirectory Source where .xp file are
   * @param  java.io.File classesDirectory Destination where to output compiled classes
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When execution of the xcc runner failed
   */
  public void executeXcc(String sourceDirectory, File classesDirectory) throws MojoExecutionException {
    Iterator i;

    // Debug info
    getLog().info("Source directory [" + sourceDirectory + "]");
    getLog().debug("Classes directory [" + classesDirectory + "]");
    getLog().debug("Sourcepaths       [" + (null == this.sourcepaths ? "NULL" : this.sourcepaths.toString()) + "]");
    getLog().debug("Classpaths        [" + (null == this.classpaths  ? "NULL" : this.classpaths.toString())  + "]");

    // Prepare xcc input
    XccRunnerInput input= new XccRunnerInput();
    input.verbose= this.verbose;

    // Add dependency classpaths
    input.addClasspath(project.getArtifacts());

    // Add custom classpaths
    input.addClasspath(this.classpaths);

    // Add sourcepaths
    if (null != this.sourcepaths) {
      i= this.sourcepaths.iterator();
      while (i.hasNext()) {
        input.addSourcepath(new File((String)i.next()));
      }
    }

    // Add emitter
    input.emitter= this.emitter;

    // Add profiles
    if (null != this.profiles) {
      i= this.profiles.iterator();
      while (i.hasNext()) {
        input.addProfile((String)i.next());
      }
    }

    // Add outputdir
    input.outputdir= classesDirectory;

    // Add source
    input.addSource(new File(sourceDirectory));

    // Configure [xcc] runner
    File executable= new File(this.runnersDirectory, "xcc");
    XccRunner runner= new XccRunner(executable, input);
    runner.setTrace(getLog());

    // Set runner working directory to [/target]
    try {
      runner.setWorkingDirectory(this.outputDirectory);
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("Cannot set [xcc] runner working directory", ex);
    }

    // Execute runner
    try {
      runner.execute();
    } catch (RunnerException ex) {
      throw new MojoExecutionException("Execution of [xcc] runner failed", ex);
    }
  }

  /**
   * Add an entry to sourcepaths
   *
   * @param  java.lang.String sourcepath
   * @return void
   */
  protected void addSourcepath(String sourcepath) {
    if (null == this.sourcepaths) this.sourcepaths= new ArrayList<String>();

    // Make sourcepath absolute
    String absoluteSourcepath= FileUtils.getAbsolutePath(sourcepath, this.basedir);
    if (null == absoluteSourcepath) return;

    // Add to sourcepaths
    this.sourcepaths.add(absoluteSourcepath);
  }

  /**
   * Add an entry to classpaths
   *
   * @param  java.lang.String classpath
   * @return void
   */
  protected void addClasspath(String classpath) {
    if (null == classpath) return;

    // Init classpaths
    if (null == this.classpaths) this.classpaths= new ArrayList<String>();

    // Make classpath absolute
    String absoluteClasspath= FileUtils.getAbsolutePath(classpath, this.basedir);
    if (null == absoluteClasspath) return;

    // Add to sourcepaths
    this.classpaths.add(absoluteClasspath);
  }

  /**
   * Copy "/src/main|test/php/**.class.php" files "/target/classes|test-classes/"
   *
   * @param  java.util.List<String> phpSourceRoots Source where raw PHP files are
   * @param  java.io.File classesDirectory Destination where to copy PHP files
   * @return void
   * @throws org.apache.maven.plugin.MojoExecutionException When copy of the PHP source files failed
   */
  protected void copyPhpSources(List<String> phpSourceRoots, File classesDirectory) throws MojoExecutionException {

    // Debug info
    getLog().debug("PHP source directories [" + (null == phpSourceRoots ? "NULL" : phpSourceRoots.toString()) + "]");

    // Ignore non-existing raw PHP files
    if (null == phpSourceRoots || phpSourceRoots.isEmpty()) {
      getLog().info("There are no PHP sources to copy");
      return;
    }

    // Create resources
    List<Resource> resources= new ArrayList<Resource>();

    // Build a resource for each phpSourceRoots
    for (String phpSourceRoot : phpSourceRoots) {

      // Check directory exists
      if (null == FileUtils.getAbsolutePath(phpSourceRoot, this.basedir)) {
        getLog().info("Skip non-existing PHP source directory [" + phpSourceRoot + "]");
        continue;
      }

      Resource resource= new Resource();
      resource.addInclude("**/*.class.php");
      resource.setFiltering(false);
      resource.setDirectory(phpSourceRoot);
      resources.add(resource);

      // Filter resources
      try {
        MavenResourceUtils.copyResources(resources, classesDirectory, this.project, this.session, this.mavenResourcesFiltering);

      } catch(IOException ex) {
        throw new MojoExecutionException("Failed to copy PHP sources", ex);
      }
    }
  }
}