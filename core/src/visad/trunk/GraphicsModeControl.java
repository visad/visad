
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

  /** set the width of line rendering; this is over-ridden by
      ConstantMaps to Display.LineWidth */
  public abstract void setLineWidth(float width)
         throws VisADException, RemoteException;

  public abstract void setLineWidth(float width, boolean dummy);

  public abstract float getPointSize();

  /** set the size for point rendering; this is over-ridden by
      ConstantMaps to Display.PointSize */
  public abstract void setPointSize(float size)
         throws VisADException, RemoteException;

  public abstract void setPointSize(float size, boolean dummy);

  public abstract boolean getPointMode();

  /** if mode is true this will cause some rendering as points
      rather than lines or surfaces */
  public abstract void setPointMode(boolean mode)
         throws VisADException, RemoteException;

  public abstract boolean getTextureEnable();

  /** if enable is true this will enable use of texture
      mapping, where appropriate */
  public abstract void setTextureEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract boolean getScaleEnable();

  /** if enable is true this will enable numerical
      scales along display spatial axes */
  public abstract void setScaleEnable(boolean enable)
         throws VisADException, RemoteException;

  public abstract int getTransparencyMode();

  /** sets a graphics-API-specific transparency mode (e.g.,
      SCREEN_DOOR, BLENDED) */
  public abstract void setTransparencyMode(int mode)
         throws VisADException, RemoteException;

  /** sets a graphics-API-specific projection policy (e.g.,
      PARALLEL_PROJECTION, PERSPECTIVE_PROJECTION) */
  public abstract void setProjectionPolicy(int policy)
         throws VisADException, RemoteException;

  public abstract int getProjectionPolicy();

  public abstract void setMissingTransparent(boolean missing)
         throws VisADException, RemoteException;

  public abstract boolean getMissingTransparent();

  /** a method to copy any data object */
  public abstract Object clone();

}

