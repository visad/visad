/*
 *  gnu/regexp/CharIndexedStringBuffer.java
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

class CharIndexedStringBuffer implements CharIndexed {
  private StringBuffer s;
  private int m_index;

  CharIndexedStringBuffer(StringBuffer str, int index) {
    s = str;
    m_index = index;
  }

  public char charAt(int index) {
    return ((m_index + index) < s.length()) ? s.charAt(m_index + index) : CharIndexed.OUT_OF_BOUNDS;
  }

  public boolean isValid() {
    return (m_index < s.length());
  }

  public boolean move(int index) {
    return ((m_index += index) < s.length());
  }
}
