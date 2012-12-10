/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.io;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;

/**
 * Simple class to read/write pth files
 *
 */
public class PthFile {
  private List<String> entries;
  private String comment;
  private boolean useBang= true;

  /**
   * Constructor
   *
   */
  public PthFile() {
    this.entries= new ArrayList<String>();
  }

  /**
   * Set file comment
   *
   * @param  java.lang.String comment
   * @return void
   */
  public void setComment(String comment) {
    this.comment= comment;
  }

  /**
   * Whether to use the bang (exclamation mark) in front of "patch" entries
   *
   * @param  boolean bang
   * @return void
   */
  public void useBang(boolean bang) {
    this.useBang= bang;
  }

  /**
   * Add a new entry to this pth file
   *
   * @param  java.lang.String entry
   * @return void
   */
  public void addEntry(String entry) {
    this.addEntry(entry, false);
  }


  /**
   * Add a new entry to this pth file
   *
   * @param  java.lang.String entry
   * @param  boolean isPatch
   * @return void
   */
  public void addEntry(String entry, boolean isPatch) {
    if (this.entries.contains(entry)) return;

    if (isPatch) {
      this.entries.add(0, (this.useBang ? "!" : "") + entry);
    } else {
      this.entries.add(entry);
    }
  }

  /**
   * Add new entries to this pth file
   *
   * @param  java.util.List<java.lang.String> entries
   * @return void
   */
  public void addEntries(List<String> entries) {
    for (String entry : entries) {
      this.entries.add(entry);
    }
  }

  /**
   * Add a new file entry to this pth file
   *
   * @param  java.io.File file
   * @return void
   */
  public void addFileEntry(File file) {
    this.addFileEntry(file, false);
  }

  /**
   * Add a new file entry to this pth file
   *
   * @param  java.io.File file
   * @param  boolean isPatch
   * @return void
   */
  public void addFileEntry(File file, boolean isPatch) {
    this.addEntry(file.getAbsolutePath(), isPatch);
  }

  /**
   * Add new file entries to this pth file
   *
   * @param  java.util.List<java.io.File> files
   * @return void
   */
  public void addFileEntries(List<File> files) {
    for (File file : files) {
      this.addFileEntry(file);
    }
  }

  /**
   * Add new artifact entry to this pth file. Non xar artifacts will be ignored.
   *
   * @param  org.apache.maven.artifact.Artifact artifact
   * @return void
   */
  public void addArtifactEntry(Artifact artifact) {
    if (!artifact.getType().equals("xar")) return;

    // Add to list
    boolean isPatch= (null != artifact.getClassifier() && artifact.getClassifier().equals("patch"));
    this.addFileEntry(artifact.getFile(), isPatch);
  }

  /**
   * Add new artifact entries to this pth file
   *
   * @param  java.util.List<org.apache.maven.artifact.Artifact> artifacts
   * @return void
   */
  public void addArtifactEntries(List<Artifact> artifacts) {
    for (Artifact artifact : artifacts) {
      this.addArtifactEntry(artifact);
    }
  }

  /**
   * Dumps entries to specified file
   *
   * @param  java.io.File file
   * @return void
   * @throws java.io.IOException
   */
  public void dump(File file) throws IOException {
    PrintStream out= new PrintStream(file, "UTF-8");

    // Comment
    if (null != this.comment) {
      out.printf("# %s", this.comment);
      out.println();
    }

    // Write entries
    for (String entry : this.entries) {
      out.printf("%s", entry);
      out.println();
    }

    // Close stream
    out.flush();
    out.close();
  }
}
