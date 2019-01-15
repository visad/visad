/*

@(#) $Id: RGBMap.java,v 1.13 2000-02-18 20:44:03 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2019 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import java.rmi.RemoteException;

import visad.VisADException;

/**
 * A simple RGB colormap with no interpolation between the internally
 * stored values.  Click and drag with the left mouse button to draw
 * the color curves. Click with the middle or right mouse button to
 * alternate between the red, green and blue curves.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.13 $, $Date: 2000-02-18 20:44:03 $
 * @since Visad Utility Library, 0.5
 */

public class RGBMap
  extends BaseRGBMap
{
  /** Construct an RGBMap with the default resolution of 256 */
  public RGBMap()
    throws RemoteException, VisADException
  {
    super(false);
  }

  /** The RGBMap map is represented internally by an array of
   * floats
   * @param resolution the length of the array
   */
  public RGBMap(int resolution)
    throws RemoteException, VisADException
  {
    super(resolution, false);
  }

  public RGBMap(float[][] vals)
    throws RemoteException, VisADException
  {
    super(vals != null ? vals : defaultTable(DEFAULT_RESOLUTION, false));
  }
}
