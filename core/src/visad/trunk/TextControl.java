//
// TextControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import java.text.*;
import java.rmi.*;

import visad.util.Util;

/**
   TextControl is the VisAD class for controlling Text display scalars.<P>
*/
public class TextControl extends Control {

  private Font font = null;

  private boolean center = false;

  private double size = 1.0;

  // WLH 31 May 2000
  // draw on sphere surface
  private boolean sphere = false;

  private NumberFormat format = null;

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

  // WLH 31 May 2000
  public void setSphere(boolean s)
         throws VisADException, RemoteException {
    sphere = s;
    changeControl(true);
  }

  // WLH 31 May 2000
  public boolean getSphere() {
    return sphere;
  }

  // WLH 16 June 2000
  public void setNumberFormat(NumberFormat f)
         throws VisADException, RemoteException {
    format = f;
    changeControl(true);
  }

  // WLH 16 June 2000
  public NumberFormat getNumberFormat() {
    return format;
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

  // WLH 16 June 2000
  private boolean formatEquals(NumberFormat newFormat)
  {
    if (format == null) {
      if (newFormat != null) {
        return false;
      }
    } else if (newFormat == null) {
      return false;
    } else if (!format.equals(newFormat)) {
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

    // WLH 31 May 2000
    if (sphere != tc.sphere) {
      changed = true;
      sphere = tc.sphere;
    }

    // WLH 16 June 2000
    if (!formatEquals(tc.format)) {
      changed = true;
      format = tc.format;
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

    // WLH 31 May 2000
    if (sphere != tc.sphere) {
      return false;
    }

    // WLH 16 June 2000
    if (!formatEquals(tc.format)) {
      return false;
    }

    if (!Util.isApproximatelyEqual(size, tc.size)) {
      return false;
    }

    return true;
  }
}

