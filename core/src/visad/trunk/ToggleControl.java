
//
// ToggleControl.java
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
   ToggleControl is the VisAD class for toggling other Control-s
   on and off.<P>
*/
public class ToggleControl extends Control {

  private boolean on;
  private Control parent;

  static final ToggleControl prototype = new ToggleControl();

  public ToggleControl(DisplayImpl d, Control p) {
    super(d);
    parent = p;
    on = true;
  }
 
  ToggleControl() {
    this(null, null);
  }

  /** should never be called */
  public Control cloneButContents(DisplayImpl d)
         throws VisADException, RemoteException {
    throw new DisplayException("ToggleControl.cloneButContents");
  }

  public boolean getOn() {
    return on;
  }

  public void setOn(boolean o) {
    on = o;
    changeControl();
  }

}

