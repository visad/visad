
//
// ColorControl.java
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

import java.rmi.*;

/**
   ColorControl is the VisAD class for controlling 3-component Color
   display scalars (e.g., ColorRGB, ColorHSV, ColorCMY).<P>
*/
public class ColorControl extends Control {

  private Function table;

  static final ColorControl prototype = new ColorControl();

  public ColorControl(DisplayImpl d) {
    super(d);
  }
 
  ColorControl() {
    super();
  }

  public void setTable(Function func)
         throws VisADException, RemoteException {
    if (!func.getType().equalsExceptName(FunctionType.REAL_1TO3_FUNCTION)) {
      throw new DisplayException("ScalarMap: function must be 1D-to-3D");
    }
    table = func;
  }

  public Control cloneButContents(DisplayImpl d) throws RemoteException {
    ColorControl control = new ColorControl(d);
    if (table != null) {
      control.table = (FlatField) table.dataClone();
    }
    return control;
  }

}

