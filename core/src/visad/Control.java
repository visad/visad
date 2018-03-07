//
// Control.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
   Control is the abstract VisAD superclass for controls for
   DisplayRealTypes.<P>
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

  /**
   * construct a Control for the given DisplayImpl
   * @param d - DisplayImpl this Control is associated with
   */
  public Control(DisplayImpl d) {
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
    display = d;
    Index = Instance = -1;
    if (display != null) displayRenderer = display.getDisplayRenderer();
  }

  /**
   * @return DisplayRenderer assciated with this Control
   */
  public DisplayRenderer getDisplayRenderer() {
    return displayRenderer;
  }

  /**
   * invoked every time values of this Control change
   * @param tick  true to notify the Display for possible re-transform
   */
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

  /**
   * add a ControlListener
   * @param listener  ControlListener to add
   */
  public void addControlListener(ControlListener listener) {
    ListenerVector.addElement(listener);
  }

  /**
   * remove a ControlListener
   * @param listener  ControlListener to remove
   */
  public void removeControlListener(ControlListener listener) {
    if (listener != null) {
      ListenerVector.removeElement(listener);
    }
  }

  /**
   * end this control (called by ScalarMap.nullDisplay())
   */
  public void nullControl() {
    ListenerVector.removeAllElements();
  }

  /**
   * increment long counter NewTick: NewTick > OldTick indicates
   * that there is event in this Control that the DisplayImpl
   * must process;
   * this method is invoked every time Control changes
   * @return incremented value of NewTick counter
   */
  public long incTick() {
    NewTick += 1;
    if (NewTick == Long.MAX_VALUE) NewTick = Long.MIN_VALUE + 1;
    if (display != null) display.controlChanged();
// System.out.println(getClass().getName() + "  set  NewTick = " + NewTick);
    return NewTick;
  }

  /**
   * set tickFlag if NewTick > OldTick, and reset OldTick = NewTick
   * also invoke subSetTicks() to propagate to any sub-Controls
   */
  public synchronized void setTicks() {
    if (isSet) return; // WLH 22 Aug 99
    isSet = true;
    tickFlag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
// if (tickFlag) System.out.println(getClass().getName() + "  set  tickFlag = " + tickFlag);
    OldTick = NewTick;
    subSetTicks();
  }

  /**
   * peek at future value of checkTicks()
   * @param r DataRenderer to check if changes to this Control
   *          require re-transform
   * @param link DataDisplayLink involved in decision whether
   *             changes to this Control require re-transform
   * @return true if checkTicks() will return true after next setTicks()
   */
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

  /**
   * check if this Control changed and requires re-Transform
   * @param r DataRenderer to check if changes to this Control 
   *          require re-transform
   * @param link DataDisplayLink involved in decision whether
   *             changes to this Control require re-transform
   * @return true if Control changed and requires re-Transform
   */
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

  /**
   * reset tickFlag and propagate to sub-Controls
   */
  public synchronized void resetTicks() {
// if (tickFlag) System.out.println(getClass().getName() + "  reset");
    tickFlag = false;
    subResetTicks();
    isSet = false;
  }

  /**
   * run setTicks on any sub-Controls;
   * this default for no sub-Controls
   */
  public void subSetTicks() {
  }

  /**
   * run checkTicks on any sub-Controls
   * this default for no sub-Controls
   * @param r DataRenderer to check if changes to this Control
   *          require re-transform
   * @param link DataDisplayLink involved in decision whether
   *             changes to this Control require re-transform
   * @return 'logical or' of values from checkTicks on sub-Controls
   */
  public boolean subCheckTicks(DataRenderer r, DataDisplayLink link) {
    return false;
  }

  /**
   * run peekTicks on any sub-Controls
   * this default for no sub-Controls
   * @param r DataRenderer to check if changes to this Control
   *          require re-transform
   * @param link DataDisplayLink involved in decision whether
   *             changes to this Control require re-transform
   * @return 'logical or' of values from peekTicks on sub-Controls
   */
  public boolean subPeekTicks(DataRenderer r, DataDisplayLink link) {
    return false;
  }

  /**
   * run resetTicks on any sub-Controls
   * this default for no sub-Controls
   */
  public void subResetTicks() {
  }

  /**
   * build String representation of current animation step
   * and pass it to DisplayRenderer.setAnimationString()
   * called by java3d.AnimationControlJ3D and java2d.AnimationControlJ2D
   * @param real - RealType mapped to Display.Animation
   * @param set - Set from AnimationSetControl
   * @param value - real value associated with current animation step
   * @param current - index of current animation step
   * @throws VisADException a VisAD error occurred
   */
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

  /**
   * set index of this Control in display.ControlVector
   * @param index new value to index to set
   */
  void setIndex(int index) {
    Index = index;
  }

  /**
   * @return index of this Control in display.ControlVector
   */
  int getIndex() {
    return Index;
  }

  /**
   * set 'instance number' (index + 1 ?) of this Control
   * in display.ControlVector
   * @param instance new value to 'instance number' to set
   */
  void setInstanceNumber(int instance) {
    Instance = instance;
  }

  /**
   * @return 'instance number' (index + 1 ?) of this Control
   *         in display.ControlVector
   */
  public int getInstanceNumber() {
    return Instance;
  }

  /**
   * @return DisplayImpl associated with this Control
   */
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

  /**
   * @return a copy of this Control
   */
  public Object clone()
  {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }

  /**
   * Indicates whether or not this instance equals an Object
   * @param o  an Object
   * @return true if and only if this instance is equal to o
   */
  public boolean equals(Object o)
  {
    if (o == null || !getClass().isInstance(o)) {
      return false;
    }

    return (Instance == ((Control )o).Instance);
  }

  /**
   * @return a simple String representation of this Control
   */
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
