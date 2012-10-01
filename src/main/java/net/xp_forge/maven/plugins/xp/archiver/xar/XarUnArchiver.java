/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.archiver.xar;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.AbstractUnArchiver;

import net.xp_forge.xar.XarEntry;
import net.xp_forge.xar.XarArchive;

/**
 * A plexus unarchiver implementation for XAR file format
 *
 */
public class XarUnArchiver extends AbstractUnArchiver {
   private XarArchive archive;

    /**
     * Constructor
     *
     */
    public XarUnArchiver() {
    }

    /**
     * Constructor
     *
     */
    public XarUnArchiver(File srcFile) {
      super(srcFile);
    }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected void execute() throws ArchiverException {
    File srcFile       = this.getSourceFile();
    File destDirectory = this.getDestDirectory();
    //getLogger().debug("Expanding [" + srcFile + "] into [" + destDirectory + "]");

    // Load archive
    try {
      this.archive= new XarArchive(srcFile);
    } catch (IOException ex) {
      throw new ArchiverException("Cannot load XAR archive [" + srcFile + "]", ex);
    }

    // Create destDirectory
    if (!destDirectory.exists()) {
      destDirectory.mkdirs();
    }

    // Extract archive entries
    for (XarEntry entry : archive.getEntries()) {
      File outFile= new File(destDirectory, entry.getName().replace('/', File.separatorChar));
      try {
        //getLogger().debug("Expanding [" + entry.getName() + "] into [" + outFile + "]");
        this.setFileContents(outFile, entry.getInputStream());
      } catch (IOException ex) {
        throw new ArchiverException("Error while expanding [" + entry.getName() + "]", ex);
      }
    }

    // Cleanup
    this.archive= null;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  protected void execute(String path, File outputDirectory) throws ArchiverException {
    File srcFile= this.getSourceFile();
    String archivePath= path;
    //getLogger().debug("Expanding [" + srcFile + "#" + archivePath + "] into [" + outputDirectory + "]");

    // Remove starting slash from path; if the case
    if (archivePath.startsWith("/")) {
      archivePath= archivePath.substring(1);
    }

    // Remove trailing slash from path; if the case
    if (archivePath.endsWith("/")) {
      archivePath= archivePath.substring(0, archivePath.length() - 1);
    }

    // Load archive
    try {
      this.archive= new XarArchive(srcFile);
    } catch (IOException ex) {
      throw new ArchiverException("Cannot load XAR archive [" + srcFile + "]", ex);
    }

    // Create outputDirectory
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    // Extract archive entries that start with archivePath
    File outFile;
    for (XarEntry entry : archive.getEntries()) {
      String entryName= entry.getName();

      // Full entry match
      if (entryName.equals(archivePath)) {
        int pos= entryName.lastIndexOf('/');
        if (-1 == pos) {
          outFile= new File(outputDirectory, entryName);
        } else {
          outFile= new File(outputDirectory, entryName.substring(pos + 1));
        }

      // Directory match
      } else if (entryName.startsWith(archivePath + "/")) {
        outFile= new File(outputDirectory, entryName.substring(archivePath.length() + 1));

      // No match; try next entry
      } else {
        continue;
      }

      try {
        //getLogger().debug("Expanding [" + entry.getName() + "] into [" + outputDirectory + "]");
        this.setFileContents(outFile, entry.getInputStream());
      } catch (IOException ex) {
        throw new ArchiverException("Error while expanding [" + entry.getName() + "]", ex);
      }
    }

    // Cleanup
    this.archive= null;
  }

  /**
   * Save contents to the specified file
   *
   * @param  java.io.File file
   * @param  java.io.InputStream is
   * @throw  java.io.IOException when I/O errors occur
   */
  private void setFileContents(File file, InputStream is) throws IOException {

    // Make dirs
    File parent= file.getParentFile();
    if (null != parent && !parent.exists()) {
      parent.mkdirs();
    }

    // Save InputStream contents to file
    FileOutputStream os= null;
    try {
      os= new FileOutputStream(file);
      byte[] buffer= new byte[2048];
      int bytesRead;
      while (-1 != (bytesRead= is.read(buffer, 0, 2048))) {
        os.write(buffer, 0, bytesRead);
      }
      is.close();
      os.flush();
      os.close();
    } finally {
      if (null != os) {
        os.close();
      }
    }
  }
}
