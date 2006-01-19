//
// FlowControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
   FlowControl is the VisAD abstract super-class for controlling
   Flow display scalars.<P>
*/
public abstract class FlowControl extends Control {

  float flowScale;

  // DRM add 09-Sep-1999
  /** Northern Hemisphere orientation for wind barbs */
  public static final int NH_ORIENTATION = 0;
  /** Southern Hemisphere orientation for wind barbs */
  public static final int SH_ORIENTATION = 1;
  int barbOrientation;
  boolean adjustFlowToEarth = true;  

  boolean HorizontalVectorSlice;
  boolean VerticalVectorSlice;
  boolean HorizontalStreamSlice;
  boolean VerticalStreamSlice;
  boolean[] TrajectorySet;

  double HorizontalVectorSliceHeight;
  double HorizontalStreamSliceHeight;

  /** Streamline flags
  -------------------------------*/
  boolean streamlinesEnabled;
  float   streamlineDensity;
  float   arrowScale;
  float   stepFactor;

  // WLH  need Vertical*Slice location parameters

  public FlowControl(DisplayImpl d) {
    super(d);
    flowScale = 0.02f;
    HorizontalVectorSlice = false;
    VerticalVectorSlice = false;
    HorizontalStreamSlice = false;
    VerticalStreamSlice = false;
    barbOrientation = SH_ORIENTATION;    // DRM 9-Sept-1999
    TrajectorySet = null;

    HorizontalVectorSliceHeight = 0.0;
    HorizontalStreamSliceHeight = 0.0;

    streamlinesEnabled = false;
    streamlineDensity  = 1f;
    arrowScale         = 1f;
    stepFactor         = 2f;
  }

  /** set scale length for flow vectors (default is 0.02f) */
  public void setFlowScale(float scale)
         throws VisADException, RemoteException {
    flowScale = scale;
    changeControl(true);
  }

  /** get scale length for flow vectors */
  public float getFlowScale() {
    return flowScale;
  }

  /**
   * set barb orientation for wind barbs (default is southern hemisphere)
   *
   * @param  orientation   wind barb orientation
   *                       (NH_ORIENTATION or SH_ORIENTATION);
   */
  public void setBarbOrientation(int orientation)
         throws VisADException, RemoteException
  {
    // make sure it is one or the other
    if (orientation == SH_ORIENTATION || orientation == NH_ORIENTATION)
       barbOrientation = orientation;
    else
      throw new VisADException( "Invalid orientation value: " + orientation);
    changeControl(true);
  }

  /**
   * Get barb orientation for wind barbs
   *
   * @return orientation (false = northern hemisphere)
   */
  public int getBarbOrientation() {
    return barbOrientation;
  }

  /**
   * Get whether values should be adjusted to the earth 
   *
   * @param  adjust   true to adjust
   */
  public void setAdjustFlowToEarth(boolean adjust)
         throws VisADException, RemoteException
  {
    adjustFlowToEarth = adjust;
    changeControl(true);
  }

  /**
   * Get barb orientation for wind barbs
   *
   * @return orientation (false = northern hemisphere)
   */
  public boolean getAdjustFlowToEarth() {
    return adjustFlowToEarth;
  }

  public void enableStreamlines(boolean flag)
         throws VisADException, RemoteException {
    streamlinesEnabled = flag;
    changeControl(true);
  }

  public void setStreamlineDensity(float density)
         throws VisADException, RemoteException {
    streamlineDensity = density;
    changeControl(true);
  }

  public void setArrowScale(float arrowScale)
         throws VisADException, RemoteException {
    this.arrowScale = arrowScale;
    changeControl(true);
  }

  public void setStepFactor(float stepFactor)
         throws VisADException, RemoteException {
    this.stepFactor = stepFactor;
    changeControl(true);
  }

  public boolean streamlinesEnabled() {
    return streamlinesEnabled;
  }

  public float getStreamlineDensity() {
    return streamlineDensity;
  }

  public float getArrowScale() {
    return arrowScale;
  }

