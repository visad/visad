//
// AnimationControlJ3D.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.java3d;

import visad.*;
import visad.browser.Convert;

import java.rmi.*;
import java.util.StringTokenizer;

/**
   AnimationControlJ3D is the VisAD class for controlling Animation
   display scalars under Java3D.<P>

   WLH - manipulate a list of Switch nodes in scene graph.<P>
*/
public class AnimationControlJ3D extends AVControlJ3D
       implements Runnable, AnimationControl {

  protected int current = 0;//DML: made protected so subclass can use it.
  private boolean direction; // true = forward
  private long step; // time in milliseconds between animation steps
  private long[] stepValues = {500}; // times in milliseconds between animation steps
  private transient AnimationSetControl animationSet;
  private ToggleControl animate;
  private RealType real;
  private boolean computeSet = true;

  /** AnimationControlJ3D is Serializable, mark as transient */
  private transient Thread animationThread;

  public AnimationControlJ3D(DisplayImplJ3D d, RealType r) {
    super(d);
    real = r;
    current = 0;
    direction = true;
    step = 500;
    stepValues = new long[] {step};
    animationSet = new AnimationSetControl(d, this);
    // initialize the stepValues array
    try
    {
        Set set = animationSet.getSet();
        if (set != null) stepValues = new long[set.getLength()];
    }
    catch (VisADException v) {;}
    for (int i = 0; i<stepValues.length; i++)
    {
        stepValues[i] = step;
    }
    d.addControl(animationSet);
    animate = new ToggleControl(d, this);
    d.addControl(animate);
    try {
      animate.setOn(false);
    }
    catch (VisADException v) {
    }
    catch (RemoteException v) {
    }
    if (d != null) {
      animationThread = new Thread(this);
      animationThread.start();
    }
  }

  AnimationControlJ3D() {
    this(null, null);
  }

  public void nullControl() {
    stop();
    super.nullControl();
  }

  public void stop() {
    animationThread = null;
  }

  public void run() {
    Thread me = Thread.currentThread();
    while (animationThread == me) {
      try {
        if (animate != null && animate.getOn()) {
          takeStep();
        }
      }
      catch (VisADException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControlJ3D.run: " + v.toString());
      }
      catch (RemoteException v) {
        v.printStackTrace();
        throw new VisADError("AnimationControlJ3D.run: " + v.toString());
      }
      try {
        synchronized (this) {
          if (0 <= current && current < stepValues.length) {
            wait(stepValues[current]);
          }
          else {
            wait(500);
          }
        }
      }
      catch(InterruptedException e) {
        // control doesn't normally come here
      }
    } // end while (animationThread == me)
    animationSet = null;
    animate = null;
    real = null;
  }

  public int getCurrent() {
    return current;
  }

  /** set the current ordinal step number = c */
  public void setCurrent(int c)
         throws VisADException, RemoteException {
    if (animationSet != null) {
      current = animationSet.clipCurrent(c);
      init();
    }
    else {
      current = 0;
    }
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /** set the current step by the value of the RealType
      mapped to Display.Animation */
  public void setCurrent(double value)
         throws VisADException, RemoteException {
    if (animationSet != null) {
      current = animationSet.getIndex(value);
      init();
    }
    else {
      current = 0;
    }
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /**
   * Set the animation direction.
   *
   * @param    dir     true for forward, false for backward
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           direction remains unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  public void setDirection(boolean dir)
         throws VisADException, RemoteException {
    direction = dir;
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /** Get the animation direction.
   *
   *  @return   true for forward, false for backward
   */
  public boolean getDirection()
  {
    return direction;
  }

  /**
   * Return the dwell time for the current step
   */
  public long getStep() {
    if (stepValues == null || current < 0 ||
        stepValues.length <= current) return 500;
    else return stepValues[current];
  }

  /**
   * return an array of the dwell times for all the steps.
   */
  public long[] getSteps()
  {
      return stepValues;
  }

  /**
   * set the dwell time for all steps
   *
   * @param  st   dwell time in milliseconds
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           dwell time remains unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  public void setStep(int st) throws VisADException, RemoteException {
    if (st <= 0) {
      throw new DisplayException("AnimationControlJ3D.setStep: " +
                                 "step must be > 0");
    }
    step = st;
    for (int i=0; i < stepValues.length; i++)
    {
        stepValues[i] = st;
    }
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(false);
  }

  /**
   * set the dwell time for individual steps.
   *
   * @param   steps   an array of dwell rates for each step in the animation
   *                  If the length of the array is less than the number of
   *                  frames in the animation, the subsequent step values will
   *                  be set to the value of the last step.
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           dwell times remain unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  public void setSteps(int[] steps)
         throws VisADException, RemoteException {
    // verify that the values are valid
    for (int i = 0; i < stepValues.length; i++)
    {
        stepValues[i] =
          (i < steps.length) ? steps[i] : steps[steps.length-1];
        if (stepValues[i] <= 0)
            throw new DisplayException("AnimationControlJ3D.setSteps: " +
                                 "step " + i + " must be > 0");
    }
    // WLH 5 May 2000
    // changeControl(true);
    changeControl(true);
  }

  /**
   * advance one step (forward or backward)
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           step is taken.
   * @throws  RemoteException  Java RMI exception
   */
  public void takeStep() throws VisADException, RemoteException {
    if (direction) current++;
    else current--;
    if (animationSet != null) {
      current = animationSet.clipCurrent(current);
      init();
    }
    getDisplayRenderer().render_trigger();
    changeControl(false);
  }

  public void init() throws VisADException {
    if (animationSet != null) { 
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

  /**
   * <p>Sets the set of times in this animation control.  If the argument 
   * set is equal to the current set, then nothing is done.</p>
   *
   * @param s                     The set of times.
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
  public void setSet(Set s)
         throws VisADException, RemoteException {
    if (s == null && animationSet != null && 
        animationSet.getSet() == null) return;  // check for null/null
    if (animationSet == null || s == null || 
        (s != null && !s.equals(animationSet.getSet()))) {
      setSet(s, false);
      // have to do this if animationSet == null
      if (s == null) {
        stepValues = new long[] {step};
        current = 0;
      } else if (s.getLength() != stepValues.length) {
        stepValues = new long[s.getLength()];
        for (int i = 0; i < stepValues.length; i++)
        {
          stepValues[i] = step;
        }
      }
    }
  }

  /**
   * <p>Sets the set of times in this animation control.  If the argument 
   * set is equal to the current set, then nothing is done.</p>
   *
   * @param s                     The set of times.
   * @param noChange              changeControl(!noChange) to not trigger 
   *                              re-transform, used by ScalarMap.setRange
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException {
    if (animationSet != null) {
      if (s == null) {
          stepValues = new long[] {step};
          current = 0;
      } else if (s.getLength() != stepValues.length) {
          stepValues = new long[s.getLength()];
          for (int i = 0; i < stepValues.length; i++)
          {
              stepValues[i] = step;
          }
      }
      animationSet.setSet(s, noChange);
    }
  }

  /** return true if automatic stepping is on */
  public boolean getOn() {
    if (animate != null) {
      return animate.getOn();
    }
    else {
      return false;
    }
  }

  /**
   * Set automatic stepping on or off.
   *
   * @param  o  true = turn stepping on, false = turn stepping off
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           change in automatic stepping occurs.
   * @throws  RemoteException  Java RMI exception
   */
  public void setOn(boolean o)
         throws VisADException, RemoteException {
    if (animate != null) {
      animate.setOn(o);
    }
  }

  /**
   * toggle automatic stepping between off and on
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           change in automatic stepping occurs.
   * @throws  RemoteException  Java RMI exception
   */
  public void toggle()
         throws VisADException, RemoteException {
    if (animate != null) {
      animate.setOn(!animate.getOn());
    }
  }

  public RealType getRealType() {
    return real;
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

  /** get a String that can be used to reconstruct this
      AnimationControl later */
  public String getSaveString() {
    int numSteps;
    long[] steps;
    if (stepValues == null) {
      numSteps = 1;
      steps = new long[1];
      steps[0] = 500;
    }
    else {
      numSteps = stepValues.length;
      steps = stepValues;
    }
    StringBuffer sb = new StringBuffer(35 + 12 * numSteps);
    sb.append(animate != null && animate.getOn());
    sb.append(' ');
    sb.append(direction);
    sb.append(' ');
    sb.append(current);
    sb.append(' ');
    sb.append(numSteps);
    for (int i=0; i<numSteps; i++) {
      sb.append(' ');
      sb.append((int) steps[i]);
    }
    sb.append(' ');
    sb.append(computeSet);
    return sb.toString();
  }

  /** reconstruct this AnimationControl using the specified save string */
  public void setSaveString(String save)
    throws VisADException, RemoteException
  {
    if (save == null) throw new VisADException("Invalid save string");
    StringTokenizer st = new StringTokenizer(save);
    int numTokens = st.countTokens();
    if (numTokens < 4) throw new VisADException("Invalid save string");

    // get animation settings
    boolean on = Convert.getBoolean(st.nextToken());
    boolean dir = Convert.getBoolean(st.nextToken());
    int cur = Convert.getInt(st.nextToken());
    int numSteps = Convert.getInt(st.nextToken());
    if (numSteps <= 0) {
      throw new VisADException("Number of steps is not positive");
    }
    if (numTokens < 4 + numSteps) {
      throw new VisADException("Not enough step entries");
    }
    int[] steps = new int[numSteps];
    for (int i=0; i<numSteps; i++) {
      steps[i] = Convert.getInt(st.nextToken());
      if (steps[i] <= 0) {
        throw new VisADException("Step #" + (i + 1) + "is not positive");
      }
    }
    boolean cs = st.hasMoreTokens() ? Convert.getBoolean(st.nextToken()) : getComputeSet();

    // set values
    setOn(on);
    setDirection(dir);
    setSteps(steps);
    setCurrent(cur);
    setComputeSet(cs);
  }

  /** copy the state of a remote control to this control */
  public void syncControl(Control rmt)
    throws VisADException
  {
    if (rmt == null) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with null Control object");
    }

    if (!(rmt instanceof AnimationControlJ3D)) {
      throw new VisADException("Cannot synchronize " + getClass().getName() +
                               " with " + rmt.getClass().getName());
    }

    AnimationControlJ3D ac = (AnimationControlJ3D )rmt;

    boolean changed = false;

    /* *** DON'T TRY TO SYNC CURRENT FRAME!!! *** */
    // if (current != ac.current) {
    //   changed = true;
    //   if (animationSet != null) {
    //     current = animationSet.getIndex(ac.current);
    //     canvas.renderTrigger();
    //   } else {
    //     current = 0;
    //   }
    // }
    if (direction != ac.direction) {
      changed = true;
      direction = ac.direction;
    }
    if (step != ac.step) {
      changed = true;
      step = ac.step;
    }
    if (animate != ac.animate) {
      changed = true;
      animate = ac.animate;
    }
    if (real != ac.real) {
      changed = true;
      real = ac.real;
    }

    if (computeSet != ac.computeSet) {
      changed = true;
      computeSet = ac.computeSet;
    }

    if (changed) {
      try {
        // WLH 5 May 2000
        // changeControl(true);
        changeControl(false);
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

    AnimationControlJ3D ac = (AnimationControlJ3D )o;
    /**** IGNORE FRAME POSITION ****/
    // if (current != ac.current) {
    //   return false;
    // }
    if (direction != ac.direction) {
      return false;
    }
    if (step != ac.step) {
      return false;
    }
    if (animate != ac.animate) {
      return false;
    }
    if (real != ac.real) {
      return false;
    }

    if (computeSet != ac.computeSet) {
      return false;
    }

    return true;
  }

  /**
   * Set the flag to automatically compute the animation set if it is
   * null
   * @param compute   false to allow application to control set computation
   *                  if set is null.
   */
  public void setComputeSet(boolean compute) {
      computeSet = compute;
  }

  /**
   * Get the flag to automatically compute the animation set if it is
   * null
   * 
   * @return true if should compute set automatically when null
   */
  public boolean getComputeSet() {
    return computeSet;
  }

}
