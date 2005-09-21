//
// ContourControl.java
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
import java.util.StringTokenizer;
import java.awt.event.*;
import java.util.Vector;

import visad.browser.Convert;
import visad.util.Util;

/**
   ContourControl is the VisAD class for controlling IsoContour display scalars.<P>
*/
public class ContourControl extends Control {

  private boolean mainContours;
  // for 3-D mainContours
  private float surfaceValue;
  // for 2-D mainContours
  // these are the 'old' descriptors for 2-D contour lines
  private float contourInterval;
  private float lowLimit;
  private float hiLimit;
  private float base;
  private boolean labels;

  private boolean public_set = false; // application called setLevels()

  //
  // these are the 'new' descriptors for 2-D contour lines
  // includes lowLimit, hiLimit and base from the 'old' descriptors
  // true if contourInterval is valid
  private boolean arithmeticProgression = true;
  // contour line levels
  private float[] levels = null;
  private boolean dash = false;

  private boolean horizontalContourSlice;
  private boolean verticalContourSlice;

  private float horizontalSliceLow;
  private float horizontalSliceHi;
  private float horizontalSliceStep;
  private float verticalSliceLow;
  private float verticalSliceHi;
  private float verticalSliceStep;

  boolean contourFill;

  private static double init_scale = 0.51;
  private boolean autoSizeLabels = false;
  private double labelSizeFactor = 1;
  private transient ZoomDoneListener zoom;
  private ProjectionControl pcntrl;
  private ControlListener projListener;
  private double ratio = 1.20;

  /**
   * Construct a new ContourControl for the display
   * @param d    Display to associate with this
   */
  public ContourControl(DisplayImpl d) {
    super(d);
    mainContours = true;
    labels = false;
    surfaceValue = Float.NaN;
    contourInterval = Float.NaN;
    lowLimit = Float.NaN;
    hiLimit = Float.NaN;
    base = Float.NaN;

    horizontalContourSlice = false;
    verticalContourSlice = false;

    horizontalSliceLow = Float.NaN;
    horizontalSliceHi = Float.NaN;
    horizontalSliceStep = Float.NaN;
    verticalSliceLow = Float.NaN;
    verticalSliceHi = Float.NaN;
    verticalSliceStep = Float.NaN;

    contourFill = false;

    pcntrl = d.getProjectionControl();
    double[] matrix          = pcntrl.getMatrix();
    double[] rot             = new double[3];
    double[] trans           = new double[3];
    double[] scale           = new double[1];
    MouseBehavior mouse      = d.getMouseBehavior();
    if (mouse != null) {
      mouse.instance_unmake_matrix(rot, scale, trans, matrix);
      if (!(init_scale==init_scale)) init_scale = scale[0];
    }

    zoom = new ZoomDoneListener(this, pcntrl, mouse, init_scale);
    d.addDisplayListener(zoom);
  }

