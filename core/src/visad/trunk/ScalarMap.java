//
// ScalarMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.*;

/**
   A ScalarMap object defines a mapping from a RealType
   to a DisplayRealType.  A set of ScalarMap objects
   define how data are dislayed.<P>

   The mapping of values is linear.  Any non-linear mapping
   must be handled by Display CoordinateSystem-s.<P>
*/
public class ScalarMap extends Object implements java.io.Serializable {

  private ScalarType Scalar;
  private DisplayRealType DisplayScalar;

  // index into Display.RealTypeVector
  private int ScalarIndex;
  // index into Display.DisplayRealTypeVector
  private int DisplayScalarIndex;
  // index into ValueArray
  int ValueIndex;

  // control associated with DisplayScalar, or null
  private transient Control control;
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

  /** incremented by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;

  /** location of axis scale if DisplayScalar is XAxis, YAxis or ZAxis */
  private int axis = -1;
  private int axis_ordinal = -1;
  private boolean scale_flag = false;
  private boolean back_scale_flag = false;
  private float[] scale_color = {1.0f, 1.0f, 1.0f};

  /** Vector of ScalarMapListeners */
  private transient Vector ListenerVector = new Vector();

  public ScalarMap(ScalarType scalar, DisplayRealType display_scalar)
         throws VisADException {
    this(scalar, display_scalar, true);
  }

  ScalarMap(ScalarType scalar, DisplayRealType display_scalar,
            boolean needNonNullScalar)
         throws VisADException {
    if (scalar == null && needNonNullScalar) {
      throw new DisplayException("ScalarMap: scalar is null");
    }
    if (display_scalar == null) {
      throw new DisplayException("ScalarMap: display_scalar is null");
    }
    if (display_scalar.equals(Display.List)) {
      throw new DisplayException("ScalarMap: display_scalar may not be List");
    }
    boolean text = display_scalar.getText();
    if (scalar != null) {
      if (text && !(scalar instanceof TextType)) {
        throw new DisplayException("ScalarMap: RealType scalar cannot be " +
                                   "used with TextType display_scalar");
      }
      if (!text && !(scalar instanceof RealType)) {
        throw new DisplayException("ScalarMap: TextType scalar cannot be " +
                                   "used with RealType display_scalar");
      }
    }
    control = null;
    Scalar = scalar;
    DisplayScalar = display_scalar;
    display = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
    isScaled = DisplayScalar.getRange(displayRange);
    isManual = false;
    dataRange[0] = Double.NaN;
    dataRange[1] = Double.NaN;
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
  }

  /** invoke incTick on every application call to setRange */
  public long incTick() {
    if (display != null) display.controlChanged();
    NewTick += 1;
    if (NewTick == Long.MAX_VALUE) NewTick = Long.MIN_VALUE + 1;
    return NewTick;
  }
 
  /** set tickFlag according to OldTick and NewTick */
  public synchronized void setTicks() {
    tickFlag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
/*
System.out.println(Scalar + " -> " + DisplayScalar +
                   "  set  tickFlag = " + tickFlag);
*/
    OldTick = NewTick;
    if (control != null) control.setTicks();
  }
 
