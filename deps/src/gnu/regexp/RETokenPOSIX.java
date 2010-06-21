/*
 *  gnu/regexp/RETokenPOSIX.java
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
import java.util.Hashtable;

class RETokenPOSIX extends REToken {
  int m_type;
  boolean m_insens;
  boolean m_negated;

  static final int  ALNUM = 0;
  static final int  ALPHA = 1;
  static final int  BLANK = 2;
  static final int  CNTRL = 3;
  static final int  DIGIT = 4;
  static final int  GRAPH = 5;
  static final int  LOWER = 6;
  static final int  PRINT = 7;
  static final int  PUNCT = 8;
  static final int  SPACE = 9;
  static final int  UPPER = 10;
  static final int XDIGIT = 11;

  // Array indices correspond to constants defined above.
  static final String[] s_nameTable =  {
    "alnum", "alpha", "blank", "cntrl", "digit", "graph", "lower",
    "print", "punct", "space", "upper", "xdigit" 
  };

  // The RE constructor uses this to look up the constant for a string
  static int intValue(String key) {
    for (int i = 0; i < s_nameTable.length; i++) {
      if (s_nameTable[i].equals(key)) return i;
    }
    return -1;
  }

  RETokenPOSIX(int f_subIndex, int f_type,boolean f_insens, boolean f_negated) {
    super(f_subIndex);
    m_type = f_type;
    m_insens = f_insens;
    m_negated = f_negated;
  }

  int getMinimumLength() {
    return 1;
  }

  int[] match(CharIndexed input, int index, int eflags,REMatch mymatch) {
    char ch = input.charAt(index);
    if (ch == CharIndexed.OUT_OF_BOUNDS)
      return null;
    
    boolean retval = false;
    switch (m_type) {
    case ALNUM:
      retval = Character.isLetterOrDigit(ch);
      break;
    case ALPHA:
      retval = Character.isLetter(ch);
      break;
    case BLANK:
      retval = ((ch == ' ') || (ch == '\t'));
      break;
    case CNTRL:
      retval = Character.isISOControl(ch);
      break;
    case DIGIT:
      retval = Character.isDigit(ch);
      break;
    case GRAPH:
      retval = (!(Character.isWhitespace(ch) || Character.isISOControl(ch)));
      break;
    case LOWER:
      retval = ((m_insens && Character.isLetter(ch)) || Character.isLowerCase(ch));
      break;
    case PRINT:
      retval = Character.isLetterOrDigit(ch);
      break;
    case PUNCT:
      retval = ("`~!@#$%^&*()-_=+[]{}\\|;:'\"/?,.<>".indexOf(ch)!=-1);
      break;
    case SPACE:
      retval = Character.isWhitespace(ch);
      break;
    case UPPER:
      retval = ((m_insens && Character.isLetter(ch)) || Character.isUpperCase(ch));
      break;
    case XDIGIT:
      retval = (Character.isDigit(ch) || ("abcdefABCDEF".indexOf(ch)!=-1));
      break;
    }

    if (m_negated) retval = !retval;
    if (retval) return next(input,index+1,eflags,mymatch);
    else return null;
  }

  void dump(StringBuffer os) {
    if (m_negated) os.append('^');
    os.append("[:" + s_nameTable[m_type] + ":]");
  }
}
