
//
// AnimationControl.java
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

package visad;

import java.rmi.*;

/**
   AnimationControl is the VisAD interface for controlling Animation
   display scalars.<P>
*/
public interface AnimationControl extends AVControl {

  public void stop();
 
  public void run();

  public void setCurrent(int c)
         throws VisADException, RemoteException;
 
  public void setCurrent(float value)
         throws VisADException, RemoteException;
 
  public int getCurrent();

  public void setDirection(boolean dir)
         throws VisADException, RemoteException;

  public long getStep();

  public void setStep(int st)
         throws VisADException, RemoteException;

  public void takeStep()
         throws VisADException, RemoteException;

  public void init() throws VisADException;

  public Set getSet();

  public void setSet(Set s)
         throws VisADException, RemoteException;
 
  /** changeControl(!noChange) to not trigger re-transform,
      used by ScalarMap.setRange */
  public void setSet(Set s, boolean noChange)
         throws VisADException, RemoteException;

  public boolean getOn();

  public void setOn(boolean o)
         throws VisADException, RemoteException;

  public void toggle()
         throws VisADException, RemoteException;

}

