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

import java.awt.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** MeasureList maintains a list of measurements between points in a field. */
public class MeasureList {

  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** List of measurements. */
  private Vector measureList;

  /** Constructs a list of measurements. */
  public MeasureList(BioVisAD biovis) throws VisADException, RemoteException {
    bio = biovis;
    measureList = new Vector();
  }

  /** Adds a measurement line to the measurement list. */
  public void addMeasurement() { addMeasurement(2); }

  /**
   * Adds a measurement with the given number of endpoints
   * to the measurement list.
   */
  public void addMeasurement(int len) { addMeasurement(len, bio.getSlice()); }

  /**
   * Adds a measurement with the given number of endpoints
   * and Z-slice value to the measurement list.
   */
  public void addMeasurement(int len, int slice) {
    addMeasurement(len, slice, Color.white,
      (MeasureGroup) bio.groups.elementAt(0));
  }

  /**
   * Adds a measurement with the given number of endpoints,
   * Z-slice value, color and group to the measurement list.
   */
  public void addMeasurement(int len, int slice,
    Color color, MeasureGroup group)
  {
    // generate some random acceptable endpoint start locations; endpoints
    // are equally spaced points around a circle on the given Z-slice value
    Dimension size = bio.display2.getComponent().getSize();
    double[] e1 = bio.pool2.pixelToDomain(0, 0);
    double[] e2 = bio.pool2.pixelToDomain(size.width, size.height);
    double cx = (e1[0] + e2[0]) / 2;
    double cy = (e1[1] + e2[1]) / 2;
    double rx = Math.abs(e2[0] - cx);
    double ry = Math.abs(e2[1] - cy);
    double r = 0.75 * (rx < ry ? rx : ry);
    double theta = 2 * Math.PI * Math.random();
    double inc = 2 * Math.PI / len;
    RealTuple[] tuples = new RealTuple[len];
    for (int i=0; i<len; i++) {
      double t = theta + i * inc;
      Real[] reals = {
        new Real(bio.dtypes[0], r * Math.cos(t) + cx),
        new Real(bio.dtypes[1], r * Math.sin(t) + cy),
        new Real(BioVisAD.Z_TYPE, slice)
      };
      try { tuples[i] = new RealTuple(reals); }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
    addMeasurement(new Measurement(tuples, color, group), true);
  }

  /** Adds a measurement to the measurement list. */
  void addMeasurement(Measurement m, boolean updatePool) {
    if (measureList.contains(m)) return;
    measureList.add(m);
    if (updatePool) {
      bio.pool2.add(m);
      if (bio.pool3 != null) bio.pool3.add(m);
    }
  }

  /** Removes a measurement from the measurement list. */
  public void removeMeasurement(Measurement m) {
    if (!measureList.contains(m)) return;
    measureList.remove(m);
    m.kill();
    bio.pool2.refresh();
    if (bio.pool3 != null) bio.pool3.refresh();
  }

  /** Removes all measurements, notifying the measurement pool if specified. */
  void removeAllMeasurements(boolean updatePool) {
    Measurement[] m = new Measurement[measureList.size()];
    measureList.copyInto(m);
    for (int i=0; i<m.length; i++) m[i].killed = true;
    measureList.removeAllElements();
    if (updatePool) {
      Measurement[] mm = getMeasurements();
      bio.pool2.set(mm);
      if (bio.pool3 != null) bio.pool3.set(mm);
    }
  }

  /** Gets the list of measurements in array form. */
  public Measurement[] getMeasurements() {
    int len = measureList.size();
    Measurement[] m = new Measurement[len];
    measureList.copyInto(m);
    return m;
  }

}
