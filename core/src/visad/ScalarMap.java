//
// ScalarMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
public class ScalarMap extends Object
        implements Cloneable, java.io.Serializable, Comparable {

  // WLH 31 Aug 2000
  // display Unit to use rather than Scalar default Unit
  private Unit overrideUnit = null;
  // scale and offset for converting from Scalar default Unit to overrideUnit
  private double override_scale, override_offset;

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
  
  /** ranges of values of Scalar in default units*/
  private double[] defaultUnitRange = new double[2];

  /** scale and offset */
  private double scale, offset;

  /** incremented by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;

  private String scalarName = null;

  /** location of axis scale if DisplayScalar is XAxis, YAxis or ZAxis 
  private int axis = -1;
  private int axis_ordinal = -1;
   < removed for AxisScale 10-Oct-2000 > */
  private boolean scale_flag = false;
  private boolean back_scale_flag = false;
  //private float[] scale_color = {1.0f, 1.0f, 1.0f}; <DRM 10-Oct-2000>
  private boolean scale_on = true;
  private boolean underscore_to_blank = false;

  /** Vector of ScalarMapListeners */
  private transient Vector ListenerVector = new Vector();

  /** AxisScale */
  private AxisScale axisScale = null;  // added DRM 10-Oct-2000

  /**
   * Construct a <CODE>ScalarMap</CODE> that maps the scalar to
   * the display_scalar.
   * @param  scalar  ScalarType (must be RealType at present)
   * @param  display_scalar   DisplayScalar to map to.  If the
   *                          display_scalar is one of the spatial
   *                          axes (X, Y, Z) an AxisScale will be
   *                          created.
   * @throws VisADException   VisAD error
   */
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
/* WLH 15 June 2000
      if (text && !(scalar instanceof TextType)) {
        throw new DisplayException("ScalarMap: RealType scalar cannot be " +
                                   "used with TextType display_scalar");
      }
*/
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
    defaultUnitRange[0] = dataRange[0];
    defaultUnitRange[1] = dataRange[1];
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
    if (Scalar != null) scalarName = Scalar.getName();
    if (DisplayScalar.equals(Display.XAxis) ||
        DisplayScalar.equals(Display.YAxis) ||
        DisplayScalar.equals(Display.ZAxis)) {
        axisScale = new AxisScale(this);
    }
  }

  /** re-enable auto-scaling for this ScalarMap */
  public void resetAutoScale() {
    isManual = false;
  }

  /** disable auto-scaling for this ScalarMap */
  public void disableAutoScale() {
    isManual = true;
  }

  /** determine whether this ScalarMap is auto-scaled */
  public boolean isAutoScale() {
    return !isManual;
  }

  // WLH 22 August 2001
  public boolean doInitialize() {
    if (DisplayScalar.equals(Display.IsoContour)) {
      if (control != null) {
        float[] lowhibase = new float[3];
        boolean[] dashes = new boolean[1];
        float[] levs =
          ((ContourControl) control).getLevels(lowhibase, dashes);
        return (levs == null);
      }
      else {
        return false;
      }
    }
    else {
      return isScaled && !isManual;
    }
  }

  // WLH 31 Aug 2000
  /** 
   * Set display Unit to override default Unit of Scalar;
   *  MUST be called before any data are displayed 
   * @param  unit  unit that data will be displayed with
   * @throws  VisADException  <CODE>unit</CODE> is not convertable with
   *                          the default unit or scalar is not a RealType.
   */
  public void setOverrideUnit(Unit unit) throws VisADException {
    if (!(Scalar instanceof RealType)) {
      throw new UnitException("Scalar is not RealType");
    }
    Unit rtunit = ((RealType) Scalar).getDefaultUnit();
    if (!Unit.canConvert(unit, rtunit)) {
      throw new UnitException("unit not convertable with RealType default");
    }
    if (unit != null) {
      overrideUnit = unit;
      override_offset = overrideUnit.toThis(0.0, rtunit);
      override_scale = overrideUnit.toThis(1.0, rtunit) - override_offset;
    }
  }

  // WLH 31 Aug 2000
  /**
   * Return the override unit.
   * @return  Unit being used in the display.
   */
  public Unit getOverrideUnit() {
    return overrideUnit;
  }

  /**
   * Get the name being used on the axis scale.
   * @return  name of the scale - either the default or the one set by
   *          <CODE>setScalarName</CODE>
   * @see  #setScalarName(String name)
   */
  public String getScalarName() {
    return scalarName;
  }

  /**
   * Set the name being used on the axis scale.
   * @param new name for the scalar.
   * @see  AxisScale#setTitle(String name)
   */
  public void setScalarName(String name) {
    scalarName = name;
    if (axisScale != null) axisScale.setTitle(scalarName);
  }

  /** invoke incTick on every application call to setRange */
  public long incTick() {
    // WLH 19 Feb 2001 - move to after increment NewTick
    // if (display != null) display.controlChanged();
    NewTick += 1;
    if (NewTick == Long.MAX_VALUE) NewTick = Long.MIN_VALUE + 1;
/*
System.out.println(Scalar + " -> " + DisplayScalar +
                   "  incTick = " + NewTick);
*/
    if (display != null) display.controlChanged();
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

  /** 
   * Get the ScalarType that is the map domain 
   * @return  ScalarType of map domain
   */
  public ScalarType getScalar() {
    return Scalar;
  }

  /** 
   * Get the DisplayRealType that is the map range 
   * @return  DisplayRealType of map range
   */
  public DisplayRealType getDisplayScalar() {
    return DisplayScalar;
  }

  /** 
   * Get the DisplayImpl this ScalarMap is linked to 
   * @return  display that this ScalarMap is linked to
   */
  public DisplayImpl getDisplay() {
    return display;
  }

  /**
   * Clear the link to the VisAD display.  This will subsequently
   * cause {@link #getDisplay()} and {@link #getControl()} to return
   * <code>null</code>; consequently, information stored in the Control
   * might have to be reestablished.  This method invokes the method {@link
   * ScalarMapListener#controlChanged(ScalarMapControlEvent)} on all registered
   * {@link ScalarMapListener}s with this instance as the event source, {@link
   * ScalarMapEvent#CONTROL_REMOVED} as the event ID, and the control as the
   * event control.
   *
   * @throws RemoteException    Java RMI failure
   * @throws VisADException     VisAD failure
   */
  synchronized void nullDisplay()
    throws RemoteException, VisADException
  {
    if (control != null) {
      control.nullControl();
      ScalarMapControlEvent evt;
      evt = new ScalarMapControlEvent(this, ScalarMapEvent.CONTROL_REMOVED,
                                      control);
      notifyCtlListeners(evt);
    }
    control = null;

    display = null;
    ScalarIndex = -1;
    DisplayScalarIndex = -1;
    scale_flag = back_scale_flag;

    if (axisScale != null) axisScale.setAxisOrdinal(-1);
  }

  /** 
   * Set the DisplayImpl this ScalarMap is linked to 
   * @param  d   display to link to
   * @throws  VisADException  map is already linked to a DisplayImpl or
   *                          other VisAD error
   */
  synchronized void setDisplay(DisplayImpl d)
               throws VisADException {
    if (d.equals(display)) return;
    if (display != null) {
      throw new DisplayException("ScalarMap.setDisplay: ScalarMap cannot belong" +
                                 " to two Displays");
    }
    display = d;
    if (scale_flag) makeScale();
// System.out.println("setDisplay " + Scalar + " -> " + DisplayScalar);
    // WLH 27 Nov 2000
    if (!(this instanceof ConstantMap)) {
      ProjectionControl pcontrol = display.getProjectionControl();
      try {
        setAspectCartesian(pcontrol.getAspectCartesian());
      }
      catch (RemoteException e) {
      }
    }
  }

  /**
   * Gets the Control for the DisplayScalar.  The Control is constructed when
   * this ScalarMap is linked to a Display via an invocation of the {@link
   * Display#addMap(ScalarMap)} method.  Not all ScalarMaps have Controls,
   * generally depending on the ScalarMap's DisplayRealType.  If a ScalarMap is
   * removed from a Display (via the {@link Display#clearMaps()} method, then,
   * in general, any information in the ScalarMap's control will be lost and
   * must be reestablished.
   *
   * @return                    The Control for the DisplayScalar or <code>
   *                            null</code> if one has not yet been set.
   */
  public Control getControl() {
    return control;
  }

  /**
   * Creates the Control for the associated DisplayScalar.  This method invokes
   * the method {@link ScalarMapListener#controlChanged(ScalarMapControlEvent)}
   * on all registered {@link ScalarMapListener}s with this instance as
   * the event source and {@link ScalarMapEvent#CONTROL_ADDED} or {@link
   * ScalarMapEvent#CONTROL_REPLACED} as the event ID -- depending on whether
   * this is the first control or not.  The event control is the previous
   * control if the event ID is {@link ScalarMapEvent#CONTROL_REPLACED}.  If the
   * event ID is {@link ScalarMapEvent#CONTROL_ADDED}, then the event control is
   * the created control or <code>null</code> -- depending on whether or not the
   * control was successfully created.
   *
   * @throws RemoteException    Java RMI failure
   * @throws VisADException     VisAD failure
   */
  synchronized void setControl() throws VisADException, RemoteException {
    int evtID;
    Control evtCtl;
    if (control != null) {
      evtID = ScalarMapEvent.CONTROL_REPLACED;
      evtCtl = control;
    } else {
      evtID = ScalarMapEvent.CONTROL_ADDED;
      evtCtl = null;
    }

    if (display == null) {
      throw new DisplayException("ScalarMap.setControl: not part of " +
                                 "any Display");
    }
    control = display.getDisplayRenderer().makeControl(this);
    if (control != null) {
      display.addControl(control);

      if (evtCtl == null) {
        evtCtl = control;
      }
    }

    if (control != null || evtCtl != null) {
      notifyCtlListeners(new ScalarMapControlEvent(this, evtID, evtCtl));
    }
  }

  /** return value is true if data (RealType) values are linearly
   *  scaled to display (DisplayRealType) values;
   *  if so, then values are scaled by:
   *  display_value = data_value * so[0] + so[1];
   *  (data[0], data[1]) defines range of data values (either passed
   *  in to setRange or computed by autoscaling logic) and
   *  (display[0], display[1]) defines range of display values;
   *  so, data, display must each be passed in as double[2] arrays;
   *  note if overrideUnit != null, so and data are in overrideUnit 
   *  @param  so       array to contain scale and offset
   *  @param  data     array to contain the data range
   *  @param  display  array to contain the display range
   *  @return  true if data are linearly scaled
   */
  public boolean getScale(double[] so, double[] data, double[] display) {
    // WLH 31 Aug 2000
    if (overrideUnit != null) {
      so[0] = scale * override_scale;
      so[1] = scale * override_offset + offset;
    }
    else {
      so[0] = scale;
      so[1] = offset;
    }
    data[0] = dataRange[0];
    data[1] = dataRange[1];
    display[0] = displayRange[0];
    display[1] = displayRange[1];
    return isScaled;
  }

  /**
   * Returns the current range of the {@link RealType} data.  The range is
   * implicitly set by autoscaling logic or may be explicitly set by the {@link
   * #setRange(double,double)} method.  Note that if overrideUnit != null,
   * then dataRange is in overrideUnit.
   *
   * @return                    The current range of the {@link RealType} data.
   *                            The array is new and may be safely modified.
   */
  public double[] getRange() {
    double[] range = {dataRange[0], dataRange[1]};
    return range;
  }

  /** explicitly set the range of data (RealType) values according
   *  to Unit conversion between this ScalarMap's RealType and
   *  DisplayRealType (both must have Units and they must be
   *  convertable; if neither this nor setRange is invoked, then
   *  the range will be computed from the initial values of Data
   *  objects linked to the Display by autoscaling logic. 
   *  @throws  VisADException   VisAD error
   *  @throws  RemoteException  Java RMI error
   */
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

  /**
   * Explicitly sets the range of {@link RealType} data values that is mapped to
   * the natural range of {@link DisplayRealType} display values.  This method
   * is used to define a linear map from Scalar to DisplayScalar values.  If
   * neither this nor {@link #setRangeByUnits()} is invoked, then the range will
   * be computed by autoscaling logic from the initial values of Data objects
   * linked to the Display.  If the range of data values is (0.0, 1.0), for
   * example, this method may be invoked with low = 1.0 and hi = 0.0 to invert
   * the display scale.
   *
   * @param low                 One end of the range of applicable data.
   * @param hi                  The other end of the range of applicable data.
   * @throws VisADException     VisAD failure.
   * @throws RemoteException    Java RMI failure.
   */
  public void setRange(double low, double hi)
         throws VisADException, RemoteException {
    setRange(low, hi, VisADEvent.LOCAL_SOURCE);
  }

  /** explicitly set the range of data (RealType) values; used for
   *  linear map from Scalar to DisplayScalar values;
   *  if neither this nor setRangeByUnits is invoked, then the
   *  range will be computed from the initial values of Data
   *  objects linked to the Display by autoscaling logic;
   *  if the range of data values is (0.0, 1.0), for example, this
   *  method may be invoked with low = 1.0 and hi = 0.0 to invert
   *  the display scale .
   *  @param  low         lower range value (see notes above)
   *  @param  hi          upper range value (see notes above)
   *  @param  remoteID    id of remote scale
   *  @throws  VisADException   VisAD error
   *  @throws  RemoteException  Java RMI error
   */
  public void setRange(double low, double hi, int remoteId)
         throws VisADException, RemoteException {
    if (DisplayScalar.equals(Display.Animation)) {
      System.err.println("Warning: setRange on " +
        "ScalarMap to Display.Animation has no effect.");
      return;
    }
    isManual = true;
    setRange(null, low, hi, false, remoteId);
    if (scale == scale && offset == offset) {
      incTick(); // did work, so wake up Display
    }
    else {
      isManual = false; // didn't work, so don't lock out auto-scaling
    }
  }

  /** set range used for linear map from Scalar to DisplayScalar values;
      this is the call for automatic scaling */
  public void setRange(DataShadow shadow)
         throws VisADException, RemoteException {
    if (!isManual) setRange(shadow, 0.0, 0.0, false, VisADEvent.LOCAL_SOURCE);
  }

  /** set range used for linear map from Scalar to
      DisplayScalar values */
  private synchronized void setRange(DataShadow shadow, double low, double hi,
          boolean unit_flag) throws VisADException, RemoteException {
    setRange(shadow, low, hi, unit_flag, VisADEvent.LOCAL_SOURCE);
  }

  /** set range used for linear map from Scalar to
      DisplayScalar values */
  private synchronized void setRange(DataShadow shadow, double low, double hi,
          boolean unit_flag, int remoteId)
         throws VisADException, RemoteException {
    int i = ScalarIndex;
    if (shadow != null) {
      // WLH - 23 Sept 99
      if (DisplayScalar.equals(Display.Latitude) ||
          DisplayScalar.equals(Display.Longitude)) {
        Unit data_unit =
          (Scalar instanceof RealType) ? ((RealType) Scalar).getDefaultUnit() :
                                         null;
        Unit display_unit = DisplayScalar.getDefaultUnit();
        if (data_unit != null && display_unit != null &&
            Unit.canConvert(data_unit, display_unit)) {
          dataRange[0] = data_unit.toThis(displayRange[0], display_unit);
          dataRange[1] = data_unit.toThis(displayRange[1], display_unit);
        }
        else {
          if (i < 0 || i >= shadow.ranges[0].length) return;
          dataRange[0] = shadow.ranges[0][i];
          dataRange[1] = shadow.ranges[1][i];
        }
      }
      else {
        if (i < 0 || i >= shadow.ranges[0].length) return;
        dataRange[0] = shadow.ranges[0][i];
        dataRange[1] = shadow.ranges[1][i];
      }
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
      // WLH 31 Aug 2000
      // manual range is in overrideUnit. so convert to Scalar default Unit
      if (overrideUnit != null) {
        dataRange[0] = (dataRange[0] - override_offset) / override_scale;
        dataRange[1] = (dataRange[1] - override_offset) / override_scale;
      }
    }
/*
if (shadow != null || remoteId != VisADEvent.LOCAL_SOURCE) {
  System.out.println(Scalar + " -> " + DisplayScalar + " range: " + dataRange[0] +
                     " to " + dataRange[1] + " " + display.getName());
}
*/
    // at this point dataRange is range for Scalar default Unit
    //   even if (overrideUnit != null)
    // DRM 17 Feb 2006 - so set the defaultUnitRange to be these values.
    defaultUnitRange[0] = dataRange[0];
    defaultUnitRange[1] = dataRange[1];
    if (defaultUnitRange[0] == defaultUnitRange[1]) {
      double half = defaultUnitRange[0] / 2000.0;
      if (half < 0.5) half = 0.5;
      defaultUnitRange[0] -= half;
      defaultUnitRange[1] += half;
    }

    if (isScaled) {
      computeScaleAndOffset();
    }
    else { // if (!isScaled)
      if (dataRange[0] == Double.MAX_VALUE ||
          dataRange[1] == -Double.MAX_VALUE) {
        dataRange[0] = Double.NaN;
        dataRange[1] = Double.NaN;
      }
      

      // WLH 31 Aug 2000
      if (overrideUnit != null) {
        // now convert dataRange to overrideUnit
        dataRange[0] = defaultUnitRange[0] * override_scale + override_offset;
        dataRange[1] = defaultUnitRange[1] * override_scale + override_offset;
      }

    }
/*
System.out.println(Scalar + " -> " + DisplayScalar + " range: " + dataRange[0] +
                   " to " + dataRange[1] + " scale: " + scale + " " + offset);
*/
    if (DisplayScalar.equals(Display.Animation) && shadow != null) {
      if (control != null && ((AnimationControl)control).getComputeSet()) {
        Set set = shadow.animationSampling;
        /* DRM: 04 Jan 2003
        if (set == null) {
          return;
        }
        */
        ((AnimationControl) control).setSet(set, true);
      }
    }
    else if (DisplayScalar.equals(Display.IsoContour)) {
      if (control != null) {

        // WLH 10 July 2002
        // don't set if application has called control.setLevels()
        float[] lowhibase = new float[3];
        boolean[] dashes = new boolean[1];

        boolean public_set =
          ((ContourControl) control).getPublicSet();
        if (!public_set) {
          boolean[] bvalues = new boolean[2];
          float[] values = new float[5];
          ((ContourControl) control).getMainContours(bvalues, values);
          if (shadow == null) {
            // don't set surface value for auto-scale
            values[0] = (float) dataRange[0]; // surfaceValue
          }
          // CTR: 29 Jul 1999: interval should never be zero
          float f = (float) (dataRange[1] - dataRange[0]) / 10.0f;
          if (f != 0.0f) values[1] = f; // contourInterval
          values[2] = (float) dataRange[0]; // lowLimit
          values[3] = (float) dataRange[1]; // hiLimit
          values[4] = (float) dataRange[0]; // base
          ((ContourControl) control).setMainContours(bvalues, values,
                                                     true, true);
        }
      }
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
      ScalarMapEvent evt;
      evt = new ScalarMapEvent(this, (shadow == null ?
                                      ScalarMapEvent.MANUAL :
                                      ScalarMapEvent.AUTO_SCALE), remoteId);
      Vector listeners_clone = null;
      synchronized (ListenerVector) {
        listeners_clone = (Vector) ListenerVector.clone();
      }
      Enumeration listeners = listeners_clone.elements();
      while (listeners.hasMoreElements()) {
        ScalarMapListener listener =
          (ScalarMapListener) listeners.nextElement();
        listener.mapChanged(evt);
      }
    }
  }

  private void computeScaleAndOffset() {

    if (dataRange[0] == Double.MAX_VALUE ||
        dataRange[1] == -Double.MAX_VALUE) {
      dataRange[0] = Double.NaN;
      dataRange[1] = Double.NaN;
      scale = Double.NaN;
      offset = Double.NaN;
    }
    else {
      if (dataRange[0] == dataRange[1]) {
        // WLH 11 April 2000
        double half = dataRange[0] / 2000.0;
        if (half < 0.5) half = 0.5;
        dataRange[0] -= half;
        dataRange[1] += half;
      }

      // WLH 31 Aug 2000
      if (overrideUnit != null) {
        // now convert dataRange to overrideUnit
        dataRange[0] = defaultUnitRange[0] * override_scale + override_offset;
        dataRange[1] = defaultUnitRange[1] * override_scale + override_offset;
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

  /** add a ScalarMapListener, to be notified whenever setRange is
   *  invoked 
   *  @param  listener   <CODE>ScalarMapListener</CODE> to recieve notification
   *                     of changes.
   */
  public void addScalarMapListener(ScalarMapListener listener) {
    if (ListenerVector == null) {
      ListenerVector = new Vector();
    }
    ListenerVector.addElement(listener);
    if (dataRange[0] == dataRange[0] &&
        dataRange[1] == dataRange[1]) {
      try {
        listener.mapChanged(new ScalarMapEvent(this, ScalarMapEvent.MANUAL));
      }
      catch (VisADException e) {
      }
      catch (RemoteException e) {
      }
    }
  }

  /** remove a ScalarMapListener 
   *  @param  listener   <CODE>ScalarMapListener</CODE> to remove from the list
   */
  public void removeScalarMapListener(ScalarMapListener listener) {
    if (listener != null && ListenerVector != null) {
      ListenerVector.removeElement(listener);
    }
  }

  /** Send a <CODE>ScalarMapEvent</CODE> to all control listeners */
  private void notifyCtlListeners(ScalarMapControlEvent evt)
    throws RemoteException, VisADException
  {
    if (ListenerVector != null) {
      Vector listeners_clone = null;
      synchronized (ListenerVector) {
        listeners_clone = (Vector) ListenerVector.clone();
      }
      Enumeration listeners = listeners_clone.elements();
      while (listeners.hasMoreElements()) {
        ScalarMapListener listener =
          (ScalarMapListener) listeners.nextElement();
        listener.controlChanged(evt);
      }
    }
  }

  /**
   * Change underscore characters (_) in the Scalar name to blanks.
   * Can be used to change the displayed scalar name on the axis.
   * @param  u2b   true to change, false to change back
   * @see #setScalarName  as an alternative
   */
  public void setUnderscoreToBlank(boolean u2b) {
    underscore_to_blank = u2b;
    if (Scalar != null) {
      scalarName = Scalar.getName();
      if (underscore_to_blank) {
        scalarName = scalarName.replace('_', ' ');
      }
      // set the label on the scale as well.  DRM 17-Nov-2000
      if (axisScale != null) axisScale.setTitle(scalarName);
    }
  }

  private static final double SCALE = 0.06;
  private static final double OFFSET = 1.05;

  /**
   * Create the scale that is displayed.  This is called automatically
   * when <CODE>setRange(lo, hi)</CODE> and <CODE>setDisplay</CODE> are
   * called.  It makes a call to <CODE>AxisScale.makeScale()</CODE> where
   * the actual hard work is done.
   * @throws VisADException   VisAD error.
   */
  public void makeScale() throws VisADException {
    if (axisScale != null) 
    {
        DisplayRenderer displayRenderer = null;
        if (display == null) return;
        displayRenderer = display.getDisplayRenderer();
        if (displayRenderer == null) return;
        boolean scaleMade = axisScale.makeScale();
        if (scaleMade)
        {
          //displayRenderer.setScale(axis, axis_ordinal, array, scale_color);
          if (scale_on) {
            displayRenderer.setScale(axisScale);
          } else {
            displayRenderer.clearScale(axisScale);
          }
          scale_flag = false;
        }
    }
  }

  /**
   * Enable the display of the scale for this map.  This can be used
   * to selectively turn on or off the scales in a display.  Must be
   * used in conjunction with <CODE>GraphicsModeControl.setScaleEnable()</CODE>
   * or <CODE>DisplayRenderer.setScaleOn(boolean on)</CODE>.  
   * @param  on  true will enable display of axis, false will disable display
   * @see visad.GraphicsModeControl#setScaleEnable(boolean enable)
   * @see visad.DisplayRenderer#setScaleOn(boolean on)
   * @see visad.AxisScale#setVisible(boolean visible)
   */
  public void setScaleEnable(boolean on) {
    scale_on = on;
    if (axisScale != null) axisScale.setVisible(on);
  }

  /**
   * See if the AxisScale is visible or not.
   * @return true if the AxisScale is visible
   */
  public boolean getScaleEnable() { return scale_on; }

  /** 
   * Set color of axis scales; color must be float[3] with red,
   * green and blue components; DisplayScalar must be XAxis,
   * YAxis or ZAxis.  Preferred method is to use <CODE>AxisScale.setColor<CODE>
   * methods.
   * @param  color  array of R,G,B values of color.
   * @throws  VisADException  non-spatial DisplayScalar or wrong length
   *                          of color array
   * @see #getAxisScale()
   * @see visad.AxisScale#setColor(Color color)
   * @see visad.AxisScale#setColor(float[] color)
   */
  public void setScaleColor(float[] color) throws VisADException {
    if (!DisplayScalar.equals(Display.XAxis) &&
        !DisplayScalar.equals(Display.YAxis) &&
        !DisplayScalar.equals(Display.ZAxis)) {
     throw new DisplayException("ScalarMap.setScaleColor: DisplayScalar " +
                                "must be XAxis, YAxis or ZAxis");
    }
    if (color == null || color.length != 3) {
     throw new DisplayException("ScalarMap.setScaleColor: color is " +
                                "null or wrong length");
    }
    // DRM 10-Oct 2000
    axisScale.setColor(color);
  }

  public boolean badRange() {
    // WLH 15 Feb 2002
    boolean bad = (isScaled && (scale != scale || offset != offset));
    if (DisplayScalar.equals(Display.Animation)) {
      if (control != null) {
        Set set = ((AnimationControl) control).getSet();
        bad |= (set == null);
      }
      else {
        bad = true;
      }
    }
    return bad;
    // return (isScaled && (scale != scale || offset != offset));
  }

  /** return an array of display (DisplayRealType) values by
   *  linear scaling (if applicable) the data_values array
   *  (RealType values) 
   * @param   values to scale as doubles
   * @return  array of display values
   */
  public float[] scaleValues(double[] values) {
/* WLH 23 June 99
    if (values == null || badRange()) return null;
*/
    if (values == null) return null;
    float[] new_values = new float[values.length];
    if (badRange()) {
      for (int i=0; i<values.length; i++) new_values[i] = Float.NaN;
    }
    else {
// double[] old_values = values;
      // WLH 31 Aug 2000
      //if (overrideUnit != null) {
      // DRM 11 Jun 2003
      if (overrideUnit != null &&
          !overrideUnit.equals(((RealType) Scalar).getDefaultUnit())) {
        try {
          values =
            overrideUnit.toThis(values, ((RealType) Scalar).getDefaultUnit());
        }
        catch (UnitException e) {
        }
      }
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
/*
if (overrideUnit != null) {
  System.out.println("values = " + old_values[0] + " " + values[0] + " " +
                     new_values[0]);
}
*/
    }
/* SRE 27 Oct 99
    System.out.println(
      "ScalarMap.scaleValues(double[]): values[0] = " + values[0] +
      "; new_values[0] = " + new_values[0]);
*/
    return new_values;
  }

  /** return an array of display (DisplayRealType) values by
   *  linear scaling (if applicable) the data_values array
   *  (RealType values) 
   * @param   values to scale as floats
   * @return  array of display values
   */
  public float[] scaleValues(float[] values) {
    return scaleValues(values, true);
  }

  /** return an array of display (DisplayRealType) values by
   *  linear scaling (if applicable) the data_values array
   *  (RealType values) 
   * @param   values to scale as floats
   * @param   newArray   false to scale in place
   * @return  array of display values
   */
  public float[] scaleValues(float[] values, boolean newArray) {
/* WLH 23 June 99
    if (values == null || badRange()) return null;
*/
    if (values == null) return null;
    float[] new_values = null;
    if (badRange()) {
      new_values = (newArray) ? new float[values.length] : values;
      for (int i=0; i<values.length; i++) new_values[i] = Float.NaN;
    }
    else {
// float[] old_values = values;
      // WLH 31 Aug 2000
      //if (overrideUnit != null) {
      // DRM 11 Jun 2003
      if (overrideUnit != null &&
          !overrideUnit.equals(((RealType) Scalar).getDefaultUnit())) {
        try {
          values =
            overrideUnit.toThis(values, ((RealType) Scalar).getDefaultUnit(), newArray);
        }
        catch (UnitException e) {
        }
      }
      if (isScaled) {
        new_values = (newArray) ? new float[values.length] : values;
        for (int i=0; i<values.length; i++) {
          if (values[i] == values[i]) {
            new_values[i] = (float) (offset + scale * values[i]);
          } else {
            new_values[i] = Float.NaN;
          }
        }
      }
      else {
        new_values = values;
      }
/*
if (overrideUnit != null) {
  System.out.println("values = " + old_values[0] + " " + values[0] + " " +
                     new_values[0]);
}
*/
    }
/* SRE 27 Oct 99
    System.out.println(
      "ScalarMap.scaleValues(double[]): values[0] = " + values[0] +
      "; new_values[0] = " + new_values[0]);
*/
    return new_values;
  }

  /** return an array of display (DisplayRealType) values by
   *  linear scaling (if applicable) the data_values array
   *  (RealType values); results are scaled by the given scale factor
   * @param   values to scale as bytes
   * @return  array of display values
   */
  public byte[] scaleValues(byte[] values, int factor) throws VisADException {
    if (values == null) return null;
    byte[] new_values = null;
    if (badRange()) {
      new_values = new byte[values.length];
    }
    else {
      if (overrideUnit != null &&
          !overrideUnit.equals(((RealType) Scalar).getDefaultUnit())) {
        throw new VisADException(
          "scaleValues(byte[]): non-default units not supported");
      }
      if (isScaled) {
        new_values = new byte[values.length];
        for (int i=0; i<values.length; i++) {
          float v = (float) values[i];
          if (v < 0) v += 256;
          v = (float) (factor * (offset + scale * v));
          if (v < 0) v = 0;
          else if (v > 255) v = 255;
          new_values[i] = (byte) v;
        }
      }
      else {
        new_values = values;
      }
    }
    return new_values;
  }

  /** return an array of data (RealType) values by inverse
   *  linear scaling (if applicable) the display_values array
   *  (DisplayRealType values); this is useful for direct
   *  manipulation and cursor labels 
   * @param  display values
   * @return data values
   */
  public float[] inverseScaleValues(float[] values) {
    return inverseScaleValues(values, true);
  }

  /** return an array of data (RealType) values by inverse
   *  linear scaling (if applicable) the display_values array
   *  (DisplayRealType values); this is useful for direct
   *  manipulation and cursor labels 
   * @param  display values
   * @param  newArray  false to transform in place
   * @return data values
   */
  public float[] inverseScaleValues(float[] values, boolean newArray) {
    if (values == null) return null;
    float[] new_values = (newArray) ? new float[values.length] : values;
    if (isScaled) {
      for (int i=0; i<values.length; i++) {
        if (values[i] == values[i]) {
           new_values[i] = (float) ((values[i] - offset) / scale);
        } else {
           new_values[i] = Float.NaN;
        }
      }
    }
    else {
      if (newArray) {
        for (int i=0; i<values.length; i++) {
          new_values[i] = values[i];
        }
      }
    }
    // WLH 31 Aug 2000
    if (overrideUnit != null) {
// float[] old_values = new_values;
      try {
        new_values =
          overrideUnit.toThat(new_values, ((RealType) Scalar).getDefaultUnit(), false); // already copied above
      }
      catch (UnitException e) {
      }
/*
System.out.println("inverse values = " + values[0] + " " + old_values[0] + " " +
                   new_values[0]);
*/
    }
    return new_values;
  }

  /** ensure that non-Manual components of flow_tuple have equal
      dataRanges symmetric about 0.0 */
  public static void equalizeFlow(Vector mapVector, DisplayTupleType flow_tuple)
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

  /** Get index of DisplayScalar in display.DisplayRealTypeVector */
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

  /**
   * Compares this ScalarMap with another object.
   * @param o                     The other object.
   * @return                      A value that is negative, zero, or positive
   *                              depending on whether this instance is less
   *                              than, equal to, or greater than the other
   *                              object, respectively.
   * @throws ClassCastException   if the other object isn't a {@link ScalarMap}.
   * @throws NullPointerException if the other object is <code>null</code>.
   */
  public int compareTo(Object o)
  {
    return -((ScalarMap)o).compareTo(this);
  }

  /**
   * Compares this ScalarMap with another ScalarMap.  The ScalarType-s are
   * first compared; if they compare equal, then the DisplayRealType-s are
   * compared.
   * @param that                The other ScalarMap.
   * @return            A value that is negative, zero, or positive depending on
   *                    whether this ScalarMap is considered less than, equal
   *                    to, or greater than the other ScalarMap, respectively.
   */
  protected int compareTo(ScalarMap that)
  {
    int         comp = getScalar().compareTo(that.getScalar());
    if (comp == 0)
      comp = getDisplayScalar().compareTo(that.getDisplayScalar());
    return comp;
  }

  /**
   * Indicates if this ScalarMap is the same as another object.
   * @param o           The other object.
   * @return            <code>true</code> if and only if the other object is a
   *                    ScalarMap and compares equal to this ScalarMap.
   */
  public boolean equals(Object o)
  {
    return o instanceof ScalarMap && compareTo(o) == 0;
  }

  /**
   * Returns the hash code for this ScalarMap.  If <code>scalarMap1.equals(
   * scalarMap2)</code> is true, then <code>scalarMap1.hashCode() ==
   * scalarMap2.hashCode()</code>.
   * @return            The hash code for this ScalarMap.
   */
  public int hashCode()
  {
    ScalarType s = getScalar();
    DisplayRealType ds = getDisplayScalar();

    int hash = 0;
    if (s != null) {
      if (ds != null) {
        hash = s.hashCode() ^ ds.hashCode();
      } else {
        hash = s.hashCode();
      }
    } else if (ds != null) {
      hash = ds.hashCode();
    }

    return hash;
  }

  /**
   * Create and return a copy of this ScalarMap.
   * @return  copy of the ScalarMap or <CODE>null</CODE> if a copy couldn't
   *          be created.
   */
  public Object clone()
  {
    try {
      ScalarMap sm = new ScalarMap(Scalar, DisplayScalar);
      copy(sm);
      return sm;
    } catch (Exception e) {
      return null;
    }
  }

  protected void copy(ScalarMap map)
    throws VisADException, RemoteException
  {
    map.isScaled = isScaled;
    map.isManual = isManual;
    map.dataRange[0] = dataRange[0];
    map.dataRange[1] = dataRange[1];
    map.defaultUnitRange[0] = defaultUnitRange[0];
    map.defaultUnitRange[1] = defaultUnitRange[1];
    map.displayRange[0] = displayRange[0];
    map.displayRange[1] = displayRange[1];
    map.scale = scale;
    map.offset = offset;
    map.axisScale = (axisScale != null) ? axisScale.clone(map) : null;
    map.scale_flag = scale_flag;
    map.back_scale_flag = back_scale_flag;
    if (map.display != null) {
      map.setControl();
    }
  }

  /**
   * Returns a string representation of the ScalarMap.
   * @return  a string that "textually represents" this ScalarMap.
   */
  public String toString() {
    return toString("");
  }

  /**
   * Returns a string representation of the ScalarMap with the specified
   * prefix prepended.
   * @param pre  prefix to prepend to the representation
   * @return  a string that "textually represents" this ScalarMap with
   *          <CODE>pre</CODE> prepended.
   */
  public String toString(String pre) {
    return pre + "ScalarMap: " + Scalar.toString() +
           " -> " + DisplayScalar.toString() + "\n";
  }

  /**
   * Get the AxisScale associated with this ScalarMap.
   * @return the AxisScale or null if not a spatial ScalarMap
   */
  public AxisScale getAxisScale()
  {
      return axisScale;
  }

  /**
   * set aspect ratio of XAxis, YAxis & ZAxis in ScalarMaps rather
   * than matrix (i.e., don't distort text fonts);
   * won't work for spherical, polar, cylindrical coordinates
   * @param aspect ratios; 3 elements for Java3D, 2 for Java2D
   * @throws VisADException a VisAD error occurred
   * @throws RemoteException an RMI error occurred
   */
  void setAspectCartesian(double[] aspect)
       throws VisADException, RemoteException {
    double asp = Double.NaN;
    if (DisplayScalar.equals(Display.XAxis)) asp = aspect[0];
    if (DisplayScalar.equals(Display.YAxis)) asp = aspect[1];
    if (DisplayScalar.equals(Display.ZAxis)) asp = aspect[2];
    if (asp == asp) {
      isScaled = DisplayScalar.getRange(displayRange);
      displayRange[0] *= asp;
      displayRange[1] *= asp;
      computeScaleAndOffset();

      makeScale();
// needs work in AxisScale ****
    }
    // note XAxis, YAxis and ZAxis have no unit, so cannot be setRangeByUnits()
  }

}
