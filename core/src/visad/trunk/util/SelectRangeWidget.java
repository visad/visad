/*
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

/* AWT packages */
import java.awt.*;
import java.awt.event.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A slider widget that allows users to select a lower and upper bound.<P> */
public class SelectRangeWidget extends RangeSlider
                               implements ScalarMapListener {

  /** This SelectRangeWidget's associated Control. */
  private RangeControl rangeControl;

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      auto-scaling range values. */
  public SelectRangeWidget(ScalarMap smap) throws VisADException,
                                                  RemoteException {
    this(smap, smap.getRange(), true);
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) and auto-scaling range. */
  public SelectRangeWidget(ScalarMap smap, float min, float max)
                           throws VisADException, RemoteException {
    this(smap, new double[] {min, max}, true);
    smap.setRange((double) min, (double) max);
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) and specified auto-scaling behavior. */
  public SelectRangeWidget(ScalarMap smap, float min, float max,
         boolean update) throws VisADException, RemoteException {
    this(smap, new double[] {min, max}, update);
  }

  private SelectRangeWidget(ScalarMap smap, double[] range, boolean update)
                            throws VisADException, RemoteException {
    // if range is NaN, default to (0.0 - 1.0)
    super(range[0] == range[0] ? (float) range[0] : 0.0f,
          range[1] == range[1] ? (float) range[1] : 1.0f);
    if (range[0] != range[0]) range[0] = 0.0;
    if (range[1] != range[1]) range[1] = 1.0;

    // set auto-scaling enabled (listen for new min and max)
    if (update) smap.addScalarMapListener(this);

    // set control
    rangeControl = (RangeControl) smap.getControl();
    rangeControl.setRange(new float[] {(float) range[0], (float) range[1]});
  }

  /** ScalarMapListener method used with delayed auto-scaling. */
  public void mapChanged(ScalarMapEvent e) {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    try {
      float[] newRange = new float[] {(float) range[0], (float) range[1]};
      rangeControl.setRange(newRange);
      setBounds(newRange[0], newRange[1]);
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** Recomputes percent variables, updates control, then paints. */
  void percPaint() {
    super.percPaint();
    try {
      rangeControl.setRange(new float[] {
        0.01f*minPercent*(maxVal-minVal)+minVal,
        0.01f*maxPercent*(maxVal-minVal)+minVal
      });
    }
    catch (VisADException exc) { }
    catch (RemoteException exc ) { }
  }

}

