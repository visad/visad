
//
// AnimationSetControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

  static final AnimationSetControl prototype = new AnimationSetControl();

  public AnimationSetControl(DisplayImpl d, AnimationControl p) {
    super(d);
    parent = p;
    set = null;
  }
 
  AnimationSetControl() {
    this(null, null);
  }

  /** should never be called */
  public Control cloneButContents(DisplayImpl d)
         throws VisADException, RemoteException {
    throw new DisplayException("AnimationSetControl.cloneButContents");
  }

  public Set getSet() {
    return set;
  }

  int clipCurrent(int current) throws VisADException {
    int set_length = set.getLength();
    if (current < 0) {
      current = set_length;
    }
    else if (current >= set_length) {
      current = 0;
    }
    return current;
  }

  float getValue(int current) throws VisADException {
    int[] indices = new int[1];
    indices[0] = clipCurrent(current);
    float[][] values = set.indexToValue(indices);
    return values[0][0];
  }

  public void setSet(Set s) throws VisADException {
    setSet(s, false);
  }

  /** noChange = true to not trigger changeControl, used by
      ScalarMap.setRange */
  void setSet(Set s, boolean noChange) throws VisADException {
    set = s;
    if (parent != null) {
      parent.setCurrent(0);
    }
    if (!noChange) changeControl();
  }

}

