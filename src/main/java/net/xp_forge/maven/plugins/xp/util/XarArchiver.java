/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.util;

import java.io.File;
import java.io.IOException;

import net.xp_forge.xar.XarEntry;
import net.xp_forge.xar.XarArchive;

import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.util.ArchiveEntryUtils;
import org.codehaus.plexus.archiver.util.ResourceUtils;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

/**
 * A plexus archiver implementation for XAR file format
 *
 */
public class XarArchiver extends AbstractArchiver {
   private XarArchive archive;

  /**
   * {@inheritDoc}
   *
   */
  public void execute() throws ArchiverException, IOException {

    // Get resources
    final ResourceIterator it= getResources();
    if (!it.hasNext()) {
      throw new ArchiverException("No archive entries found");
    }

    // Get destination file
    final File destFile= this.getDestFile();
    if (null == destFile) {
      throw new ArchiverException("Destination must not be null");
    }
    if (destFile.isDirectory()) {
      throw new ArchiverException("Destination must not be a directory");
    }

    // If destination exists, load archive
    if (destFile.exists()) {
      this.archive= new XarArchive(destFile);

    // Create an empty archive
    } else {
      this.archive= new XarArchive();
    }

    // Add resources
    while (it.hasNext()) {
      ArchiveEntry entry= it.next();
      PlexusIoResource entryRes= entry.getResource();

      // Resource is directory
      if (entryRes.isDirectory()) continue;

      // Add file to archive
      getLogger().debug("XAR: Add [" + entry.getName() + "] -> [" + entry.getFile() + "]");
      this.archive.addEntry(new XarEntry(entry.getName(), entry.getFile()));
    }
  }

  /**
   * {@inheritDoc}
   *
   */
  protected void close() throws IOException {
    getLogger().debug("XAR: Close archive [" + this.getDestFile() + "]");
    this.archive.save(this.getDestFile());
  }

  /**
   * {@inheritDoc}
   *
   */
  protected String getArchiveType() {
    return "xar";
  }
}
