/*
 *  gnu/regexp/RETokenBackRef.java
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

class RETokenBackRef extends REToken {
  private int num;
  private boolean insens;
  
  RETokenBackRef(int f_subIndex, int mynum, boolean ins) {
    super(f_subIndex);
    insens = ins;
    num = mynum;
  }

  // should implement getMinimumLength() -- any ideas?

  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) {
    int b,e;
    b = mymatch.start[num];
    e = mymatch.end[num];
    if ((b==-1)||(e==-1)) return null; // this shouldn't happen, but...
    for (int i=b; i<e; i++) {
      if (input.charAt(index+i-b) != input.charAt(i)) return null;
    }

    return next(input,index+e-b,eflags,mymatch);
  }

  void dump(StringBuffer os) {
    os.append('\\').append(num);
  }
}


