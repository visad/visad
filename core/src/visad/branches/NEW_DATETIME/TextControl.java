
//
// TextControl.java
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
import java.awt.Font;

/**
   TextControl is the VisAD class for controlling Text display scalars.<P>
*/
public class TextControl extends Control {

  private Font font = null;

  private boolean center = false;

  private double size = 1.0;

  public TextControl(DisplayImpl d) {
    super(d);
  }

  /** set the Font; in the initial release this has no effect */
  public void setFont(Font f)
         throws VisADException, RemoteException {
    font = f;
    changeControl(true);
  }

  /** return the Font */
  public Font getFont() {
    return font;
  }

  /** set the centering flag; if true, text will be centered at
      mapped locations; if false, text will be to the right
      of mapped locations */
  public void setCenter(boolean c)
         throws VisADException, RemoteException {
    center = c;
    changeControl(true);
  }

  /** return the centering flag */
  public boolean getCenter() {
    return center;
  }

  /** set the size of characters; the default is 1.0 */
  public void setSize(double s)
         throws VisADException, RemoteException {
    size = s;
    changeControl(true);
  }
 
  /** return the size */
  public double getSize() {
    return size;
  }

}

