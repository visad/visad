
//
// ScalarMap.java
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

import java.rmi.*;

/**
   mapping from RealType to DisplayRealType;
   A set of these define how data are dislayed
*/
public class ScalarMap extends Object implements java.io.Serializable {

  private RealType Scalar;
  private DisplayRealType DisplayScalar;

  // index into Display.RealTypeVector
  private int ScalarIndex;
  // index into Display.DisplayRealTypeVector
  private int DisplayScalarIndex;
  // index into ValueArray
  int ValueIndex;

  // control associated with DisplayScalar, or null
  private Control control;
  // unique Display this ScalarMap is part of
  private transient DisplayImpl display;

  // true if Scalar values need to be scaled
  boolean isScaled;
  // ranges of values of DisplayScalar
  double[] displayRange = new double[2];

  // ranges of values of Scalar
  private double[] dataRange = new double[2];
  // scale and offset
  private double scale, offset;

  // any non-linear value mapping must be handled by
  // Display CoordinateSystem-s

  public ScalarMap(RealType scalar, DisplayRealType display_scalar)
         throws VisADException {
    if (scalar == null && !(this instanceof ConstantMap)) {
      throw new DisplayException("ScalarMap: scalar is null");
    }
    if (display_scalar == null) {
      throw new DisplayException("ScalarMap: display_scalar is null");
    }
    if (display_scalar.equals(Display.List)) {
      throw new DisplayException("ScalarMap: display_scalar may not be List");
    }
    control = null;
    Scalar = scalar;
    DisplayScalar = display_scalar;
    display = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
    isScaled = DisplayScalar.getRange(displayRange);
  }

  public RealType getScalar() {
    return Scalar;
  }

  public DisplayRealType getDisplayScalar() {
    return DisplayScalar;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  synchronized void nullDisplay() {
    display = null;
    control = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
  }

  synchronized void setDisplay(DisplayImpl d)
               throws VisADException, RemoteException {
    if (d.equals(display)) return;
    if (display != null) {
      throw new DisplayException("ScalarMap.setDisplay: ScalarMap cannot belong" +
                                 " to two Displays");
    }
    display = d;
  }

  public Control getControl() {
    return control;
  }

  /** create controls for DisplayScalar-s */
  synchronized void setControl() throws VisADException, RemoteException {
    if (display == null) {
      throw new DisplayException("ScalarMap.setControl: not part of " +
                                 "any Display");
    }
    if (this instanceof ConstantMap) return;
    Control proto = DisplayScalar.getControl();
    if (proto == null) return;
    control = proto.copy(this);
  }

  void setRange(DataShadow shadow) throws VisADException {
    int i = ScalarIndex;
    dataRange[0] = shadow.ranges[0][i];
    dataRange[1] = shadow.ranges[1][i];
    if (isScaled) {
      if (dataRange[0] < dataRange[1]) {
        scale = (displayRange[1] - displayRange[0]) /
                (dataRange[1] - dataRange[0]);
        offset = displayRange[0] - scale * dataRange[0];
      }
      else {
        scale = 0.0;
        offset = displayRange[0];
      }
    }
    if (DisplayScalar == Display.Animation) {
      Set set = shadow.animationSampling;
      if (set == null) {
        // WLH - should never happen
        // set = shadow.animationRangeSampling;
        throw new DisplayException("ScalarMap.setRange: animationRangeSampling");
      }
      if (set == null) {
        set = new Linear1DSet(Scalar, dataRange[0], dataRange[1], 100);
      }
      ((AnimationControl) control).setSet(set);
    }
  }

  int getDisplayScalarIndex() {
    return DisplayScalarIndex;
  }

  int getScalarIndex() {
    return ScalarIndex;
  }

  void setScalarIndex(int index) {
    ScalarIndex = index;
  }

  void setDisplayScalarIndex(int index) {
    DisplayScalarIndex = index;
  }

  public void setValueIndex(int index) {
    ValueIndex = index;
  }

  public int getValueIndex() {
    return ValueIndex;
  }

  public String toString() {
    return toString("");
  }

  public String toString(String pre) {
    return pre + "ScalarMap: " + Scalar.toString() +
           " -> " + DisplayScalar.toString() + "\n";
  }

}

