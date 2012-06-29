/**
 * This file is part of the XP-Framework
 *
 * Maven plugin for XP-Framework
 * Copyright (c) 2011, XP-Framework Team
 */
package net.xp_forge.maven.plugins.xp.util;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Simple class to read/write ini files
 *
 */
public class IniProperties {
  private static final int BUFF_SIZE= 1024;

  private Properties globalProperties;
  private Map<String, Properties> properties;

  enum ParseState {
    NORMAL,
    ESCAPE,
    ESC_CRNL,
    COMMENT
  }

  /**
   * Constructor
   *
   */
  public IniProperties() {
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
   * Load ini as properties from input stream
   *
   * @param  java.io.InputStream in
   * @return void
   * @throw  java.io.IOException when I/O errors occur
   */
  public void load(InputStream in) throws IOException {
    byte[] buffer = new byte[IniProperties.BUFF_SIZE];
    int bytesRead = in.read(buffer, 0, IniProperties.BUFF_SIZE);

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
      bytesRead= in.read(buffer, 0, IniProperties.BUFF_SIZE);
    }
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
   * Dumps properties to output stream
   *
   * @param  java.io.PrintStream out
   * @return void
   */
  public void dump(PrintStream out) {

    // Global properties
    Iterator<String> props= this.properties();
    while (props.hasNext()) {
      String name= props.next();
      out.printf("%s=%s\n", name, IniProperties.dumpEscape(this.getProperty(name)));
    }

    // sections
    Iterator<String> sections= this.sections();
    while (sections.hasNext()) {
      String section= sections.next();
      out.printf("\n[%s]\n", section);
      props= this.properties(section);
      while (props.hasNext()) {
        String name= props.next();
        out.printf("%s=%s\n", name, IniProperties.dumpEscape(this.getProperty(section, name)));
      }
    }
  }

  private static String dumpEscape(String s) {
    return s.replaceAll("\\\\", "\\\\\\\\")
      .replaceAll(";", "\\\\;")
      .replaceAll("#", "\\\\#")
      .replaceAll("(\r?\n|\r)", "\\\\$1");
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
}
