//
// Radar2DCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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
   Radar2DCoordinateSystem is the VisAD CoordinateSystem class
   for radar (range, azimuth) with an Earth (Latitude, Longitude) Reference,
   and with azimuth in degrees and range in meters.<P>
*/
public class Radar2DCoordinateSystem extends CoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.meter, CommonUnit.degree};

  private float centlat, centlon;
  private float radlow, radres, azlow, azres;
  private double coscentlat, lonscale, latscale;

  /** construct a CoordinateSystem for (range, azimuth)
      relative to an Earth (Latitude, Longitude) Reference;
      this constructor supplies units =
      {CommonUnit.meter, CommonUnit.degree}
      to the super constructor, in order to ensure Unit
      compatibility with its use of trigonometric functions */
  public Radar2DCoordinateSystem(RealTupleType reference, float clat, float clon,
                               float radl, float radr, float azl, float azr)
         throws VisADException {
    super(reference, coordinate_system_units);
    centlat = clat;
    centlon = clon;
    radlow = radl;
    radres = radr;
    azlow = azl;
    azres = azr;
    coscentlat = Math.cos(Data.DEGREES_TO_RADIANS * centlat);
    lonscale = ShadowType.METERS_PER_DEGREE * coscentlat;
    latscale = ShadowType.METERS_PER_DEGREE;
// System.out.println("lonscale = " + lonscale + " latscale = " + latscale);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference double len = " + len);
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
// System.out.println(i + " missing  rad = " + rad);
      }
      else {
        double az = azlow + azres * tuples[1][i];
        double cosaz = Math.cos(Data.DEGREES_TO_RADIANS * az);
        double sinaz = Math.sin(Data.DEGREES_TO_RADIANS * az);
        // assume azimuth = 0 at north, then clockwise
        value[0][i] = centlat + cosaz * rad / latscale;
        value[1][i] = centlon + sinaz * rad / lonscale;
/*
System.out.println(tuples[0][i] + " " + tuples[1][i] + " -> " +
                   value[0][i] + " " + value[1][i] +
                   " az, rad = " + az + " " + rad);
*/
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference double len = " + len);
    double[][] value = new double[2][len];
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      value[1][i] = (Math.sqrt(slat * slat + slon * slon) - radlow) / radres;
      value[0][i] = (Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres;
      if (value[0][i] < 0.0) value[0][i] += 180.0;
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference float len = " + len);
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Float.NaN;
        value[1][i] = Float.NaN;
      }
      else {
        double az = azlow + azres * tuples[1][i];
        double cosaz = Math.cos(Data.DEGREES_TO_RADIANS * az);
        double sinaz = Math.sin(Data.DEGREES_TO_RADIANS * az);
        // assume azimuth = 0 at north, then clockwise
        value[0][i] = (float) (centlat + cosaz * rad / latscale);
        value[1][i] = (float) (centlon + sinaz * rad / lonscale);
      }
    }
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference float len = " + len);
    float[][] value = new float[2][len];
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      value[1][i] = (float) ((Math.sqrt(slat * slat + slon * slon) - radlow) / radres);
      value[0][i] = (float) ((Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres);
      if (value[0][i] < 0.0) value[0][i] += 180.0f;
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof Radar2DCoordinateSystem);
  }

}