  /**
   * set parameters for IsoContour depictions, if not already set
   * @param  bvalues   must be dimensioned boolean[2], where
   *         bvalues[0]  enable contours
   *         bvalues[1]  enable labels (if applicable)
   * @param  fvalues   must be dimensioned float[5], where
   *         fvalues[0]  level for iso-surface
   *         fvalues[1]  interval for iso-lines
   *         fvalues[2]  low limit for iso-lines
   *         fvalues[3]  high limit for iso-lines
   *         fvalues[4]  base for iso-lines
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  void setMainContours(boolean[] bvalues, float[] fvalues)
         throws VisADException, RemoteException {
    setMainContours(bvalues, fvalues, false, false);
  }

  /**
   * set parameters for IsoContour depictions, if not already set
   * @param  bvalues   must be dimensioned boolean[2], where
   *         bvalues[0]  enable contours
   *         bvalues[1]  enable labels (if applicable)
   * @param  fvalues   must be dimensioned float[5], where
   *         fvalues[0]  level for iso-surface
   *         fvalues[1]  interval for iso-lines
   *         fvalues[2]  low limit for iso-lines
   *         fvalues[3]  high limit for iso-lines
   *         fvalues[4]  base for iso-lines
   * @param  noChange  true to not trigger re-transform (false for
   *                   auto-scale)
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  void setMainContours(boolean[] bvalues, float[] fvalues, boolean noChange)
         throws VisADException, RemoteException {
    setMainContours(bvalues, fvalues, noChange, false);
  }

  /**
   * set parameters for IsoContour depictions
   * @param  bvalues   must be dimensioned boolean[2], where
   *         bvalues[0]  enable contours
   *         bvalues[1]  enable labels (if applicable)
   * @param  fvalues   must be dimensioned float[5], where
   *         fvalues[0]  level for iso-surface
   *         fvalues[1]  interval for iso-lines
   *         fvalues[2]  low limit for iso-lines
   *         fvalues[3]  high limit for iso-lines
   *         fvalues[4]  base for iso-lines
   * @param  noChange  true to not trigger re-transform (false for
   *                   auto-scale)
   * @param  override  true to set float values even if already set
   *                   (i.e., not NaNs)
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  void setMainContours(boolean[] bvalues, float[] fvalues, boolean noChange,
                       boolean override)
         throws VisADException, RemoteException {
    if (fvalues == null || fvalues.length != 5 ||
        bvalues == null || bvalues.length != 2) {
      throw new DisplayException("ContourControl.setMainContours: " +
                                 "bad array length");
    }
    boolean setLevels = false;
    float[] levs = null;
    boolean[] dashes = null;
    float myBase = 0;
    synchronized(this) {
      mainContours = bvalues[0];
      labels = bvalues[1];

      // WLH 13 Sept 2000
      if (override) {
        surfaceValue = fvalues[0];
        contourInterval = fvalues[1];
        lowLimit = fvalues[2];
        hiLimit = fvalues[3];
        base = fvalues[4];
      }
      else {
        if (surfaceValue != surfaceValue) surfaceValue = fvalues[0];
        if (contourInterval != contourInterval) contourInterval = fvalues[1];
        if (lowLimit != lowLimit) lowLimit = fvalues[2];
        if (hiLimit != hiLimit) hiLimit = fvalues[3];
        if (base != base) base = fvalues[4];
      }
      // adapt to 'new' descriptors
      if (arithmeticProgression) {
        if (contourInterval == contourInterval && base == base &&
            lowLimit == lowLimit && hiLimit == hiLimit) {
          dashes = new boolean[] {false};
          levs =
            Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
          myBase = base;
          setLevels = true;
        }
        else {
          dash = false;
          levels = null;
        }
      }
    }

    /**
     * The following methods are "alien" because they are outside the control of
     * this class.  If they were invoked in a synchronized block, then deadlock
     * could occur if one of the methods waits on a thread that it creates that
     * calls back into this class and tries to obtain a lock (it's happened
     * before).  See Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's
     * "Effective Java" for more information.
     */
    if (setLevels)
      setLevels(levs, myBase, dashes[0], false);

