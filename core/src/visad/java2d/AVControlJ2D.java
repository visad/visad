//
// AVControlJ2D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
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

  transient Vector switches = new Vector();

  public AVControlJ2D(DisplayImplJ2D d) {
    super(d);
  }

  void addPair(VisADSwitch sw, Set se, DataRenderer re) {
    switches.addElement(new SwitchSet(sw, se, re));
  }

  abstract void init() throws VisADException;

  public void selectSwitches(double value, Set animation_set)
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

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    AVControlJ2D av = (AVControlJ2D )o;

    if (switches == null) {
      return (av.switches == null);
    } else if (av.switches == null) {
      // don't assume anything, since it could be a remote control
    } else {
      if (switches.size() != av.switches.size()) {
        return false;
      }
      for (int i = switches.size() - 1; i > 0; i--) {
        if (!switches.elementAt(i).equals(av.switches.elementAt(i))) {
          return false;
        }
      }
    }

    return true;
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

