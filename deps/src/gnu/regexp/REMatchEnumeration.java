/*
 *  gnu/regexp/REMatchEnumeration.java
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
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An REMatchEnumeration enumerates regular expression matches over a given
 * input text.  You obtain a reference to an enumeration using the
 * <code>getMatchEnumeration()</code> methods on an instance of RE.
 * <P>
 * REMatchEnumeration does lazy computation; that is, it will not search for
 * a match until it needs to.  If you'd rather just get all the matches at
 * once in a big array, use the <code>getAllMatches()</code> methods on RE.
 * However, using an enumeration can help speed performance when the entire
 * text does not need to be searched immediately.
 * <P>
 * The enumerated type is especially useful when searching on an InputStream,
 * because the InputStream read position cannot be guaranteed after calling
 * <code>getMatch()</code> (see the description of that method for an
 * explanation of why).  Enumeration also saves a lot of overhead required
 * when calling <code>getMatch()</code> multiple times.
 * 
 * @author <A HREF="mailto:wes@cacas.org">Wes Biggs</A>
 */
public class REMatchEnumeration implements Enumeration {
  private static final int YES = 1;
  private static final int MAYBE = 0;
  private static final int NO = -1;
  
  private int m_more;
  private REMatch m_match;
  private RE m_expr;
  private CharIndexed m_input;
  private int m_index;
  private int m_eflags;
  private StringBuffer m_buffer;

  // Package scope constructor is used by RE.getMatchEnumeration()
  REMatchEnumeration(RE expr, CharIndexed input, int index, int eflags) {
    m_more = MAYBE;
    m_expr = expr;
    m_input = input;
    m_index = index;
    m_eflags = eflags;
  }

  /** Returns true if there are more matches in the input text. */
  public boolean hasMoreElements() {
    return hasMoreMatches(null);
  }

  /** Returns true if there are more matches in the input text. */
  public boolean hasMoreMatches() {
    return hasMoreMatches(null);
  }

  /** Returns true if there are more matches in the input text.
   * Saves the text leading up to the match (or to the end of the input)
   * in the specified buffer.
   */
  public boolean hasMoreMatches(StringBuffer f_buffer) {
    if (m_more == MAYBE) {
      m_match = m_expr.getMatchImpl(m_input,m_index,m_eflags,f_buffer);
      if (m_match != null) {
	m_index = m_match.getEndIndex();
	m_input.move((m_match.end[0] > 0) ? m_match.end[0] : 1);
	m_more = YES;
      } else m_more = NO;
    }
    return (m_more == YES);
  }

  /** Returns the next match in the input text. */
  public Object nextElement() throws NoSuchElementException {
    return nextMatch();
  }

  /** 
   * Returns the next match in the input text. This method is provided
   * for convenience to avoid having to explicitly cast the return value
   * to class REMatch.
   */
  public REMatch nextMatch() throws NoSuchElementException {
    if (hasMoreElements()) {
      m_more = MAYBE;
      return m_match;
    }
    throw new NoSuchElementException();
  }
}
