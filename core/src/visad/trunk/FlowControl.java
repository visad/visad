//
// FlowControl.java
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

/**
   FlowControl is the VisAD abstract super-class for controlling
   Flow display scalars.<P>
*/
public abstract class FlowControl extends Control {

  float flowScale;

  boolean HorizontalVectorSlice;
  boolean VerticalVectorSlice;
  boolean HorizontalStreamSlice;
  boolean VerticalStreamSlice;
  boolean[] TrajectorySet;

  double HorizontalVectorSliceHeight;
  double HorizontalStreamSliceHeight;

  // WLH  need Vertical*Slice location parameters

  public FlowControl(DisplayImpl d) {
    super(d);
    flowScale = 0.02f;
    HorizontalVectorSlice = false;
    VerticalVectorSlice = false;
    HorizontalStreamSlice = false;
    VerticalStreamSlice = false;
    TrajectorySet = null;
 
    HorizontalVectorSliceHeight = 0.0;
    HorizontalStreamSliceHeight = 0.0;
  }
 
  /** set scale length for flow vectors (default is 0.02f) */
  public void setFlowScale(float scale)
         throws VisADException, RemoteException {
    flowScale = scale;
    changeControl(true);
  }

  public float getFlowScale() {
    return flowScale;
  }

}

