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
import visad.*;

/** Measurement represents the values of a measurement line or point. */
public class Measurement {

  /** Endpoint values of the measurement. */
  private RealTuple[] values;

  /** Color of the measurement line. */
  private Color color = Color.white;

  /** Group of the measurement. */
  private LineGroup group;

  /** Constructs a measurement. */
  public Measurement(RealTuple[] values, Color color, LineGroup group) {
    this.values = values;
    this.color = color;
    this.group = group;
  }

  /** Sets the measurement endpoint values. */
  public void setValues(RealTuple[] values) {
    this.values = values;
  }

  /** Sets the measurement group. */
  public void setGroup(LineGroup group) { this.group = group; }

  /** Sets the measurement line color. */
  public void setColor(Color color) { this.color = color; }

  /** Gets the measurement endpoint values. */
  public RealTuple[] getValues() { return values; }

  /** Gets the measurement group. */
  public LineGroup getGroup() { return group; }

  /** Gets the measurement line color. */
  public Color getColor() { return color; }

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

}
