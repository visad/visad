
//
// AVControlJ2D.java
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

package visad.java2d;

import visad.*;

import java.util.Vector;
import java.util.Enumeration;

/**
   AVControlJ2D is the VisAD abstract superclass for AnimationControlJ2D
   and ValueControlJ2D.<P>
*/
public abstract class AVControlJ2D extends Control implements AVControl {

  Vector switches = new Vector();

  public AVControlJ2D(DisplayImplJ2D d) {
    super(d);
  }

  void addPair(VisADSwitch sw, Set se, DataRenderer re) {
    switches.addElement(new SwitchSet(sw, se, re));
  }

  abstract void init() throws VisADException;

  void selectSwitches(double value, Set animation_set)
       throws VisADException {
    // check for missing
    if (value != value) return;
    double[][] fvalues = new double[1][1];
    fvalues[0][0] = value;
    Enumeration pairs = ((Vector) switches.clone()).elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();
      Set set = ss.set;
      double[][] values = null;
      RealTupleType out = ((SetType) set.getType()).getDomain();
      if (animation_set != null) {
        RealTupleType in =
          ((SetType) animation_set.getType()).getDomain();
        values = CoordinateSystem.transformCoordinates(
                             out, set.getCoordinateSystem(),
                             set.getSetUnits(), null /* errors */,
                             in, animation_set.getCoordinateSystem(),
                             animation_set.getSetUnits(),
                             null /* errors */, fvalues);
      }
      else {
        // use RealType for value Unit and CoordinateSystem
        // for SelectValue
        values = CoordinateSystem.transformCoordinates(
                             out, set.getCoordinateSystem(),
                             set.getSetUnits(), null /* errors */,
                             out, out.getCoordinateSystem(),
                             out.getDefaultUnits(), null /* errors */,
                             fvalues);
      }
      // compute set index from converted value
      int [] indices;
      if (set.getLength() == 1) {
        indices = new int[] {0};
      }
      else {
        indices = set.doubleToIndex(values);
      }
/*
System.out.println("selectSwitches: value = " + value +
                   " indices[0] = " + indices[0] +
                   " values[0][0] = " + values[0][0]);
*/
      ss.swit.setWhichChild(indices[0]);
    }
  }

  /** clear all 'pairs' in switches that involve re */
  public void clearSwitches(DataRenderer re) {
    Enumeration pairs = ((Vector) switches.clone()).elements();
    while (pairs.hasMoreElements()) {
      SwitchSet ss = (SwitchSet) pairs.nextElement();
      if (ss.renderer.equals(re)) {
        switches.removeElement(ss);
      }
    }
  }

  /** SwitchSet is an inner class of AVControlJ2D for
      (VisADSwitch, Set, DataRenderer) structures */
  private class SwitchSet extends Object {
    VisADSwitch swit;
    Set set;
    DataRenderer renderer;
 
    SwitchSet(VisADSwitch sw, Set se, DataRenderer re) {
      swit = sw;
      set = se;
      renderer = re;
    }
  }

}

