
//
// AVControl.java
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

import javax.media.j3d.*;
import java.vecmath.*;

import java.util.*;

/**
   AVControl is the VisAD abstract superclass for AnimationControl
   and ValueControl.<P>
*/
public abstract class AVControl extends Control {

  Vector switches = new Vector();

  public AVControl(DisplayImpl d) {
    super(d);
  }

  void addPair(Switch sw, Set se, Renderer re) {
    switches.addElement(new SwitchSet(sw, se, re));
  }

  abstract void init() throws VisADException;

  void selectSwitches(double value) throws VisADException {
    float[][] fvalues = new float[1][1];
    fvalues[0][0] = (float) value;
    Enumeration pairs = switches.elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();
      Set set = ss.set;
      // assume value is in default CoordinateSystem and Unit of
      // set.getType().getDomain(), convert to CoordinateSystem
      // and Unit of set
      RealTupleType type = ((SetType) set.getType()).getDomain();
      float[][] values = CoordinateSystem.transformCoordinates(
                             type, set.getCoordinateSystem(),
                             set.getSetUnits(), null /* errors */,
                             type, type.getCoordinateSystem(),
                             type.getDefaultUnits(), null /* errors */,
                             fvalues);
      // compute set index from converted value
      int [] indices = set.valueToIndex(values);
      ss.swit.setWhichChild(indices[0]);
    }
  }

  /** never called (?) */
  void clearSwitches() {
    switches.removeAllElements();
  }

  /** clear all 'pairs' in switches that involve re */
  void clearSwitches(Renderer re) {
    Enumeration pairs = switches.elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();
      if (ss.renderer.equals(re)) {
        switches.removeElement(ss);
      }
    }
  }

  /** SwitchSet is an inner class of AVControl for
      (Switch, Set, Renderer) structures */
  private class SwitchSet extends Object {
    Switch swit;
    Set set;
    Renderer renderer;
 
    SwitchSet(Switch sw, Set se, Renderer re) {
      swit = sw;
      set = se;
      renderer = re;
    }
  }

}

