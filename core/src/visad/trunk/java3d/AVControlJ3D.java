
//
// AVControlJ3D.java
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

package visad.java3d;

import visad.*;

import javax.media.j3d.*;

import java.util.Vector;
import java.util.Enumeration;

/**
   AVControlJ3D is the VisAD abstract superclass for AnimationControlJ3D
   and ValueControlJ3D.<P>
*/
public abstract class AVControlJ3D extends Control implements AVControl {

  Vector switches = new Vector();

  public AVControlJ3D(DisplayImplJ3D d) {
    super(d);
  }

  void addPair(Switch sw, Set se, DataRenderer re) { // J3D
    switches.addElement(new SwitchSet(sw, se, re));
  }

  abstract void init() throws VisADException;

  void selectSwitches(double value) throws VisADException {
    // check for missing
    if (value != value) return;
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
      ss.swit.setWhichChild(indices[0]); // J3D
    }
  }

  /** clear all 'pairs' in switches that involve re */
  public void clearSwitches(DataRenderer re) {
    Enumeration pairs = switches.elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();
      if (ss.renderer.equals(re)) {
        switches.removeElement(ss);
      }
    }
  }

  /** SwitchSet is an inner class of AVControlJ3D for
      (Switch, Set, DataRenderer) structures */
  private class SwitchSet extends Object {
    Switch swit; // J3D
    Set set;
    DataRenderer renderer;
 
    SwitchSet(Switch sw, Set se, DataRenderer re) { // J3D
      swit = sw;
      set = se;
      renderer = re;
    }
  }

}

