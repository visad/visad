/*
 *  gnu/regexp/REToken.java
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
import java.io.ByteArrayOutputStream;

abstract class REToken {
  // used by RETokenStart and RETokenEnd  
  static final String newline = System.getProperty("line.separator"); 

  protected REToken m_next = null;
  protected REToken m_uncle = null;
  protected int m_subIndex;

  protected REToken(int f_subIndex) {
    m_subIndex = f_subIndex;
  }

  int getMinimumLength() {
    return 0;
  }

  void setUncle(REToken f_uncle) {
    m_uncle = f_uncle;
  }

  abstract int[] match(CharIndexed input, int index, int eflags, REMatch mymatch);
  
  protected int[] next(CharIndexed input, int index, int eflags, REMatch mymatch) {
    mymatch.end[m_subIndex] = index;
    if (m_next == null) {
      if (m_uncle == null) {
	return new int[] { index };
      } else {
	if (m_uncle.match(input,index,eflags,mymatch) == null) {
	  return null;
	} else {
	  return new int[] { index };
	}
      }
    } else {
	return m_next.match(input,index,eflags,mymatch);
    }
  }
  
  boolean chain(REToken next) {
    m_next = next;
    return true; // Token was accepted
  }

  void dump(StringBuffer os) { 
  }

  void dumpAll(StringBuffer os) {
    dump(os);
    if (m_next != null) m_next.dumpAll(os);
  }
}
