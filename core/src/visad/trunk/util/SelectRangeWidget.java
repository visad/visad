
//
// SelectRangeWidget.java
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
    this(smap, Float.NaN, Float.NaN, true);
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) and auto-scaling range. */
  public SelectRangeWidget(ScalarMap smap, float min, float max)
                           throws VisADException, RemoteException {
    this(smap, min, max, true);
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) and specified auto-scaling behavior. */
  public SelectRangeWidget(ScalarMap smap, float min, float max,
         boolean update) throws VisADException, RemoteException {
    super(RangeSlider.nameOf(smap), min == min && max == max ? min : 0.0f,
          min == min && max == max ? max : 1.0f);

    // verify scalar map
    if (!Display.SelectRange.equals(smap.getDisplayScalar())) {
      throw new DisplayException("SelectRangeWidget: ScalarMap must " +
                                 "be to Display.SelectRange");
    }
    rangeControl = (RangeControl) smap.getControl();

    // enable auto-scaling
    if (update) smap.addScalarMapListener(this);
    else {
      smap.setRange(min, max);
      updateWidget(min, max);
    }
  }

  /** Update control and graphical widget components. */
  void updateWidget(float min, float max) throws VisADException,
                                                 RemoteException {
    rangeControl.setRange(new float[] {min, max});
    setBounds(min, max);
  }

  /** ScalarMapListener method used with delayed auto-scaling. */
  public void mapChanged(ScalarMapEvent e) {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    try {
      updateWidget((float) range[0], (float) range[1]);
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

