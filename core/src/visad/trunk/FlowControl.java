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

  // DRM add 09-Sep-1999
  /** Northern Hemisphere orientation for wind barbs */
  public static final int NH_ORIENTATION = 0;
  /** Southern Hemisphere orientation for wind barbs */
  public static final int SH_ORIENTATION = 1;
  int barbOrientation;

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
    barbOrientation = SH_ORIENTATION;    // DRM 9-Sept-1999
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

}
