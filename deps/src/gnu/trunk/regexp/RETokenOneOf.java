/*
 *  gnu/regexp/RETokenOneOf.java
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
import java.util.Vector;

class RETokenOneOf extends REToken {
  private Vector options;
  private boolean negative;

  // This constructor is used for convenience when we know the set beforehand,
  // e.g. \d --> new RETokenOneOf("0123456789",false, ..)
  //      \D --> new RETokenOneOf("0123456789",true, ..)

  RETokenOneOf(int f_subIndex, String f_options,boolean f_negative,boolean f_insens) {
    super(f_subIndex);
    options = new Vector();
    negative = f_negative;
    for (int i=0; i<f_options.length(); i++)
      options.addElement(new RETokenChar(f_subIndex,f_options.charAt(i),f_insens));
  }

  RETokenOneOf(int f_subIndex, Vector f_options,boolean f_negative) {
    super(f_subIndex);
    options = f_options;
    negative = f_negative;
  }

  int getMinimumLength() {
    int min = Integer.MAX_VALUE;
    int x;
    for (int i=0; i < options.size(); i++) {
      if ((x = ((REToken) options.elementAt(i)).getMinimumLength()) < min)
	min = x;
    }
    return min;
  }

  int[] match(CharIndexed input, int index, int eflags, REMatch mymatch) {
    if (negative && (input.charAt(index) == CharIndexed.OUT_OF_BOUNDS)) 
      return null;

    int[] newIndex;
    int[] possibles = new int[0];
    REToken tk;
    for (int i=0; i < options.size(); i++) {
	tk = (REToken) options.elementAt(i);
      newIndex = tk.match(input,index,eflags,mymatch);

      // Voodoo.
      if ((newIndex == null) && (tk instanceof RE) && (tk.m_subIndex > 0)) {
	  mymatch.reset(tk.m_subIndex + 1);
      }

      if (newIndex != null) { // match was successful
	if (negative) return null;
	// Add newIndex to list of possibilities.

	int[] temp = new int[possibles.length + newIndex.length];
	System.arraycopy(possibles,0,temp,0,possibles.length);
	for (int j = 0; j < newIndex.length; j++) 
	  temp[possibles.length + j] = newIndex[j];
	possibles = temp;
      }
    } // try next option
    // Now possibles is array of all possible matches.
    // Try next with each possibility.

    int[] doables = new int[0];
    for (int i = 0; i < possibles.length; i++) {
      newIndex = next(input,possibles[i],eflags,mymatch);
      if (newIndex != null) {
	int[] temp = new int[doables.length + newIndex.length];
	System.arraycopy(doables,0,temp,0,doables.length);
	for (int j = 0; j < newIndex.length; j++) 
	  temp[doables.length + j] = newIndex[j];
	doables = temp;
      } else {
	  // Voodoo.
	  if (m_subIndex > 0) {
	      mymatch.reset(m_subIndex + 1);
	  }
      }

    }

    if (doables.length > 0)
      return (negative) ? 
	null : doables;
    else return (negative) ? 
	   next(input,index+1,eflags,mymatch) : null;

    // index+1 works for [^abc] lists, not for generic lookahead (--> index)
  }

  void dump(StringBuffer os) {
    os.append(negative ? "[^" : "(?:");
    for (int i = 0; i < options.size(); i++) {
      if (!negative && (i > 0)) os.append('|');
      ((REToken) options.elementAt(i)).dumpAll(os);
    }
    os.append(negative ? ']' : ')');
  }  

  // Overrides REToken.chain
  boolean chain(REToken f_next) {
    super.chain(f_next);
    for (int i = 0; i < options.size(); i++)
      ((REToken) options.elementAt(i)).setUncle(f_next);
    return true;
  }

  /*
  void setUncle(REToken f_next) {
    for (int i = 0; i < options.size(); i++)
      ((REToken) options.elementAt(i)).setUncle(f_next);
  }
  */
}
