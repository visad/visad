
//
// RemoteGraphicsModeControl.java
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
 
import java.rmi.Remote;
import java.rmi.RemoteException;
 
/**
   RemoteGraphicsModeControl is the interface for Remote GraphicsModeControl-s.
*/
public interface RemoteGraphicsModeControl extends RemoteControl
{
  /** return 'true' if the display is being rendered as a 2D object */
  public abstract boolean getMode2D() throws VisADException, RemoteException;

  /** get the width of line rendering */
  public abstract float getLineWidth() throws VisADException, RemoteException;

  /** get the size for point rendering */
  public abstract float getPointSize() throws VisADException, RemoteException;

  /** if true, this will cause some rendering as points
      rather than lines or surfaces */
  public abstract boolean getPointMode() throws VisADException, RemoteException;

  /** if true, this will enable use of texture mapping, where appropriate */
  public abstract boolean getTextureEnable() throws VisADException, RemoteException;

  /** if true, this will enable numerical scales along display spatial axes */
  public abstract boolean getScaleEnable() throws VisADException, RemoteException;

  /** get graphics-API-specific transparency mode
      (e.g., SCREEN_DOOR, BLENDED) */
  public abstract int getTransparencyMode() throws VisADException, RemoteException;

  /** get graphics-API-specific projection policy
      (e.g., PARALLEL_PROJECTION, PERSPECTIVE_PROJECTION) */
  public abstract int getProjectionPolicy() throws VisADException, RemoteException;

  public abstract int getPolygonMode() throws VisADException, RemoteException;

  public abstract boolean getMissingTransparent()
         throws VisADException, RemoteException;

  public abstract int getCurvedSize()
         throws VisADException, RemoteException;

}
