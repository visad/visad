/*
 *  gnu/regexp/RETokenEnd.java
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

class RETokenEnd extends REToken {
  private boolean newline;

  RETokenEnd(int f_subIndex,boolean f_newline) { 
    super(f_subIndex);
    newline = f_newline;
  }

  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) {
    // this may not work on systems that use \r\n as line separator. FIXME
    // use System.getProperty("line.separator");
    char ch = input.charAt(index);
    if (ch == CharIndexed.OUT_OF_BOUNDS)
      return ((eflags & RE.REG_NOTEOL)>0) ? 
	null : next(input,index,eflags,mymatch);
    return (newline && (ch == '\n')) ? 
      next(input,index,eflags,mymatch) : null;
  }

  void dump(StringBuffer os) {
    os.append('$');
  }
}
