//
// TrajectoryParams.java
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

/**
   TrajectoryParams is a class containing parameters for controlling Trajectory Flow rendering.<P>
*/
public class TrajectoryParams {

  public static enum SmoothParams {
    LIGHT (0.05f, 0.9f, 0.05f),
    MEDIUM (0.15f, 0.7f, 0.15f),
    HEAVY (0.25f, 0.5f, 0.25f),
    NONE (0.0f, 1.0f, 0.0f);

    public float w0;
    public float w1;
    public float w2;

    SmoothParams(float w0, float w1, float w2) {
      this.w0 = w0;
      this.w1 = w1;
      this.w2 = w2;
    }
  }
  
  public static final int LINE = 0;
  public static final int RIBBON = 1;
  public static final int CYLINDER = 2;
  public static final int DEFORM_RIBBON = 3;
  public static final int POINT = 4;

  double trajVisibilityTimeWindow = 86400.0;
  double trajRefreshInterval = 86400.0;
  int numIntrpPts = 6;
  int startSkip = 2;
  SmoothParams smoothParams = SmoothParams.MEDIUM;
  boolean forward = true;
  int direction = 1;  //1: forward, -1: backward
  boolean doIntrp = true;
  float markerSize = 1f;
  boolean markerEnabled = false;
  boolean manualIntrpPts = false;
  boolean autoSizeMarker = true;
  boolean cachingEnabled = true;
  
  int trajForm = LINE;
  float cylWidth = 0.00014f;
  float ribbonWidthFac = 1f;
  int zStart = 0;
  int zStartSkip = 0;

  // these are endPoints if direction is backward
  float[][] startPoints = null;
  RealTupleType startType = Display.DisplaySpatialCartesianTuple;

  public TrajectoryParams() {
  }
  
