/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

/**
 * Utility class
 *
 */
public final class MavenResourceUtils {

  /**
   * Utility classes should not have a public or default constructor
   *
   */
  private MavenResourceUtils() {
  }

  /**
   * Wrapper over MavenResourcesExecution: single resource
   *
   * @param  org.apache.maven.model.Resource resource
   * @param  java.io.File targetDirectory
   * @param  org.apache.maven.project.MavenProject project
   * @param  org.apache.maven.execution.MavenSession session
   * @param  org.apache.maven.shared.filtering.MavenResourcesFiltering mavenResourcesFiltering
   * @throws java.io.IOException when I/O errors occur
   */
  public static void copyResource(Resource resource, File targetDirectory, MavenProject project, MavenSession session, MavenResourcesFiltering mavenResourcesFiltering) throws IOException {
    List<Resource> resources= new ArrayList<Resource>();
    resources.add(resource);
    MavenResourceUtils.copyResources(resources, targetDirectory, project, session, mavenResourcesFiltering);
  }

  /**
   * Wrapper over MavenResourcesExecution: multiple resources
   *
   * @param  java.util.List<org.apache.maven.model.Resource> resources
   * @param  java.io.File targetDirectory
   * @param  org.apache.maven.project.MavenProject project
   * @param  org.apache.maven.execution.MavenSession session
   * @param  org.apache.maven.shared.filtering.MavenResourcesFiltering mavenResourcesFiltering
   * @throws java.io.IOException when I/O errors occur
   */
  public static void copyResources(List<Resource> resources, File targetDirectory, MavenProject project, MavenSession session, MavenResourcesFiltering mavenResourcesFiltering) throws IOException {

    // Check no resources added
    if (resources.isEmpty()) return;

    // Init executor
    MavenResourcesExecution mavenResourcesExecution= new MavenResourcesExecution(
      resources,
      targetDirectory,
      project,
      "UTF-8",
      null,
      Collections.EMPTY_LIST,
      session
    );

    // Configure executor
    mavenResourcesExecution.setEscapeWindowsPaths(true);
    mavenResourcesExecution.setInjectProjectBuildFilters(false);
    mavenResourcesExecution.setOverwrite(true);
    mavenResourcesExecution.setIncludeEmptyDirs(false);
    mavenResourcesExecution.setSupportMultiLineFiltering(false);

    // Filter resources
    try{
      mavenResourcesFiltering.filterResources(mavenResourcesExecution);

    } catch (MavenFilteringException ex) {
      throw new IOException("Failed to copy resources", ex);
    }
  }
}
