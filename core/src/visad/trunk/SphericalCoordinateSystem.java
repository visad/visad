
//
// SphericalCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
   SphericalCoordinateSystem is the VisAD class for cordinate
   systems for (Latitude, Longitude, Radius).<P>
*/
class SphericalCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {Unit.degree, Unit.degree, null};

  public SphericalCoordinateSystem(RealTupleType reference) throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** trusted constructor for initializers */
  SphericalCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      if (tuples[2][i] < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
        value[2][i] = Double.NaN;
      }
      else {
        double coslat = Math.cos(tuples[0][i]);
        double sinlat = Math.sin(tuples[0][i]);
        double coslon = Math.cos(tuples[1][i]);
        double sinlon = Math.sin(tuples[1][i]);
        value[0][i] = tuples[2][i] * sinlon * sinlat;
        value[1][i] = tuples[2][i] * coslon * sinlat;
        value[2][i] = tuples[2][i] * coslat;
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[2][i] = Math.sqrt(tuples[0][i] * tuples[0][i] +
                              tuples[1][i] * tuples[1][i] +
                              tuples[2][i] * tuples[2][i]);
      value[1][i] = Math.acos(tuples[2][i] / value[1][i]);
      value[0][i] = Math.atan2(tuples[1][i], tuples[0][i]);
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      if (tuples[2][i] < 0.0) {
        value[0][i] = Float.NaN;
        value[1][i] = Float.NaN;
        value[2][i] = Float.NaN;
      }
      else {
        float coslat = (float) Math.cos(tuples[0][i]);
        float sinlat = (float) Math.sin(tuples[0][i]);
        float coslon = (float) Math.cos(tuples[1][i]);
        float sinlon = (float) Math.sin(tuples[1][i]);
        value[0][i] = tuples[2][i] * sinlon * sinlat;
        value[1][i] = tuples[2][i] * coslon * sinlat;
        value[2][i] = tuples[2][i] * coslat;
      }
    }
    return value;
  }
 
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[2][i] = (float) Math.sqrt(tuples[0][i] * tuples[0][i] +
                                      tuples[1][i] * tuples[1][i] +
                                      tuples[2][i] * tuples[2][i]);
      value[1][i] = (float) Math.acos(tuples[2][i] / value[1][i]);
      value[0][i] = (float) Math.atan2(tuples[1][i], tuples[0][i]);
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof SphericalCoordinateSystem);
  }

}

