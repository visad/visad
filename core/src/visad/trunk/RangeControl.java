
//
// RangeControl.java
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
   RangeControl is the VisAD class for controlling SelectRange display scalars.<P>
*/
public class RangeControl extends Control {

  private double RangeLow;
  private double RangeHi;

  static final RangeControl prototype = new RangeControl();

  public RangeControl(DisplayImpl d) {
    super(d);
  }
 
  RangeControl() {
    super();
  }

  public Control cloneButContents(DisplayImpl d) {
    RangeControl control = new RangeControl(d);
    control.RangeLow = 0.0;
    control.RangeHi = 0.0;

    return control;
  }

}

