
//
// AnimationControl.java
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
   AnimationControl is the VisAD class for controlling Animation display scalars.<P>

   WLH - manipulate a list of Switch nodes in scene graph.<P>
*/
public class AnimationControl extends AVControl
       implements Runnable {

  private int current;
  private boolean direction; // true = forward
  private long step; // time in milleseconds between animation steps
  private AnimationSetControl animationSet;
  private ToggleControl animate;

  /** AnimationControl is Serializable, mark as transient */
  private transient Thread animationThread;

  static final AnimationControl prototype = new AnimationControl();

  public AnimationControl(DisplayImpl d) {
    super(d);
    if (d != null) {
      animationThread = new Thread(this);
      animationThread.start();
    }
  }

  AnimationControl() {
    this(null);
  }

  public void stop() {
    if (animationThread != null) {
      animationThread.stop();
    }
    animationThread = null;
  }
 
  public void run() {
    while (true) {
      try {
        if (animate.getOn()) {
          takeStep();
        }
      }
      catch(VisADException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControl.run: " + v.toString());
      }
/*
      catch(RemoteException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControl.run: " + v.toString());
      }
*/
      try {
        synchronized (this) {
          wait(step);
        }
      }
      catch(InterruptedException e) {
        // control doesn't normally come here
      }
    }
  }

  public void setCurrent(int c) throws VisADException {
    current = c;
    changeControl();
    if (animationSet != null) {
      init();
    }
  }
 
  public void setDirection(boolean dir) {
    direction = dir;
    changeControl();
  }

  public void setStep(int st) throws VisADException {
    if (st < 0) {
      throw new DisplayException("AnimationControl.setStep: step must be > 0");
    }
    step = st;
    changeControl();
  }

  public void takeStep() throws VisADException {
    if (direction) current++;
    else current--;
    if (animationSet != null) {
      current = animationSet.clipCurrent(current);
      init();
    }
    changeControl();
  }

  void init() throws VisADException {
    if (animationSet != null) {
      selectSwitches((double) animationSet.getValue(current));
    }
  }

  public Set getSet() {
    if (animationSet != null) {
      return animationSet.getSet();
    }
    else {
      return null;
    }
  }

  public void setSet(Set s) throws VisADException {
    setSet(s, false);
  }
 
  /** noChange = true to not trigger changeControl, used by
      ScalarMap.setRange */
  void setSet(Set s, boolean noChange) throws VisADException {
    if (animationSet != null) {
      animationSet.setSet(s, noChange);
    }
  }

  public boolean getOn() {
    if (animate != null) {
      return animate.getOn();
    }
    else {
      return false;
    }
  }

  public void setOn(boolean o) {
    if (animate != null) {
      animate.setOn(o);
    }
  }

  public void toggle() {
    if (animate != null) {
      animate.setOn(!animate.getOn());
    }
  }

  public Control cloneButContents(DisplayImpl d) {
    AnimationControl control = new AnimationControl(d);
    control.current = 0;
    control.direction = true;
    control.step = 100;
    control.animationSet = new AnimationSetControl(d, this);
    d.addControl(control.animationSet);
    control.animate = new ToggleControl(d, this);
    d.addControl(control.animate);
    return control;
  }

  boolean subTicks(Renderer r, DataDisplayLink link) {
    if (animationSet != null) {
      return animationSet.checkTicks(r, link) || animate.checkTicks(r, link);
    }
    else {
      return false;
    }
  }

}

