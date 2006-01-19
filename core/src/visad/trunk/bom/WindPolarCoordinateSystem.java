//
// WindPolarCoordinateSystem.java
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

package visad.bom;

import visad.*;

/**
   WindPolarCoordinateSystem is the VisAD CoordinateSystem class
   for (Longitude, Radius) with a Cartesian Reference,
   and with Longitude in degrees.<P>
*/
public class WindPolarCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.degree, CommonUnit.meterPerSecond};

  /** construct a CoordinateSystem for (longitude, radius)
      relative to a 2-D Cartesian reference;
      this constructor supplies units =
      {CommonUnit.Degree, CommonUnit.meterPerSecond}
      to the super constructor, in order to ensure Unit
      compatibility with its use of trigonometric functions */
  public WindPolarCoordinateSystem(RealTupleType reference)
         throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** constructor to set units */
  public WindPolarCoordinateSystem(RealTupleType reference, Unit[] units)
         throws VisADException {
    super(reference, units);
  }

  /** simple constructor for "static" conversions */
  public WindPolarCoordinateSystem() throws VisADException {
    this(new RealTupleType(RealType.Longitude, RealType.Radius));
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("WindPolarCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      if (tuples[1][i] < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
      }
      else {
        double coslon = Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        double sinlon = Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        value[0][i] = -tuples[1][i] * sinlon;
        value[1][i] = -tuples[1][i] * coslon;
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("WindPolarCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      value[1][i] = Math.sqrt(tuples[0][i] * tuples[0][i] +
                              tuples[1][i] * tuples[1][i]);
      value[0][i] =
        Data.RADIANS_TO_DEGREES * Math.atan2(-tuples[0][i], -tuples[1][i]);
      if (value[0][i] < 0.0) value[0][i] += 360.0;
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("WindPolarCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      if (tuples[1][i] < 0.0) {
        value[0][i] = Float.NaN;
        value[1][i] = Float.NaN;
      }
      else {
        float coslon = (float) Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        float sinlon = (float) Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        value[0][i] = -tuples[1][i] * sinlon;
        value[1][i] = -tuples[1][i] * coslon;
      }
    }
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("WindPolarCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      value[1][i] = (float) Math.sqrt(tuples[0][i] * tuples[0][i] +
                                      tuples[1][i] * tuples[1][i]);
      value[0][i] = (float)
        (Data.RADIANS_TO_DEGREES * Math.atan2(-tuples[0][i], -tuples[1][i]));
      if (value[0][i] < 0.0) value[0][i] += 360.0;
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof WindPolarCoordinateSystem);
  }

}