  public float getStepFactor() {
    return stepFactor;
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return "" + flowScale + " " + barbOrientation;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    if (st.countTokens() < 2) throw new VisADException("Invalid save string");
    float scale = Convert.getFloat(st.nextToken());
    int orientation = Convert.getInt(st.nextToken());
    flowScale = scale;
    barbOrientation = orientation;
    changeControl(true);
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof FlowControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    FlowControl fc = (FlowControl )rmt;

    boolean changed = false;

    if (!Util.isApproximatelyEqual(flowScale, fc.flowScale)) {
      changed = true;
      flowScale = fc.flowScale;
    }

    if (barbOrientation != fc.barbOrientation) {
      changed = true;
      barbOrientation = fc.barbOrientation;
    }
    if (HorizontalVectorSlice != fc.HorizontalVectorSlice) {
      changed = true;
      HorizontalVectorSlice = fc.HorizontalVectorSlice;
    }
    if (VerticalVectorSlice != fc.VerticalVectorSlice) {
      changed = true;
      VerticalVectorSlice = fc.VerticalVectorSlice;
    }
    if (HorizontalStreamSlice != fc.HorizontalStreamSlice) {
      changed = true;
      HorizontalStreamSlice = fc.HorizontalStreamSlice;
    }
    if (VerticalStreamSlice != fc.VerticalStreamSlice) {
      changed = true;
      VerticalStreamSlice = fc.VerticalStreamSlice;
    }
    if (TrajectorySet == null) {
      if (fc.TrajectorySet != null) {
        changed = true;
        TrajectorySet = fc.TrajectorySet;
      }
    } else if (fc.TrajectorySet == null) {
      changed = true;
      TrajectorySet = null;
    } else if (TrajectorySet.length != fc.TrajectorySet.length) {
      changed = true;
      TrajectorySet = fc.TrajectorySet;
    } else {
      for (int i = 0; i < TrajectorySet.length; i++) {
        if (TrajectorySet[i] != fc.TrajectorySet[i]) {
          changed = true;
          TrajectorySet[i] = fc.TrajectorySet[i];
        }
      }
    }

    if (!Util.isApproximatelyEqual(HorizontalVectorSliceHeight,
                                   fc.HorizontalVectorSliceHeight))
    {
      changed = true;
      HorizontalVectorSliceHeight = fc.HorizontalVectorSliceHeight;
    }
    if (!Util.isApproximatelyEqual(HorizontalStreamSliceHeight,
                                   fc.HorizontalStreamSliceHeight))
    {
      changed = true;
      HorizontalStreamSliceHeight = fc.HorizontalStreamSliceHeight;
    }

    if (streamlinesEnabled != fc.streamlinesEnabled) {
      changed = true;
      streamlinesEnabled = fc.streamlinesEnabled;
    }

    if (!Util.isApproximatelyEqual(streamlineDensity, fc.streamlineDensity)) {
      changed = true;
      streamlineDensity = fc.streamlineDensity;
    }

    if (!Util.isApproximatelyEqual(arrowScale, fc.arrowScale)) {
      changed = true;
      arrowScale = fc.arrowScale;
    }

    if (!Util.isApproximatelyEqual(stepFactor, fc.stepFactor)) {
      changed = true;
      stepFactor = fc.stepFactor;
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

    FlowControl fc = (FlowControl )o;

    if (!Util.isApproximatelyEqual(flowScale, fc.flowScale)) {
      return false;
    }

    if (barbOrientation != fc.barbOrientation) {
      return false;
    }
    if (HorizontalVectorSlice != fc.HorizontalVectorSlice) {
      return false;
    }
    if (VerticalVectorSlice != fc.VerticalVectorSlice) {
      return false;
    }
    if (HorizontalStreamSlice != fc.HorizontalStreamSlice) {
      return false;
    }
    if (VerticalStreamSlice != fc.VerticalStreamSlice) {
      return false;
    }
    if (TrajectorySet == null) {
      if (fc.TrajectorySet != null) {
        return false;
      }
    } else if (fc.TrajectorySet == null) {
      return false;
    } else if (TrajectorySet.length != fc.TrajectorySet.length) {
      return false;
    } else {
      for (int i = 0; i < TrajectorySet.length; i++) {
        if (TrajectorySet[i] != fc.TrajectorySet[i]) {
          return false;
        }
      }
    }

    if (!Util.isApproximatelyEqual(HorizontalVectorSliceHeight,
                                   fc.HorizontalVectorSliceHeight))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(HorizontalStreamSliceHeight,
                                   fc.HorizontalStreamSliceHeight))
    {
      return false;
    }

    if (streamlinesEnabled != fc.streamlinesEnabled) {
      return false;
    }
    if (!Util.isApproximatelyEqual(streamlineDensity, fc.streamlineDensity))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(arrowScale, fc.arrowScale))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(stepFactor, fc.stepFactor))
    {
      return false;
    }

    return true;
  }

  public Object clone()
  {
    FlowControl fc = (FlowControl )super.clone();
    if (TrajectorySet != null) {
      fc.TrajectorySet = (boolean[] )TrajectorySet.clone();
    }

    return fc;
  }
}
