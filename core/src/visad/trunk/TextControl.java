//
// TextControl.java
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

import java.awt.Font;
import java.rmi.*;

import visad.util.Util;

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

  private boolean fontEquals(Font newFont)
  {
    if (font == null) {
      if (newFont != null) {
        return false;
      }
    } else if (newFont == null) {
      return false;
    } else if (!font.equals(newFont)) {
      return false;
    }

    return true;
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof TextControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    TextControl tc = (TextControl )rmt;

    boolean changed = false;

    if (!fontEquals(tc.font)) {
      changed = true;
      font = tc.font;
    }

    if (center != tc.center) {
      changed = true;
      center = tc.center;
    }

    if (!Util.isApproximatelyEqual(size, tc.size)) {
      changed = true;
      size = tc.size;
    }

    if (changed) {
      try {
        changeControl(true);
      } catch (RemoteException re) {
        throw new VisADException("Could not indicate that control" +
                                 " changed: " + re.getMessage());
      }
    }
  }

  public boolean equals(Object o)
  {
    if (!super.equals(o)) {
      return false;
    }

    TextControl tc = (TextControl )o;

    if (!fontEquals(font)) {
      return false;
    }

    if (center != tc.center) {
      return false;
    }

    if (!Util.isApproximatelyEqual(size, tc.size)) {
      return false;
    }

    return true;
  }
}

