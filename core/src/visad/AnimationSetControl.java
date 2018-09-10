//
// AnimationSetControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
  private boolean isManual;

  /**
   * construct an AnimationSetControl for the given DisplayImpl
   * and AnimationControl
   * @param d - DisplayImpl this AnimationSetControl is associated with
   * @param p - parent AnimationControl of this AnimationSetControl
   */
  public AnimationSetControl(DisplayImpl d, AnimationControl p) {
    super(d);
    parent = p;
    set = null;
    isManual = false;
  }

  /** 
   * @return Set of RealType values for animation steps, in RealType
   * mapped to Animation
   */
  public Set getSet() {
    return set;
  }

  /**
   * @return value of current clipped to limits defined by this
   * AnimationSetControl
   * @param current value to clip
   * @throws VisADException if a VisAD error occurs
   */
  public int clipCurrent(int current) throws VisADException {
    if (set == null || current >= set.getLength()) {
      current = 0;
    }
    else if (current < 0) {
      current = set.getLength() - 1;
    }
    return current;
  }

  /**
   * @return current step converted to value of RealType
   * mapped to Animation
   * @param current index of current step
   * @throws VisADException if a VisAD error occurs
   */
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

  /**
   * @return animation step ordinal corresponding to value
   * of RealType mapped to Animation
   * @param value - RealType value
   * @throws VisADException if a VisAD error occurs
   */
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

  /** 
   * set Set of Animation value
   * @param s - Set of RealType values for Animation steps
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public void setSet(Set s)
         throws VisADException, RemoteException {
    setSet(s, false);
  }

  /**
   * set Set of Animation value
   * @param s - Set of RealType values for Animation steps
   * @param noChange = true to not trigger changeControl (used by
      ScalarMap.setRange())
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException {
    if (set != null && set.equals(s) || 
        set == null && s == null) {
        return;
    }

    if (noChange) {
      // don't auto-scale is a previous non-auto-scale call
      if (isManual) return;
    }
    else {
      // a non-auto-scale call
      isManual = true;
    }

    set = s;
    if (parent != null) {
      parent.setCurrent(clipCurrent(parent.getCurrent()));
    }
    changeControl(!noChange);
  }

  /**
   * @return String representation of this AnimationSetControl
   */
  public String getSaveString() {
    return null;
  }

  /**
   * reconstruct this AnimationSetControl using the specified save string
   * @param save - String representation for reconstruction
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    throw new UnimplementedException(
      "Cannot setSaveString on this type of control");
  }

  /**
   * copy the state of a remote control to this control
   * @param rmt remote Control whose state is copied
   * @throws VisADException if a VisAD error occurs
   */
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

  /**
   * @return true if o is identical with this
   * @param o - Object tested for equality with this
   */
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
