
//
// Control.java
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

import java.util.*;
import java.rmi.*;

/**
   Control is the VisAD superclass for controls for display scalars.<P>
*/
public abstract class Control extends Object
       implements Cloneable, java.io.Serializable {

  /** incremented by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;

  /** unique Display this Control is part of */
  transient DisplayImpl display;
  transient DisplayRenderer displayRenderer;
  /** index of this in display.ControlVector */
  private int Index;

  /** Vector of ControlListeners */
  transient Vector ListenerVector = new Vector();

  public Control(DisplayImpl d) {
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
    display = d;
    if (display != null) displayRenderer = display.getDisplayRenderer();
  }

  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  /** invoked every time values of this Control change;
      tick is true to notify the Display */
  public void changeControl(boolean tick)
         throws VisADException, RemoteException {
    if (tick) incTick();
    if (ListenerVector != null) {
      synchronized (ListenerVector) {
        Enumeration listeners = ListenerVector.elements();
        while (listeners.hasMoreElements()) {
          ControlListener listener =
            (ControlListener) listeners.nextElement();
          listener.controlChanged(new ControlEvent(this));
        }
      }
    }
  }

  public void addControlListener(ControlListener listener) {
    ListenerVector.addElement(listener);
  }

  public void removeControlListener(ControlListener listener) {
    if (listener != null) {
      ListenerVector.removeElement(listener);
    }
  }

  /** invoke incTick every time Control changes */
  public long incTick() {
    if (display != null) display.controlChanged();
    NewTick += 1;
    if (NewTick == Long.MAX_VALUE) NewTick = Long.MIN_VALUE + 1;
    return NewTick;
  }
 
  /** set tickFlag according to OldTick and NewTick */
  public synchronized void setTicks() {
    tickFlag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
    OldTick = NewTick;
  }

  /** return true if Control changed and requires re-Transform */
  public synchronized boolean checkTicks(DataRenderer r, DataDisplayLink link) {
    return (tickFlag && r.isTransformControl(this, link)) || subTicks(r, link);
  }

  /** run checkTicks on any sub-Controls;
      this default for no sub-Controls */
  public boolean subTicks(DataRenderer r, DataDisplayLink link) {
    return false;
  }
 
  /** reset tickFlag */
  synchronized void resetTicks() {
    tickFlag = false;
  }

  void setIndex(int index) {
    Index = index;
  }

  int getIndex() {
    return Index;
  }

}

