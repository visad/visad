/*

@(#) $Id: RGBMap.java,v 1.12 1999-08-24 23:09:21 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * A simple RGB colormap with no interpolation between the internally
 * stored values.  Click and drag with the left mouse button to draw
 * the color curves. Click with the middle or right mouse button to
 * alternate between the red, green and blue curves.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.12 $, $Date: 1999-08-24 23:09:21 $
 * @since Visad Utility Library, 0.5
 */

public class RGBMap
  extends BaseRGBMap
{
  /** Construct an RGBMap with the default resolution of 256 */
  public RGBMap() {
    super(false);
  }

  /** The RGBMap map is represented internally by an array of
   * floats
   * @param resolution the length of the array
   */
  public RGBMap(int resolution) {
    super(resolution, false);
  }

  public RGBMap(float[][] vals) {
    super (vals, false);
  }
}
