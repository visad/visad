//
// MeasureList.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

  /** Associated matrix of measurements. */
  private MeasureMatrix mm;

  /** List of measurements. */
  private Vector measureList;

  /** Default endpoint values for line. */
  private RealTuple[] lnVals;

  /** Default endpoint values for point. */
  private RealTuple[] ptVals;

  /** Slice of measurements on this list. */
  private int slice;

  /** RealTypes for measurements. */
  private RealType[] types;

  /** Pool of measurements. */
  private MeasurePool pool;

  /** Pool of measurements for 3-D display. */
  private MeasurePool pool3d;

  /** Constructs a list of measurements. */
  public MeasureList(MeasureMatrix mm, Real[] p1r, Real[] p2r, Real[] pxr,
    int slice, MeasurePool pool, MeasurePool pool3d)
    throws VisADException, RemoteException
  {
    this.mm = mm;
    measureList = new Vector();
    types = new RealType[p1r.length];
    for (int i=0; i<p1r.length; i++) types[i] = (RealType) p1r[i].getType();
    lnVals = new RealTuple[2];
    lnVals[0] = new RealTuple(p1r);
    lnVals[1] = new RealTuple(p2r);
    ptVals = new RealTuple[1];
    ptVals[0] = new RealTuple(pxr);
    this.slice = slice;
    this.pool = pool;
    this.pool3d = pool3d;
  }

  /** Adds a measurement line to the measurement list. */
  public void addMeasurement() { addMeasurement(false); }

  /** Adds a measurement (line or point) to the measurement list. */
  public void addMeasurement(boolean point) {
    addMeasurement(point, Color.white,
      (MeasureGroup) MeasureGroup.groups.elementAt(0));
  }

  /** Adds a measurement (line or point) to the measurement list. */
  public void addMeasurement(boolean point, Color color, MeasureGroup group) {
    RealTuple[] vals = point ? ptVals : lnVals;
    Measurement m =
      new Measurement(point ? ptVals : lnVals, color, group);
    addMeasurement(m, true);
  }

  /** Adds a measurement to the measurement list. */
  void addMeasurement(Measurement m, boolean updatePool) {
    if (measureList.contains(m)) return;
    measureList.add(m);
    if (updatePool) {
      pool.add(m);
      if (pool3d != null) pool3d.add(m);
    }
  }

  /** Removes a measurement from the measurement list. */
  public void removeMeasurement(Measurement m) { removeMeasurement(m, true); }

  void removeMeasurement(Measurement m, boolean updatePool) {
    if (!measureList.contains(m)) return;
    measureList.remove(m);
    if (updatePool) {
      Measurement[] mm = getMeasurements();
      pool.set(mm);
      if (pool3d != null) pool3d.set(mm);
    }
  }

  void removeAllMeasurements(boolean updatePool) {
    measureList.removeAllElements();
    if (updatePool) {
      Measurement[] mm = getMeasurements();
      pool.set(mm);
      if (pool3d != null) pool3d.set(mm);
    }
  }

  /** Gets the list of measurements in array form. */
  public Measurement[] getMeasurements() {
    int len = measureList.size();
    Measurement[] m = new Measurement[len];
    measureList.copyInto(m);
    return m;
  }

  /** Gets the RealTypes for the measurements. */
  public RealType[] getTypes() { return types; }

}
