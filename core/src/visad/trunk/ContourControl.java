
//
// ContourControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

/**
   ContourControl is the VisAD class for controlling IsoContour display scalars.<P>
*/
public class ContourControl extends Control {

  private boolean HorizontalContourSlice;
  private boolean VerticalContourSlice;
  private boolean ContourSuface;

  private double SurfaceValue;
  private double HorizontalSliceLow;
  private double HorizontalSliceHi;
  private double HorizontalSliceStep;
  private double VerticalSliceLow;
  private double VerticalSliceHi;
  private double VerticalSliceStep;

  static final ContourControl prototype = new ContourControl();

  public ContourControl(DisplayImpl d) {
    super(d);
  }
 
  ContourControl() {
    super();
  }

  public Control cloneButContents(DisplayImpl d) {
    ContourControl control = new ContourControl(d);
    control.HorizontalContourSlice = false;
    control.VerticalContourSlice = false;
    control.ContourSuface = false;

    control.SurfaceValue = 0.0;
    control.HorizontalSliceLow = 0.0;
    control.HorizontalSliceHi = 0.0;
    control.HorizontalSliceStep = 1.0;
    control.VerticalSliceLow = 0.0;
    control.VerticalSliceHi = 0.0;
    control.VerticalSliceStep = 1.0;

    return control;
  }

}

