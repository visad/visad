
//
// GraphicsModeControl.java
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
   GraphicsModeControl is the VisAD interface class for controlling various
   mode settings for rendering.<P>

   A GraphicsModeControl is not linked to any DisplayRealType or
   ScalarMap.  It is linked to a DisplayImpl.<P>
*/
public abstract class GraphicsModeControl extends Control
       implements Cloneable {

  public GraphicsModeControl(DisplayImpl d) {
    super(d);
  }

  public abstract boolean getMode2D();

  public abstract float getLineWidth();

  public abstract void setLineWidth(float width)
         throws VisADException, RemoteException;

  public abstract float getPointSize();

  public abstract void setPointSize(float size)
         throws VisADException, RemoteException;

  public abstract boolean getPointMode();

  public abstract void setPointMode(boolean mode)
         throws VisADException, RemoteException;

  public abstract boolean getTextureEnable();

  public abstract void setTextureEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract boolean getScaleEnable();

  public abstract void setScaleEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract int getTransparencyMode();

  public abstract void setTransparencyMode(int mode)
         throws VisADException, RemoteException;

  public abstract void setProjectionPolicy(int policy)
         throws VisADException, RemoteException;

  public abstract int getProjectionPolicy();

  /** a method to copy any data object */
  public abstract Object clone();

}

