//
// AnimationSetControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.rmi.*;

/**
   AnimationSetControl is the VisAD class for sampling Animation
   steps.<P>
*/
public class AnimationSetControl extends Control {

  private Set set;
  private transient AnimationControl parent;

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
      current = set.getLength() - 1;
    }
    return current;
  }

  public double getValue(int current) throws VisADException {
    int[] indices = new int[1];
    indices[0] = clipCurrent(current);
    if (set == null) {
      return Double.NaN;
    }
    else {
      double[][] values = set.indexToDouble(indices);
      return values[0][0];
    }
  }

  public int getIndex(double value) throws VisADException {
    if (set == null) {
      return 0;
    }
    else {
      double[][] values = new double[][] {{value}};
      int[] indices = set.doubleToIndex(values);
      return ((indices[0] < 0) ? 0 : indices[0]);
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
      // parent.setCurrent(0);
      parent.setCurrent(clipCurrent(parent.getCurrent()));
    }
    changeControl(!noChange);
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return null;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    throw new UnimplementedException(
      "Cannot setSaveString on this type of control");
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof AnimationSetControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    AnimationSetControl asc = (AnimationSetControl )rmt;

    boolean changeSet = false;

    if (set == null) {
      if (asc.set != null) {
        changeSet = true;
      }
    } else if (asc.set == null) {
      changeSet = true;
    } else if (!set.equals(asc.set)) {
      changeSet = true;
    }

    if (changeSet) {
      try {
        setSet(asc.set);
      } catch (RemoteException re) {
        throw new VisADException("Could not set Set: " + re.getMessage());
      }
    }
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    AnimationSetControl asc = (AnimationSetControl )o;

    if (set == null) {
      if (asc.set != null) {
        return false;
      }
    } else if (asc.set == null) {
      return false;
    } else if (!set.equals(asc.set)) {
      return false;
    }

    return true;
  }
}
