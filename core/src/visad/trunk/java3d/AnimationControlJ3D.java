
//
// AnimationControlJ3D.java
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

package visad.java3d;
 
import visad.*;

import java.rmi.*;

/**
   AnimationControlJ3D is the VisAD class for controlling Animation
   display scalars under Java3D.<P>

   WLH - manipulate a list of Switch nodes in scene graph.<P>
*/
public class AnimationControlJ3D extends AVControlJ3D
       implements Runnable, AnimationControl {

  private int current;
  private boolean direction; // true = forward
  private long step; // time in milleseconds between animation steps
  private AnimationSetControl animationSet;
  private ToggleControl animate;

  private boolean alive = true;

  /** AnimationControlJ3D is Serializable, mark as transient */
  private transient Thread animationThread;

  public AnimationControlJ3D(DisplayImplJ3D d) {
    super(d);
    if (d != null) {
      animationThread = new Thread(this);
      animationThread.start();
    }
    current = 0;
    direction = true;
    step = 100;
    animationSet = new AnimationSetControl(d, this);
    d.addControl(animationSet);
    animate = new ToggleControl(d, this);
    d.addControl(animate);
  }

  AnimationControlJ3D() {
    this(null);
  }

  public void stop() {
    animationThread = null;
    alive = false;
  }
 
  public void run() {
    while (alive) {
      try {
        if (animate.getOn()) {
          takeStep();
        }
      }
      catch(VisADException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControlJ3D.run: " + v.toString());
      }
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
      throw new DisplayException("AnimationControlJ3D.setStep: step must be > 0");
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

  public void init() throws VisADException {
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
  public void setSet(Set s, boolean noChange) throws VisADException {
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

  public boolean subTicks(DataRenderer r, DataDisplayLink link) {
    if (animationSet != null) {
      return animationSet.checkTicks(r, link) || animate.checkTicks(r, link);
    }
    else {
      return false;
    }
  }

}

