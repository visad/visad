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


  double trajVisibilityTimeWindow = 86400.0;
  int numIntrpPts = 6;
  int startSkip = 2;
  SmoothParams smoothParams = SmoothParams.MEDIUM;
  int direction = 1;  //1: forward, -1: backward

  // these are endPoints if direction is backward
  //float[][] startPoints = new float[][] {{25.0f, 26.0f, 24.2f, 25.5f}, {-83.5f, -83.0f, -81.0f, -80.0f}, {0f, 0f, 0f, 0f}};
  float[][] startPoints = null;
  RealTupleType startType = null;

  public TrajectoryParams() {
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, int numIntrpPts, int startSkip, SmoothParams smoothParams) {
    this.trajVisibilityTimeWindow = trajVisibilityTimeWindow;
    this.numIntrpPts = numIntrpPts;
    this.startSkip = startSkip;
    this.smoothParams = smoothParams;
  }

  public TrajectoryParams(double trajVisibilityTimeWindow, int numIntrpPts, int startSkip) {
     this(trajVisibilityTimeWindow, numIntrpPts, startSkip, SmoothParams.MEDIUM);
  }

  public double getTrajVisibilityTimeWindow() {
    return trajVisibilityTimeWindow;
  }

  public int getNumIntrpPts() {
    return numIntrpPts;
  }

  public int getStartSkip() {
    return startSkip;
  }

  public SmoothParams getSmoothParams() {
    return smoothParams;
  }

  public int getDirection() {
    return direction;
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

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TrajectoryParams)) {
      return false;
    }
    else {
      TrajectoryParams trajParams = (TrajectoryParams) obj;
      if (this.trajVisibilityTimeWindow != trajParams.trajVisibilityTimeWindow) {
        return false;
      }
      else if (this.numIntrpPts != trajParams.numIntrpPts) {
        return false;
      }
      else if (this.startSkip != trajParams.startSkip) {
        return false;
      }
      else if (this.smoothParams != trajParams.smoothParams) {
        return false;
      }
    }
    return true;
  }

}
