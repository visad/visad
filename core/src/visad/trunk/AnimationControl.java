//
// AnimationControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
   display scalars.<P>
*/
public interface AnimationControl extends AVControl {

  void stop();

  void run();

  /** set the current ordinal step number = c */
  void setCurrent(int c)
         throws VisADException, RemoteException;

  /** set the current step by the value of the RealType
      mapped to Display.Animation */
  void setCurrent(double value)
         throws VisADException, RemoteException;

  /** get the current ordinal step number */
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
   * Return the dwell time for the current step
   */
  long getStep();

  /**
   * return an array of the dwell times for all the steps.
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
   * @param   steps   an array of dwell rates for each step in the animation
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

  void init() throws VisADException;

  /** get Set of RealType values for animation steps */
  Set getSet();

  void setSet(Set s)
         throws VisADException, RemoteException;

  /** changeControl(!noChange) to not trigger re-transform,
      used by ScalarMap.setRange */
  void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException;

  /** return true if automatic stepping is on */
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

}
