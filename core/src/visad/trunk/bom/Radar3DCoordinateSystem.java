//
// Radar3DCoordinateSystem.java
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
   Radar3DCoordinateSystem is the VisAD CoordinateSystem class
   for radar (range, azimuth, elevation) with an Earth
   (Latitude, Longitude, Altitude) Reference, and with
   azimuth and elevation in degrees, and range in meters.<P>
*/
public class Radar3DCoordinateSystem extends CoordinateSystem {

  public static final double EARTH_RADIUS =
    ShadowType.METERS_PER_DEGREE * Data.RADIANS_TO_DEGREES;

  private static Unit[] coordinate_system_units =
    {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree};

  private float centlat, centlon;
  private float radlow, radres, azlow, azres, elevlow, elevres;
  private double coscentlat, lonscale, latscale;

  /** construct a CoordinateSystem for (range, azimuth, elevation)
      relative to an Earth (Latitude, Longitude, Altitude) Reference;
      this constructor supplies units =
      {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree}
      to the super constructor, in order to ensure Unit
      compatibility with its use of trigonometric functions */
  public Radar3DCoordinateSystem(RealTupleType reference, float clat, float clon,
                                 float radl, float radr, float azl, float azr,
                                 float elevl, float elevr)
         throws VisADException {
    super(reference, coordinate_system_units);
    centlat = clat;
    centlon = clon;
    radlow = radl;
    radres = radr;
    azlow = azl;
    azres = azr;
    elevlow = elevl;
    elevres = elevr;
    coscentlat = Math.cos(Data.DEGREES_TO_RADIANS * centlat);
    lonscale = ShadowType.METERS_PER_DEGREE * coscentlat;
    latscale = ShadowType.METERS_PER_DEGREE;
// System.out.println("lonscale = " + lonscale + " latscale = " + latscale);
  }

  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference double len = " + len);
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
        value[2][i] = Double.NaN;
// System.out.println(i + " missing  rad = " + rad);
      }
      else {
        double az = azlow + azres * tuples[1][i];
        double cosaz = Math.cos(Data.DEGREES_TO_RADIANS * az);
        double sinaz = Math.sin(Data.DEGREES_TO_RADIANS * az);
        // assume azimuth = 0 at north, then clockwise
        double elev = elevlow + elevres * tuples[2][i];
        double coselev = Math.cos(Data.DEGREES_TO_RADIANS * elev);
        double sinelev = Math.sin(Data.DEGREES_TO_RADIANS * elev);
        double rp = Math.sqrt(EARTH_RADIUS * EARTH_RADIUS + rad * rad +
                              2.0 * sinelev * EARTH_RADIUS * rad);
        value[2][i] = rp - EARTH_RADIUS; // altitude
        double angle = Math.asin(coselev * rad / rp); // really sin(elev+90)
        double radp = EARTH_RADIUS * angle;
        value[0][i] = centlat + cosaz * radp / latscale;
        value[1][i] = centlon + sinaz * radp / lonscale;
/*
System.out.println(tuples[0][i] + " " + tuples[1][i] + " " + tuples[2][i] +
                   " -> " +
                   value[0][i] + " " + value[1][i] + " " + value[2][i] +
                   " az, rad = " + az + " " + rad);
*/
      }
    }
    return value;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference double len = " + len);
    double[][] value = new double[3][len];
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      double radp = Math.sqrt(slat * slat + slon * slon);
      double angle = radp / EARTH_RADIUS;
      double rp = EARTH_RADIUS + tuples[2][i];
      double rad = Math.sqrt(EARTH_RADIUS * EARTH_RADIUS + rp * rp -
                             2.0 * rp * EARTH_RADIUS * Math.cos(angle));
      double elev =
        Data.RADIANS_TO_DEGREES * Math.acos(Math.sin(angle) * rp / rad);
      value[0][i] = (rad - radlow) / radres;
      value[1][i] =
        (Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres;
      value[2][i] = (elev - elevlow) / elevres;
      if (value[1][i] < 0.0) value[1][i] += 360.0;
    }
    return value;
  }

  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference float len = " + len);
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Float.NaN;
        value[1][i] = Float.NaN;
        value[2][i] = Float.NaN;
      }
      else {
        double az = azlow + azres * tuples[1][i];
        double cosaz = Math.cos(Data.DEGREES_TO_RADIANS * az);
        double sinaz = Math.sin(Data.DEGREES_TO_RADIANS * az);
        // assume azimuth = 0 at north, then clockwise
        double elev = elevlow + elevres * tuples[2][i];
        double coselev = Math.cos(Data.DEGREES_TO_RADIANS * elev);
        double sinelev = Math.sin(Data.DEGREES_TO_RADIANS * elev);
        double rp = Math.sqrt(EARTH_RADIUS * EARTH_RADIUS + rad * rad +
                              2.0 * sinelev * EARTH_RADIUS * rad);
        value[2][i] = (float) (rp - EARTH_RADIUS); // altitude
        double angle = Math.asin(coselev * rad / rp); // really sin(elev+90)
        double radp = EARTH_RADIUS * angle;
        value[0][i] = (float) (centlat + cosaz * radp / latscale);
        value[1][i] = (float) (centlon + sinaz * radp / lonscale);
      }
    }
    return value;
  }

  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference float len = " + len);
    float[][] value = new float[3][len];
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      double radp = Math.sqrt(slat * slat + slon * slon);
      double angle = radp / EARTH_RADIUS;
      double rp = EARTH_RADIUS + tuples[2][i];
      double rad = Math.sqrt(EARTH_RADIUS * EARTH_RADIUS + rp * rp -
                             2.0 * rp * EARTH_RADIUS * Math.cos(angle));
      double elev =
        Data.RADIANS_TO_DEGREES * Math.acos(Math.sin(angle) * rp / rad);
      value[0][i] = (float) ((rad - radlow) / radres);
      value[1][i] = (float)
        ((Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres);
      value[2][i] = (float) ((elev - elevlow) / elevres);
      if (value[1][i] < 0.0f) value[1][i] += 360.0f;
    }
    return value;
  }

  public boolean equals(Object cs) {
    return (cs instanceof Radar3DCoordinateSystem);
  }

}

