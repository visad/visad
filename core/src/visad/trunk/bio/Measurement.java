//
// Measurement.java
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
import visad.util.Util;

/**
 * A Measurement object represents the values of one measurement (a collection
 * of points forming a single point, line, or higher vertex shape).
 * One Measurement object can be shared by multiple MeasureThings
 * (i.e., multiple displays), and all MeasureThings will be updated when
 * any one changes, or when the Measurement is changed directly.
 */
public class Measurement {

  // -- FIELDS --

  /** Endpoint values of the measurement. */
  private RealTuple[] values;

  /** Linked MeasureThing objects. */
  private Vector things;

  /** Color of the measurement line. */
  private Color color;

  /** Group of the measurement. */
  private MeasureGroup group;

  /** ID for "standard" measurement. */
  int stdId = -1;

  /** Flag marking whether measurement has been deleted. */
  boolean killed = false;


  // -- CONSTRUCTORS --

  /** Constructs a measurement. */
  public Measurement(RealTuple[] values) {
    this(values, Color.white, null);
  }

  /**
   * Constructs a measurement equal to the given measurement,
   * except for its Z-slice value, which is set equal to the specified value.
   */
  public Measurement(Measurement m, int slice) {
    this(BioVisAD.copy(m.values, slice), m.color, m.group);
    stdId = m.stdId;
  }

  /** Constructs a measurement with associated color and measurement group. */
  public Measurement(RealTuple[] values, Color color, MeasureGroup group) {
    this.values = values;
    this.things = new Vector();
    this.color = color;
    this.group = group;
  }


  // -- API METHODS --

  /** Sets the measurement endpoint values. */
  public void setValues(RealTuple[] values) { setValues(values, null); }

  /** Sets the measurement line color. */
  public void setColor(Color color) {
    if (this.color.equals(color)) return;
    this.color = color;
    refreshThings(null);
  }

  /** Deletes this measurement. */
  public void kill() {
    if (killed) return;
    killed = true;
    refreshThings(null);
  }

  /** Sets the measurement group. */
  public void setGroup(MeasureGroup group) { this.group = group; }

  /** Gets number of endpoints in this measurement. */
  public int getLength() { return values.length; }

  /** Gets dimensionality of each endpoint in this measurement. */
  public int getDimension() { return values[0].getDimension(); }

  /** Gets the measurement endpoint values. */
  public RealTuple[] getValues() { return values; }

  /** Gets the current distance between the endpoints. */
  public double getDistance() { return getDistance(1, 1, 1); }

  /**
   * Gets the current distance between the endpoints, using
   * the given conversion values between pixels and microns,
   * and distance between measurement slices.
   */
  public double getDistance(double mx, double my, double sd) {
    return getDistance(doubleValues(), mx, my, sd);
  }

  /** Gets the current endpoint values as an array of doubles. */
  public double[][] doubleValues() {
    int len = values.length;
    if (len < 1) return null;
    for (int i=0; i<len; i++) if (values[i] == null) return null;

    int dim = values[0].getDimension();
    double[][] vals = new double[dim][len];
    try {
      for (int i=0; i<dim; i++) {
        for (int j=0; j<len; j++) {
          Real r = (Real) values[j].getComponent(i);
          vals[i][j] = r.getValue();
        }
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    return vals;
  }

  /** Gets the measurement line color. */
  public Color getColor() { return color; }

  /** Gets the measurement group. */
  public MeasureGroup getGroup() { return group; }


  // -- INTERNAL API METHODS --

  /** Links the given MeasureThing to the measurement. */
  void addThing(MeasureThing thing) {
    synchronized (things) {
      things.add(thing);
      thing.refresh();
    }
  }

  /** Unlinks the given MeasureThing from the measurement. */
  void removeThing(MeasureThing thing) {
    synchronized (things) {
      things.remove(thing);
    }
  }

  /** Unlinks all MeasureThings from the measurement. */
  void removeAllThings() {
    synchronized (things) {
      things.removeAllElements();
    }
  }

  /** Sets the measurement endpoint values, from the given MeasureThing. */
  void setValues(RealTuple[] values, MeasureThing thing) {
    this.values = values;
    refreshThings(thing);
  }


  // -- HELPER METHODS --

  /**
   * Refreshes all MeasureThings, except the specified one,
   * to match the measurement.
   */
  private void refreshThings(MeasureThing thing) {
    MeasureThing[] t;
    synchronized (things) {
      t = new MeasureThing[things.size()];
      things.copyInto(t);
    }
    for (int i=0; i<t.length; i++) if (t[i] != thing) t[i].refresh();
  }

  /**
   * Gets the distance between the specified endpoints, using
   * the given conversion values between pixels and microns,
   * and distance between measurement slices.
   */
  static double getDistance(double[][] values,
    double mx, double my, double sd)
  {
    if (values.length != 3) return Double.NaN;
    double distx = mx * (values[0][1] - values[0][0]);
    double disty = my * (values[1][1] - values[1][0]);
    double distz = sd * (values[2][1] - values[2][0]);
    return Math.sqrt(distx * distx + disty * disty + distz * distz);
  }

}
