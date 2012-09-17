/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.archiver.xar;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import net.xp_forge.xar.XarEntry;
import net.xp_forge.xar.XarArchive;

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
  @Override
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
      String entryName= entry.getName().replace('\\', '/');

      // Resource is directory
      if (entryRes.isDirectory()) continue;

      // Add file to archive
      getLogger().debug("XAR: Add [" + entryName + "] -> [" + entry.getFile() + "]");
      this.archive.addEntry(new XarEntry(entryName, entry.getFile()));
    }
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected void close() throws IOException {
    if (null == this.archive) return;

    getLogger().debug("XAR: Close archive [" + this.getDestFile() + "]");
    this.archive.save(this.getDestFile());
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected String getArchiveType() {
    return "xar";
  }
}
