//
// AVControlJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

  transient Vector switches = new Vector();

  private int interval = -1;

  public AVControlJ3D(DisplayImplJ3D d) {
    super(d);
  }

  public void addPair(Switch sw, Set se, DataRenderer re) {
    switches.addElement(new SwitchSet(sw, se, re));
  }
  
  public void addPair(Switch sw, Set se, DataRenderer re, TrajectoryAVHandlerJ3D adptr) {
    switches.addElement(new SwitchSet(sw, se, re, adptr));
  }

  public void nullControl() {
    switches.removeAllElements();
    super.nullControl();
  }

  public abstract void init() throws VisADException;

  /**
   * Sets the time between two steps in the animation set. This value
   * is then used to determine the range of acceptable samples to view
   * out of each of the renderers associated with the AV_Control
   *
   * @param interval the time interval in minutes between steps in the
   * animation set
   */
  public void setInterval(int interval)
  {
    this.interval = interval;
  }

  /**
   * Method that returns an index in the set that is between the
   * lower and upper bounds
   */
  private int getIndexForRange(Set set, double lower, double upper)
    throws VisADException
  {
    double [][] values = set.getDoubles(false);

    for(int i=0; i<values[0].length; i++) {
      if(values[0][i] >= lower && values[0][i] < upper) {
        return i;
      }
    }
    return -1;
  }

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

      // from Luke Matthews of BOM
      if(interval == -1) {
        //default behaviour
        if (set.getLength() == 1) {
          indices = new int[] {0};
        }
        else {
          indices = set.doubleToIndex(values);
        }
      }
      else {
        double resInSecs = (double)interval*60;
        double lower = values[0][0] - (resInSecs/2.0);
        double upper = values[0][0] + (resInSecs/2.0);
        indices = new int[] {getIndexForRange(set, lower, upper)};
      }

      if (0 <= indices[0] && indices[0] < ss.swit.numChildren()) {
        ss.setWhichChild(indices[0]);
      }
      else {
        ss.setWhichChild(Switch.CHILD_NONE);
      }
    } // end while (pairs.hasMoreElements())
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

  public Vector getSwitches() {
    return switches;
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    AVControlJ3D av = (AVControlJ3D )o;

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

  /** SwitchSet is an inner class of AVControlJ3D for
      (Switch, Set, DataRenderer) structures */
  protected class SwitchSet extends Object {
    Switch swit;
    Set set;
    DataRenderer renderer;
    AVHandler handler;

    SwitchSet(Switch sw, Set se, DataRenderer re) {
      swit = sw;
      set = se;
      renderer = re;
    }
    
    SwitchSet(Switch sw, Set se, DataRenderer re, AVHandler adptr) {
      swit = sw;
      set = se;
      renderer = re;
      handler = adptr;
    }
    
    void setWhichChild(int idx) {
      if (handler == null) {
        swit.setWhichChild(idx);
      }
      else {
        handler.setWhichChild(idx);
      }       
    }
  }

}

