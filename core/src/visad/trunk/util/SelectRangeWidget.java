//
// SelectRangeWidget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A slider widget that allows users to select a lower and upper bound.<P> */
public class SelectRangeWidget extends RangeSlider
                               implements ScalarMapListener,
                                          ControlListener
{

  /** This SelectRangeWidget's associated Control. */
  private RangeControl rangeControl;

  /** this will be labeled with the name of smap's RealType, and
      the range of RealType values defining the bounds of the
      selectable range is taken from smap.getRange(). This allows
      a SelectRangeWidget to be used with a range of values defined
      by auto-scaling from displayed Data. If smap's range values
      are not available at the time this constructor is invoked,
      the SelectRangeWidget becomes a ScalarMapListener and sets
      its range when smap's range is set.
      The DisplayRealType of smap must be Display.SelectRange and
      should already be added to a Display. */
  public SelectRangeWidget(ScalarMap smap) throws VisADException,
                                                  RemoteException {
    this(smap, true);
  }

  /** this will be labeled with the name of smap's RealType, and
      the range of RealType values (min, max) defines the
      bounds of the selectable range.
      The DisplayRealType of smap must be Display.SelectRange and
      should already be added to a Display.
      @deprecated - set range in map instead
  */
  public SelectRangeWidget(ScalarMap smap, float min, float max)
                           throws VisADException, RemoteException {
    this(smap, true);
    if (min == min || max == max) {
      System.err.println("Warning:  SelectRangeWidget initial range " +
                         " values ignored");
    }
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) and specified auto-scaling behavior.
      @deprecated - set range in map instead
  */
  public SelectRangeWidget(ScalarMap smap, float min, float max,
         boolean update) throws VisADException, RemoteException {
    this(smap, true);
    if (min == min || max == max) {
      System.err.println("Warning:  SelectRangeWidget initial range " +
                         " values ignored");
    }
  }

  /** this will be labeled with the name of smap's RealType, and
      the range of RealType values defining the bounds of the
      selectable range is taken from smap.getRange(). This allows
      a SelectRangeWidget to be used with a range of values defined
      by auto-scaling from displayed Data. If smap's range values
      are not available at the time this constructor is invoked,
      the SelectRangeWidget becomes a ScalarMapListener and sets
      its range when smap's range is set.
      The DisplayRealType of smap must be Display.SelectRange and
      should already be added to a Display. */
  public SelectRangeWidget(ScalarMap smap, boolean update)
    throws VisADException, RemoteException {
    super(RangeSlider.nameOf(smap), 0.0f, 1.0f);

    // verify scalar map
    if (!Display.SelectRange.equals(smap.getDisplayScalar())) {
      throw new DisplayException("SelectRangeWidget: ScalarMap must " +
                                 "be to Display.SelectRange");
    }

    // copy range from ScalarMap
    double[] smapRange = smap.getRange();
    float[] wr = widenRange((float) smapRange[0], (float) smapRange[1]);
    resetValues(wr[0], wr[1]);
    // resetValues((float )smapRange[0], (float )smapRange[1]); // HERE

    // get range control
    rangeControl = (RangeControl) smap.getControl();

    // enable auto-scaling
    if (update) smap.addScalarMapListener(this);
    else setBounds(minLimit, maxLimit);

    // listen for changes to the control
    rangeControl.addControlListener(this);

    // set slider values to match control's values
    float[] range = rangeControl.getRange();
    if (range == null) {
      range = new float[2];
      range[0] = minLimit;
      range[1] = maxLimit;
    }
    if (range[0] != range[0] || range[1] != range[1]) {
      range[0] = minLimit;
      range[1] = maxLimit;
    }
    setValues(range[0], range[1]);
  }

  /** Update control and graphical widget components. */
  private void updateWidget(float min, float max) throws VisADException,
                                                 RemoteException {
    rangeControl.setRange(new float[] {min, max});
    float[] wr = widenRange(min, max);
    setBounds(wr[0], wr[1]);
    // setBounds(min, max); // HERE
  }

  private float[] widenRange(float lo, float hi) {
    float newLo = lo;
    float newHi = hi;

    float widen = 0.001f * (hi - lo);
    if (Math.abs(widen) < 0.0001f) {
      return new float[] {lo - widen, hi + widen};
    }

    if( ( hi - lo ) > 0. ) {
      newLo = (float)Math.floor( lo );
      newHi = (float)Math.ceil( hi );
    }
    else {
      newLo = (float)Math.ceil( lo );
      newHi = (float)Math.floor( hi );
    }
    return new float[] { newLo, newHi };
  }

  /** ScalarMapListener method used with delayed auto-scaling. */
  public void mapChanged(ScalarMapEvent e) {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    try {
      float r0 = (float) range[0];
      float r1 = (float) range[1];

      if (minValue != minValue || !Util.isApproximatelyEqual(r0, minValue) ||
          maxValue != maxValue || !Util.isApproximatelyEqual(r1, maxValue))
      {
        updateWidget(r0, r1);
      }
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /**
   * ScalarMapListener method used to detect new control.
   */
  public void controlChanged(ScalarMapControlEvent evt)
    throws RemoteException, VisADException
  {
    int id = evt.getId();
    if (rangeControl != null && (id == ScalarMapEvent.CONTROL_REMOVED ||
                                 id == ScalarMapEvent.CONTROL_REPLACED))
    {
      rangeControl.removeControlListener(this);
    }

    if (id == ScalarMapEvent.CONTROL_REPLACED ||
        id == ScalarMapEvent.CONTROL_ADDED)
    {
      rangeControl = (RangeControl )(evt.getScalarMap().getControl());
      controlChanged(new ControlEvent(rangeControl));
      rangeControl.addControlListener(this);
    }
  }

  /** tell parent when the value changes */
  public void valuesUpdated() {
    try {
      rangeControl.setRange(new float[] {minValue, maxValue});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc ) { }
  }


  /** ControlListener method for RangeControl */
  public void controlChanged(ControlEvent e)
    throws VisADException, RemoteException
  {
    float[] range = rangeControl.getRange();
    setValues(range[0], range[1]);
  }
}

