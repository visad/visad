
//
// AnimationSetControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.rmi.*;

/**
   AnimationSetControl is the VisAD class for sampling Animation
   steps.<P>
*/
public class AnimationSetControl extends Control {

  private Set set;
  private AnimationControl parent;

  public AnimationSetControl(DisplayImpl d, AnimationControl p) {
    super(d);
    parent = p;
    set = null;
  }
 
  public Set getSet() {
    return set;
  }

  public int clipCurrent(int current) throws VisADException {
    if (set == null || current >= set.getLength()) {
      current = 0;
    }
    else if (current < 0) {
      current = set.getLength();
    }
    return current;
  }

  public float getValue(int current) throws VisADException {
    int[] indices = new int[1];
    indices[0] = clipCurrent(current);
    if (set == null) {
      return Float.NaN;
    }
    else {
      float[][] values = set.indexToValue(indices);
      return values[0][0];
    }
  }

  public void setSet(Set s)
         throws VisADException, RemoteException {
    setSet(s, false);
  }

  /** noChange = true to not trigger changeControl, used by
      ScalarMap.setRange */
  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException {
    set = s;
    if (parent != null) {
      parent.setCurrent(0);
    }
    changeControl(!noChange);
  }

}

