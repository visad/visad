//
// AnimationControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

/**
   AnimationControl is the VisAD interface for controlling Animation
   display scalars. Current implementations also implement Runnable<P>
*/
public interface AnimationControl extends AVControl {

  /**
   * stop activity in this AnimationControl
   */
  void stop();

  /**
   * a single invocation implements anmation behavior
   * until stop() is called
   */
  void run();

  /**
   * set the current ordinal step number
   * @param c - value for current ordinal step number
   * @throws VisADException - a VisAD error occurred
   * @throws RemoteException - an RMI error occurred
   */
  void setCurrent(int c)
         throws VisADException, RemoteException;

  /**
   * set the current step by the value of the RealType mapped to
   * Display.Animation
   * @param value - RealType value that is converted to an
   *                ordinal step number
   * @throws VisADException - a VisAD error occurred
   * @throws RemoteException - an RMI error occurred
   */
  void setCurrent(double value)
         throws VisADException, RemoteException;

  /**
   * @return the current ordinal step number
   */
  int getCurrent();

  /**
   * Set the animation direction.
   *
   * @param    dir     true for forward, false for backward
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           direction remains unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  void setDirection(boolean dir)
         throws VisADException, RemoteException;

  /** Get the animation direction.
   *
   *  @return	true for forward, false for backward
   */
  boolean getDirection();

  /**
   * @return the dwell time for the current step (in ms)
   */
  long getStep();

  /**
   * @return an array of the dwell times for all the steps (in ms)
   */
  long[] getSteps();

  /**
   * Set the dwell rate between animation steps to a constant value
   *
   * @param  st   dwell time in milliseconds
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           dwell time remains unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  void setStep(int st)
         throws VisADException, RemoteException;

  /**
   * set the dwell time for individual steps.
   *
   * @param   steps   an array of dwell times in milliseconds for each
   *                  step in the animation.
   *                  If the length of the array is less than the number of
   *                  frames in the animation, the subsequent step values will
   *                  be set to the value of the last step.
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  The
   *                           dwell times remain unchanged.
   * @throws  RemoteException  Java RMI exception
   */
  void setSteps(int[] steps)
         throws VisADException, RemoteException;

  /**
   * advance one step (forward or backward)
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           step is taken.
   * @throws  RemoteException  Java RMI exception
   */
  void takeStep()
         throws VisADException, RemoteException;

  /**
   * actually set Switches (Java3D) or VisADSwitches (Java2D) to
   * child nodes corresponding to current ordinal step number
   * @throws VisADException - a VisAD error occurred.
   */
  void init() throws VisADException;

  /**
   * @return Set of RealType values for animation steps, in RealType
   * mapped to Animation
   */
  Set getSet();

  /**
   * <p>Sets the set of times in this animation control, in RealType
   * mapped to Animation. If the argument set is equal to the current
   * set, then nothing is done.</p>
   *
   * @param s                     The set of times.
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
  void setSet(Set s)
         throws VisADException, RemoteException;

  /**
   * <p>Sets the set of times in this animation control, in RealType
   * mapped to Animation. If the argument set is equal to the current
   * set, then nothing is done.</p>
   *
   * @param s                     The set of times.
   * @param noChange              changeControl(!noChange) to not trigger 
   *                              re-transform, used by ScalarMap.setRange
   * @throws VisADException       if a VisAD failure occurs.
   * @throws RemoteException      if a Java RMI failure occurs.
   */
  void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException;

  /**
   * @return true if automatic stepping is on, false otherwise
   */
  boolean getOn();

  /**
   * Set automatic stepping on or off.
   *
   * @param  o  true = turn stepping on, false = turn stepping off
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           change in automatic stepping occurs.
   * @throws  RemoteException  Java RMI exception
   */
  void setOn(boolean o)
         throws VisADException, RemoteException;

  /**
   * toggle automatic stepping between off and on
   *
   * @throws  VisADException   Couldn't create necessary VisAD object.  No
   *                           change in automatic stepping occurs.
   * @throws  RemoteException  Java RMI exception
   */
  void toggle()
         throws VisADException, RemoteException;

  /**
   * Set the flag to automatically compute the animation set if it is
   * null
   * @param compute   false to allow application to control set computation
   *                  if set is null.
   */
  void setComputeSet(boolean compute);

  /**
   * Get the flag to automatically compute the animation set if it is
   * null
   * 
   * @return true if should compute
   */
  boolean getComputeSet();
}
