//
// MeasureList.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

  /** Minimum number of lines in the line pool. */
  public static final int MIN_POOL_SIZE = 10;

  /** Default group. */
  private static LineGroup defaultGroup = new LineGroup("None", Color.white);

  /** List of measurements. */
  private Vector measureList;

  /** Associated display. */
  private DisplayImpl display;

  /** Default endpoint values for line. */
  private RealTuple[] lnVals;

  /** Default endpoint values for point. */
  private RealTuple[] ptVals;

  /** Pool of lines. */
  private LinePool pool;

  /** Constructs a list of measurements. */
  public MeasureList(DisplayImpl display, FieldImpl field, LinePool pool,
    ScalarMap[] xyzMaps) throws VisADException, RemoteException
  {
    FunctionType type = (FunctionType) field.getType();
    RealTupleType domain = type.getDomain();
    Set set = field.getDomainSet();
    float[][] samples = set.getSamples(false);
    final int len = domain.getDimension();
    Real[] p1r = new Real[len];
    Real[] p2r = new Real[len];
    Real[] pxr = new Real[len];
    for (int i=0; i<len; i++) {
      RealType rt = (RealType) domain.getComponent(i);
      float s1 = samples[i][0];
      float s2 = samples[i][samples[i].length - 1];
      if (s1 != s1) s1 = 0;
      if (s2 != s2) s2 = 0;
      if (xyzMaps != null && xyzMaps.length > i && xyzMaps[i] != null) {
        xyzMaps[i].setRange(s1, s2);
      }
      p1r[i] = new Real(rt, s1);
      p2r[i] = new Real(rt, s2);
      pxr[i] = new Real(rt, (s1 + s2) / 2);
    }
    measureList = new Vector();
    this.display = display;
    lnVals = new RealTuple[2];
    lnVals[0] = new RealTuple(p1r);
    lnVals[1] = new RealTuple(p2r);
    ptVals = new RealTuple[1];
    ptVals[0] = new RealTuple(pxr);
    this.pool = pool;
    pool.expand(MIN_POOL_SIZE, domain);
  }

  /** Adds a measurement line to the measurement list. */
  public void addMeasurement() {
    addMeasurement(false);
  }

  /** Adds a measurement line or point to the measurement list. */
  public void addMeasurement(boolean point) {
    Measurement m = new Measurement(point ? ptVals : lnVals, defaultGroup);
    measureList.add(m);
    pool.add(m);
  }

  /** Removes a measurement line or point from the measurement list. */
  public void removeMeasurement(Measurement m) {
    measureList.remove(m);
    pool.set(getMeasurements());
  }

  /** Gets the list of measurements in array form. */
  public Measurement[] getMeasurements() {
    int len = measureList.size();
    Measurement[] m = new Measurement[len];
    measureList.copyInto(m);
    return m;
  }

}
