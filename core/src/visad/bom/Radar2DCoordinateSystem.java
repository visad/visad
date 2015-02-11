//
// Radar2DCoordinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
   Radar2DCoordinateSystem is the VisAD CoordinateSystem class
   for radar (range, azimuth) with an Earth (Latitude, Longitude) Reference,
   and with azimuth in degrees and range in meters.<P>
*/
public class Radar2DCoordinateSystem extends NavigatedCoordinateSystem {

  private static Unit[] coordinate_system_units =
    {CommonUnit.meter, CommonUnit.degree};

  private float centlat, centlon;
  private float radlow, radres, azlow, azres;
  private double coscentlat, lonscale, latscale;

  /**
   * construct a CoordinateSystem for (range, azimuth)
   * relative to an Earth (Latitude, Longitude) Reference;
   * this constructor supplies units = {CommonUnit.meter, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range and azimuth are in terms of absolute values of range and azimuth
   * away from the center point where range is in meters and azimuth = 0 at
   * north.
   *
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar2DCoordinateSystem(float clat, float clon)
    throws VisADException {
       this(RealTupleType.LatitudeLongitudeTuple, clat, clon,
            0.0f, 1.0f, 0.0f, 1.0f);
  }

  /**
   * construct a CoordinateSystem for (range, azimuth)
   * relative to an Earth (Latitude, Longitude) Reference;
   * this constructor supplies units = {CommonUnit.meter, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range and azimuth are in terms of multiples of range and azimuth
   * resolutions away from the low value (radl, azl). The absolute
   * range is (radl + range_value * radr) and the absolute azimuth
   * is (azl + azimuth_value * azr) with azimuth = 0 at north.  This
   * allows the use of Integer2DSets for the values assuming linear
   * spacing of range and azimuth.
   *
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  radl        distance from center point for first possible echo
   *                     (meters)
   * @param  radr        distance between subsequent radials (meters)
   * @param  azl         starting azimuth (degrees)
   * @param  azr         resolution of degrees between azimuths.
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
  public Radar2DCoordinateSystem(float clat, float clon,
                               float radl, float radr, float azl, float azr)
    throws VisADException {
       this(RealTupleType.LatitudeLongitudeTuple, clat, clon,
            radl, radr, azl, azr);
  }


  /**
   * construct a CoordinateSystem for (range, azimuth)
   * relative to an Earth (Latitude, Longitude) Reference;
   * this constructor supplies units = {CommonUnit.meter, CommonUnit.degree}
   * to the super constructor, in order to ensure Unit
   * compatibility with its use of trigonometric functions.  Values
   * of range and azimuth are in terms of multiples of range and azimuth
   * resolutions away from the low value (radl, azl). The absolute
   * range is (radl + range_value * radr) and the absolute azimuth
   * is (azl + azimuth_value * azr) with azimuth = 0 at north.  This
   * allows the use of Integer2DSets for the values assuming linear
   * spacing of range and azimuth.
   *
   * @param  reference   reference RealTupleType
   *                     (should be RealTupleType.LatitudeLongitudeTuple)
   * @param  clat        latitude of center point
   * @param  clon        longitude of center point
   * @param  radl        distance from center point for first possible echo
   *                     (meters)
   * @param  radr        distance between subsequent radials (meters)
   * @param  azl         starting azimuth (degrees)
   * @param  azr         resolution of degrees between azimuths.
   *
   * @throws  VisADException   necessary VisAD object couldn't be created.
   */
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

  /**
   * Convert from range/azimuth to latitude/longitude.
   * Values input are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).
   *
   * @param  tuples  range/azimuth values
   * @return  lat/lon values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public double[][] toReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference double len = " + len);
    //double[][] value = new double[2][len];
    double[][] value = tuples;
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

  /**
   * Convert from latitude/longitude to range/azimuth.
   * Returned values are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).
   *
   * @param  tuples  lat/lon values
   * @return  range/azimuth values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference double len = " + len);
    // double[][] value = new double[2][len];
    double[][] value = tuples;
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      value[0][i] = (Math.sqrt(slat * slat + slon * slon) - radlow) / radres;
      value[1][i] =
        (Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres;
      if (value[1][i] < 0.0) value[1][i] += 360.0;
    }
    return value;
  }

  /**
   * Convert from range/azimuth to latitude/longitude.
   * Values input are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).
   *
   * @param  tuples  range/azimuth values
   * @return  lat/lon values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public float[][] toReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("toReference float len = " + len);
    // float[][] value = new float[2][len];
    float[][] value = tuples;
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

  /**
   * Convert from latitude/longitude to range/azimuth.
   * Returned values are in terms of multiples of the value resolution
   * from the low value (ex: low + value * resolution).
   *
   * @param  tuples  lat/lon values
   * @return  range/azimuth values
   *
   * @throws VisADException  tuples is null or wrong dimension
   */
  public float[][] fromReference(float[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("Radar2DCoordinateSystem." +
             "fromReference: tuples wrong dimension");
    }
    int len = tuples[0].length;
// System.out.println("fromReference float len = " + len);
    //float[][] value = new float[2][len];
    float[][] value = tuples;
    for (int i=0; i<len ;i++) {
      double slat = (tuples[0][i] - centlat) * latscale;
      double slon = (tuples[1][i] - centlon) * lonscale;
      value[0][i] = (float)
        ((Math.sqrt(slat * slat + slon * slon) - radlow) / radres);
      value[1][i] = (float)
        ((Data.RADIANS_TO_DEGREES * Math.atan2(slon, slat) - azlow) / azres);
      if (value[1][i] < 0.0) value[1][i] += 360.0f;
    }
    return value;
  }

  /**
   * Check to see if this is a Radar2DCoordinateSystem
   *
   * @param cs  object to compare
   * @return true if cs is an instance of Radar2DCoordinateSystem
   */
  public boolean equals(Object cs) {
    return (cs instanceof Radar2DCoordinateSystem);
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
   * Get center point in lat/lon
   *
   * @return latlon array  where array[0] = lat, array[1] = lon
   */
  public float[] getCenterPoint()
  {
      return new float[] {centlat, centlon};
  }

  /**
   * Return String representation of this Radar2DCoordinateSystem
   *
   * @return string listing params
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("Radar 2D CoordinateSystem: \n");
    buf.append("  Center point = Lat: ");
    buf.append(PlotText.shortString(centlat));
    buf.append(" Lon: ");
    buf.append(PlotText.shortString(centlon));
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
    return buf.toString();
  }

}
