//
// ContourControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
  }

  void setMainContours(boolean[] bvalues, float[] fvalues)
         throws VisADException, RemoteException {
    setMainContours(bvalues, fvalues, false, false);
  }

  /** changeControl(!noChange) to not trigger re-transform,
      used by ScalarMap.setRange */
  void setMainContours(boolean[] bvalues, float[] fvalues, boolean noChange)
         throws VisADException, RemoteException {
    setMainContours(bvalues, fvalues, noChange, false);
  }

  /** changeControl(!noChange) to not trigger re-transform,
      used by ScalarMap.setRange */
  void setMainContours(boolean[] bvalues, float[] fvalues, boolean noChange,
                       boolean override)
         throws VisADException, RemoteException {
    if (fvalues == null || fvalues.length != 5 ||
        bvalues == null || bvalues.length != 2) {
      throw new DisplayException("ContourControl.getMainContours: " +
                                 "bad array length");
    }
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
        boolean[] dashes = {false};
        float[] levs =
          Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
        setLevels(levs, base, dashes[0], false);
        arithmeticProgression = true;
      }
      else {
        dash = false;
        levels = null;
      }
    }

    changeControl(!noChange);
  }

  /** 
   * Set level for iso-surfaces 
   * @param value   value of the iso-surface to display
   * @throws VisADException	VisAD error
   * @throws RemoteException	Java RMI failure.
   */
  public void setSurfaceValue(float value)
         throws VisADException, RemoteException {
    boolean change = !Util.isApproximatelyEqual(surfaceValue, value);
    surfaceValue = value;
    if (change) {
      changeControl(true);
    }
  }

  /**
   * Sets the parameters for contour iso-lines.  This method invokes the
   * {@link ControlListener.controlChanged(ControlEvent)} method of all
   * registered listeners;
   *
   * @param interval		The contour interval.  Must be non-zero.  If
   *				negative, then contour lines below the base will
   *				be dashed.  Must not be NaN.
   * @param low			The minimum contour value.  No contour line less
   *				than this value will be drawn.  Must not be NaN.
   * @param hi			The maximum contour value.  No contour line
   *				greater than this value will be drawn.  Must not
   *				be NaN.
   * @param ba			The base contour value.  The contour lines will
   *				be integer multiples of the interval away from
   *				this value.  Must not be NaN.
   * @throws VisADException	The interval is zero or too small.
   * @throws RemoteException	Java RMI failure.
   */
  public void setContourInterval(float interval, float low,
                                 float hi, float ba)
         throws VisADException, RemoteException {
    boolean change = (contourInterval != interval) || (base != ba) ||
                     !Util.isApproximatelyEqual(lowLimit, low) ||
                     !Util.isApproximatelyEqual(hiLimit, hi);
    contourInterval = interval;
    lowLimit = low;
    hiLimit = hi;
    base = ba;

    // adapt to 'new' descriptors
    boolean[] dashes = {false};
    float[] levs =
      Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
    // setLevels(levs, base, dashes[0], false);
    setLevels(levs, base, dashes[0], change);
    arithmeticProgression = true;

    if (change) changeControl(true);
  }

  private boolean in = false;

  /** 
   * Set low and high iso-line levels 
   * @param low			The minimum contour value.  No contour line less
   *				than this value will be drawn.  Must not be NaN.
   * @param hi			The maximum contour value.  No contour line
   *				greater than this value will be drawn.  Must not
   *				be NaN.
   * @throws VisADException	VisAD error
   * @throws RemoteException	Java RMI failure.
   */
  public void setContourLimits(float low, float hi)
         throws VisADException, RemoteException {
    if (!in) {
      in = true;
      boolean change = !Util.isApproximatelyEqual(lowLimit, low) ||
                       !Util.isApproximatelyEqual(hiLimit, hi);
      lowLimit = low;
      hiLimit = hi;

      // adapt to 'new' descriptors
      if (arithmeticProgression) {
        boolean[] dashes = {false};
        float[] levs =
          Contour2D.intervalToLevels(contourInterval, lowLimit, hiLimit, base, dashes);
        setLevels(levs, base, dashes[0], false);
      }
      else {
        int n = 0;
        for (int i=0; i<levels.length; i++) {
          if (lowLimit < levels[i] && levels[i] < hiLimit) n++;
        }
        if (n != levels.length) {
          float[] levs = new float[n];
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

      if (change) changeControl(true);
      in = false;
    }
  }

  /** 
   * Set unevenly spaced levels for 2-D contour lines;
   * levels below base are dashed if dash == true 
   * @param levels      	An array of contour values to display.
   * @param base      		The base contour value for dashing.  Levels
   *        			below base are dashed if dash is true
   * @param dash                flag for making dashed contours below the
   *                            base contour value.
   * @throws VisADException     VisAD error
   * @throws RemoteException    Java RMI failure.
   */
  public void setLevels(float[] levels, float base, boolean dash)
         throws VisADException, RemoteException {
    setLevels(levels, base, dash, true);
  }

  private void setLevels(float[] levs, float ba, boolean da,
                         boolean by_user)
          throws VisADException, RemoteException {
    if (levs == null) return;
    levels = new float[levs.length];
    float min = Float.MAX_VALUE;
    // float max = Float.MIN_VALUE;
    float max = -Float.MAX_VALUE;
    for (int i=0; i<levs.length; i++) {
      if (levs[i] < min) min = levs[i];
      if (levs[i] > max) max = levs[i];
      levels[i] = levs[i];
    }
    dash = da;
    base = ba;
    if (by_user) {
      lowLimit = min - 0.1f;
      hiLimit = max + 0.1f;
      arithmeticProgression = false;
      changeControl(true);
    }
  }

  /** get 'new' descriptors for 2-D contour lines;
      lowhibase must be float[3], dashes must be boolean[1] */
  public float[] getLevels(float[] lowhibase, boolean[] dashes) {
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

  /** set label enable to 'on' */
  public void enableLabels(boolean on)
         throws VisADException, RemoteException {
    boolean change = (labels != on);
    labels = on;
    if (change) changeControl(true);
  }

  /** set contour enable to 'on' */
  public void enableContours(boolean on)
         throws VisADException, RemoteException {
    boolean change = (mainContours != on);
    mainContours = on;
    if (change) changeControl(true);
  }

  /** get contour parameters: bvalues[0] = contour enable,
      bvalues[1] = labels enable, fvalues[0] = surface level,
      fvalues[1] = interval, fvalues[2] = low, fvalues[3] = hi,
      fvalues[4] = base; bvalues and fvalues must be passed in
      as boolean[2] and float[5] */
  public void getMainContours(boolean[] bvalues, float[] fvalues)
         throws VisADException {
    if (fvalues == null || fvalues.length != 5 ||
        bvalues == null || bvalues.length != 2) {
      throw new DisplayException("ContourControl.getMainContours: " +
                                 "bad array length");
    }
    bvalues[0] = mainContours;
    bvalues[1] = labels;
    fvalues[0] = surfaceValue;
    fvalues[1] = contourInterval;
    fvalues[2] = lowLimit;
    fvalues[3] = hiLimit;
    fvalues[4] = base;
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return mainContours + " " + labels + " " + surfaceValue + " " +
      contourInterval + " " + lowLimit + " " + hiLimit + " " + base;
  }

  /** reconstruct this control using the specified save string */
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

  /** copy the state of a remote control to this control */
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

    ContourControl cc = (ContourControl )o;

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

    return true;
  }

  public Object clone()
  {
    ContourControl cc = (ContourControl )super.clone();
    if (levels != null) {
      cc.levels = (float[] )levels.clone();
    }

    return cc;
  }
}
