
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
import java.util.*;

/**
   A ScalarMap object defines a mapping from a RealType
   to a DisplayRealType.  A set of ScalarMap objects
   define how data are dislayed.<P>

   The mapping of values is linear.  Any non-linear mapping
   must be handled by Display CoordinateSystem-s.<P>
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

  /** true if dataRange set by application;
      disables automatic setting */
  private boolean isManual;

  /** true if Scalar values need to be scaled */
  boolean isScaled;
  /** ranges of values of DisplayScalar */
  double[] displayRange = new double[2];

  /** ranges of values of Scalar */
  private double[] dataRange = new double[2];
  /** scale and offset */
  private double scale, offset;

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
    isManual = false;
  }

  /** get the RealType that is the map domain */
  public RealType getScalar() {
    return Scalar;
  }

  /** get the DisplayRealType that is the map range */
  public DisplayRealType getDisplayScalar() {
    return DisplayScalar;
  }

  /** get the DisplayImpl this ScalarMap is linked to */
  public DisplayImpl getDisplay() {
    return display;
  }

  /** clear link to DisplayImpl */
  synchronized void nullDisplay() {
    display = null;
    control = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
  }

  /** set the DisplayImpl this ScalarMap is linked to */
  synchronized void setDisplay(DisplayImpl d)
               throws VisADException, RemoteException {
    if (d.equals(display)) return;
    if (display != null) {
      throw new DisplayException("ScalarMap.setDisplay: ScalarMap cannot belong" +
                                 " to two Displays");
    }
    display = d;
  }

  /** get Control for DisplayScalar */
  public Control getControl() {
    return control;
  }

  /** create Control for DisplayScalar */
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

  public boolean getScale(double[] so, double[] data, double[] display) {
    so[0] = scale;
    so[1] = offset;
    data[0] = dataRange[0];
    data[1] = dataRange[1];
    display[0] = displayRange[0];
    display[1] = displayRange[1];
    return isScaled;
  }

  /** set range used for linear map from Scalar to DisplayScalar values; 
      this is the call for applications */
  public void setRange(double low, double hi) throws VisADException {
    isManual = true;
    setRange(null, low, hi);
    // if it didn't work, don't lock out auto-scaling
    if (scale != scale || offset != offset) isManual = false;
  }

  /** set range used for linear map from Scalar to DisplayScalar values;
      this is the call for automatic scaling */
  void setRange(DataShadow shadow) throws VisADException {
    if (!isManual) setRange(shadow, 0.0, 0.0);
  }

  /** set range used for linear map from Scalar to
      DisplayScalar values */
  private synchronized void setRange(DataShadow shadow, double low, double hi)
          throws VisADException {
    int i = ScalarIndex;
    if (shadow != null) {
      dataRange[0] = shadow.ranges[0][i];
      dataRange[1] = shadow.ranges[1][i];
    }
    else {
      dataRange[0] = low;
      dataRange[1] = hi;
    }
    if (isScaled) {
      if (dataRange[0] == dataRange[1]) {
        scale = (displayRange[1] - displayRange[0]) / 1.0; 
        offset = displayRange[0] - scale * (dataRange[0] - 0.5);
      }
      else if (dataRange[0] == Double.MAX_VALUE ||
               dataRange[1] == -Double.MAX_VALUE) {
        scale = Double.NaN;
        offset = Double.NaN;
      }
      else {
        scale = (displayRange[1] - displayRange[0]) /
                (dataRange[1] - dataRange[0]);
        offset = displayRange[0] - scale * dataRange[0];
      }
      if (Double.isInfinite(scale) || Double.isInfinite(offset)) {
        scale = Double.NaN;
        offset = Double.NaN;
      }
    }
    if (DisplayScalar == Display.Animation && shadow != null) {
      Set set = shadow.animationSampling;
      if (set == null) {
        // WLH - should never happen
        // set = shadow.animationRangeSampling;
        throw new DisplayException("ScalarMap.setRange: animationRangeSampling");
      }
      if (set == null) {
        set = new Linear1DSet(Scalar, dataRange[0], dataRange[1], 100);
      }
      ((AnimationControl) control).setSet(set, true);
    }
    else if (DisplayScalar == Display.IsoContour) {
      boolean[] bvalues = new boolean[2];
      bvalues[0] = true; // mainContours
      bvalues[1] = true; // labels
      float[] values = new float[5];
      values[0] = (float) dataRange[0]; // surfaceValue
      values[1] = (float) (dataRange[1] - dataRange[0]) / 10.0f; // contourInterval
      values[2] = (float) dataRange[0]; // lowLimit
      values[3] = (float) dataRange[1]; // hiLimit
      values[4] = (float) dataRange[0]; // base
      ((ContourControl) control).setMainContours(bvalues, values, true);
    }
  }

  boolean badRange() {
    return (isScaled && (scale != scale || offset != offset));
  }

  /** apply linear map to Scalar values */
  public float[] scaleValues(double[] values) {
    if (values == null || badRange()) return null;
    float[] new_values = new float[values.length];
    if (isScaled) {
      for (int i=0; i<values.length; i++) {
        new_values[i] = (float) (offset + scale * values[i]);
      }
    }
    else {
      for (int i=0; i<values.length; i++) {
        new_values[i] = (float) values[i];
      }
    }
      return new_values;
  }

  /** apply linear map to Scalar values */
  public float[] scaleValues(float[] values) {
    if (values == null || badRange()) return null;
    float[] new_values = new float[values.length];
    if (isScaled) {
      for (int i=0; i<values.length; i++) {
        new_values[i] = (float) (offset + scale * values[i]);
      }
    }
    else {
      for (int i=0; i<values.length; i++) {
        new_values[i] = values[i];
      }
    }
      return new_values;
  }

  /** apply inverse linear map to Scalar values */
  public double[] inverseScaleValues(float[] values) {
    if (values == null) return null;
    double[] new_values = new double[values.length];
    if (isScaled) {
      for (int i=0; i<values.length; i++) {
        new_values[i] = (values[i] - offset) / scale;
      }
    }
    else {
      for (int i=0; i<values.length; i++) {
        new_values[i] = values[i];
      }
    }
      return new_values;
  }

  /** ensure that non-Manual components of flow_tuple have equal
      dataRanges symmetric about 0.0 */
  static void equalizeFlow(Vector mapVector,
         DisplayTupleType flow_tuple) throws VisADException {
    double[] range = new double[2];
    double low = Double.MAX_VALUE;
    double hi = -Double.MAX_VALUE;
    boolean anyAuto = false;
 
    Enumeration maps = mapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = ((ScalarMap) maps.nextElement());
      DisplayRealType dtype = map.getDisplayScalar();
      DisplayTupleType tuple = dtype.getTuple();
      if (flow_tuple.equals(tuple) && !map.isManual &&
          !map.badRange()) {
        anyAuto = true;
        low = Math.min(low, map.dataRange[0]);
        hi = Math.max(hi, map.dataRange[1]);
      }
    }
    if (!anyAuto) return;
    hi = Math.max(hi, -low);
    low = -hi;
    maps = mapVector.elements();
    while(maps.hasMoreElements()) {
      ScalarMap map = ((ScalarMap) maps.nextElement());
      DisplayRealType dtype = map.getDisplayScalar();
      DisplayTupleType tuple = dtype.getTuple();
      if (flow_tuple.equals(tuple) && !map.isManual &&
          !map.badRange()) {
        map.setRange(null, low, hi);
      }
    }
  }

  /** get index of DisplayScalar in display.DisplayRealTypeVector */
  int getDisplayScalarIndex() {
    return DisplayScalarIndex;
  }

  /** get index of Scalar in display.RealTypeVector */
  int getScalarIndex() {
    return ScalarIndex;
  }

  /** set index of Scalar in display.RealTypeVector */
  void setScalarIndex(int index) {
    ScalarIndex = index;
  }

  /** set index of DisplayScalar in display.DisplayRealTypeVector */
  void setDisplayScalarIndex(int index) {
    DisplayScalarIndex = index;
  }

  /** set index of DisplayScalar in value array used by
      ShadowType.doTransform */
  public void setValueIndex(int index) {
    ValueIndex = index;
  }

  /** get index of DisplayScalar in value array used by
      ShadowType.doTransform */
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

