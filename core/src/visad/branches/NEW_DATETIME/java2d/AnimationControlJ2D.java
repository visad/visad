
//
// AnimationControlJ2D.java
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

package visad.java2d;
 
import visad.*;

import java.rmi.*;

/**
   AnimationControlJ2D is the VisAD class for controlling Animation
   display scalars under Java2D.<P>

   WLH - manipulate a list of VisADSwitch nodes in scene graph.<P>
*/
public class AnimationControlJ2D extends AVControlJ2D
       implements Runnable, AnimationControl {

  private int current;
  private boolean direction; // true = forward
  private long step; // time in milleseconds between animation steps
  private AnimationSetControlJ2D animationSet;
  private ToggleControl animate;
  private RealType real;
  private boolean no_tick = false;

  private VisADCanvasJ2D canvas;

  /** AnimationControlJ2D is Serializable, mark as transient */
  private transient Thread animationThread;

  public AnimationControlJ2D(DisplayImplJ2D d, RealType r) {
    super(d);
    real = r;
    current = 0;
    direction = true;
    step = 500;
    if (d != null) {
      canvas = ((DisplayRendererJ2D) d.getDisplayRenderer()).getCanvas();
      animationSet = new AnimationSetControlJ2D(d, this);
      d.addControl(animationSet);
      animate = new ToggleControl(d, this);
      d.addControl(animate);
      synchronized (ActionImpl.threadLock) {
        DisplayImpl.delay(ActionImpl.THREAD_DELAY);
        animationThread = new Thread(this);
        animationThread.start();
      }
    }
  }

  public void stop() {
    animationThread = null;
  }
 
  public void run() {
    Thread me = Thread.currentThread();
    while (animationThread == me) {
      try {
        if (animate != null && animate.getOn() && !no_tick) {
          takeStep();
        }
      }
      catch (VisADException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControlJ2D.run: " + v.toString());
      }
      catch (RemoteException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControlJ2D.run: " + v.toString());
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

  void setNoTick(boolean nt) {
    no_tick = nt;
  }

  public int getCurrent() {
    return current;
  }

  public void setCurrent(int c)
         throws VisADException, RemoteException {
    if (animationSet != null) {
      current = animationSet.clipCurrent(c);
/* WLH 26 June 98
      init();
*/
      canvas.renderTrigger();
    }
    else {
      current = 0;
    }
    changeControl(true);
  }
 
  public void setCurrent(double value)
         throws VisADException, RemoteException {
    if (animationSet != null) {
      current = animationSet.getIndex(value);
      canvas.renderTrigger();
    }
    else {
      current = 0;
    }
    changeControl(true);
  }

  /** Set the animation direction.
   *
   *  @param	dir	true for forward, false for backward
   */
  public void setDirection(boolean dir)
         throws VisADException, RemoteException {
    direction = dir;
    changeControl(true);
  }

  /** Get the animation direction.
   *
   *  @return	true for forward, false for backward
   */
  public boolean getDirection()
  {
    return direction;
  }

  public long getStep() {
    return step;
  }

  public void setStep(int st) throws VisADException, RemoteException {
    if (st < 0) {
      throw new DisplayException("AnimationControlJ2D.setStep: " +
                                 "step must be > 0");
    }
    step = st;
    changeControl(true);
  }

  public void takeStep() throws VisADException, RemoteException {
    if (direction) current++;
    else current--;
    if (animationSet != null) {
      current = animationSet.clipCurrent(current);
/* WLH 26 June 98
      init();
*/
      canvas.renderTrigger();
/*
System.out.println("AnimationControlJ2D.takeStep: renderTrigger " +
                   "current = " + current);
*/
    }
    changeControl(true);
  }

  public void init() throws VisADException {
    if (animationSet != null &&
        animationSet.getSet() != null) {
      double value = animationSet.getValue(current);
      Set set = animationSet.getSet();

      animation_string(real, set, value, current);
      selectSwitches(value, set);
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

  public void setSet(Set s)
         throws VisADException, RemoteException {
    setSet(s, false);
  }
 
  /** changeControl(!noChange) to not trigger re-transform,
      used by ScalarMap.setRange */
  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException {
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

  public void setOn(boolean o)
         throws VisADException, RemoteException {
    if (animate != null) {
      animate.setOn(o);
    }
  }

  public void toggle()
         throws VisADException, RemoteException {
    if (animate != null) {
      animate.setOn(!animate.getOn());
    }
  }

  public void subSetTicks() {
    if (animationSet != null) {
      animationSet.setTicks();
    }
    if (animate != null) {
      animate.setTicks();
    }
  }

  public boolean subCheckTicks(DataRenderer r, DataDisplayLink link) {
    boolean flag = false;
    if (animationSet != null) {
      flag |= animationSet.checkTicks(r, link);
    }
    if (animate != null) {
      flag |= animate.checkTicks(r, link);
    }
    return flag;
  }

  public boolean subPeekTicks(DataRenderer r, DataDisplayLink link) {
    boolean flag = false;
    if (animationSet != null) {
      flag |= animationSet.peekTicks(r, link);
    }
    if (animate != null) {
      flag |= animate.peekTicks(r, link);
    }
    return flag;
  }

  public void subResetTicks() {
    if (animationSet != null) {
      animationSet.resetTicks();
    }
    if (animate != null) {
      animate.resetTicks();
    }
  }

}

