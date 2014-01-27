//
// FlowControl.java
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

  private boolean autoScale = false;
  private ProjectionControlListener pcl = null;

  /** Streamline flags
  -------------------------------*/
  boolean streamlinesEnabled;
  float   streamlineDensity;
  float   arrowScale;
  float   stepFactor;
  float   packingFactor;
  float   cntrWeight;
  int     n_pass;
  float   reduction;

  /** Trajectory flags
  --------------------------------*/
  boolean trajectoryEnabled = false;

  // WLH  need Vertical*Slice location parameters

  /**
   * Create a FlowControl
   * @param  d  DisplayImpl that this is associated with.
   */
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
    packingFactor      = 1f;
    cntrWeight         = 3f;
    n_pass             = 0;
    reduction          = 1f;
    adjustFlowToEarth  = true;  
    autoScale          = false;

    trajectoryEnabled  = false;
  }

  /** 
   * Set scale length for flow vectors (default is 0.02f) 
   * @param scale  new scale
   */
  public void setFlowScale(float scale)
         throws VisADException, RemoteException {
    flowScale = scale;
    changeControl(true);
  }

  /** 
   * Get scale length for flow vectors 
   * @return  scale length for flow vectors
   */
  public float getFlowScale() {
    return flowScale;
  }

  /**
   * Set barb orientation for wind barbs (default is southern hemisphere)
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
   * @throws VisADException  problem setting the value
   * @throws RemoteException  problem setting the value on remote system
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

  /**
   * Enable/disable showing vectors as streamlines
   *
   * @param flag  true to display as streamlines
   * @throws VisADException  problem enabling the streamlines
   * @throws RemoteException  problem enabling the streamlines on remote system
   */
  public void enableStreamlines(boolean flag)
         throws VisADException, RemoteException {
    streamlinesEnabled = flag;
    if (trajectoryEnabled && streamlinesEnabled) {
      trajectoryEnabled = false;
    }
    changeControl(true);
  }

  /**
   * Enable/disable showing vectors as trajectories
   *
   * @param flag  true to display as trajectories
   * @throws VisADException  problem enabling the trajectories
   * @throws RemoteException  problem enabling the trajectories on remote system
   */
  public void enableTrajectory(boolean flag)
         throws VisADException, RemoteException {
    trajectoryEnabled = flag;
    if (trajectoryEnabled && streamlinesEnabled) {
      streamlinesEnabled = false;
    }
    changeControl(true);
  }


  /**
   * Set the streamline density
   * @param density the density value
   * @throws VisADException  problem setting the density
   * @throws RemoteException  problem setting the density on remote system
   */
  public void setStreamlineDensity(float density)
         throws VisADException, RemoteException {
    streamlineDensity = density;
    changeControl(true);
  }

  /**
   * Set the streamline arrow size
   * @param arrowScale the streamline arrow size
   * @throws VisADException  problem setting the arrow scale
   * @throws RemoteException  problem setting the arrow scale on remote system
   */
  public void setArrowScale(float arrowScale)
         throws VisADException, RemoteException {
    this.arrowScale = arrowScale;
    changeControl(true);
  }

  /**
   * Set the streamline step factor
   * @param stepFactor the streamline step factor
   * @throws VisADException  problem setting the step factor
   * @throws RemoteException  problem setting the step factor on remote system
   */
  public void setStepFactor(float stepFactor)
         throws VisADException, RemoteException {
    this.stepFactor = stepFactor;
    changeControl(true);
  }

  /**
   * Set the streamline packing
   * @param packing the streamline packing
   * @throws VisADException  problem setting the packing
   * @throws RemoteException  problem setting the packing on remote system
   */
  public void setStreamlinePacking(float packing) 
         throws VisADException, RemoteException {
    this.packingFactor = packing;
    changeControl(true);
  }

  /**
   * Set the streamline smoothing
   * @param cntrWeight  the center weight
   * @param n_pass  number of smoothing passes
   * @throws VisADException  problem setting the smoothing
   * @throws RemoteException  problem setting the smoothing on remote system
   */
  public void setStreamlineSmoothing(float cntrWeight, int n_pass)
         throws VisADException, RemoteException {
    this.cntrWeight = cntrWeight;
    this.n_pass = n_pass;
    changeControl(true);
  }
 
  /**
   * Set the streamline reduction
   * @param reduction the streamline reduction
   * @throws VisADException  problem setting the reduction
   * @throws RemoteException  problem setting the reduction on remote system
   */
  public void setStreamlineReduction(float reduction)
         throws VisADException, RemoteException {
    this.reduction = reduction;
    changeControl(true);
  }

  /**
   * Get the status of streamlines
   * @return  true if streamlines are enabled.
   */
  public boolean streamlinesEnabled() {
    return streamlinesEnabled;
  }

  /**
   * Get the status of streamlines
   * @return  true if streamlines are enabled.
   */
  public boolean trajectoryEnabled() {
    return trajectoryEnabled;
  }


  /**
   * Get the streamline density factor.
   * @return  the streamline density factor.
   */
  public float getStreamlineDensity() {
    return streamlineDensity;
  }

  /**
   * Get the streamline arrow scale
   * @return  the streamline arrow scale
   */
  public float getArrowScale() {
    return arrowScale;
  }

  /**
   * Get the streamline step factor
   * @return  the streamline step factor
   */
  public float getStepFactor() {
    return stepFactor;
  }

  /**
   * Get the streamline packing value
   * @return  the streamline packing value
   */
  public float getStreamlinePacking() {
    return packingFactor;
  }

  /**
   * Get the streamline smoothing value
   * @return  the streamline smoothing value
   */
  public float[] getStreamlineSmoothing() {
    return new float[] {cntrWeight, (float) n_pass};
  }

  /**
   * Get the streamline reduction value
   * @return  the streamline reduction value
   */
  public float getStreamlineReduction() {
    return reduction;
  }

  /** 
   * Get a string that can be used to reconstruct this control later 
   * @return a string representation of this control
   */
  public String getSaveString() {
    return "" + 
           getFlowScale() + " " + 
           getBarbOrientation() + " " +
           streamlinesEnabled() + " " +
           trajectoryEnabled() + " " +
           getStreamlineDensity() + " " +
           getArrowScale() + " " +
           getStepFactor() + " " +
           getStreamlinePacking() + " " +
           getStreamlineSmoothing()[0] + " " +
           getStreamlineSmoothing()[1] + " " +
           getStreamlineReduction() + " " +
           getAdjustFlowToEarth() + " " +
           getAutoScale();
  }

  /** 
   * Reconstruct this control using the specified save string 
   */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    if (st.countTokens() < 2) throw new VisADException("Invalid save string");
    float scale = Convert.getFloat(st.nextToken());
    int orientation = Convert.getInt(st.nextToken());
    boolean es = st.hasMoreTokens() ? Convert.getBoolean(st.nextToken()) : streamlinesEnabled();
    boolean tes = st.hasMoreTokens() ? Convert.getBoolean(st.nextToken()) : trajectoryEnabled();
    float sd = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStreamlineDensity();
    float as = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getArrowScale();
    float sf = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStepFactor();
    float sp = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStreamlinePacking();
    float ssc = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStreamlineSmoothing()[0];
    float ssn = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStreamlineSmoothing()[1];
    float sr = st.hasMoreTokens() ? Convert.getFloat(st.nextToken()) : getStreamlineReduction();
    boolean af = st.hasMoreTokens() ? Convert.getBoolean(st.nextToken()) : getAdjustFlowToEarth();
    boolean asc = st.hasMoreTokens() ? Convert.getBoolean(st.nextToken()) : getAutoScale();

    flowScale = scale;
    barbOrientation = orientation;
    streamlinesEnabled = es;
    trajectoryEnabled = tes;
    streamlineDensity = sd;
    arrowScale = as;
    stepFactor = sf;
    packingFactor = sp;
    cntrWeight = ssc;
    n_pass= (int) ssn;
    reduction = sr;
    adjustFlowToEarth = af;
    autoScale = asc;
    changeControl(true);
  }

  /** 
   * Copy the state of a remote control to this control 
   */
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

    if (trajectoryEnabled != fc.trajectoryEnabled) {
      changed = true;
      trajectoryEnabled = fc.trajectoryEnabled;
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

    if (!Util.isApproximatelyEqual(packingFactor, fc.packingFactor)) {
      changed = true;
      packingFactor = fc.packingFactor;
    }

    if (!Util.isApproximatelyEqual(cntrWeight, fc.cntrWeight)) {
      changed = true;
      cntrWeight = fc.cntrWeight;
    }

    if (!Util.isApproximatelyEqual(n_pass, fc.n_pass)) {
      changed = true;
      n_pass = fc.n_pass;
    }

    if (!Util.isApproximatelyEqual(reduction, fc.reduction)) {
      changed = true;
      reduction = fc.reduction;
    }

    if (autoScale != fc.autoScale) {
      // changed = true;
      setAutoScale(fc.autoScale);
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

  /**
   * Set whether the vector/barb size should scale with display zoom.
   * @param  auto  true to enable autoscaling.
   * @throws VisADException  problem setting the autoscaling
   */
  public void setAutoScale(boolean auto)
         throws VisADException {
    if (auto == autoScale) return;
    DisplayImpl display = getDisplay();
    DisplayRenderer dr = display.getDisplayRenderer();
    MouseBehavior mouse = dr.getMouseBehavior();
    ProjectionControl pc = display.getProjectionControl();
    if (auto) {
      pcl = new ProjectionControlListener(mouse, this, pc);
      pc.addControlListener(pcl);
    }
    else {
      pc.removeControlListener(pcl);
    }
    autoScale = auto;
    try {
      changeControl(true);
    }
    catch (RemoteException e) {
    }
  }

  /**
   * Get whether the vector/barb size should scale with display zoom.
   * @return  true if autoscaling is enabled.
   */
  public boolean getAutoScale() {
    return autoScale;
  }


  /**
   * Null the control.  Override superclass to remove the autoscaling listener.
   */
  public void nullControl() {
    try {
      setAutoScale(false);
    }
    catch (VisADException e) {
    }
    super.nullControl();
  }

  /**
   * See if this control equals another
   * @param o  object in question
   * @return true if they are equal.
   */
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
    if (trajectoryEnabled != fc.trajectoryEnabled) {
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
    if (!Util.isApproximatelyEqual(packingFactor, fc.packingFactor))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(cntrWeight, fc.cntrWeight))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(n_pass, fc.n_pass))
    {
      return false;
    }
    if (!Util.isApproximatelyEqual(reduction, fc.reduction))
    {
      return false;
    }
    if (autoScale != fc.autoScale) {
      return false;
    }

    return true;
  }

  /**
   * Clone this control.
   * @return a clone of this
   */
  public Object clone()
  {
    FlowControl fc = (FlowControl )super.clone();
    if (TrajectorySet != null) {
      fc.TrajectorySet = (boolean[] )TrajectorySet.clone();
    }

    return fc;
  }

  /**
   * A class for listening to changes in the control.
   */
  class ProjectionControlListener implements ControlListener {
    private boolean pfirst = true;
    private MouseBehavior mouse;
    private ProjectionControl pcontrol;
    private FlowControl flowControl;
    private double base_scale = 1.0;
    private float last_cscale = 1.0f;
    private double base_size = 1.0;

    ProjectionControlListener(MouseBehavior m, FlowControl s,
                              ProjectionControl p) {
      mouse = m;
      flowControl = s;
      pcontrol = p;
    }

    public void controlChanged(ControlEvent e)
           throws VisADException, RemoteException {
      double[] matrix = pcontrol.getMatrix();
      double[] rot = new double[3];
      double[] scale = new double[3];
      double[] trans = new double[3];
      mouse.instance_unmake_matrix(rot, scale, trans, matrix);

      if (pfirst) {
        pfirst = false;
        base_scale = scale[2];
        last_cscale = 1.0f;
        base_size = flowControl.getFlowScale();
      }
      else {
        float cscale = (float) (base_scale / scale[2]);
        float ratio = cscale / last_cscale;
        if (ratio < 0.95f || 1.05f < ratio) { // 5% change
          last_cscale = cscale;
          flowControl.setFlowScale((float) base_size * cscale);
        }
      }
    }
  }
}
