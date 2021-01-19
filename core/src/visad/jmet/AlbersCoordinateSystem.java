//
// AlbersCoordinateSystem.java
//

/*

The software in this file is Copyright(C) 2021 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

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

This is from the site at:
<http://mathworld.wolfram.com/AlbersEqual-AreaConicProjection.html>

*/

package visad.jmet;

import visad.*;
import java.awt.geom.Rectangle2D;

public class AlbersCoordinateSystem extends visad.CoordinateSystem {
  

  private static Unit[] coordinate_system_units = {SI.meter, SI.meter};
  private CoordinateSystem c;
  private double n, C, rho0;
  private double lat0, lon0;
  private double par1, par2;
  private double false_easting, false_northing;

  private double R = 6371229; 


  /** Albers Equal Area projection
  *
  * @param la0 is the latitude of the origin (deg)
  * @param lo0 is the longitude of the origin (deg)
  * @param p1 is latitude of the first parallel (deg)
  * @param p2 is latitude of the second parallel (deg)
  * @param false_easting is the value added to the x map coordinate
  *   so that x is returned positive (m) -- may be zero...
  * @param false_northing is the value added to the y map coordinate
  *   so that y is returned positive (m) -- may be zero...
  *
  * equations taken
  * from <http://mathworld.wolfram.com/AlbersEqual-AreaConicProjection.html>
  */

  public AlbersCoordinateSystem (double la0, double lo0, double p1, double p2, double false_easting, double false_northing) throws visad.VisADException {
     
    super(RealTupleType.LatitudeLongitudeTuple, coordinate_system_units);

    lat0 = la0 * Data.DEGREES_TO_RADIANS;
    lon0 = lo0 * Data.DEGREES_TO_RADIANS;
    par1 = p1 * Data.DEGREES_TO_RADIANS;
    par2 = p2 * Data.DEGREES_TO_RADIANS;
    this.false_easting = false_easting;
    this.false_northing = false_northing;

    n = (Math.sin(par1) + Math.sin(par2) ) / 2.0;

    C = Math.pow(Math.cos(par1), 2) + 2.0 * n * Math.sin(par1);

    rho0 = R * Math.sqrt(C-2.0*n*Math.sin(lat0)) / n;

  }

  /** convert from x,y to lat,lon
  *
  * @param tuples contains the x,y coordinates (grid col, row)
  * @return tuple of (lat,lon);
  *
  */
  public double[][] toReference(double[][] tuples) throws VisADException {

    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("AlbersCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    double [][] latlon = new double[2][tuples[0].length];
    double x,y,rho,rrho0, yd, theta;

    for (int i=0; i<tuples[0].length; i++) {
      
      // tuples[0][] = x, tuples[1][] = y
      x = tuples[0][i] - false_easting;
      y = tuples[1][i] - false_northing;
      rrho0 = rho0;
      if (n < 0) {
        x = -1.0;
        y = -1.0;
        rrho0 = -1.0;
      }

      yd = rrho0 - y;
      rho = Math.sqrt( x * x + yd * yd);
      theta = Math.atan2(x, yd);
      if (n < 0) rho = rho - 1.0;

      latlon[0][i]  = Data.RADIANS_TO_DEGREES * 
           (Math.asin((C - Math.pow((rho * n / R), 2)) / (2 * n)));

      latlon[1][i]  = Data.RADIANS_TO_DEGREES * (theta / n + lon0);

    }

    return latlon;

  }

  /** convert from lat,lon to x,y
  *
  * @param tuples contains the lat,lon coordinates
  *
  */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("AlbersCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    double theta, rho;
    double [][] xy = new double[2][tuples[0].length];

    for (int i=0; i<tuples[0].length; i++) {

      rho = R * Math.sqrt(C-2.0*n*Math.sin(tuples[0][i]*Data.DEGREES_TO_RADIANS)) / n;
      theta = n * (tuples[1][i]*Data.DEGREES_TO_RADIANS - lon0);

      xy[0][i] = false_easting + rho * Math.sin(theta); 
      xy[1][i] = false_northing + rho0 - rho*Math.cos(theta);
    }

    return xy;

  }

  /** determine if the Coordinate System in question is an AlbersCoordinateSystem
  *
  */
  public boolean equals(Object cs) {
    return (cs instanceof AlbersCoordinateSystem);
  }


  public static void main(String args[] ) {
    try {

      AlbersCoordinateSystem nc = new AlbersCoordinateSystem(23., -96., 29.5, 45.5,0,0);

      double[][] latlon = new double[2][1];
      double[][] xy = new double[2][1];

      latlon[0][0] = 35.;
      latlon[1][0] = -75.;
      xy = nc.fromReference(latlon);
      System.out.println(" ll=35,75 .. x="+xy[0][0]+" y="+xy[1][0]);

      latlon = nc.toReference(xy);
      System.out.println(" inverse lat="+latlon[0][0]+" lon="+latlon[1][0]);


    } catch (Exception e) {e.printStackTrace();}

  }

}