    changeControl(!noChange);
  }

  /** 
   * Set level for iso-surfaces 
   * @param value   value of the iso-surface to display
   * @throws VisADException     VisAD error
   * @throws RemoteException    Java RMI failure.
   */
  public void setSurfaceValue(float value)
         throws VisADException, RemoteException {
    boolean change;
    synchronized(this) {
      change = !Util.isApproximatelyEqual(surfaceValue, value);
      surfaceValue = value;
    }
    if (change) {
      /**
       * The following method is "alien" because it is outside the control of
       * this class.  If it were invoked in a synchronized block, then deadlock
       * could occur if the method waits on a thread that it creates that
       * calls back into this class and tries to obtain a lock (it's happened
       * before).  See Item 49 (Avoid Excessive Synchronization) in Joshua
       * Bloch's "Effective Java" for more information.
       */
      changeControl(true);
    }
  }

  /**
   * Sets the parameters for contour iso-lines.  This method invokes the
   * {@link ControlListener#controlChanged(ControlEvent)} method of all
   * registered listeners;
   *
   * @param interval            The contour interval.  Must be non-zero.  If
   *                            negative, then contour lines below the base will
   *                            be dashed.  Must not be NaN.
   * @param low                 The minimum contour value.  No contour line less
   *                            than this value will be drawn.  Must not be NaN.
   * @param hi                  The maximum contour value.  No contour line
   *                            greater than this value will be drawn.  Must not
   *                            be NaN.
   * @param ba                  The base contour value.  The contour lines will
   *                            be integer multiples of the interval away from
   *                            this value.  Must not be NaN.
   * @throws VisADException     The interval is zero or too small.
   * @throws RemoteException    Java RMI failure.
   */
  public void setContourInterval(float interval, float low,
                                 float hi, float ba)
         throws VisADException, RemoteException {
    public_set = true;
    float[] levs;
    float myBase;
    boolean[] dashes = {false};
    boolean change;
    synchronized(this) {
      change = (contourInterval != interval) || (base != ba) ||
               !Util.isApproximatelyEqual(lowLimit, low) ||
               !Util.isApproximatelyEqual(hiLimit, hi);
      contourInterval = interval;
      lowLimit = low;
      hiLimit = hi;
      myBase = base = ba;

      // adapt to 'new' descriptors
      levs =
        Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
      arithmeticProgression = true;
    }
    /**
     * The following method is "alien" because it is outside the control of this
     * class.  If it were invoked in a synchronized block, then deadlock could
     * occur if the method waits on a thread that it creates that calls back
     * into this class and tries to obtain a lock (it's happened before).  See
     * Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's "Effective
     * Java" for more information.
     */
    setLevels(levs, myBase, dashes[0], change);
  }

  /** 
   * Set low and high iso-line levels 
   * @param low                 The minimum contour value.  No contour line less
   *                            than this value will be drawn.  Must not be NaN.
   * @param hi                  The maximum contour value.  No contour line
   *                            greater than this value will be drawn.  Must not
   *                            be NaN.
   * @throws VisADException     VisAD error
   * @throws RemoteException    Java RMI failure.
   */
  public void setContourLimits(float low, float hi)
         throws VisADException, RemoteException {
    public_set = true;
    boolean change;
    boolean setLevels;
    float[] levs = null;
    float myBase = 0;
    boolean[] dashes = null;
    synchronized(this) {
      change = !Util.isApproximatelyEqual(lowLimit, low) ||
               !Util.isApproximatelyEqual(hiLimit, hi);
      lowLimit = low;
      hiLimit = hi;
      // adapt to 'new' descriptors
      if (arithmeticProgression) {
        setLevels = true;
        dashes = new boolean[] {false};
        levs =
          Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
        myBase = base;
      }
      else {
        setLevels = false;
        int n = 0;
        for (int i=0; i<levels.length; i++) {
          if (lowLimit < levels[i] && levels[i] < hiLimit) n++;
        }
        if (n != levels.length) {
          levs = new float[n];
          int k = 0;
          for (int i=0; i<levels.length; i++) {
            if (lowLimit < levels[i] && levels[i] < hiLimit) levs[k++] = levels[i];
          }
          levels = levs;
        }
        else {
          change = false;
        }
      }
    }

    /**
     * The following methods are "alien" because they are outside the control of
     * this class.  If they were invoked in a synchronized block, then deadlock
     * could occur if one of the methods waits on a thread that it creates that
     * calls back into this class and tries to obtain a lock (it's happened
     * before).  See Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's
     * "Effective Java" for more information.
     */
    if (setLevels)
      setLevels(levs, myBase, dashes[0], false);

    if (change) changeControl(true);
  }

  /**
   * @return boolean indicating whether levels have
   *                 been set by other than auto-scale
   */
  public boolean getPublicSet() {
    return public_set;
  }

  /** 
   * Set arbitrary levels for 2-D contour lines;
   * levels below base are dashed if dash == true 
   * @param levels              An array of contour values to display.
   * @param base                The base contour value for dashing.  Levels
   *                            below base are dashed if dash is true
   * @param dash                flag for making dashed contours below the
   *                            base contour value.
   * @throws VisADException     VisAD error
   * @throws RemoteException    Java RMI failure.
   */
  public void setLevels(float[] levels, float base, boolean dash)
         throws VisADException, RemoteException {
    public_set = true;
    setLevels(levels, base, dash, true);
  }

  private void setLevels(float[] levs, float ba, boolean da,
                         boolean by_user)
          throws VisADException, RemoteException {
    if (levs == null) return;
    float[] newLevels = new float[levs.length];
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (int i=0; i<levs.length; i++) {
      if (levs[i] < min) min = levs[i];
      if (levs[i] > max) max = levs[i];
      newLevels[i] = levs[i];
    }
    synchronized(this) {
      levels = newLevels;
      dash = da;
      base = ba;
      if (by_user) {
        lowLimit = min - Math.abs(.01f * min);  // DRM 25-APR-2001
        hiLimit  = max + Math.abs(.01f * max);  // DRM 25-APR-2001
      }
    }
    if (by_user) {
      /**
       * The following method is "alien" because it is outside the control of
       * this class.  If it were invoked in a synchronized block, then deadlock
       * could occur if the method waits on a thread that it creates that
       * calls back into this class and tries to obtain a lock (it's happened
       * before).  See Item 49 (Avoid Excessive Synchronization) in Joshua
       * Bloch's "Effective Java" for more information.
       */
      changeControl(true);
    }
  }

  /** get 'new' descriptors for 2-D contour lines
   * @param lowhibase   must be dimensioned float[3], where
   *        lowhibase[0]  used to return low limit
   *        lowhibase[1]  used to return high limit
   *        lowhibase[2]  used to return base
   * @param dashed      must be dimensioned boolean[1], where
   *        dashed[0]     used to return dash enable
   * @return float[] array of levels for contour curves
   */
  public synchronized float[] getLevels(float[] lowhibase, boolean[] dashes) {
    float[] levs = null;
    if (levels != null) {
      levs = new float[levels.length];
      System.arraycopy(levels, 0, levs, 0, levels.length);
    }
    lowhibase[0] = lowLimit;
    lowhibase[1] = hiLimit;
    lowhibase[2] = base;
    dashes[0] = dash;
    return levs;
  }

  /**
   * set label enable
   * @param on  new value for label enable
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public void enableLabels(boolean on)
         throws VisADException, RemoteException {
    boolean change;
    synchronized(this) {
      change = (labels != on);
      labels = on;
    }
    /**
     * The following method is "alien" because it is outside the control of this
     * class.  If it were invoked in a synchronized block, then deadlock could
     * occur if the method waits on a thread that it creates that calls back
     * into this class and tries to obtain a lock (it's happened before).  See
     * Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's "Effective
     * Java" for more information.
     */
    if (change) changeControl(true);
  }

  /** 
   * set contour enable 
   * @param on  new value for contour enable
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public void enableContours(boolean on)
         throws VisADException, RemoteException {
    boolean change;
    synchronized(this) {
      change = (mainContours != on);
      mainContours = on;
    }
    /**
     * The following method is "alien" because it is outside the control of this
     * class.  If it were invoked in a synchronized block, then deadlock could
     * occur if the method waits on a thread that it creates that calls back
     * into this class and tries to obtain a lock (it's happened before).  See
     * Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's "Effective
     * Java" for more information.
     */
    if (change) changeControl(true);
  }

  /**
   * get parameters for IsoContour depictions
   * @param  bvalues   must be dimensioned boolean[2], where
   *         bvalues[0]  used to return contour enable
   *         bvalues[1]  used to return label enable
   * @param  fvalues   must be dimensioned float[5], where
   *         fvalues[0]  used to return level for iso-surface
   *         fvalues[1]  used to return interval for iso-lines
   *         fvalues[2]  used to return low limit for iso-lines
   *         fvalues[3]  used to return high limit for iso-lines
   *         fvalues[4]  used to return base for iso-lines
   * @throws VisADException  a VisAD error occurred
   */
  public void getMainContours(boolean[] bvalues, float[] fvalues)
         throws VisADException {
    if (fvalues == null || fvalues.length != 5 ||
        bvalues == null || bvalues.length != 2) {
      throw new DisplayException("ContourControl.getMainContours: " +
                                 "bad array length");
    }
    synchronized(this) {
      bvalues[0] = mainContours;
      bvalues[1] = labels;
      fvalues[0] = surfaceValue;
      fvalues[1] = contourInterval;
      fvalues[2] = lowLimit;
      fvalues[3] = hiLimit;
      fvalues[4] = base;
    }
  }

  public void setContourFill(boolean flag)
         throws VisADException, RemoteException {
    synchronized(this) {
      contourFill = flag;
    }
    /**
     * The following method is "alien" because it is outside the control of this
     * class.  If it were invoked in a synchronized block, then deadlock could
     * occur if the method waits on a thread that it creates that calls back
     * into this class and tries to obtain a lock (it's happened before).  See
     * Item 49 (Avoid Excessive Synchronization) in Joshua Bloch's "Effective
     * Java" for more information.
     */
    changeControl(true);
  }

  /**
   * @return contourFill enable
   */
  public synchronized boolean contourFilled() {
    return contourFill;
  }

  /**
   * @return initial scale for label auto-size
   */
  public static double getInitScale() {
    return init_scale;
  }

  /**
   * set enable for label auto-size
   * @param flag  new value for label auto-size enable
   */
  public void setAutoScaleLabels(boolean flag) {
    synchronized(this) {
      autoSizeLabels = flag;
    }
  }

  /**
   * @return label auto-size enable
   */
  public boolean getAutoSizeLabels() {
    return autoSizeLabels;
  }

  /**
   * set size for label auto-size
   * @param factor  new size for label auto-size
   * @throws VisADException  a VisAD error occurred
   * @throws RemoteException  an RMI error occurred
   */
  public void setLabelSize(double factor)
         throws VisADException, RemoteException {
    synchronized(this) {
      labelSizeFactor *= factor;
    }
    changeControl(true);
  }

  /**
   * @return size for label auto-size
   */
  public double getLabelSize() {
    return labelSizeFactor;
  }

  /**
   * @return String representation of this ContourControl
   */
  public synchronized String getSaveString() {
    return mainContours + " " + labels + " " + surfaceValue + " " +
      contourInterval + " " + lowLimit + " " + hiLimit + " " + base;
  }

  /**
   * reconstruct this ContourControl using the specified save string
   * @param save - String representation for reconstruction
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    if (st.countTokens() < 7) throw new VisADException("Invalid save string");
    boolean[] b = new boolean[2];
    float[] f = new float[5];
    for (int i=0; i<2; i++) b[i] = Convert.getBoolean(st.nextToken());
    for (int i=0; i<5; i++) f[i] = Convert.getFloat(st.nextToken());
    setMainContours(b, f, false, true);
  }

  /**
   * remove previous projListener from pcntrl, and save cl as
   * new projListener
   * @param cl  new ControlListener for projListener
   * @param pcntrl ProjectionControl
   */
  public void addProjectionControlListener(ControlListener cl,
                                           ProjectionControl pcntrl)
  {
    pcntrl.removeControlListener(projListener);
    projListener = cl;
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

    if (!(rmt instanceof ContourControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    ContourControl cc = (ContourControl )rmt;

    boolean changed = false;

    synchronized(this) {
      synchronized(cc) {
        if (mainContours != cc.mainContours) {
          changed = true;
          mainContours = cc.mainContours;
        }
        if (!Util.isApproximatelyEqual(surfaceValue, cc.surfaceValue)) {
          changed = true;
          surfaceValue = cc.surfaceValue;
        }

        if (!Util.isApproximatelyEqual(contourInterval, cc.contourInterval)) {
          changed = true;
          contourInterval = cc.contourInterval;
        }
        if (!Util.isApproximatelyEqual(lowLimit, cc.lowLimit)) {
          changed = true;
          lowLimit = cc.lowLimit;
        }
        if (!Util.isApproximatelyEqual(hiLimit, cc.hiLimit)) {
          changed = true;
          hiLimit = cc.hiLimit;
        }
        if (!Util.isApproximatelyEqual(base, cc.base)) {
          changed = true;
          base = cc.base;
        }

        if (labels != cc.labels) {
          changed = true;
          labels = cc.labels;
        }
        if (arithmeticProgression != cc.arithmeticProgression) {
          changed = true;
          arithmeticProgression = cc.arithmeticProgression;
        }

        if (cc.levels == null) {
          if (levels != null) {
            changed = true;
            levels = null;
          }
        } else {
          // make sure array lengths match
          if (levels == null || levels.length != cc.levels.length) {
            changed = true;
            levels = new float[cc.levels.length];
            for (int i = 0; i < levels.length; i++) {
              levels[i] = 0;
            }
          }
          // copy remote values
          for (int i = 0; i < levels.length; i++) {
            if (!Util.isApproximatelyEqual(levels[i], cc.levels[i])) {
              changed = true;
              levels[i] = cc.levels[i];
            }
          }
        }

        if (dash != cc.dash) {
          changed = true;
          dash = cc.dash;
        }

        if (horizontalContourSlice != cc.horizontalContourSlice) {
          changed = true;
          horizontalContourSlice = cc.horizontalContourSlice;
        }
        if (verticalContourSlice != cc.verticalContourSlice) {
          changed = true;
          verticalContourSlice = cc.verticalContourSlice;
        }

        if (!Util.isApproximatelyEqual(horizontalSliceLow,
                                       cc.horizontalSliceLow))
        {
          changed = true;
          horizontalSliceLow = cc.horizontalSliceLow;
        }
        if (!Util.isApproximatelyEqual(horizontalSliceHi, cc.horizontalSliceHi)) {
          changed = true;
          horizontalSliceHi = cc.horizontalSliceHi;
        }
        if (!Util.isApproximatelyEqual(horizontalSliceStep,
                                       cc.horizontalSliceStep))
        {
          changed = true;
          horizontalSliceStep = cc.horizontalSliceStep;
        }
        if (!Util.isApproximatelyEqual(verticalSliceLow, cc.verticalSliceLow)) {
          changed = true;
          verticalSliceLow = cc.verticalSliceLow;
        }
        if (!Util.isApproximatelyEqual(verticalSliceHi, cc.verticalSliceHi)) {
          changed = true;
          verticalSliceHi = cc.verticalSliceHi;
        }
        if (!Util.isApproximatelyEqual(verticalSliceStep, cc.verticalSliceStep)) {
          changed = true;
          verticalSliceStep = cc.verticalSliceStep;
        }

        if (contourFill != cc.contourFill) {
          changed = true;
          contourFill = cc.contourFill;
        }

        if (autoSizeLabels != cc.autoSizeLabels) {
          changed = true;
          autoSizeLabels = cc.autoSizeLabels;
        }

        if (labelSizeFactor != cc.labelSizeFactor) {
          changed = true;
          labelSizeFactor = cc.labelSizeFactor;
        }
      }
    }

    if (changed) {
      try {
      /**
       * The following method is "alien" because it is outside the control of
       * this class.  If it were invoked in a synchronized block, then deadlock
       * could occur if the method waits on a thread that it creates that
       * calls back into this class and tries to obtain a lock (it's happened
       * before).  See Item 49 (Avoid Excessive Synchronization) in Joshua
       * Bloch's "Effective Java" for more information.
       */
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
  }

  /**
   * Indicates whether or not this instance equals an Object
   * @param o  an Object
   * @return true if and only if this instance is equal to o
   */
  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    ContourControl cc = (ContourControl )o;

    synchronized(this) {
      synchronized(cc) {
        if (mainContours != cc.mainContours) {
          return false;
        }
        if (!Util.isApproximatelyEqual(surfaceValue, cc.surfaceValue)) {
          return false;
        }

        if (!Util.isApproximatelyEqual(contourInterval, cc.contourInterval)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(lowLimit, cc.lowLimit)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(hiLimit, cc.hiLimit)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(base, cc.base)) {
          return false;
        }

        if (labels != cc.labels) {
          return false;
        }
        if (arithmeticProgression != cc.arithmeticProgression) {
          return false;
        }

        if (levels == null) {
          if (cc.levels != null) {
            return false;
          }
        } else {
          // make sure array lengths match
          if (cc.levels == null || levels.length != cc.levels.length) {
            return false;
          }
          // copy remote values
          for (int i = 0; i < levels.length; i++) {
            if (!Util.isApproximatelyEqual(levels[i], cc.levels[i])) {
              return false;
            }
          }
        }

        if (dash != cc.dash) {
          return false;
        }

        if (horizontalContourSlice != cc.horizontalContourSlice) {
          return false;
        }
        if (verticalContourSlice != cc.verticalContourSlice) {
          return false;
        }

        if (!Util.isApproximatelyEqual(horizontalSliceLow,
                                       cc.horizontalSliceLow))
        {
          return false;
        }
        if (!Util.isApproximatelyEqual(horizontalSliceHi, cc.horizontalSliceHi)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(horizontalSliceStep,
                                       cc.horizontalSliceStep))
        {
          return false;
        }
        if (!Util.isApproximatelyEqual(verticalSliceLow, cc.verticalSliceLow)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(verticalSliceHi, cc.verticalSliceHi)) {
          return false;
        }
        if (!Util.isApproximatelyEqual(verticalSliceStep, cc.verticalSliceStep)) {
          return false;
        }

        if (contourFill != cc.contourFill) {
          return false;
        }

        if (autoSizeLabels != cc.autoSizeLabels) {
          return false;
        }
        if (!Util.isApproximatelyEqual(labelSizeFactor, cc.labelSizeFactor)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * @return a copy of this ContourControl
   */
  public synchronized Object clone()
  {
    ContourControl cc = (ContourControl )super.clone();
    if (levels != null) {
      cc.levels = (float[] )levels.clone();
    }

    return cc;
  }

  /**
   * if zoom scale has changed sufficiently, re-transform in
   * order to recompute labels
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public void reLabel() 
         throws VisADException, RemoteException {   
    if (zoom == null) return;
    zoom.reLabel(ratio);
  }

  class ZoomDoneListener implements DisplayListener
  {
    ContourControl    c_cntrl;
    ProjectionControl p_cntrl;
    MouseBehavior     mouse;
    double            last_scale;

    ZoomDoneListener(ContourControl c_cntrl, ProjectionControl p_cntrl,
                      MouseBehavior mouse, double scale) {
      this.c_cntrl = c_cntrl;
      this.p_cntrl = p_cntrl;
      this.mouse   = mouse;
      last_scale   = scale;
    }

    public void displayChanged(DisplayEvent de)
           throws VisADException, RemoteException
    {
      /**
      if (de.getId() == DisplayEvent.KEY_RELEASED) {
        InputEvent ie = de.getInputEvent();
        if (ie != null) {
          int mod = ie.getModifiers();
          int key = ((KeyEvent)ie).getKeyCode();
          if (key == KeyEvent.VK_SHIFT)  {
            reLabel(ratio);
          }
        }
      }
      **/
      if (de.getId() == DisplayEvent.MOUSE_RELEASED_LEFT ||
          de.getId() == DisplayEvent.MOUSE_RELEASED_RIGHT) {
        reLabel(ratio);
      }
    }
     
    public void reLabel(double ratio) 
           throws VisADException, RemoteException {
      if (!c_cntrl.contourFilled() && autoSizeLabels) {
        double[] matrix        = p_cntrl.getMatrix();
        double[] rot           = new double[3];
        double[] trans         = new double[3];
        double[] scale         = new double[1];
        mouse.instance_unmake_matrix(rot, scale, trans, matrix);
        if (scale[0]/last_scale > ratio ||
            scale[0]/last_scale < 1/ratio) { //- re-label
          if (labels) c_cntrl.changeControl(true);
          last_scale = scale[0];
        }
      }
    }
  }


  /**
   * End this control (called by ScalarMap.nullDisplay()). Override
   * to remove zoom control listener.
   */
  public void nullControl() {
    if (projListener != null) {
      pcntrl.removeControlListener(projListener);
    }
    getDisplay().removeDisplayListener(zoom);
    zoom = null;
    super.nullControl();
  }

}
