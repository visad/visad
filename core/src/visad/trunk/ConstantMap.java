//
// ConstantMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import visad.util.Util;

/**
   mapping from constant to DisplayRealType
*/
public class ConstantMap extends ScalarMap {

  // no Scalar, control or function for ConstantMap
  private double Constant;

  // WLH 24 Aug 2001
  // flag to allow multiple use of a ConstantMap
  private static boolean allowMultipleUseKludge = false;

  /** construct a ConstantMap with a double constant;
      display_scalar may not be Animation, SelectValue, SelectRange
      or IsoContour */
  public ConstantMap(double constant, DisplayRealType display_scalar)
                     throws VisADException {
    super(null, display_scalar, false); // no Scalar for ConstantMap
    if (Double.isNaN(constant) || Double.isInfinite(constant)) {
      throw new DisplayException("ConstantMap: constant is missing (NaN) " +
                                 "or infinity");
    }
    if (display_scalar.equals(Display.Animation) ||
        display_scalar.equals(Display.SelectValue) ||
        display_scalar.equals(Display.SelectRange) ||
        display_scalar.equals(Display.IsoContour) ||
        display_scalar.equals(Display.Text)) {
      throw new DisplayException("ConstantMap: illegal for " + display_scalar);
    }
    if (isScaled &&
        !display_scalar.equals(Display.XAxis) &&
        !display_scalar.equals(Display.YAxis) &&
        !display_scalar.equals(Display.ZAxis) &&
        (constant < displayRange[0] || constant > displayRange[1])) {
      throw new DisplayException("ConstantMap: constant is out of range");
    }
    Constant = constant;
  }

  /** construct a ConstantMap with a Real constant;
      display_scalar may not be Animation, SelectValue, SelectRange
      or IsoContour */
  public ConstantMap(Real constant, DisplayRealType display_scalar)
                     throws VisADException {
    this(constant.getValue(), display_scalar);
  }

  // WLH 24 Aug 2001
  public static void setAllowMultipleUseKludge(boolean k) {
    allowMultipleUseKludge = k;
  }

  // WLH 24 Aug 2001
  public static boolean getAllowMultipleUseKludge() {
    return allowMultipleUseKludge;
  }

  void setControl() throws VisADException, RemoteException {
    return;
  }

  public double getConstant() {
    return Constant;
  }

  public boolean equals(Object o)
  {
    boolean	equals;
    if (!(o instanceof ConstantMap)) {
      equals = false;
    }
    else {
      ConstantMap cm = (ConstantMap )o;
      equals = this == cm || (this.compareTo(cm) == 0);
    }
    return equals;
  }

  /**
   * Compares this instance to another object.
   * @param obj		The other object.
   * @return            A value that is negative, zero, or positive depending on
   *                    whether this instance is considered less than, equal
   *                    to, or greater than the other object, respectively.
   */
  public int compareTo(Object obj)
  {
    return
      obj instanceof ConstantMap
	? compareTo((ConstantMap)obj)
	: compareTo((ScalarMap)obj);
  }

  /**
   * Compares this instance to another instance.
   * @param that	The other instance.
   * @return            A value that is negative, zero, or positive depending on
   *                    whether this instance is considered less than, equal
   *                    to, or greater than the other instance, respectively.
   */
  protected int compareTo(ConstantMap that)
  {
    int	comp = getDisplayScalar().compareTo(that.getDisplayScalar());
    if (comp == 0) {
      comp =
	Util.isApproximatelyEqual(Constant, that.Constant)
	  ? 0
	  : ((Constant - that.Constant) < 0 ? -1 : 1);
    }
    return comp;
  }

  /**
   * Compares this instance to a ScalarMap.
   * @param that	The ScalarMap.
   * @return            -1 always.  ConstantMap-s are considered less than
   *			true ScalarMap-s.
   */
  protected int compareTo(ScalarMap that)
  {
    return -1;
  }

  public Object clone()
  {
    try {
      ConstantMap cm = new ConstantMap(Constant, getDisplayScalar());
      copy(cm);
      return cm;
    } catch (Exception e) {
      return null;
    }
  }

  public String toString(String pre) {
    return pre + "ConstantMap: " + Constant +
           " -> " + getDisplayScalar().toString() + "\n";
  }

  // WLH 28 Nov 2000
  void setAspectCartesian(double[] aspect)
       throws VisADException, RemoteException {
  }

}

