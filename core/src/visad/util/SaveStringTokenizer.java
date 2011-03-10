//
// SaveStringTokenizer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.util.*;

/**
 * Parses a save string containing a series of lines of the form:
 * <dd><tt>keyword = value</tt>
 */
public class SaveStringTokenizer {

  /**
   * List of keywords found in the save string.
   */
  public String[] keywords;

  /**
   * List of values corresponding to the keywords in the save string.
   */
  public String[] values;

  /**
   * Constructs a new save string tokenizer from the given save string.
   */
  public SaveStringTokenizer(String save) {
    StringTokenizer st = new StringTokenizer(save, "\n\r");
    Vector v = new Vector();
    while (st.hasMoreTokens()) v.add(st.nextToken().trim());

    // parse keywords and values from save string
    Vector keys = new Vector();
    Vector vals = new Vector();
    int numLines = v.size();
    int t = 0;
    boolean done = false;
    while (true) {
      // get next meaningful line
      String line = null;
      int eq = -1;
      while (eq < 0) {
        // search for a non-comment line that contains an equals sign
        if (t == numLines) {
          done = true;
          break;
        }
        line = (String) v.elementAt(t++);
        if (line.charAt(0) != '#') eq = line.indexOf('=');
      }
      if (done) break;
      keys.add(line.substring(0, eq).trim());

      // get remainder of information after the equals sign
      String value = line.substring(eq + 1).trim();
      String nextLine = (t < numLines ? (String) v.elementAt(t) : null);
      boolean moreLines = false;
      while (nextLine != null && nextLine.indexOf('=') < 0) {
        if (nextLine.length() > 0 && !nextLine.startsWith("#")) {
          value = value + '\n';
          value = value + '\n' + nextLine;
          moreLines = true;
        }
        nextLine = (t < numLines - 1 ? (String) v.elementAt(++t) : null);
      }
      if (moreLines) value = value + '\n';
      vals.add(value);
    }

    int len = keys.size();
    keywords = new String[len];
    values = new String[len];
    for (int i=0; i<len; i++) {
      keywords[i] = (String) keys.elementAt(i);
      values[i] = (String) vals.elementAt(i);
    }
  }

}
