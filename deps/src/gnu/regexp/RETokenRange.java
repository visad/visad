/*
 *  gnu/regexp/RETokenRange.java
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

class RETokenRange extends REToken {
  private char lo, hi;
  private boolean insens;

  RETokenRange(int f_subIndex, char f_lo, char f_hi, boolean ins) {
    super(f_subIndex);
    lo = (insens = ins) ? Character.toLowerCase(f_lo) : f_lo;
    hi = ins ? Character.toLowerCase(f_hi) : f_hi;
  }

  int getMinimumLength() {
    return 1;
  }

  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) {
    char c = input.charAt(index);
    if (c == CharIndexed.OUT_OF_BOUNDS) return null;
    if (insens) c = Character.toLowerCase(c);
    return ((c >= lo) && (c <= hi)) ? 
      next(input,index+1,eflags,mymatch) : null;
  }

  void dump(StringBuffer os) {
    os.append(lo).append('-').append(hi);
  }
}

