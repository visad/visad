
//
// Control.java
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

import java.util.*;
import java.rmi.*;

/*
  Control-s may be linked to direct manipulation
    Behavior.initialize()
    Behavior.setSchedulingBounds(Bounds region)
    Behavior.processStimulus(Enumeration criteria)
      change values of appropriate Control
        by invoking appropriate change...  method
        invoke Control.incTick()
    Behavior.wakeupOn(WakeupCondition criteria)
      WakeupOnAWTEvent
      WakeupOnElapsedTime

  Renderer.doAction assumes that ProjectionControl and
  AnimationControl do not trigger Renderer.doTransform
*/

/**
   Control is the VisAD superclass for controls for display scalars.<P>
*/
public abstract class Control extends Object
       implements Cloneable, java.io.Serializable {

  private long NewTick;
  private long OldTick;
  private boolean tickFlag;
  // unique Display this Control is part of
  transient DisplayImpl display;
  transient DisplayRenderer displayRenderer;
  // index of this in DisplayImpl.ControlVector
  private int Index;

  public Control() {
    this(null);
  }

  public Control(DisplayImpl d) {
    OldTick = Long.MIN_VALUE;
    NewTick = Long.MIN_VALUE + 1;
    tickFlag = false;
    display = d;
    if (display != null) displayRenderer = display.getDisplayRenderer();
  }

  /** invoked every time values of this Control change;
      this is the default and should be over-ridden by
      any subclass that needs to change the scene graph,
      such as ProjectionControl, AnimtionControl, etc */
  public void changeControl() {
    incTick();
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
  public synchronized boolean checkTicks(Renderer r, DataDisplayLink link) {
    return (tickFlag && r.isTransformControl(this, link)) || subTicks(r, link);
  }

  /** run checkTicks on any sub-Controls;
      this default for no sub-Controls */
  boolean subTicks(Renderer r, DataDisplayLink link) {
    return false;
  }
 
  /** reset tickFlag */
  synchronized void resetTicks() {
    tickFlag = false;
  }

  /** create a 'new' control for map in display;
      never gets called for ToggleControl, AnimationSetControl */
  public Control copy(ScalarMap m)
         throws VisADException, RemoteException {
    DisplayImpl d = m.getDisplay();
    if (m.getDisplayScalar().isSingle()) {
      // map m's DisplayScalar is 'single' per Display
      Control control = d.getControl(getClass());
      if (control == null) {
        control = cloneButContents(d);
        d.addControl(control);
      }
      return control;
    }
    else {
      Control control = cloneButContents(d);
      d.addControl(control);
      return control;
    }
  }

  void setIndex(int index) {
    Index = index;
  }

  int getIndex() {
    return Index;
  }

  public abstract Control cloneButContents(DisplayImpl d)
         throws VisADException, RemoteException;

}

