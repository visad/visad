//
// SelectRangeWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
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

  /** this will be labeled with the name of smap's RealType;
      the range of RealType values defining the bounds of the
      selectable range is taken from smap.getRange() - this allows
      a SelectRangeWidget to be used with a range of values defined
      by auto-scaling from displayed Data; if smap's range values
      are not available at the time this constructor is invoked,
      the SelectRangeWidget becomes a ScalarMapListener and sets
      its range when smap's range is set;
      the DisplayRealType of smap must be Display.SelectRange and
      should already be added to a Display */
  public SelectRangeWidget(ScalarMap smap) throws VisADException,
                                                  RemoteException {
    this(smap, Float.NaN, Float.NaN, true);
  }

  /** this will be labeled with the name of smap's RealType;
      the range of RealType values (min, max) is defines the
      bounds of the selectable range;
      the DisplayRealType of smap must be Display.SelectRange and
      should already be added to a Display */
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

  /** tell parent when the value changes */
  void valuesUpdated() {
    try {
      rangeControl.setRange(new float[] {minValue, maxValue});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc ) { }
  }

}

