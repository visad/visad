
//
// PolarCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

/**
   PolarCoordinateSystem is the VisAD CoordinateSystem class
   for (Longitude, Radius) with a Cartesian Reference,
   and with Latitude and Longitude in degrees.<P>
*/
public class PolarCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.degree, null};

  private SphericalCoordinateSystem helperSphericalCoordinateSystem;

  private RealType zreal =
    new RealType("PolarCoordinateSystem.Z", null, true);

  public PolarCoordinateSystem(RealTupleType reference)
         throws VisADException {
    super(reference, coordinate_system_units);
    RealType[] reals = {(RealType) reference.getComponent(0),
                        (RealType) reference.getComponent(1), zreal};
    RealTupleType rtt = new RealTupleType(reals); 
    helperSphericalCoordinateSystem =
      new SphericalCoordinateSystem(rtt);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("PolarCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      if (tuples[2][i] < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
      }
      else {
        double coslon = Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        double sinlon = Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        value[0][i] = tuples[1][i] * sinlon;
        value[1][i] = tuples[1][i] * coslon;
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("PolarCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      value[1][i] = Math.sqrt(tuples[0][i] * tuples[0][i] +
                              tuples[1][i] * tuples[1][i]);
      value[0][i] =
        Data.RADIANS_TO_DEGREES * Math.atan2(tuples[0][i], tuples[1][i]);
      if (value[0][i] < 0.0) value[0][i] += 180.0;
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("PolarCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      if (tuples[2][i] < 0.0) {
        value[0][i] = Float.NaN;
        value[1][i] = Float.NaN;
      }
      else {
        float coslon = (float) Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        float sinlon = (float) Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        value[0][i] = tuples[1][i] * sinlon;
        value[1][i] = tuples[1][i] * coslon;
      }
    }
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("PolarCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      value[1][i] = (float) Math.sqrt(tuples[0][i] * tuples[0][i] +
                                      tuples[1][i] * tuples[1][i]);
      value[0][i] = (float)
        (Data.RADIANS_TO_DEGREES * Math.atan2(tuples[0][i], tuples[1][i]));
      if (value[0][i] < 0.0) value[0][i] += 180.0;
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof PolarCoordinateSystem);
  }

}

