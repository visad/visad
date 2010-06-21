/*
 *  gnu/regexp/REMatch.java
 *  Copyright (C) 1998 Wes Biggs
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gnu.regexp;

/**
 * An instance of this class represents a match
 * completed by a gnu.regexp matching function. It can be used
 * to obtain relevant information about the location of a match
 * or submatch.
 *
 * @author <A HREF="mailto:wes@cacas.org">Wes Biggs</A>
 */
public class REMatch {
  private String m_match;
  int offset, anchor;
  int[] start; // package scope for
  int[] end;   //  quick access internally
  int[] count; // runtime count of times through each subexpression

  REMatch(int f_subs, int f_index) {
    start = new int[f_subs+1];
    end = new int[f_subs+1];
    count = new int[f_subs+1];
    anchor = f_index;
    clear(f_index);
  }

  void finish(CharIndexed text) {
    start[0] = 0;
    StringBuffer sb = new StringBuffer();
    int i;
    for (i = 0; i < end[0]; i++)
      sb.append(text.charAt(i));
    m_match = sb.toString();
    for (i = 0; i < start.length; i++) {
      if (start[i] == -1) end[i] = -1;
    }
  }

    void reset(int f_subIndex) {
	for (int i = f_subIndex; i < start.length; i++) {
	    start[i] = end[i] = -1;
	    count[i] = 0;
	}
    }

  void clear(int f_index) {
    offset = f_index;
    for (int i = 0; i < start.length; i++) {
      start[i] = end[i] = -1;
      count[i] = 0;
    }
  }
      
  /**
   * Returns the string matching the pattern.  This makes it convenient
   * to write code like the following:
   * <P>
   * <code> REMatch myMatch = myExpression.getMatch(myString);<br>
   * if (myMatch != null) System.out.println("Regexp found: "+myMatch);</code>
   */
  public String toString() {
    return m_match;
  }
  
  /**
   * Returns the index within the input text where the match in its entirety
   * began.
   */
  public int getStartIndex() {
    return offset + start[0];
  }
  
  /**
   * Returns the index within the input string where the match in its entirety 
   * ends.  The return value is the next position after the end of the string;
   * therefore, a match created by the following call:
   * <P>
   * <code>REMatch myMatch = myExpression.getMatch(myString);</code>
   * <P>
   * can be viewed (given that myMatch is not null) by creating
   * <P>
   * <code>String theMatch = myString.substring(myMatch.getStartIndex(),
   * myMatch.getEndIndex());</code>
   * <P>
   * But you can save yourself that work, since the <code>toString()</code>
   * method (above) does exactly that for you.
   */
  public int getEndIndex() {
    return offset + end[0];
  }
  
  /**
   * Returns the string matching the given subexpression.
   *
   * @param sub Index of the subexpression.
   */
  public String toString(int sub) {
    if ((sub >= start.length) || (start[sub] == -1)) return "";
    return (m_match.substring(start[sub],end[sub]));
  }

  /** 
   * Returns the index within the input string used to generate this match
   * where subexpression number <i>sub</i> begins, or <code>-1</code> if
   * the subexpression does not exist.
   *
   * @param sub Subexpression index
   */
  public int getSubStartIndex(int sub) {
    if (sub >= start.length) return -1;
    int x = start[sub];
    return (x == -1) ? x : offset + x;
  }
  
  /** 
   * Returns the index within the input string used to generate this match
   * where subexpression number <i>sub</i> ends, or <code>-1</code> if
   * the subexpression does not exist.
   *
   * @param sub Subexpression index
   */
  public int getSubEndIndex(int sub) {
    if (sub >= start.length) return -1;
    int x = end[sub];
    return (x == -1) ? x : offset + x;
  }
  
  /**
   * Substitute the results of this match to create a new string.
   * This is patterned after PERL, so the tokens to watch out for are
   * <code>$0</code> through <code>$9</code>.  <code>$0</code> matches
   * the full substring matched; <code>$<i>n</i></code> matches
   * subexpression number <i>n</i>.
   *
   * @param input A string consisting of literals and <code>$<i>n</i></code> tokens.
   */
  public String substituteInto(String input) {
    // a la Perl, $0 is whole thing, $1 - $9 are subexpressions
    StringBuffer output = new StringBuffer();
    int pos;
    for (pos = 0; pos < input.length()-1; pos++) {
      if ((input.charAt(pos) == '$') && (Character.isDigit(input.charAt(pos+1)))) {
	int val = Character.digit(input.charAt(++pos),10);
	if (val < start.length) {
	  output.append(toString(val));
	} 
      } else output.append(input.charAt(pos));
    }
    if (pos < input.length()) output.append(input.charAt(pos));
    return output.toString();
  }
}
