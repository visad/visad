/*

@(#) $Id: LabeledRGBAWidget.java,v 1.18 2001-11-27 22:30:27 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2011 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import visad.ScalarMap;
import visad.VisADException;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGBA tuples based on the Vis5D color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.18 $, $Date: 2001-11-27 22:30:27 $
 * @since Visad Utility Library v0.7.1
 * @deprecated - use LabeledColorWidget
 */
public class LabeledRGBAWidget
  extends LabeledColorWidget
{
  /** this will be labeled with the name of smap's RealType and
      linked to the ColorAlphaControl in smap;
      the range of RealType values mapped to color is taken from
      smap.getRange() - this allows a color widget to be used with
      a range of values defined by auto-scaling from displayed Data;
      if smap's range values are not available at the time this
      constructor is invoked, the LabeledRGBAWidget becomes a
      ScalarMapListener and sets its range when smap's range is set;
      the DisplayRealType of smap must be Display.RGBA and should
      already be added to a Display
      @deprecated - use LabeledColorWidget instead
   */
  public LabeledRGBAWidget(ScalarMap smap)
    throws VisADException, RemoteException
  {
    super(smap);
  }

  /** this will be labeled with the name of smap's RealType and
      linked to the ColorAlphaControl in smap;
      the range of RealType values (min, max) is mapped to color
      as defined by an interactive color widget;
      the DisplayRealType of smap must be Display.RGBA and should
      already be added to a Display
      @deprecated - use LabeledColorWidget instead
   */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max)
    throws VisADException, RemoteException
  {
    super(smap);
  }

  /** this will be labeled with the name of smap's RealType and
      linked to the ColorAlphaControl in smap;
      the range of RealType values (min, max) is mapped to color
      as defined by an interactive color widget; table initializes
      the color lookup table, organized as float[TABLE_SIZE][4]
      with values between 0.0f and 1.0f;
      the DisplayRealType of smap must be Display.RGBA and should
      already be added to a Display
      @deprecated - use LabeledColorWidget instead
   */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max,
                           float[][] table)
    throws VisADException, RemoteException
  {
    super(smap, table);
  }

  /** construct a LabeledRGBAWidget linked to the ColorAlphaControl
      in smap (which must be to Display.RGBA), with range of
      values (min, max), initial color table in format
      float[TABLE_SIZE][4] with values between 0.0f and 1.0f, and
      specified auto-scaling min and max behavior
      @deprecated - use LabeledColorWidget instead
   */
  public LabeledRGBAWidget(ScalarMap smap, float min, float max,
                           float[][] table, boolean update)
    throws VisADException, RemoteException
  {
    super(smap, table, update);
  }
}
