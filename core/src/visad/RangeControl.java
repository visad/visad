//
// RangeControl.java
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
import java.util.StringTokenizer;

import visad.browser.Convert;
import visad.util.Util;

/**
   RangeControl is the VisAD class for controlling SelectRange display scalars.<P>
*/
public class RangeControl extends Control {

  private boolean initialized = false;
  private double RangeLow = Double.NaN;
  private double RangeHi = Double.NaN;

  public RangeControl(DisplayImpl d) {
    super(d);
  }

  /** initialize the range of selected values as (range[0], range[1]) */
  public void initializeRange(float[] range)
    throws VisADException, RemoteException
  {
    initializeRange(new double[] {(double) range[0], (double) range[1]});
  }

  /** initialize the range of selected values as (range[0], range[1]) */
  public void initializeRange(double[] range)
    throws VisADException, RemoteException
  {
    changeRange(range, initialized);
  }

  /** set the range of selected values as (range[0], range[1]) */
  public void setRange(float[] range)
         throws VisADException, RemoteException {
    setRange(new double[] {(double) range[0], (double) range[1]});
  }

  /** set the range of selected values as (range[0], range[1]) */
  public void setRange(double[] range)
         throws VisADException, RemoteException {
    if (RangeLow != RangeLow ||
        !Util.isApproximatelyEqual(RangeLow, range[0]) ||
        RangeHi != RangeHi ||
        !Util.isApproximatelyEqual(RangeHi, range[1]))
    {
      changeRange(range, true);
    }
  }

  private void changeRange(double[] range, boolean notify)
    throws VisADException, RemoteException
  {
    RangeLow = range[0];
    RangeHi = range[1];
    initialized = (RangeLow == RangeLow && RangeHi == RangeHi);
    if (notify) {
      changeControl(true);
    }
  }

  /** return the range of selected values */
  public float[] getRange() {
    float[] range = new float[2];
    range[0] = (float) RangeLow;
    range[1] = (float) RangeHi;
    return range;
  }

  /** return the range of selected values */
  public double[] getDoubleRange() {
    double[] range = new double[2];
    range[0] = RangeLow;
    range[1] = RangeHi;
    return range;
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return RangeLow + " " + RangeHi;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    if (st.countTokens() < 2) throw new VisADException("Invalid save string");
    double[] r = new double[2];
    for (int i=0; i<2; i++) r[i] = Convert.getDouble(st.nextToken());
    initializeRange(r);
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof RangeControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    RangeControl rc = (RangeControl )rmt;

    boolean changed = false;

    if (!Util.isApproximatelyEqual(RangeLow, rc.RangeLow)) {
      changed = true;
      RangeLow = rc.RangeLow;
    }
    if (!Util.isApproximatelyEqual(RangeHi, rc.RangeHi)) {
      changed = true;
      RangeHi = rc.RangeHi;
    }
    initialized = (RangeLow == RangeLow && RangeHi == RangeHi);

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    RangeControl rc = (RangeControl )o;

    if (!Util.isApproximatelyEqual(RangeLow, rc.RangeLow)) {
      return false;
    }
    if (!Util.isApproximatelyEqual(RangeHi, rc.RangeHi)) {
      return false;
    }

    return true;
  }

  public String toString()
  {
    return "RangeControl[" + RangeLow + "," + RangeHi + "]";
  }
}

