//
// GRIBCoordinateSystem.java
//

/*

The software in this file is Copyright(C) 1999 by Tom Whittaker.
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
*/

package visad.jmet;

import visad.*;
import visad.data.hdfeos.*;
import java.awt.geom.Rectangle2D;

public class GRIBCoordinateSystem extends visad.MapProjection {

  private static Unit[] coordinate_system_units = {null, null};
  private static CoordinateSystem c;
  private static double spacing;
  private boolean isLambert=false;
  private boolean isLatLon=false;
  private double La1, Lo1, LoMax, Di, Dj;
  private double aspectRatio = 1.0;
  private Rectangle2D range;


  /** constructor for a Lambert conformal (GRIB type code = 3)
  *
  */
  public GRIBCoordinateSystem(RealTupleType ref, int gridTypeCode,
    double La1, double Lo1, double DxDy, 
    double Latin1, double Latin2, double Lov) 
                   throws VisADException {

    super(ref, coordinate_system_units);

    if (gridTypeCode == 3) {
      doLambert(ref, La1, Lo1, DxDy, Latin1, Latin2, Lov);

      } else {
        System.out.println("GRIB Grid type not Lambert = "+gridTypeCode);
        throw new VisADException
               ("Invalid grid type for Lambert = "+gridTypeCode);
      }

  }

  /** constructor for a latitude-longitude (GRIB type code = 0)
  *
  */
  public GRIBCoordinateSystem(RealTupleType ref, int gridTypeCode,
    int Ni, int Nj, double La1, double Lo1, double La2, double Lo2,
    double Di, double Dj) throws VisADException {

    super(ref, coordinate_system_units);

    if (gridTypeCode == 0) {
      doLatLon(ref, Ni, Nj, La1, Lo1, La2, Lo2, Di, Dj) ;

      } else {
        System.out.println("GRIB Grid type not Lat/Lon = "+gridTypeCode);
        throw new VisADException
               ("Invalid grid type for Lat/Lon = "+gridTypeCode);
      }

  }
  /** constructor for well-known grid numbers
  *
  */
  public GRIBCoordinateSystem(RealTupleType ref, int gridNumber) 
                   throws VisADException {
  
    super(ref, coordinate_system_units);
    if (gridNumber == 211) {
      
      doLambert(ref, 12.190, -133.459, 81.2705, 25.0, 25.0, -95.0);
      range = new Rectangle2D.Double(0.0, 0.0, 92., 65.);

    } else if (gridNumber == 236) {

      doLambert(ref, 16.281, 233.862, 40.635, 25.0, 25.0, 265.0);
      range = new Rectangle2D.Double(0.0, 0.0, 150., 113.);
      
    } else {
        System.out.println("GRIB Grid type unknown = "+gridNumber);
        throw new VisADException("Unknown grid number = "+gridNumber);
    }
    
  }

  private void doLatLon(RealTupleType ref, int Ni, int Nj, double La1, 
     double Lo1, double La2, double Lo2, double Di, double Dj) throws
     VisADException {

     isLambert = false;
     isLatLon = true;
     this.La1 = La1;
     this.Lo1 = Lo1;
     this.Di = Di;
     this.Dj = Dj;
     LoMax = Lo1 + Di*(Ni - 1);
     aspectRatio = (Di/Dj);
     range = 
         new Rectangle2D.Double(0.0, 0.0, (double) (Ni - 1), (double) (Nj - 1));

     //System.out.println("la1, lo1, ,LoMax, di, dj ="+La1+" "+Lo1+" "+LoMax+" "+ Di+" "+Dj);

  }

  private void doLambert( RealTupleType ref, double La1, double Lo1, 
      double DxDy, double Latin1, double Latin2, double Lov) throws
      VisADException {

    isLambert = true;
    spacing = DxDy*1000.0;
    double earth = 6371230.0;
    aspectRatio = 1.0;

    c = new LambertConformalConic(ref,
      earth, earth,
      Latin1*Data.DEGREES_TO_RADIANS, Latin2*Data.DEGREES_TO_RADIANS, 
      Lov*Data.DEGREES_TO_RADIANS, Latin1*Data.DEGREES_TO_RADIANS,
      0, 0);

    double[][] lonlat = new double[2][1];
    double[][] xy = new double[2][1];

    lonlat[0][0] = Lo1*Data.DEGREES_TO_RADIANS;
    lonlat[1][0] = La1*Data.DEGREES_TO_RADIANS;

    xy = c.fromReference(lonlat);

    c = new LambertConformalConic(ref,
      earth, earth,
      Latin1*Data.DEGREES_TO_RADIANS, Latin2*Data.DEGREES_TO_RADIANS, 
      Lov*Data.DEGREES_TO_RADIANS, Latin1*Data.DEGREES_TO_RADIANS,
      -xy[0][0], -xy[1][0]);

  }



