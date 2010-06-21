/*
 *  gnu/regexp/RETokenChar.java
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

class RETokenChar extends REToken {
  private char[] ch;
  private boolean insens;

  RETokenChar(int f_subIndex, char c, boolean ins) {
    super(f_subIndex);
    ch = new char [1];
    ch[0] = (insens = ins) ? Character.toLowerCase(c) : c;
  }

  int getMinimumLength() {
    return ch.length;
  }
  
  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) {
    int z = ch.length;
    char c;
    for (int i=0; i<z; i++) {
      c = input.charAt(index+i);
      if (( (insens) ? Character.toLowerCase(c) : c ) != ch[i]) return null;
    }
    return next(input,index+z,eflags,mymatch);
  }

  // Overrides REToken.chain() to optimize for strings
  boolean chain(REToken next) {
    if (next instanceof RETokenChar) {
      RETokenChar cnext = (RETokenChar) next;
      // assume for now that next can only be one character
      int newsize = ch.length + cnext.ch.length;
      
      char[] chTemp = new char [newsize];
      
      System.arraycopy(ch,0,chTemp,0,ch.length);
      System.arraycopy(cnext.ch,0,chTemp,ch.length,cnext.ch.length);
      
      ch = chTemp;
      return false;
    } else return super.chain(next);
  }

  void dump(StringBuffer os) {
    os.append(ch);
  }
}