  public TrajectoryParams(TrajectoryParams params) {
    this.trajVisibilityTimeWindow = params.getTrajVisibilityTimeWindow();
    this.trajRefreshInterval = params.getTrajRefreshInterval();
    this.numIntrpPts = params.getNumIntrpPts();
    this.startSkip = params.getStartSkip();
    this.smoothParams = params.getSmoothParams();
    this.forward = params.getDirectionFlag();
    this.direction = params.getDirection();
    this.doIntrp = params.getDoIntrp();
    this.markerSize = params.getMarkerSize();
    this.markerEnabled = params.getMarkerEnabled();
    this.manualIntrpPts = params.getManualIntrpPts();
    this.startPoints = params.getStartPoints();
    this.startType = params.getStartType();
    this.autoSizeMarker = params.getAutoSizeMarker();
    this.cachingEnabled = params.getCachingEnabled();
    this.trajForm = params.getTrajectoryForm();
    this.cylWidth = params.getCylinderWidth();
    this.ribbonWidthFac = params.getRibbonWidthFactor();
    this.zStart = params.getZStartIndex();
    this.zStartSkip = params.getZStartSkip();
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, double trajRefreshInterval, int numIntrpPts, int startSkip, SmoothParams smoothParams) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajRefreshInterval;
    this.numIntrpPts = numIntrpPts;
    this.startSkip = startSkip;
    this.smoothParams = smoothParams;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, int numIntrpPts, int startSkip, SmoothParams smoothParams) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajVisibilityTimeWindow;
    this.numIntrpPts = numIntrpPts;
    this.startSkip = startSkip;
    this.smoothParams = smoothParams;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, int numIntrpPts, int startSkip) {
     this(trajVisibilityTimeWindow, numIntrpPts, startSkip, SmoothParams.MEDIUM);
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, double trajRefreshInterval) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajRefreshInterval;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajVisibilityTimeWindow;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, double trajRefreshInterval, int startSkip) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajRefreshInterval;
    this.startSkip = startSkip;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, int startSkip) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.trajRefreshInterval = trajVisibilityTimeWindow;
    this.startSkip = startSkip;
  }

  public void setTrajVisibilityTimeWindow(double trajVisibilityTimeWindow) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
  }

  public void setTrajRefreshInterval(double trajRefreshInterval) {
    this.trajRefreshInterval = trajRefreshInterval;
  }
  
  public void setDirectionFlag(boolean forward) {
     this.forward = forward;
     if (forward) {
        this.direction = 1;
     }
     else {
        this.direction = -1;
     }
  }
  
  public boolean getDirectionFlag() {
     return forward;
  }
  
  public void setDoIntrp(boolean yesno) {
    this.doIntrp = yesno;
  }
  
  public void setNumIntrpPts(int numIntrpPts) {
    this.numIntrpPts = numIntrpPts;
    this.manualIntrpPts = true;
  }
  
  public void setStartSkip(int skip) {
    this.startSkip = skip;
  }
  
  public void setZStartSkip(int skip) {
     this.zStartSkip = skip;
  }
  
  public void setZStartIndex(int idx) {
     this.zStart = idx;
  }
  
  public void setManualIntrpPts(boolean isManual) {
    this.manualIntrpPts = isManual;
  }
  
  public boolean getManualIntrpPts() {
    return this.manualIntrpPts;
  }

  public void setMarkerSize(float size) {
    this.markerSize = size;
  }
  
  public void setMarkerEnabled(boolean yesno) {
    this.markerEnabled = yesno;
  }
  
  public void setCachingEnabled(boolean yesno) {
     this.cachingEnabled = yesno;
  }
  
  public void setTrajectoryForm(int form) {
     trajForm = form;
  }
  
  public void setCylinderWidth(float width) {
     cylWidth = width;
  }
  
  public void setRibbonWidthFactor(float fac) {
     this.ribbonWidthFac = fac;
  }

  public double getTrajVisibilityTimeWindow() {
    return trajVisibilityTimeWindow;
  }

  public double getTrajRefreshInterval() {
    return trajRefreshInterval;
  }

  public int getNumIntrpPts() {
    return numIntrpPts;
  }

  public int getStartSkip() {
    return startSkip;
  }
  
  public int getZStartSkip() {
    return zStartSkip;
  }
  
  public int getZStartIndex() {
    return zStart;
  }

  public SmoothParams getSmoothParams() {
    return smoothParams;
  }
  
  public int getTrajectoryForm() {
    return trajForm;
  }
  
  public float getCylinderWidth() {
     return cylWidth;
  }
  
  public float getRibbonWidthFactor() {
     return ribbonWidthFac;
  }

  public int getDirection() {
    return direction;
  }

  public boolean getDoIntrp() {
    return this.doIntrp;
  }

  public float getMarkerSize() {
    return this.markerSize;
  }
  
  public boolean getMarkerEnabled() {
    return this.markerEnabled;
  }
  
  public void setStartPoints(float[][] startPts) {
    this.startPoints = startPts;
    this.startType = Display.DisplaySpatialCartesianTuple;
  }

  public void setStartPoints(RealTupleType startType, float[][] startPts) {
    this.startType = startType;
    this.startPoints = startPts;
  }

  public float[][] getStartPoints() {
    return startPoints;
  }
 
  public RealTupleType getStartType() {
    return startType;
  }
  
  public boolean getAutoSizeMarker() {
     return autoSizeMarker;
  }
  
  public void setAutoSizeMarker(boolean autoSizeMarker) {
     this.autoSizeMarker = autoSizeMarker;
  }
  
  public boolean getCachingEnabled() {
     return this.cachingEnabled;
  }
  
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TrajectoryParams)) {
      return false;
    }
    else {
      TrajectoryParams trajParams = (TrajectoryParams) obj;
      if (this.trajVisibilityTimeWindow != trajParams.trajVisibilityTimeWindow) {
        return false;
      }
      else if (this.trajRefreshInterval != trajParams.trajRefreshInterval) {
        return false;
      }
      else if (this.numIntrpPts != trajParams.numIntrpPts) {
        return false;
      }
      else if (this.startSkip != trajParams.startSkip) {
        return false;
      }
      else if (this.zStart != trajParams.zStart) {
        return false;
      }      
      else if (this.zStartSkip != trajParams.zStartSkip) {
        return false;
      }      
      else if (this.smoothParams != trajParams.smoothParams) {
        return false;
      }
      else if (this.trajForm != trajParams.trajForm) {
        return false;
      }
      else if (this.doIntrp != trajParams.doIntrp) {
         return false;
      }
      else if (this.forward != trajParams.forward) {
         return false;
      }
      else if (this.direction != trajParams.direction) {
         return false;
      }
      else if (this.markerEnabled != trajParams.markerEnabled) {
         return false;
      }
      else if (this.cylWidth != trajParams.cylWidth) {
        return false;
      }
      else if (this.ribbonWidthFac != trajParams.ribbonWidthFac) {
        return false;
      }
    }
    return true;
  }

}
