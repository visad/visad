
//
// Flow1Control.java
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
   Flow1Control is the VisAD class for controlling Flow1 display scalars.<P>
*/
public class Flow1Control extends FlowControl {

  static final Flow1Control prototype = new Flow1Control();

  public Flow1Control(DisplayImpl d) {
    super(d);
  }
 
  Flow1Control() {
    this(null);
  }

  public Control cloneButContents(DisplayImpl d) {
    Flow1Control control = new Flow1Control(d);
    control.flowScale = 0.02f;
    control.HorizontalVectorSlice = false;
    control.VerticalVectorSlice = false;
    control.HorizontalStreamSlice = false;
    control.VerticalStreamSlice = false;
    control.TrajectorySet = null;

    control.HorizontalVectorSliceHeight = 0.0;
    control.HorizontalStreamSliceHeight = 0.0;

    return control;
  }

}

