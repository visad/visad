
//
// ConstantMap.java
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

package visad;

/**
   mapping from constant to DisplayRealType
*/
public class ConstantMap extends ScalarMap {

  // no Scalar, control or function for ConstantMap
  private double Constant;

  public ConstantMap(double constant, DisplayRealType display_scalar)
                     throws VisADException {
    super(null, display_scalar); // no Scalar for ConstantMap
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
        (constant < displayRange[0] || constant > displayRange[1])) {
      throw new DisplayException("ConstantMap: constant is out of range");
    }
    Constant = constant;
  }

  public ConstantMap(Real constant, DisplayRealType display_scalar)
                     throws VisADException {
    this(constant.getValue(), display_scalar);
  }

  public double getConstant() {
    return Constant;
  }

  public String toString(String pre) {
    return pre + "ConstantMap: " + Constant +
           " -> " + getDisplayScalar().toString() + "\n";
  }

}

