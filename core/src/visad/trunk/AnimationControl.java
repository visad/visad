//
// AnimationControl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

  /** Set the animation direction.
   *
   *  @param    dir     true for forward, false for backward
   */
  void setDirection(boolean dir)
         throws VisADException, RemoteException;

  /** Get the animation direction.
   *
   *  @return	true for forward, false for backward 
   */
  boolean getDirection();

  long getStep();

  /** set the dwell time for each step, in milliseconds */
  void setStep(int st)
         throws VisADException, RemoteException;

  /** advance one step (forward or backward) */
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

  /** turn on automatic stepping if on = true, turn it
      off if on = false */
  void setOn(boolean o)
         throws VisADException, RemoteException;

  /** toggle automatic stepping between off and on */
  void toggle()
         throws VisADException, RemoteException;

}

