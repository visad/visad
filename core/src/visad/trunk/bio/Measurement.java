//
// Measurement.java
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
import javax.swing.SwingUtilities;
import visad.*;

/** Measurement represents the values of a measurement line or point. */
public class Measurement {

  /** Associated matrix of measurements. */
  private MeasureMatrix mm;

  /** Endpoint values of the measurement. */
  private RealTuple[] values;

  /** Linked MeasureThing objects. */
  private Vector things;

  /** Color of the measurement line. */
  private Color color = Color.white;

  /** Group of the measurement. */
  private LineGroup group;

  /** ID for "standard" measurement. */
  protected int stdId;

  /** Constructs a measurement. */
  public Measurement(MeasureMatrix mm, RealTuple[] values, Color color,
    LineGroup group)
  {
    this.mm = mm;
    this.values = values;
    this.things = new Vector();
    this.color = color;
    this.group = group;
    this.stdId = -1;
  }

  /** Sets the measurement endpoint values. */
  public void setValues(RealTuple[] values) {
    if (values.length != this.values.length) {
      System.err.println("Warning: measurement lengths don't match!");
      return;
    }
    double[][] vals1 = doubleValues(values);
    double[][] vals2 = doubleValues(this.values);
    int dim = vals2.length;
    int len = values.length;
    int mdim = vals1.length;

    // verify new values are distinct from old ones
    boolean equal = true;
    for (int i=0; i<mdim && equal; i++) {
      for (int j=0; j<len; j++) {
        if (vals1[i][j] != vals2[i][j]) {
          equal = false;
          break;
        }
      }
    }
    if (equal) return;

    // preserve slice values on 2-D shift
    if (mdim < dim) {
      // fill in missing slice values
      for (int i=0; i<len; i++) {
        try {
          Real[] nw = values[i].getRealComponents();
          Real[] old = this.values[i].getRealComponents();
          Real[] merged = new Real[dim];
          System.arraycopy(nw, 0, merged, 0, nw.length);
          System.arraycopy(old, nw.length, merged, nw.length, dim - nw.length);
          values[i] = new RealTuple(merged);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    }

    // manage direct manipulation in 3-D window
    if (mdim == 3) {
      // remove measurement from all slices
      int numSlices = mm.getNumberOfSlices();
      for (int i=0; i<numSlices; i++) {
        mm.getMeasureList(i).removeMeasurement(this, false);
      }

      // re-add measurement to appropriate slices
      for (int i=0; i<len; i++) {
        try {
          Real[] rc = values[i].getRealComponents();
          Real r = rc[2];
          double v = r.getValue();
          int iv = (int) Math.round(v);
          if (iv < 0) iv = 0;
          else if (iv >= numSlices) iv = numSlices - 1;
          if (v != iv) {
            // snap measurement to nearest slice
            r = new Real((RealType) r.getType(), iv, r.getUnit(), r.getError());
            values[i] = new RealTuple(new Real[] {rc[0], rc[1], r});
          }
          // add measurement to appropriate slice
          mm.getMeasureList(iv).addMeasurement(this, false);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
      }
    }

    this.values = values;
    refreshThings();
  }

  /** Links the given MeasureThings to the measurement. */
  void addThing(MeasureThing thing) {
    synchronized (things) {
      things.add(thing);
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

  /** Refreshes all MeasureThings to match the measurement. */
  protected void refreshThings() {
    synchronized (things) {
      final MeasureThing[] t = new MeasureThing[things.size()];
      things.copyInto(t);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          for (int i=0; i<t.length; i++) t[i].refresh();
        }
      });
    }
  }

  /** Sets the measurement group. */
  public void setGroup(LineGroup group) { this.group = group; }

  /** Sets the measurement line color. */
  public void setColor(Color color) {
    if (this.color.equals(color)) return;
    this.color = color;
    refreshThings();
  }

  /** Sets the measurement standard ID. */
  public void setStdId(int id) { stdId = id; }

  /** Gets the measurement endpoint values. */
  public RealTuple[] getValues() { return values; }

  /** Gets the measurement group. */
  public LineGroup getGroup() { return group; }

  /** Gets the measurement line color. */
  public Color getColor() { return color; }

  /** Gets the measurement standard ID. */
  public int getStdId() { return stdId; }

  /** Gets whether this measurement is a point (rather than a line). */
  public boolean isPoint() { return values.length == 1; }

  /** Gets the current distance between the endpoints. */
  public double getDistance() {
    double[][] vals = doubleValues();
    double sum = 0;
    for (int i=0; i<vals.length; i++) {
      double distance = vals[i][1] - vals[i][0];
      sum += distance * distance;
    }
    return Math.sqrt(sum);
  }

  /** Gets the current endpoint values as an array of doubles. */
  public double[][] doubleValues() { return doubleValues(values); }

  /** Converts the given RealTuple array to an array of doubles. */
  private static double[][] doubleValues(RealTuple[] values) {
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

  /** Gets a copy of this measurement. */
  public Object clone() {
    RealTuple[] vals = new RealTuple[values.length];
    System.arraycopy(values, 0, vals, 0, values.length);
    Measurement m = new Measurement(mm, vals, color, group);
    m.setStdId(stdId);
    return m;
  }

}
