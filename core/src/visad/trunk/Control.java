//
// Control.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.rmi.*;

/**
   Control is the VisAD superclass for controls for DisplayRealTypes.<P>
*/
public abstract class Control extends Object
       implements Cloneable, java.io.Serializable {

  /** incremented by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;
  /** flag to indicate after setTicks and bfore resetTicks */
  private boolean isSet = false;

  /** unique Display this Control is part of */
  transient DisplayImpl display;
  transient DisplayRenderer displayRenderer;
  /** index of this in display.ControlVector */
  private int Index;
  /** instance of this in display.ControlVector */
  private int Instance;

  /** Vector of ControlListeners */
  private transient Vector ListenerVector = new Vector();

  public Control(DisplayImpl d) {
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
    display = d;
    Index = Instance = -1;
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
      Vector clv = null;
      synchronized (ListenerVector) {
        clv = (Vector) ListenerVector.clone();
      }
      Enumeration listeners = clv.elements();
      while (listeners.hasMoreElements()) {
        ControlListener listener =
          (ControlListener) listeners.nextElement();
        listener.controlChanged(new ControlEvent(this));
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

  // WLH 6 Aug 2001
  /** end this control */
  public void nullControl() {
    ListenerVector.removeAllElements();
  }

  /** invoke incTick every time Control changes */
  public long incTick() {
    NewTick += 1;
    if (NewTick == Long.MAX_VALUE) NewTick = Long.MIN_VALUE + 1;
    if (display != null) display.controlChanged();
// System.out.println(getClass().getName() + "  set  NewTick = " + NewTick);
    return NewTick;
  }

  /** set tickFlag according to OldTick and NewTick */
  public synchronized void setTicks() {
    if (isSet) return; // WLH 22 Aug 99
    isSet = true;
    tickFlag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
// if (tickFlag) System.out.println(getClass().getName() + "  set  tickFlag = " + tickFlag);
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
boolean flag = (tickFlag && r.isTransformControl(this, link)) || subCheckTicks(r, link);
if (tickFlag) {
  System.out.println(getClass().getName() + "  check  tickFlag = " + tickFlag +
                   " trans = " + r.isTransformControl(this, link) + " sub = " +
                   subCheckTicks(r, link));
}
*/
    return (tickFlag && r.isTransformControl(this, link)) || subCheckTicks(r, link);
  }

  /** reset tickFlag */
  public synchronized void resetTicks() {
// if (tickFlag) System.out.println(getClass().getName() + "  reset");
    tickFlag = false;
    subResetTicks();
    isSet = false;
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

  /** used by java3d.AnimationControlJ3D and
      java2d.AnimationControlJ2D */
  public void animation_string(RealType real, Set set, double value,
              int current) throws VisADException {
    if (set != null) {
      Unit[] units = set.getSetUnits();

      // WLH 31 Aug 2000
      Vector tmap = display.getMapVector();
      Unit overrideUnit = null;
      for (int i=0; i<tmap.size(); i++) {
        ScalarMap map = (ScalarMap) tmap.elementAt(i);
        Control c = map.getControl();
        if (this.equals(c)) {
          overrideUnit = map.getOverrideUnit();
        }
      }
      // units not part of Time string
      if (overrideUnit != null && units != null &&
          !overrideUnit.equals(units[0]) && 
          (!Unit.canConvert(units[0], CommonUnit.secondsSinceTheEpoch) ||
           units[0].getAbsoluteUnit().equals(units[0]))) {
        value = overrideUnit.toThis(value, units[0]);
        units[0] = overrideUnit;
      }
  
      String s = real.getName() + " = " +
        new Real(real, value, units == null ? null : units[0]).toValueString();
      String t = Integer.toString(current+1) + " of " +
                 Integer.toString(set.getLength());
      getDisplayRenderer().setAnimationString(new String[] {s, t});
    } else { // null set
      getDisplayRenderer().setAnimationString(new String[] {null, null});
    }
  }

  void setIndex(int index) {
    Index = index;
  }

  int getIndex() {
    return Index;
  }

  void setInstanceNumber(int instance) {
    Instance = instance;
  }

  public int getInstanceNumber() {
    return Instance;
  }

  public DisplayImpl getDisplay() {
    return display;
  }

  /**
   * @return String representation of this Control
   */
  public abstract String getSaveString();

  /**
   * reconstruct this Control using the specified save string
   * @param save - String representation for reconstruction
   * @throws VisADException if a VisAD error occurs
   * @throws RemoteException if an RMI error occurs
   */
  public abstract void setSaveString(String save)
    throws VisADException, RemoteException;

  /**
   * copy the state of a remote control to this control
   * @param rmt remote Control whose state is copied
   * @throws VisADException if a VisAD error occurs
   */
  public abstract void syncControl(Control rmt)
    throws VisADException;

  public Object clone()
  {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  public boolean equals(Object o)
  {
    if (o == null || !getClass().isInstance(o)) {
      return false;
    }

    return (Instance == ((Control )o).Instance);
  }

  public String toString()
  {
    String cn = getClass().getName();
    int pt = cn.lastIndexOf('.');
    final int ds = cn.lastIndexOf('$');
    if (ds > pt) {
      pt = ds;
    }
    if (pt == -1) {
      pt = 0;
    } else {
      pt++;
    }
    return cn.substring(pt) + "@" + Index + "#" + Instance;
  }

}
