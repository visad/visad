
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
  private transient Vector ListenerVector = new Vector();

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

  /** add a ControlListener */
  public void addControlListener(ControlListener listener) {
    ListenerVector.addElement(listener);
  }

  /** remove a ControlListener */
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
// System.out.println(getClass().getName() + "  set  tickFlag = " + tickFlag);
    OldTick = NewTick;
    subSetTicks();
  }

  public synchronized boolean peekTicks(DataRenderer r, DataDisplayLink link) {
/*
boolean flag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
System.out.println(getClass().getName() + "  peek  flag = " + flag +
                   " trans = " + r.isTransformControl(this, link) + " sub = " +
                   subPeekTicks(r, link));
*/
    return ((OldTick < NewTick || (NewTick < 0 && 0 < OldTick)) &&
            r.isTransformControl(this, link)) || subPeekTicks(r, link);
  }

  /** return true if Control changed and requires re-Transform */
  public synchronized boolean checkTicks(DataRenderer r, DataDisplayLink link) {
/*
System.out.println(getClass().getName() + "  check  tickFlag = " + tickFlag +
                   " trans = " + r.isTransformControl(this, link) + " sub = " +
                   subCheckTicks(r, link));
*/
    return (tickFlag && r.isTransformControl(this, link)) || subCheckTicks(r, link);
  }

  /** reset tickFlag */
  public synchronized void resetTicks() {
// System.out.println(getClass().getName() + "  reset");
    tickFlag = false;
    subResetTicks();
  }

  /** run setTicks on any sub-Controls;
      this default for no sub-Controls */
  public void subSetTicks() {
  }

  /** run checkTicks on any sub-Controls;
      this default for no sub-Controls */
  public boolean subCheckTicks(DataRenderer r, DataDisplayLink link) {
    return false;
  }
 
  /** run peekTicks on any sub-Controls;
      this default for no sub-Controls */
  public boolean subPeekTicks(DataRenderer r, DataDisplayLink link) {
    return false;
  }

  /** run resetTicks on any sub-Controls;
      this default for no sub-Controls */
  public void subResetTicks() {
  }

  void setIndex(int index) {
    Index = index;
  }

  int getIndex() {
    return Index;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

}

