
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
   AnimationControl is the VisAD interface for controlling Animation
   display scalars.<P>
*/
public interface AnimationControl extends AVControl {

  public void stop();
 
  public void run();

  public void setCurrent(int c) throws VisADException;
 
  public void setDirection(boolean dir);

  public void setStep(int st) throws VisADException;

  public void takeStep() throws VisADException;

  public void init() throws VisADException;

  public Set getSet();

  public void setSet(Set s) throws VisADException;
 
  /** noChange = true to not trigger changeControl, used by
      ScalarMap.setRange */
  public void setSet(Set s, boolean noChange) throws VisADException;

  public boolean getOn();

  public void setOn(boolean o);

  public void toggle();

  public boolean subTicks(DataRenderer r, DataDisplayLink link);

}

