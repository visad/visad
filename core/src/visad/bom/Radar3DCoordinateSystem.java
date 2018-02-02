//
// Radar3DCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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
import visad.georef.*;

/**
   Radar3DCoordinateSystem is the VisAD CoordinateSystem class
   for radar (range, azimuth, elevation_angle) with an Earth
   (Latitude, Longitude, Altitude) Reference, and with
   azimuth and elevation angle in degrees, and range in meters.<P>
*/
public class Radar3DCoordinateSystem extends NavigatedCoordinateSystem {

  public static final double EARTH_RADIUS =
    ShadowType.METERS_PER_DEGREE * Data.RADIANS_TO_DEGREES;

  private static Unit[] coordinate_system_units =
    {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree};

  private float centlat, centlon, centalt;
  private float radlow, radres, azlow, azres, elevlow, elevres;
  private double coscentlat, lonscale, latscale;

  /**
   * construct a CoordinateSystem for (range, azimuth, elevation_angle)
   * relative to an Earth (Latitude, Longitude, Altitude) Reference;
   * this constructor supplies units =
   * {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range, azimuth, and elevation angle are in terms of absolute values of
   * range, azimuth and elevation angle (tilt) from the center point where
   * range is in meters, azimuth = 0 at north and elevation angle is in
   * degrees.
   *
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  calt        altitude (meters) of center point
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar3DCoordinateSystem(float clat, float clon, float calt)
         throws VisADException {
    this(new RealTupleType(
             RealType.Latitude, RealType.Longitude, RealType.Altitude),
         clat, clon, calt, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
  }

  /**
   * construct a CoordinateSystem for (range, azimuth, elevation_angle)
   * relative to an Earth (Latitude, Longitude, Altitude) Reference;
   * this constructor supplies units =
   * {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range, azimuth and elevation angle are in terms of multiples of range,
   * azimuth and elevation angle resolutions away from the low values
   * (radl, azl, elevl). The absolute range is (radl + range_value * radr)
   * the absolute azimuth is (azl + azimuth_value * azr) with azimuth = 0 at
   * north and the absolute elevation angle is
   * (elevl + elevation_angle_value * elevr).  This allows the use of
   * Integer3DSets for the values assuming linear spacing of range, azimuth
   * and elevation angle.
   *
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  radl        distance from center point for first possible echo
   *                     (meters)
   * @param  radr        distance between subsequent range increments (meters)
   * @param  azl         starting azimuth (degrees)
   * @param  azr         resolution of degrees between azimuths.
   * @param  elevl       starting elevation angle (tilt) (degrees)
   * @param  elevr       resolution of degrees between elevation angles.
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar3DCoordinateSystem(float clat, float clon, float calt,
                                 float radl, float radr, float azl, float azr,
                                 float elevl, float elevr)
         throws VisADException {
    this(new RealTupleType(
             RealType.Latitude, RealType.Longitude, RealType.Altitude),
         clat, clon, calt, radl, radr, azl, azr, elevl, elevr);
  }

  /**
   * construct a CoordinateSystem for (range, azimuth, elevation_angle)
   * relative to an Earth (Latitude, Longitude, Altitude) Reference;
   * this constructor supplies units =
   * {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range, azimuth and elevation angle are in terms of multiples of range,
   * azimuth and elevation angle resolutions away from the low values
   * (radl, azl, elevl). The absolute range is (radl + range_value * radr)
   * the absolute azimuth is (azl + azimuth_value * azr) with azimuth = 0 at
   * north and the absolute elevation angle is
   * (elevl + elevation_angle_value*elevr). This allows the use of
   * Integer3DSets for the values assuming linear spacing of range, azimuth
   * and elevation angle.
   *
   * @deprecated use constructors with station altitude to get a true
   *             altitude above sea level.
   * @param  reference   reference RealTupleType
   *                     (should be Latitude, Longitude, Altitude)
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  radl        distance from center point for first possible echo
   *                     (meters)
   * @param  radr        distance between subsequent range values (meters)
   * @param  azl         starting azimuth (degrees)
   * @param  azr         resolution of degrees between azimuths.
   * @param  elevl       starting elevation angle (tilt) (degrees)
   * @param  elevr       resolution of degrees between elevation angles.
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar3DCoordinateSystem(RealTupleType reference, float clat, float clon,
                                 float radl, float radr, float azl, float azr,
                                 float elevl, float elevr)
         throws VisADException {
    this(reference, clat, clon, 0.0f, radl, radr, azl, azr, elevl, elevr);
  }

  /**
   * construct a CoordinateSystem for (range, azimuth, elevation_angle)
   * relative to an Earth (Latitude, Longitude, Altitude) Reference;
   * this constructor supplies units =
   * {CommonUnit.meter, CommonUnit.degree, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range, azimuth and elevation angle are in terms of multiples of range,
   * azimuth and elevation angle resolutions away from the low values
   * (radl, azl, elevl). The absolute range is (radl + range_value * radr)
   * the absolute azimuth is (azl + azimuth_value * azr) with azimuth = 0 at
   * north and the absolute elevation angle is
   * (elevl + elevation_angle_value*elevr). This allows the use of
   * Integer3DSets for the values assuming linear spacing of range, azimuth
   * and elevation angle.
   *
   * @param  reference   reference RealTupleType
   *                     (should be Latitude, Longitude, Altitude)
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  calt        altitude (meters) of center point
   * @param  radl        distance from center point for first possible echo
   *                     (meters)
   * @param  radr        distance between subsequent range values (meters)
   * @param  azl         starting azimuth (degrees)
   * @param  azr         resolution of degrees between azimuths.
   * @param  elevl       starting elevation angle (tilt) (degrees)
   * @param  elevr       resolution of degrees between elevation angles.
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar3DCoordinateSystem(RealTupleType reference,
                                 float clat, float clon, float calt,
                                 float radl, float radr, float azl, float azr,
                                 float elevl, float elevr)
         throws VisADException {
    super(reference, coordinate_system_units);
    centlat = clat;
    centlon = clon;
    centalt = calt;
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

  /**
   * Convert from range/azimuth/elevation to latitude/longitude/altitude.
   * Values input are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).  Returned Altitude
   * is in meters above the station elevation if this was constructed
   * without the calt parameter.
   *
   * @param  tuples  range/azimuth/elevation values
   * @return  lat/lon/altitude values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference double len = " + len);
    // double[][] value = new double[3][len];
    double[][] value = tuples;
/* WLH 7 April 2000
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
        value[2][i] = Double.NaN;
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
      }
    }
*/
    double er = EARTH_RADIUS + centalt;
    for (int i=0; i<len ;i++) {
      double rad = radlow + radres * tuples[0][i];
      if (rad < 0.0) {
        value[0][i] = Double.NaN;
        value[1][i] = Double.NaN;
        value[2][i] = Double.NaN;
      }
      else {
        double az = azlow + azres * tuples[1][i];
        double cosaz = Math.cos(Data.DEGREES_TO_RADIANS * az);
        double sinaz = Math.sin(Data.DEGREES_TO_RADIANS * az);
        // assume azimuth = 0 at north, then clockwise
        double elev = elevlow + elevres * tuples[2][i];
        double coselev = Math.cos(Data.DEGREES_TO_RADIANS * elev);
        double sinelev = Math.sin(Data.DEGREES_TO_RADIANS * elev);
        double rp = Math.sqrt(er * er + rad * rad +
                              2.0 * sinelev * er * rad);

        value[2][i] = rp - er + centalt; // altitude

        double angle = Math.asin(coselev * rad / rp); // really sin(elev+90)
        double radp = er * angle;
        value[0][i] = centlat + cosaz * radp / latscale;
        value[1][i] = centlon + sinaz * radp / lonscale;
      }
    }
    return value;
  }

  /**
   * Convert from latitude/longitude/altitude to range/azimuth/elevation.
   * Values returned are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution)
   *
   * @param  tuples  lat/lon/altitude values
   * @return  range/azimuth/elevation values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference double len = " + len);
    // double[][] value = new double[3][len];
    double[][] value = tuples;
/* WLH 7 April 2000
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
*/
    double er = EARTH_RADIUS + centalt;
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      double radp = Math.sqrt(slat * slat + slon * slon);
      double angle = radp / er;

      double alt_over = tuples[2][i] - centalt;
      double rp = er + alt_over;

      double rad = Math.sqrt(er * er + rp * rp -
                             2.0 * rp * er * Math.cos(angle));
      double elev =
        Data.RADIANS_TO_DEGREES * Math.acos(Math.sin(angle) * rp / rad);
      if (alt_over < 0.0f) elev = -elev;
      value[0][i] = (rad - radlow) / radres;
      value[1][i] =
        (Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres;
      value[2][i] = (elev - elevlow) / elevres;
      if (value[1][i] < 0.0) value[1][i] += 360.0;
    }

    return value;
  }

  /**
   * Convert from range/azimuth/elevation to latitude/longitude/altitude.
   * Values input are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).  Returned Altitude
   * is in meters above the station elevation if this was constructed
   * without the calt parameter.
   *
   * @param  tuples  range/azimuth/elevation values
   * @return  lat/lon/altitude values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference float len = " + len);
    // float[][] value = new float[3][len];
    float[][] value = tuples;
/* WLH 7 April 2000
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
*/
    double er = EARTH_RADIUS + centalt;
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
        double rp = Math.sqrt(er * er + rad * rad +
                              2.0 * sinelev * er * rad);

        value[2][i] = (float) (rp - er) + centalt; // altitude

        double angle = Math.asin(coselev * rad / rp); // really sin(elev+90)
        double radp = er * angle;
        value[0][i] = (float) (centlat + cosaz * radp / latscale);
        value[1][i] = (float) (centlon + sinaz * radp / lonscale);
      }
    }
    return value;
  }

  /**
   * Convert from latitude/longitude/altitude to range/azimuth/elevation.
   * Values returned are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution)
   *
   * @param  tuples  lat/lon/altitude values
   * @return  range/azimuth/elevation values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 3) {
      throw new CoordinateSystemException("Radar3DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference float len = " + len);
    // float[][] value = new float[3][len];
    float[][] value = tuples;
/* WLH 7 April 2000
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
*/
    double er = EARTH_RADIUS + centalt;
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      double radp = Math.sqrt(slat * slat + slon * slon);
      double angle = radp / er;

      double alt_over = tuples[2][i] - centalt;
      double rp = er + alt_over;

      double rad = Math.sqrt(er * er + rp * rp -
                             2.0 * rp * er * Math.cos(angle));
      double elev =
        Data.RADIANS_TO_DEGREES * Math.acos(Math.sin(angle) * rp / rad);
      if (alt_over < 0.0f) elev = -elev;
      value[0][i] = (float) ((rad - radlow) / radres);
      value[1][i] = (float)
        ((Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres);
      value[2][i] = (float) ((elev - elevlow) / elevres);
      if (value[1][i] < 0.0f) value[1][i] += 360.0f;
    }
    return value;
  }

  /**
   * Check to see if this is a Radar3DCoordinateSystem
   *
   * @param cs  object to compare
   * @return true if cs is an instance of Radar3DCoordinateSystem
   */
  public boolean equals(Object cs) {
    return (cs instanceof Radar3DCoordinateSystem);
  }

  /**
   * Return the elevation angle parameters
   *
   * @return  array[] (len == 2) where array[0] = elevl, array[1] = elevr
   */
  public float[] getElevationParameters()
  {
      return new float[] {elevlow, elevres};
  }

  /**
   * Return the azimuth parameters
   *
   * @return  array[] (len == 2) where array[0] = azl, array[1] = azr
   */
  public float[] getAzimuthParameters()
  {
      return new float[] {azlow, azres};
  }

  /**
   * Return the range parameters
   *
   * @return  array[] (len == 2) where array[0] = radl, array[1] = radr
   */
  public float[] getRangeParameters()
  {
      return new float[] {radlow, radres};
  }

  /**
   * Get center point in lat/lon/alt
   *
   * @return latlon array  where array[0] = lat, array[1] = lon, array[2] = alt
   */
  public float[] getCenterPoint()
  {
      return new float[] {centlat, centlon, centalt};
  }

  /**
   * Return String representation of this Radar3DCoordinateSystem
   *
   * @return string listing params
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Radar 3D CoordinateSystem: \n");
    buf.append("  Center point = Lat: ");
    buf.append(PlotText.shortString(centlat));
    buf.append(" Lon: ");
    buf.append(PlotText.shortString(centlon));
    buf.append(" Alt: ");
    buf.append(PlotText.shortString(centalt));
    buf.append("\n");
    buf.append("  Range params = ");
    buf.append(PlotText.shortString(radlow));
    buf.append(",");
    buf.append(PlotText.shortString(radres));
    buf.append("\n");
    buf.append("  Azimuth params = ");
    buf.append(PlotText.shortString(azlow));
    buf.append(",");
    buf.append(PlotText.shortString(azres));
    buf.append("\n");
    buf.append("  Elevation params = ");
    buf.append(PlotText.shortString(elevlow));
    buf.append(",");
    buf.append(PlotText.shortString(elevres));
    return buf.toString();
  }
}