  public synchronized boolean peekTicks(DataRenderer r, DataDisplayLink link) {
    if (control == null) {
/*
boolean flag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
if (flag) {
  System.out.println(Scalar + " -> " + DisplayScalar + "  peek  flag = " + flag);
}
*/
      return (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
    }
    else {
/*
boolean flag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
boolean cflag = control.peekTicks(r, link);
if (flag || cflag) {
  System.out.println(Scalar + " -> " + DisplayScalar + "  peek   flag = " +
                     flag + " cflag = " + cflag);
}
*/
      return (OldTick < NewTick || (NewTick < 0 && 0 < OldTick)) ||
             control.peekTicks(r, link);
    }
  }

  /** return true if application called setRange */
  public synchronized boolean checkTicks(DataRenderer r, DataDisplayLink link) {
    if (control == null) {
/*
System.out.println(Scalar + " -> " + DisplayScalar + "  check  tickFlag = " +
                   tickFlag);
*/
      return tickFlag;
    }
    else {
/*
boolean cflag = control.checkTicks(r, link);
System.out.println(Scalar + " -> " + DisplayScalar + "  check  tickFlag = " + 
                   tickFlag + " cflag = " + cflag); 
*/
      return tickFlag || control.checkTicks(r, link);
    }
  }

  /** reset tickFlag */
  synchronized void resetTicks() {
// System.out.println(Scalar + " -> " + DisplayScalar + "  reset");
    tickFlag = false;
    if (control != null) control.resetTicks();
  }

  /** get the ScalarType that is the map domain */
  public ScalarType getScalar() {
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
    // CTR: 6 October 1998 -- stop animation before killing control
    if (control instanceof AnimationControl) {
      ((AnimationControl) control).stop();
    }

    display = null;
    control = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
    scale_flag = back_scale_flag;
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
    if (scale_flag) makeScale();
  }

  /**
   * Gets the Control for the DisplayScalar.  The Control is constructed 
   * when this ScalarMap is linked to a Display via an invocation of the 
   * Display's <code>addMap()</code> method.  Not all ScalarMaps have Controls,
   * generally depending on the ScalarMap's DisplayRealType.
   * @return			The Control for the DisplayScalar or <code>
   *				null</code> if one has not yet been set.
   */
  public Control getControl() {
    return control;
  }

  /** create Control for DisplayScalar */
  synchronized void setControl() throws VisADException, RemoteException {
    if (display == null) {
      throw new DisplayException("ScalarMap.setControl: not part of " +
                                 "any Display");
    }
    control = display.getDisplayRenderer().makeControl(this);
    display.addControl(control);
  }

  /** return value is true if data (RealType) values are linearly
      scaled to display (DisplayRealType) values;
      if so, then values are scaled by:
      display_value = data_value * so[0] + so[1];
      (data[0], data[1]) defines range of data values (either passed
      in to setRange or computed by autoscaling logic) and
      (display[0], display[1]) defines range of display values;
      so, data, display must each be passed in as double[2] arrays */
  public boolean getScale(double[] so, double[] data, double[] display) {
    so[0] = scale;
    so[1] = offset;
    data[0] = dataRange[0];
    data[1] = dataRange[1];
    display[0] = displayRange[0];
    display[1] = displayRange[1];
    return isScaled;
  }

  public double[] getRange() {
    double[] range = {dataRange[0], dataRange[1]};
    return range;
  }

  /** explicitly set the range of data (RealType) values according
      to Unit conversion between this ScalarMap's RealType and
      DisplayRealType (both must have Units and they must be
      convertable; if neither this nor setRange is invoked, then
      the range will be computed from the initial values of Data
      objects linked to the Display by autoscaling logic. */
  public void setRangeByUnits()
         throws VisADException, RemoteException {
    isManual = true;
    setRange(null, 0.0, 0.0, true);
    if (scale == scale && offset == offset) {
      incTick(); // did work, so wake up Display
    }
    else {
      isManual = false; // didn't work, so don't lock out auto-scaling
    }
  }

  /** explicitly set the range of data (RealType) values; used for
      linear map from Scalar to DisplayScalar values;
      if neither this nor setRangeByUnits is invoked, then the
      range will be computed from the initial values of Data
      objects linked to the Display by autoscaling logic;
      if the range of data values is (0.0, 1.0), for example, this
      method may be invoked with low = 1.0 and hi = 0.0 to invert
      the display scale */
  public void setRange(double low, double hi)
         throws VisADException, RemoteException {
    isManual = true;
    setRange(null, low, hi, false);
    if (scale == scale && offset == offset) {
      incTick(); // did work, so wake up Display
    }
    else {
      isManual = false; // didn't work, so don't lock out auto-scaling
    }
  }

  /** set range used for linear map from Scalar to DisplayScalar values;
      this is the call for automatic scaling */
  void setRange(DataShadow shadow)
         throws VisADException, RemoteException {
    if (!isManual) setRange(shadow, 0.0, 0.0, false);
  }

  /** set range used for linear map from Scalar to
      DisplayScalar values */
  private synchronized void setRange(DataShadow shadow, double low, double hi,
          boolean unit_flag) throws VisADException, RemoteException {
    int i = ScalarIndex;
    if (shadow != null) {
      if (i < 0) return;
      dataRange[0] = shadow.ranges[0][i];
      dataRange[1] = shadow.ranges[1][i];
    }
    else if (unit_flag) {
      Unit data_unit =
        (Scalar instanceof RealType) ? ((RealType) Scalar).getDefaultUnit() :
                                       null;
      Unit display_unit = DisplayScalar.getDefaultUnit();
      if (data_unit == null || display_unit == null) {
        throw new UnitException("ScalarMap.setRangeByUnits: null Unit");
      }
      dataRange[0] = data_unit.toThis(displayRange[0], display_unit);
      dataRange[1] = data_unit.toThis(displayRange[1], display_unit);
/*
System.out.println("data_unit = " + data_unit + " display_unit = " + display_unit);
System.out.println("dataRange = " + dataRange[0] + " " + dataRange[1] +
" displayRange = " + displayRange[0] + " " + displayRange[1]);
*/
    }
    else {
      dataRange[0] = low;
      dataRange[1] = hi;
    }
    if (isScaled) {
      if (dataRange[0] == Double.MAX_VALUE ||
          dataRange[1] == -Double.MAX_VALUE) {
        dataRange[0] = Double.NaN;
        dataRange[1] = Double.NaN;
        scale = Double.NaN;
        offset = Double.NaN;
      }
      else {
        if (dataRange[0] == dataRange[1]) {
          dataRange[0] -= 0.5;
          dataRange[1] += 0.5;
        }
        scale = (displayRange[1] - displayRange[0]) /
                (dataRange[1] - dataRange[0]);
        offset = displayRange[0] - scale * dataRange[0];
      }
      if (Double.isInfinite(scale) || Double.isInfinite(offset) ||
          scale != scale || offset != offset) {
        dataRange[0] = Double.NaN;
        dataRange[1] = Double.NaN;
        scale = Double.NaN;
        offset = Double.NaN;
      }
    }
    else { // if (!isScaled)
      if (dataRange[0] == Double.MAX_VALUE ||
          dataRange[1] == -Double.MAX_VALUE) {
        dataRange[0] = Double.NaN;
        dataRange[1] = Double.NaN;
      }
    }
/*
System.out.println(Scalar + " -> " + DisplayScalar + " range: " + dataRange[0] +
                   " to " + dataRange[1] + " scale: " + scale + " " + offset);
*/
    if (DisplayScalar.equals(Display.Animation) && shadow != null) {
      Set set = shadow.animationSampling;
      if (set == null) {
        return;
        // WLH - should never happen
        // set = shadow.animationRangeSampling;
        // throw new DisplayException("ScalarMap.setRange: animationRangeSampling");
      }
      // dglo 24 Nov 1998 -- Dead code
      // if (set == null) {
      //   set = new Linear1DSet(Scalar, dataRange[0], dataRange[1], 100);
      // }
      ((AnimationControl) control).setSet(set, true);
    }
    else if (DisplayScalar.equals(Display.IsoContour)) {
      boolean[] bvalues = new boolean[2];
      float[] values = new float[5];
      ((ContourControl) control).getMainContours(bvalues, values);
      if (shadow == null) {
        // don't set surface value for auto-scale
        values[0] = (float) dataRange[0]; // surfaceValue
      }
      values[1] = (float) (dataRange[1] - dataRange[0]) / 10.0f; // contourInterval
      values[2] = (float) dataRange[0]; // lowLimit
      values[3] = (float) dataRange[1]; // hiLimit
      values[4] = (float) dataRange[0]; // base
      ((ContourControl) control).setMainContours(bvalues, values, true);
    }
    else if (DisplayScalar.equals(Display.XAxis) ||
             DisplayScalar.equals(Display.YAxis) ||
             DisplayScalar.equals(Display.ZAxis)) {
      if (dataRange[0] != Double.MAX_VALUE &&
          dataRange[1] != -Double.MAX_VALUE &&
          dataRange[0] == dataRange[0] &&
          dataRange[1] == dataRange[1] &&
          dataRange[0] != dataRange[1] &&
          scale == scale && offset == offset) {
        if (display != null) {
          makeScale();
        }
        else {
          scale_flag = true;
        }
        back_scale_flag = true;
      }
    }

    if (dataRange[0] == dataRange[0] &&
        dataRange[1] == dataRange[1] && ListenerVector != null) {
      synchronized (ListenerVector) {
        boolean auto = (shadow != null);
        Enumeration listeners = ListenerVector.elements();
        while (listeners.hasMoreElements()) {
          ScalarMapListener listener =
            (ScalarMapListener) listeners.nextElement();
          listener.mapChanged(new ScalarMapEvent(this, auto));
        }
      }
    }
  }

  /** add a ScalarMapListener, to be notified whenever setRange is
      invoked */
  public synchronized void addScalarMapListener(ScalarMapListener listener) {
    if (ListenerVector == null) {
      ListenerVector = new Vector();
    }
    ListenerVector.addElement(listener);
    if (dataRange[0] == dataRange[0] &&
        dataRange[1] == dataRange[1]) {
      try {
        listener.mapChanged(new ScalarMapEvent(this, false));
      }
      catch (VisADException e) {
      }
      catch (RemoteException e) {
      }
    }
  }
 
  /** remove a ScalarMapListener */
  public void removeScalarMapListener(ScalarMapListener listener) {
    if (listener != null && ListenerVector != null) {
      ListenerVector.removeElement(listener);
    }
  }

  private static final double SCALE = 0.07;
  private static final double OFFSET = 1.05;

  private void makeScale()
          throws VisADException, RemoteException {
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    axis = (DisplayScalar.equals(Display.XAxis)) ? 0 :
           (DisplayScalar.equals(Display.YAxis)) ? 1 : 2;
    if (axis_ordinal < 0) {
      axis_ordinal = displayRenderer.getAxisOrdinal(axis);
    }
    VisADLineArray[] arrays = new VisADLineArray[4];
    boolean twoD = displayRenderer.getMode2D();

// now create scale along axis at axis_ordinal position in array
// twoD may help define orientation

    // compute graphics positions
    double[] base = null; // vector from one character to another
    double[] up = null; // vector from bottom of character to top
    double[] startn = null; // -1.0 position 
    double[] startp = null; // +1.0 position 

    double XMIN = -1.0;
    double YMIN = -1.0;
    double ZMIN = -1.0;

    double line = 2.0 * axis_ordinal * SCALE;

    double ONE = 1.0;
    if (dataRange[0] > dataRange[1]) ONE = -1.0;
    if (axis == 0) {
      base = new double[] {SCALE, 0.0, 0.0};
      up = new double[] {0.0, SCALE, SCALE};
      startp = new double[] {ONE, YMIN * (OFFSET + line), ZMIN * (OFFSET + line)};
      startn = new double[] {-ONE, YMIN * (OFFSET + line), ZMIN * (OFFSET + line)};
    }
    else if (axis == 1) {
      base = new double[] {0.0, -SCALE, 0.0};
      up = new double[] {SCALE, 0.0, SCALE};
      startp = new double[] {XMIN * (OFFSET + line), ONE, ZMIN * (OFFSET + line)};
      startn = new double[] {XMIN * (OFFSET + line), -ONE, ZMIN * (OFFSET + line)};
    }
    else if (axis == 2) {
      base = new double[] {0.0, 0.0, -SCALE};
      up = new double[] {SCALE, SCALE, 0.0};
      startp = new double[] {XMIN * (OFFSET + line), YMIN * (OFFSET + line), ONE};
      startn = new double[] {XMIN * (OFFSET + line), YMIN * (OFFSET + line), -ONE};
    }
    if (twoD) {
      base[2] = 0.0;
      up[2] = 0.0;
      startn[2] = 0.0;
      startp[2] = 0.0;
      if (axis == 2) return;
    }

    // compute tick mark values
    double range = Math.abs(dataRange[1] - dataRange[0]);
    double min = Math.min(dataRange[0], dataRange[1]);
    double max = Math.max(dataRange[0], dataRange[1]);
    double tens = 1.0;
    if (range < tens) {
      tens /= 10.0;
      while (range < tens) tens /= 10.0;
    }
    else {
      while (10.0 * tens <= range) tens *= 10.0;
    }
    // now tens <= range < 10.0 * tens;
    double ratio = range / tens;
    if (ratio < 2.0) {
      tens /= 5.0;
    }
    else if (ratio < 4.0) {
      tens /= 2.0;
    }

    int bot = (int) Math.ceil(min / tens);
    int top = (int) Math.floor(max / tens);
    arrays[0] = new VisADLineArray();
    int nticks = (top - bot) + 1;
    float[] coordinates = new float[6 * (nticks + 1)];
    // draw base line
    for (int i=0; i<3; i++) {
      coordinates[i] = (float) startn[i];
      coordinates[3 + i] = (float) startp[i];
    }

    // draw tick marks
    int k = 6;
    for (int j=bot; j<=top; j++) {
      double val = j * tens;
      double a = (val - min) / (max - min);
      for (int i=0; i<3; i++) {
        if ((k + 3 + i) < coordinates.length) {
          // guard against error that cannot happen, but was seen?
          coordinates[k + i] = (float) ((1.0 - a) * startn[i] + a * startp[i]);
          coordinates[k + 3 + i] = (float) (coordinates[k + i] - 0.5 * up[i]);
        }
      }
      k += 6;
    }
    arrays[0].vertexCount = 2 * (nticks + 1);
    arrays[0].coordinates = coordinates;

    double[] startbot = new double[3];
    double[] starttop = new double[3];
    double[] startlabel = new double[3];
    double botval = bot * tens;
    double topval = top * tens;
    double abot = (botval - min) / (max - min);
    double atop = (topval - min) / (max - min);
    for (int i=0; i<3; i++) {
      startbot[i] = (1.0 - abot) * startn[i] + abot * startp[i] - 1.5 * up[i];
      starttop[i] = (1.0 - atop) * startn[i] + atop * startp[i] - 1.5 * up[i];
      startlabel[i] = 0.5 * (startn[i] + startp[i]) - 1.5 * up[i];
    }

    // draw RealType name
    arrays[1] = PlotText.render_label(Scalar.getName(), startlabel,
                                      base, up, true);
    // draw number at bottom tick mark
    arrays[2] = PlotText.render_label(PlotText.shortString(botval), startbot,
                                      base, up, true);
    // draw number at top tick mark
    arrays[3] = PlotText.render_label(PlotText.shortString(topval), starttop,
                                      base, up, true);

    VisADLineArray array = VisADLineArray.merge(arrays);
    displayRenderer.setScale(axis, axis_ordinal, array, scale_color);
    scale_flag = false;
  }

  /** set color of axis scales; color must be float[3] with red,
      green and blue components; DisplayScalar must be XAxis,
      YAxis or ZAxis */
  public void setScaleColor(float[] color) throws VisADException {
     throw new DisplayException("ScalarMap.setScaleColor: DisplayScalar " +
    if (DisplayScalar != Display.XAxis &&
        DisplayScalar != Display.YAxis &&
        DisplayScalar != Display.ZAxis) {
                                "must be XAxis, YAxis or ZAxis");
    }
    if (color == null || color.length != 3) {
     throw new DisplayException("ScalarMap.setScaleColor: color is " +
                                "null or wrong length");
    }
    scale_color[0] = color[0];
    scale_color[1] = color[1];
    scale_color[2] = color[2];
  }

  boolean badRange() {
    return (isScaled && (scale != scale || offset != offset));
  }

  /** return an array of display (DisplayRealType) values by
      linear scaling (if applicable) the data_values array
      (RealType values) */
  public float[] scaleValues(double[] values) {
/* WLH 23 June 99
    if (values == null || badRange()) return null;
*/
    if (values == null) return null;
    float[] new_values = new float[values.length];
    if (badRange()) {
      for (int i=0; i<values.length; i++) new_values[i] = Float.NaN;
    }
    else if (isScaled) {
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

  /** return an array of display (DisplayRealType) values by
      linear scaling (if applicable) the data_values array
      (RealType values) */
  public float[] scaleValues(float[] values) {
/* WLH 23 June 99
    if (values == null || badRange()) return null;
*/
    if (values == null) return null;
    float[] new_values = null;
    if (badRange()) {
      new_values = new float[values.length];
      for (int i=0; i<values.length; i++) new_values[i] = Float.NaN;
    }
    else if (isScaled) {
      new_values = new float[values.length];
      for (int i=0; i<values.length; i++) {
        new_values[i] = (float) (offset + scale * values[i]);
      }
    }
    else {
      new_values = values;
    }
    return new_values;
  }

  /** return an array of data (RealType) values by inverse
      linear scaling (if applicable) the display_values array
      (DisplayRealType values); this is useful for direct
      manipulation and cursor labels */
  public float[] inverseScaleValues(float[] values) {
    if (values == null) return null;
    float[] new_values = new float[values.length];
    if (isScaled) {
      for (int i=0; i<values.length; i++) {
        new_values[i] = (float) ((values[i] - offset) / scale);
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
  static void equalizeFlow(Vector mapVector, DisplayTupleType flow_tuple)
         throws VisADException, RemoteException {
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
        map.setRange(null, low, hi, false);
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