  /** convert from x,y to lon,lat
  *
  * @param tuples contains the x,y coordinates (grid col, row)
  * @return tuple of (lat,lon);
  *
  */
  public double[][] toReference(double[][] tuples) throws VisADException {

    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GRIBCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    if (isLambert) {
      for (int i=0; i<tuples[0].length; i++) {
        tuples[0][i] = tuples[0][i]*spacing;
        tuples[1][i] = tuples[1][i]*spacing;
      }


      double[][] latlon = c.toReference(tuples);

      for (int i=0; i<latlon[0].length; i++) {
        double t = latlon[0][i]*Data.RADIANS_TO_DEGREES;
        latlon[0][i] = latlon[1][i]*Data.RADIANS_TO_DEGREES;
        latlon[1][i] = t;
        //latlon[1][i] = -t;
      }

      return latlon;

    } else if (isLatLon) {

      double [][] latlon = new double[2][tuples[0].length];

      for (int i=0; i<tuples[0].length; i++) {
        latlon[0][i] = La1 + Dj*tuples[1][i];
        latlon[1][i] = Lo1 + Di*tuples[0][i];
      }

      return latlon;

    } else {

      return null;
    }

  }

  /** convert from lon,lat to x,y
  *
  * @param tuples contains the lat,lon coordinates 
  *
  */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GRIBCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    if (isLambert) {

      for (int i=0; i<tuples[0].length; i++) {
        double t = tuples[0][i]*Data.DEGREES_TO_RADIANS;
        tuples[0][i] = tuples[1][i]*Data.DEGREES_TO_RADIANS;
        //tuples[0][i] = -tuples[1][i]*Data.DEGREES_TO_RADIANS;
        tuples[1][i] = t;
      }

      double[][] xy = c.fromReference(tuples);

      for (int i=0; i<xy[0].length; i++) {
        xy[0][i] = xy[0][i]/spacing;
        xy[1][i] = xy[1][i]/spacing;
      }

      return xy;

    } else if (isLatLon) {

      double [][] xy = new double[2][tuples[0].length];

      for (int i=0; i<tuples[0].length; i++) {
        double Lo = tuples[1][i];
        if (LoMax > 180.0 && Lo < 0.0 && Lo < Lo1) Lo = 360. + Lo;
        xy[0][i] = (Lo - Lo1)/Di;
        xy[1][i] = (tuples[0][i] - La1)/Dj;
      }

      return xy;

    } else {
      return null;
    }

  }

  /** determine if the CoordSys in question is an GRIBCoordinateSystem
  *
  */
  public boolean equals(Object cs) {
    return (cs instanceof GRIBCoordinateSystem);
  }

  /**
   *  return the bounding box for this projection
   */
  public Rectangle2D getDefaultMapArea() { return range; }

  public double getAspectRatio() {
    return aspectRatio;
  }

  public static boolean isGridNumberKnown(int gridNumber) {
    final int[] knownGrids = {211,236};
    for (int i=0; i<knownGrids.length; i++) {
      if (knownGrids[i] == gridNumber) return (true);
    }
    return (false);
  }



  public static void main(String args[] ) {
    try {
    RealTupleType ref = new RealTupleType
         (RealType.Latitude, RealType.Longitude);

    GRIBCoordinateSystem nc = new GRIBCoordinateSystem(ref, 211);

    double[][] latlon = new double[2][1];
    double[][] xy = new double[2][1];

    xy[0][0] = 53.;
    xy[1][0] = 25.;

    latlon = nc.toReference(xy);
    System.out.println(" (53,25) lat="+latlon[0][0]+" lon="+latlon[1][0]);

    // first, the (1,1) point...
    latlon[1][0] = -133.459;
    latlon[0][0] = 12.190;

    xy = nc.fromReference(latlon);
    System.out.println(" at (1,1) x="+xy[0][0]+" y="+xy[1][0]);
    //now the upper right..
    latlon[1][0] = -49.385;
    latlon[0][0] = 57.290;

    xy = nc.fromReference(latlon);
    System.out.println(" max row/col x="+xy[0][0]+" y="+xy[1][0]);

    //now the given point at 35n, 95w

    latlon[1][0] = -95.;
    latlon[0][0] = 35.;

    xy = nc.fromReference(latlon);
    System.out.println(" at 35N/95W x="+xy[0][0]+" y="+xy[1][0]);
    } catch (Exception e) {e.printStackTrace();}

  }

}
