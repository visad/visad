
//
// EASECoordinateSystem.java
//

/*

The software in this file is Copyright(C) 2015 by Tom Whittaker.
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
http://nsidc.org/data/ease/ease_grid.html

*/

package visad.jmet;

import visad.*;
import visad.data.hdfeos.*;
import java.awt.geom.Rectangle2D;

public class EASECoordinateSystem extends visad.georef.MapProjection {
  

  private static Unit[] coordinate_system_units = {null, null};
  private CoordinateSystem c;
  private double spac, row0, col0, scale;
  private int nrows, ncols;
  private double R = 6371.228; 
  private double cos30;


  /** ctor
  *
  * @param spacing is the spacing in km between grid points
  * @param row_origin is the row origin
  * @param column_origin is the column origin
  * @param number_rows is the number of rows
  * @param number_columns is the number of columns
  *
  */

  public EASECoordinateSystem(double spacing, double row_origin, double
      column_origin, int number_rows, int number_columns) 
      throws visad.VisADException {
     
    super(RealTupleType.LatitudeLongitudeTuple, coordinate_system_units);

    spac = spacing;
    scale = R / spac;
    row0 = row_origin;
    col0 = column_origin;
    nrows = number_rows;
    ncols = number_columns;
    cos30 = Math.cos(30. * Data.DEGREES_TO_RADIANS);

  }

  /** convert from x,y to lat,lon
  *
  * @param tuples contains the x,y coordinates (grid col, row)
  * @return tuple of (lat,lon);
  *
  */
  public double[][] toReference(double[][] tuples) throws VisADException {

    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("EASECoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    double [][] latlon = new double[2][tuples[0].length];

    for (int i=0; i<tuples[0].length; i++) {
      latlon[0][i] = -(Math.asin( (tuples[1][i] - row0)*cos30/scale))*
          Data.RADIANS_TO_DEGREES;

      latlon[1][i] = ((tuples[0][i] - col0)/(scale * cos30))*
          Data.RADIANS_TO_DEGREES;
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
      throw new CoordinateSystemException("EASECoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    double [][] xy = new double[2][tuples[0].length];

    for (int i=0; i<tuples[0].length; i++) {
      xy[0][i] = col0 + scale * tuples[1][i]*Data.DEGREES_TO_RADIANS * cos30;
      xy[1][i] = row0 - scale * Math.sin(tuples[0][i]*Data.DEGREES_TO_RADIANS)/cos30;
    }

    return xy;

  }

  /** determine if the Coordinate System in question is an EASECoordinateSystem
  *
  */
  public boolean equals(Object cs) {
    return (cs instanceof EASECoordinateSystem);
  }

  /**
   *  return the bounding box for this projection
   */
  public Rectangle2D getDefaultMapArea() { 
    return new Rectangle2D.Double(0., 0., (double)ncols, (double)nrows); 
  }

  /** return the ratio of the grid spacing between rows and columns
  */
  public double getAspectRatio() {
    return (1.0);
  }


  public static void main(String args[] ) {
    try {

      EASECoordinateSystem nc = new EASECoordinateSystem(3.13344, 1538., 3122., 721, 721);
      //EASECoordinateSystem nc = new EASECoordinateSystem(25., 292.5, 691., 586, 1383);

      double[][] latlon = new double[2][1];
      double[][] xy = new double[2][1];

      xy[0][0] = 0.;
      xy[1][0] = 0.;
      latlon = nc.toReference(xy);
      System.out.println(" (0,0) lat="+latlon[0][0]+" lon="+latlon[1][0]);

      xy[0][0] = 720.;
      xy[1][0] = 720.;
      latlon = nc.toReference(xy);
      System.out.println(" (720,720) lat="+latlon[0][0]+" lon="+latlon[1][0]);

      xy = nc.fromReference(latlon);
      System.out.println(" inverse at above, x="+xy[0][0]+" y="+xy[1][0]);

    } catch (Exception e) {e.printStackTrace();}

  }

}
