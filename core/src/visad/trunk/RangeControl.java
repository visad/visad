
//
// RangeControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.rmi.*;

/**
   RangeControl is the VisAD class for controlling SelectRange display scalars.<P>
*/
public class RangeControl extends Control {

  private float RangeLow;
  private float RangeHi;

  public RangeControl(DisplayImpl d) {
    super(d);
    RangeLow = 0.0f;
    RangeHi = 0.0f;
  }
 
  /** set the range of selected values as (range[0], range[1]) */
  public void setRange(float[] range)
         throws VisADException, RemoteException {
    RangeLow = range[0];
    RangeHi = range[1];
    changeControl(true);
  }

  /** return the range of selected values */
  public float[] getRange() {
    float[] range = new float[2];
    range[0] = RangeLow;
    range[1] = RangeHi;
    return range;
  }

}

