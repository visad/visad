
//
// ValueControl.java
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
   ValueControl is the VisAD class for controlling SelectValue display scalars.<P>
*/
public class ValueControl extends Control {

  private double Value;

  static final ValueControl prototype = new ValueControl();

  public ValueControl(DisplayImpl d) {
    super(d);
  }
 
  ValueControl() {
    this(null);
  }

  public Control cloneButContents(DisplayImpl d) {
    ValueControl control = new ValueControl(d);
    control.Value = 0.0;

    return control;
  }

}

