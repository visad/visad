/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.install;

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A {@link java.io.File File} object for a <tt>java</tt> executable
 * which also extracts the JVM's version information.
 */
public class JavaFile
  extends File
{
  // list of regular expressions used to extract version info
  private static final String[] regexpStr = new String[] {
    "^java version \"(\\d+)\\.(\\d+).*\"",
    "^java version \"HP-UX Java [A-Z]\\.\\d+\\.(\\d)(\\d+).*\"",
  };

  // list of regular expressions built from regexpStr above
  private static RE[] regexp = null;

  // the Runtime object used to run 'java -version'
  private static Runtime runtime = null;

  // extracted version info
  private String fullString;
  private int major, minor;

  /**
   * @see java.io.File#File(String)
   */
  public JavaFile(String path)
  {
    this(new File(path));
  }

  /**
   * @see java.io.File#File(String)
   */
  public JavaFile(File path)
  {
    this(path.getParent(), path.getName());
  }

  /**
   * @see java.io.File#File(String, String)
   */
  public JavaFile(String parent, String child)
  {
    this(new File(parent), child);
  }

  /**
   * @see java.io.File#File(File, String)
   */
  public JavaFile(File path, String name)
  {
    super(path, name);

    findVersion();
  }

  /**
   * Extract this JVM's version information.<br>
   * Try to find something meaningful in the output of
   * 'java -version'.
   */
  private final void findVersion()
  {
    if (regexp == null) {
      initRegExp();
    }

    if (runtime == null) {
      runtime = Runtime.getRuntime();
    }

    Process proc;
    try {
      proc = runtime.exec(this + " -version");
    } catch (IOException ioe) {
      System.err.println("While running '" + this + " -version':");
      ioe.printStackTrace();
      proc = null;
    }

    boolean found = false;
    if (proc != null) {
      InputStream is = proc.getErrorStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(is));

      while (!found) {
        String line;
        try {
          line = in.readLine();
        } catch (IOException ioe) {
          ioe.printStackTrace();
          break;
        }

        if (line == null) {
          break;
        }

        for (int i = 0; i < regexp.length; i++) {
          REMatch match = regexp[i].getMatch(line);
          if (match != null) {
            fullString = line.substring(match.getStartIndex(),
                                        match.getEndIndex());
            major = parseInt(line.substring(match.getSubStartIndex(1),
                                            match.getSubEndIndex(1)));
            minor = parseInt(line.substring(match.getSubStartIndex(2),
                                            match.getSubEndIndex(2)));
            found = true;
            break;
          }
        }
      }
    }

    if (!found) {
      fullString = null;
      major = minor = -1;
    }
  }

  /**
   * Get the version string returned by this JVM.
   * For a 1.3.2 JVM, this method might return something like
   * <tt>"1.3.2_01"</tt>.
   *
   * @return the version string for this JVM.
   */
  public String getVersionString() { return fullString; }

  /**
   * Extract an integer from a String.
   *
   * @param intStr the string to parse.
   *
   * @return the extracted integer, or <tt>-1</tt> if there
   *         was a parsing error.
   */
  private static final int parseInt(String intStr)
  {
    try {
      return Integer.parseInt(intStr);
    } catch (NumberFormatException nfe) {
    }

    return -1;
  }

  /**
   * Get the major version number for this JVM.
   * For a 1.3.2 JVM, this method would return <tt>1</tt>.
   *
   * @return the major version number for this JVM.
   */
  public int getMajor() { return major; }

  /**
   * Get the minor version number for this JVM.
   * For a 1.3.2 JVM, this method would return <tt>3</tt>.
   *
   * @return the minor version number for this JVM.
   */
  public int getMinor() { return minor; }

  /**
   * Initialize the list of regular expressions.
   */
  private final synchronized void initRegExp()
  {
    synchronized (regexpStr) {
      regexp = new RE[regexpStr.length];

      int bad = 0;
      for (int i = 0; i < regexpStr.length; i++) {
        try {
          regexp[i] = new RE(regexpStr[i]);
        } catch (REException ree) {
          System.err.println("For regexp \"" + regexpStr[i] + "\":");
          ree.printStackTrace();
          bad++;
          regexp[i] = null;
        }
      }

      if (bad > 0) {
        RE[] tmpRE = new RE[regexp.length - bad];
        for (int i = 0, j = 0; i < regexp.length; i++) {
          if (regexp[i] != null) {
            tmpRE[j++] = regexp[i];
          }
        }
        regexp = tmpRE;
      }
    }
  }

  /**
   * See if this JVM is at least as recent as the requested
   * major/minor pair.<br>
   * <br>
   * For example, if a Java2 JVM is required, this routine
   * would be called with <tt>major=1</tt> and <tt>minor=2</tt>.
   *
   * @param major JVM major version number
   * @param minor JVM minor version number
   *
   * @return true if the JVM is at least the requested version.
   */
  public final boolean matchMinimum(int major, int minor)
  {
    return (this.major > major ||
            (this.major == major && this.minor >= minor));
  }
}
