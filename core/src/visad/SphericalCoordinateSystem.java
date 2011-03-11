//
// SphericalCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

/**
   SphericalCoordinateSystem is the VisAD CoordinateSystem class
   for (Latitude, Longitude, Radius) with a Cartesian Reference,
   and with Latitude and Longitude in degrees.<P>
*/
public class SphericalCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.degree, CommonUnit.degree, null};

  /** construct a CoordinateSystem for (latitude, longitude,
      radius) relative to a 3-D Cartesian reference;
      this constructor supplies units =
      {CommonUnit.Degree, CommonUnit.Degree, null} to the super
      constructor, in order to ensure Unit compatibility with its
      use of trigonometric functions */
  public SphericalCoordinateSystem(RealTupleType reference)
         throws VisADException {
    super(reference, coordinate_system_units);
  }

  /** trusted constructor for initializers */
  SphericalCoordinateSystem(RealTupleType reference, boolean b) {
    super(reference, coordinate_system_units, b);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
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
        double coslat = Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        double sinlat = Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        double coslon = Math.cos(Data.DEGREES_TO_RADIANS * tuples[1][i]);
        double sinlon = Math.sin(Data.DEGREES_TO_RADIANS * tuples[1][i]);
        value[0][i] = tuples[2][i] * coslon * coslat;
        value[1][i] = tuples[2][i] * sinlon * coslat;
        value[2][i] = tuples[2][i] * sinlat;
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      value[2][i] = Math.sqrt(tuples[0][i] * tuples[0][i] +
                              tuples[1][i] * tuples[1][i] +
                              tuples[2][i] * tuples[2][i]);
      value[0][i] =
        Data.RADIANS_TO_DEGREES * Math.asin(tuples[2][i] / value[2][i]);
      value[1][i] =
        Data.RADIANS_TO_DEGREES * Math.atan2(tuples[1][i], tuples[0][i]);
      if (value[1][i] < 0.0) value[1][i] += 360.0;
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
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
        float coslat = (float) Math.cos(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        float sinlat = (float) Math.sin(Data.DEGREES_TO_RADIANS * tuples[0][i]);
        float coslon = (float) Math.cos(Data.DEGREES_TO_RADIANS * tuples[1][i]);
        float sinlon = (float) Math.sin(Data.DEGREES_TO_RADIANS * tuples[1][i]);
        value[0][i] = tuples[2][i] * coslon * coslat;
        value[1][i] = tuples[2][i] * sinlon * coslat;
        value[2][i] = tuples[2][i] * sinlat;
      }
    }
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("SphericalCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      value[2][i] = (float) Math.sqrt(tuples[0][i] * tuples[0][i] +
                                      tuples[1][i] * tuples[1][i] +
                                      tuples[2][i] * tuples[2][i]);
      value[0][i] = (float)
        (Data.RADIANS_TO_DEGREES * Math.asin(tuples[2][i] / value[2][i]));
      value[1][i] = (float)
        (Data.RADIANS_TO_DEGREES * Math.atan2(tuples[1][i], tuples[0][i]));
      if (value[1][i] < 0.0f) value[1][i] += 360.0f;
    }
    return value;
  }

  public static float[] getNormal(float[] xyz) throws VisADException {
    CoordinateSystem cs = Display.DisplaySphericalCoordSys; 
    float[][] llrA = cs.fromReference(new float[][] {{xyz[0]}, {xyz[1]}, {xyz[2]}});
    float[][] llrB = new float[][] {{llrA[0][0]}, {llrA[1][0]}, {llrA[2][0] + 1f}};
    float[][] xyzB = cs.toReference(llrB);

    float delX = xyzB[0][0] - xyz[0];
    float delY = xyzB[1][0] - xyz[1];
    float delZ = xyzB[2][0] - xyz[2];

    float mag = (float) Math.sqrt(delX*delX+delY*delY+delZ*delZ);
    return new float[] {delX/mag, delY/mag, delZ/mag};
  }

  public static float[] getUnitI(float[] xyz) throws VisADException {
    CoordinateSystem cs = Display.DisplaySphericalCoordSys;
    float[][] llrA = cs.fromReference(new float[][] {{xyz[0]}, {xyz[1]}, {xyz[2]}});
    float[][] llrB = new float[][] {{llrA[0][0]}, {llrA[1][0] + 0.05f}, {llrA[2][0]}};
    float[][] xyzB = cs.toReference(llrB);

    float delX = xyzB[0][0] - xyz[0];
    float delY = xyzB[1][0] - xyz[1];
    float delZ = xyzB[2][0] - xyz[2];

    float mag = (float) Math.sqrt(delX*delX+delY*delY+delZ*delZ);
    return new float[] {delX/mag, delY/mag, delZ/mag};
  }


  public boolean equals(Object cs) {
    return (cs instanceof SphericalCoordinateSystem);
  }

}

