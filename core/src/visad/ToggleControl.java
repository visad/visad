//
// ToggleControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import visad.browser.Convert;

/**
   ToggleControl is the VisAD class for toggling other Control-s
   on and off.<P>
*/
public class ToggleControl extends Control {

  private boolean on;
  private Control parent;

  public ToggleControl(DisplayImpl d, Control p) {
    super(d);
    parent = p;
    on = true;
  }

  public Control getParent() {
    return parent;
  }

  public boolean getOn() {
    return on;
  }

  public void setOn(boolean o)
         throws VisADException, RemoteException {
    on = o;
    changeControl(true);
  }

  private boolean parentEquals(Control newParent)
  {
    if (parent == null) {
      if (newParent != null) {
        return false;
      }
    } else if (newParent == null) {
      return false;
    } else if (!parent.equals(newParent)) {
      return false;
    }

    return true;
  }

  /** get a string that can be used to reconstruct this control later */
  public String getSaveString() {
    return "" + on;
  }

  /** reconstruct this control using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    setOn(Convert.getBoolean(save));
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof ToggleControl)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    ToggleControl tc = (ToggleControl )rmt;

    boolean changed = false;

    if (on != tc.on) {
      changed = true;
      on = tc.on;
    }

    if (!parentEquals(tc.parent)) {
      changed = true;
      parent = tc.parent;
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

    ToggleControl tc = (ToggleControl )o;

    if (on != tc.on) {
      return false;
    }

    if (!parentEquals(tc.parent)) {
      return false;
    }

    return true;
  }
}

