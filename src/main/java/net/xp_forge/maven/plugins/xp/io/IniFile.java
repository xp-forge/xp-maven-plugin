/**
 * This file is part of the XP-Framework
 *
 * XP-Framework Maven plugin
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.io;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Simple class to read/write ini files
 *
 */
public class IniFile {
  private static final int BUFF_SIZE= 1024;

  private Properties globalProperties;
  private Map<String, Properties> properties;
  private String comment;

  /**
   * Constructor
   *
   */
  public IniFile() {
    this.globalProperties= new Properties();
    this.properties= new HashMap<String, Properties>();
  }

  /**
   * Load ini as properties from input file
   *
   * @param  java.io.File file
   * @return void
   * @throw  java.io.IOException
   */
  public void load(File file) throws IOException {
    this.load(new FileInputStream(file));
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
   * Load ini as properties from input stream
   *
   * @param  java.io.InputStream in
   * @return void
   * @throw  java.io.IOException when I/O errors occur
   */
  public void load(InputStream in) throws IOException {
    byte[] buffer = new byte[IniFile.BUFF_SIZE];
    int bytesRead = in.read(buffer, 0, IniFile.BUFF_SIZE);

    ParseState state      = ParseState.NORMAL;
    boolean isSectionOpen = false;
    String currentSection = null;
    String key            = null;
    String value          = null;
    StringBuilder sb      = new StringBuilder();

    while (bytesRead >= 0) {
      for (int i= 0; i < bytesRead; i++) {
        char c= (char) buffer[i];

        // Comment, skip to EOL
        if (ParseState.COMMENT == state) {
          if (('\r' == c) ||('\n' == c)) {
            state= ParseState.NORMAL;
          } else {
            continue;
          }
        }

        if (ParseState.ESCAPE == state) {
          sb.append(c);

          // If the EOL is \r\n, \ escapes both chars
          if ('\r' == c) {
            state= ParseState.ESC_CRNL;
          } else {
            state= ParseState.NORMAL;
          }
          continue;
        }

        switch (c) {

          // Section begin
          case '[':
            sb= new StringBuilder();
            isSectionOpen= true;
            break;

          // Section end
          case ']':
            if (isSectionOpen) {
              currentSection= sb.toString().trim();
              sb= new StringBuilder();
              this.properties.put(currentSection, new Properties());
              isSectionOpen= false;
            } else {
              sb.append(c);
            }
            break;

          // Escape char, take the next char as is
          case '\\':
            state= ParseState.ESCAPE;
            break;

          // Comment - only at the beginning of a line
          case '#':
          case ';':
            if (null == key && 0 == sb.length()) {
              state= ParseState.COMMENT;
              break;
            }

          // Assignment operator
          case '=':
            if (null == key) {
              key= sb.toString().trim();
              sb= new StringBuilder();
            } else {
              sb.append(c);
            }
            break;

          // Newline
          case '\r':
          case '\n':
            if ((ParseState.ESC_CRNL == state) && ('\n' == c)) {
              sb.append(c);
              state= ParseState.NORMAL;
            } else {
              if (sb.length() > 0) {
                value= sb.toString().trim();
                sb= new StringBuilder();

                if (null != key) {
                  if (null == currentSection) {
                    this.setProperty(key, value);
                  } else {
                    this.setProperty(currentSection, key, value);
                  }
                }
              }
              key   = null;
              value = null;
            }
            break;

          default:
            sb.append(c);
        }
      }

      // Read more from file
      bytesRead= in.read(buffer, 0, IniFile.BUFF_SIZE);
    }
  }

  /**
   * Pack Strings array to String
   *
   * @param  java.util.List<java.lang.String> list
   * @return java.lang.String
   */
  private String packListToString(List<String> list) {

    // Sanity check
    if (null == list) return null;

    // Init string
    StringBuffer buff= new StringBuffer();
    buff.append("Array[");

    Iterator it= list.iterator();
    while (it.hasNext()) {
      buff.append((String)it.next());
      if (it.hasNext()) buff.append("•");
    }

    // Return array
    buff.append("]");
    return buff.toString();
  }

  /**
   * Check if the provided String contains a packed list
   *
   * @param  java.lang.String str
   * @return boolean
   */
  private boolean isPackedList(String str) {
    return (str.startsWith("Array[") && str.endsWith("]"));
  }

  /**
   * Unpack String to Strings array
   *
   * @param  java.lang.String str
   * @return java.util.List<java.lang.String>
   */
  private List<String> unpackStringToList(String str) {

    // Sanity check
    if (null == str) return null;

    // String is not packed
    if (!this.isPackedList(str)) {
      List<String> retVal= new ArrayList<String>();
      retVal.add(str);
      return retVal;
    }

    // Explode
    return Arrays.asList(str.substring(6, str.length() - 1).split("•"));
  }

  /**
   * Get global property by name
   *
   * @param  java.lang.String name
   * @return java.lang.String null if the specified property does not exist
   */
  public String getProperty(String name) {
    return this.globalProperties.getProperty(name);
  }

  /**
   * Set global property
   *
   * @param  java.lang.String name
   * @param  java.lang.String value
   * @return void
   */
  public void setProperty(String name, String value) {
    this.globalProperties.setProperty(name, value);
  }

  /**
   * Set global property
   *
   * @param  java.lang.String name
   * @param  java.util.List<java.lang.String> values
   * @return void
   */
  public void setProperty(String name, List<String> values) {
    this.setProperty(name, this.packListToString(values));
  }

  /**
   * Return iterator over global properties
   *
   * @return java.util.Iterator<java.lang.String>
   */
  @SuppressWarnings("unchecked")
  public Iterator<String> properties() {
    return new IteratorFromEnumeration<String>(
      (Enumeration<String>)this.globalProperties.propertyNames()
    );
  }

  /**
   * Get property value for specified section and name
   *
   * @param  java.lang.String section
   * @param  java.lang.String name
   * @return java.lang.String null if the specified property does not exist
   */
  public String getProperty(String section, String name) {
    Properties p= this.properties.get(section);
    return null == p ? null : p.getProperty(name);
  }

  /**
   * Set property value for specified section and name
   *
   * Note: Creates section if not existing
   *
   * @param  java.lang.String section
   * @param  java.lang.String name
   * @param  java.lang.String value
   * @return void
   */
  public void setProperty(String section, String name, String value) {
    Properties p= this.properties.get(section);
    if (null == p) {
      p= new Properties();
      this.properties.put(section, p);
    }
    p.setProperty(name, value);
  }

  /**
   * Set property value for specified section and name
   *
   * Note: Creates section if not existing
   *
   * @param  java.lang.String section
   * @param  java.lang.String name
   * @param  java.util.List<java.lang.String> values
   * @return void
   */
  public void setProperty(String section, String name, List<String> values) {
    this.setProperty(section, name, this.packListToString(values));
  }

  /**
   * Return property iterator for specified section. Returns null if
   * specified section does not exist
   *
   * @param  java.lang.String section
   * @return java.util.Iterator<java.lang.String>
   */
  @SuppressWarnings("unchecked")
  public Iterator<String> properties(String section) {
    Properties p= this.properties.get(section);
    if (null == p) {
      return null;
    }
    return new IteratorFromEnumeration<String>(
      (Enumeration<String>)p.propertyNames()
    );
  }

  /**
   * Return iterator of names of section
   *
   * @return java.util.Iterator<java.lang.String>
   */
  public Iterator<String> sections() {
    return this.properties.keySet().iterator();
  }

  /**
   * Dumps properties to specified file
   *
   * @param  java.io.File file
   * @return void
   */
  public void dump(File file) throws IOException {
    PrintStream out= new PrintStream(file, "UTF-8");

    // Comment
    if (null != this.comment) {
      out.printf("; %s", this.comment);
      out.println();
    }

    // Global properties
    Iterator<String> props= this.properties();
    while (props.hasNext()) {
      String name= props.next();
      for (String val : this.unpackStringToList(this.getProperty(name))) {
        out.printf("%s=%s", name, IniFile.dumpEscape(val));
        out.println();
      }
    }

    // Sections
    Iterator<String> sections= this.sections();
    while (sections.hasNext()) {
      String section= sections.next();
      out.println();
      out.printf("[%s]", section);
      out.println();
      props= this.properties(section);
      while (props.hasNext()) {
        String name= props.next();
        for (String val : this.unpackStringToList(this.getProperty(section, name))) {
          out.printf("%s=%s", name, IniFile.dumpEscape(val));
          out.println();
        }
      }
    }

    // Close stream
    out.flush();
    out.close();
  }

  private static String dumpEscape(String s) {
    return s;
//    return s.replaceAll("\\\\", "\\\\\\\\")
//      .replaceAll(";", "\\\\;")
//      .replaceAll("#", "\\\\#")
//      .replaceAll("(\r?\n|\r)", "\\\\$1");
  }

  /**
   * Private class used to coerce java.util.Enumerator to java.util.Iterator
   *
   */
  private static class IteratorFromEnumeration<E> implements Iterator {
    private Enumeration<E> e;

    /**
     * Constructor
     *
     * @param java.util.Enumeration<E> e
     */
    public IteratorFromEnumeration(Enumeration<E> e) {
      this.e = e;
    }

    /**
     * {@inheritDoc}
     *
     */
    public boolean hasNext() {
      return this.e.hasMoreElements();
    }

    /**
     * {@inheritDoc}
     *
     */
    public E next() {
      return this.e.nextElement();
    }

    /**
     * {@inheritDoc}
     *
     */
    public void remove() {
      throw new UnsupportedOperationException("Can't change underlying enumeration");
    }
  }

  enum ParseState {
    NORMAL,
    ESCAPE,
    ESC_CRNL,
    COMMENT
  }
}
