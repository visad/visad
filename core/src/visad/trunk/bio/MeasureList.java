//
// MeasureList.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bio;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** MeasureList maintains a list of measurements between points in a field. */
public class MeasureList {

  /** Default group. */
  static final LineGroup DEFAULT_GROUP = new LineGroup("None");

  /** List of measurements. */
  private Vector measureList;

  /** Default endpoint values for line. */
  private RealTuple[] lnVals;

  /** Default endpoint values for point. */
  private RealTuple[] ptVals;

  /** Pool of lines. */
  private LinePool pool;

  /** Constructs a list of measurements. */
  public MeasureList(Real[] p1r, Real[] p2r, Real[] pxr, LinePool pool)
    throws VisADException, RemoteException
  {
    measureList = new Vector();
    lnVals = new RealTuple[2];
    lnVals[0] = new RealTuple(p1r);
    lnVals[1] = new RealTuple(p2r);
    ptVals = new RealTuple[1];
    ptVals[0] = new RealTuple(pxr);
    this.pool = pool;
  }

  /** Adds a measurement line to the measurement list. */
  public void addMeasurement() { addMeasurement(false); }

  /** Adds a measurement line or point to the measurement list. */
  public void addMeasurement(boolean point) {
    addMeasurement(point, Color.white, DEFAULT_GROUP);
  }

  /** Adds a measurement line or point to the measurement list. */
  public void addMeasurement(boolean point, Color color, LineGroup group) {
    Measurement m = new Measurement(point ? ptVals : lnVals, color, group);
    addMeasurement(m, true);
  }

  void addMeasurement(Measurement m, boolean updatePool) {
    measureList.add(m);
    if (updatePool) pool.add(m);
  }

  /** Removes a measurement line or point from the measurement list. */
  public void removeMeasurement(Measurement m) { removeMeasurement(m, true); }

  void removeMeasurement(Measurement m, boolean updatePool) {
    measureList.remove(m);
    if (updatePool) pool.set(getMeasurements());
  }

  /** Gets the list of measurements in array form. */
  public Measurement[] getMeasurements() {
    int len = measureList.size();
    Measurement[] m = new Measurement[len];
    measureList.copyInto(m);
    return m;
  }

}
