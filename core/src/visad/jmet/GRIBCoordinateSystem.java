//
// GRIBCoordinateSystem.java
//

/*

The software in this file is Copyright(C) 2011 by Tom Whittaker.
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

import java.awt.geom.Rectangle2D;

import visad.CoordinateSystem;
import visad.CoordinateSystemException;
import visad.Data;
import visad.RealTupleType;
import visad.RealType;
import visad.Unit;
import visad.VisADException;
import visad.data.hdfeos.LambertConformalConic;
import visad.data.hdfeos.PolarStereographic;

public class GRIBCoordinateSystem extends visad.georef.MapProjection {

  private static Unit[] coordinate_system_units = {null, null};
  private CoordinateSystem c;
  private double spacing;
  private boolean isLambert=false;
  private boolean isLatLon=false;
  private boolean isPolarStereo=false;
  private double La1, Lo1, LoMax, Di, Dj;
  private double aspectRatio = 1.0;
  //private double earth = 6367470.0;
  private double[] range = new double[4];

  /**
   * Constructor for a Polar Stereographic (GRIB type code = 5) with
   * RealTupleType.LatitudeLongitudeTuple as a reference.
   *
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  DxDy            spacing between grid points at 60N
   * @param  lov             orientation of the grid (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  public GRIBCoordinateSystem(int gridTypeCode, 
    double La1, double Lo1, double DxDy, double lov)
                   throws VisADException {
    this(RealTupleType.LatitudeLongitudeTuple, gridTypeCode, 
         La1, Lo1, DxDy, lov);
  }

  /**
   * Constructor for a Polar Stereographic (GRIB type code = 5) with
   * RealTupleType.LatitudeLongitudeTuple as a reference.
   *
   * @param  ref             reference RealTupleType (should be lat/lon)
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  DxDy            spacing between grid points at 60N
   * @param  lov             orientation of the grid (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  public GRIBCoordinateSystem(RealTupleType ref, int gridTypeCode,
    double La1, double Lo1, double DxDy, double lov)
                   throws VisADException {

    super(ref, coordinate_system_units);

    if (gridTypeCode == 5) {
      doPolarStereo(ref, 2, 2, La1, Lo1, DxDy, lov); 
    } else {

        System.out.println("GRIB Grid type not Polar Stereographic = "+
                                            gridTypeCode);
        throw new VisADException
               ("Invalid grid type for PolarStereographic = "+gridTypeCode);
    }
  }

  /**
   * Constructor for a Lambert conformal (GRIB type code = 3)
   *
   * @param  ref             reference RealTupleType (should be lat/lon)
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  DxDy            ?
   * @param  Latin1          first intersecting latitude (degrees)
   * @param  Latin2          second intersecting latitude (degrees)
   * @param  Lov             orientation of the grid (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  
  public GRIBCoordinateSystem(RealTupleType ref, int gridTypeCode,
    double La1, double Lo1, double DxDy,
    double Latin1, double Latin2, double Lov)
                   throws VisADException {

    this(ref, gridTypeCode, 2, 2,
         La1, Lo1, DxDy, Latin1, Latin2, Lov);
  }

  /**
   * Constructor for a Lambert conformal (GRIB type code = 3) with
   * RealTupleType.LatitudeLongitudeTuple as a reference.
   *
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  DxDy            ?
   * @param  Latin1          first intersecting latitude (degrees)
   * @param  Latin2          second intersecting latitude (degrees)
   * @param  Lov             orientation of the grid (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  
  public GRIBCoordinateSystem(int gridTypeCode,
    double La1, double Lo1, double DxDy,
    double Latin1, double Latin2, double Lov)
                   throws VisADException {
    this(RealTupleType.LatitudeLongitudeTuple, gridTypeCode, 2, 2,
         La1, Lo1, DxDy, Latin1, Latin2, Lov);
  }

  /**
   * Constructor for a latitude-longitude (GRIB type code = 0) with
   * RealTupleType.LatitudeLongitudeTuple as a reference.
   *
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  Ni              number of points (W-E) along a latitude circle
   * @param  Nj              number of points (N-S) along a longitude circle
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  La2             latitude of last grid point (degrees)
   * @param  Lo2             longitude of last grid point (degrees)
   * @param  Di              longitudinal direction increment (degrees)
   * @param  Dj              latitudinal direction increment (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  
  public GRIBCoordinateSystem(int gridTypeCode,
    int Ni, int Nj, double La1, double Lo1, double La2, double Lo2,
    double Di, double Dj) throws VisADException {

    this(RealTupleType.LatitudeLongitudeTuple, gridTypeCode,
         Ni, Nj, La1, Lo1, La2, Lo2,  Di, Dj);
  }

  /**
   * constructor for a latitude-longitude (GRIB type code = 0). Reference
   * should be RealTupleType.LatitudeLongitudeTuple.
   *
   * [this is overloaded to also handle LambertConformal (above, type = 3)]
   *
   * @param  ref             reference RealTupleType (should be lat/lon)
   * @param  gridTypeCode    GRIB-1 grid type
   * @param  Ni              number of points (W-E) along a latitude circle
   * @param  Nj              number of points (N-S) along a longitude circle
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  La2             latitude of last grid point (degrees)
   * @param  Lo2             longitude of last grid point (degrees)
   * @param  Di              longitudinal direction increment (degrees)
   * @param  Dj              latitudinal direction increment (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   * 
   * When type=3, then the parameters are (see construtor for 3):
   * ref = ref, Ni = Ni, Nj = Nj, La1 = La1, Lo1 = Lo1, La2 = DxDy,
   * Lo2 = Latin1, Di = Latin2, Dj = lov 
   */
  
  public GRIBCoordinateSystem(RealTupleType ref, int gridTypeCode,
    int Ni, int Nj, double La1, double Lo1, double La2, double Lo2,
    double Di, double Dj) throws VisADException {

    super(ref, coordinate_system_units);

    if (gridTypeCode == 0) {
      doLatLon(ref, Ni, Nj, La1, Lo1, La2, Lo2, Di, Dj) ;

    } else if (gridTypeCode == 3) {
      //note the meaning of these parameters is defined in the actual
      // Lambert constructor signature!
      doLambert(ref, Ni, Nj, La1, Lo1, La2, Lo2, Di, Dj);

    } else {
        System.out.println("GRIB Grid type not Lat/Lon = "+gridTypeCode);
        throw new VisADException
               ("Invalid grid type for Lat/Lon = "+gridTypeCode);
    }

  }

  /**
   * Constructor for a simple latitude-longitude (GRIB type code = 0) with
   * RealTupleType.LatitudeLongitudeTuple as a reference.
   *
   * @param  Ni              number of points (W-E) along a latitude circle
   * @param  Nj              number of points (N-S) along a longitude circle
   * @param  La1             latitude of first grid point (degrees)
   * @param  Lo1             longitude of first grid point (degrees)
   * @param  Di              longitudinal direction increment (degrees)
   * @param  Dj              latitudinal direction increment (degrees)
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */

  public GRIBCoordinateSystem(int Ni, int Nj, double La1, double Lo1, 
            double Di, double Dj) throws VisADException {

    this(RealTupleType.LatitudeLongitudeTuple, 0,
           Ni, Nj, La1, Lo1, 0., 0., Di, Dj) ;
  }



  /**
   * constructor for well-known grid numbers. Uses
   * RealTupleType.LatitudeLongitudeTuple for a reference.
   *
   * @param  gridNumber  GRIB grid number
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  public GRIBCoordinateSystem(int gridNumber)
                   throws VisADException {
    this(RealTupleType.LatitudeLongitudeTuple, gridNumber);
  }

  /** constructor for well-known grid numbers
   *
   * @param  ref             reference RealTupleType (should be lat/lon)
   * @param  gridNumber  GRIB grid number
   *
   * @exception VisADException  couldn't create the necessary VisAD object
   */
  public GRIBCoordinateSystem(RealTupleType ref, int gridNumber)
                   throws VisADException {

    super(ref, coordinate_system_units);

    if (gridNumber == 201) {
      doPolarStereo(ref, 65,65,-20.826, -150.0, 381.0, -105.0);

    } else if (gridNumber == 202) {
      doPolarStereo(ref, 65,43, 7.838, -141.028, 190.5, -105.0);


    } else if (gridNumber == 203) {
      doPolarStereo(ref, 45,39, 19.132, -185.837, 190.5, -150.0);

    } else if (gridNumber == 211) {
      doLambert(ref, 93, 65, 12.190, -133.459, 81.2705, 25.0, 25.0, -95.0);

    } else if (gridNumber == 221) {
      doLambert(ref, 349, 277, 1.0000, -145.000, 32.46341, 50.0, 50.0, -107.0);

    } else if (gridNumber == 226) {
      doLambert(ref, 737, 517, 12.190, -133.459, 10.1588125, 25.0, 25.0, -95.0);

    } else if (gridNumber == 227) {
      doLambert(ref, 1473, 1025, 12.190, -133.459, 5.07940625, 25.0, 25.0, -95.0);

    } else if (gridNumber == 241) {
      doLambert(ref, 549, 445, -4.850, -151.100, 22.000, 45.0, 45.0, -111.0);

    } else if (gridNumber == 252) {
      doLambert(ref, 301, 225, 16.281, -126.138, 20.317625, 25.0, 25.0, -95.0);

    } else if (gridNumber == 2525) { // 5km FAA PDT grid pseudo-number 
      doLambert(ref, 1200, 896, 16.30812906980,  -126.1204451156423, 5.07940 , 25.0, 25.0, -95.0);

    } else if (gridNumber == 212) {
      doLambert(ref, 185, 129, 12.190, -133.459, 40.63525, 25.0, 25.0, -95.0);

    } else if (gridNumber == 213) {
      doPolarStereo(ref, 129, 85, 7.838, -141.028, 95.250, -105.0);

    } else if (gridNumber == 215) {
      doLambert(ref, 369, 257, 12.190, -133.459, 20.317625, 25.0, 25.0, -95.0);

    } else if (gridNumber == 236) {
      doLambert(ref, 151, 113, 16.281, 233.862, 40.635, 25.0, 25.0, 265.0);

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
     range[0] = 0.0;
     range[1] = 0.0;
     range[2] = (double) Ni;
     range[3] = (double) Nj;
  }

  private void doPolarStereo( RealTupleType ref, int Nx, int Ny, 
      double La1, double Lo1, double DxDy, double Lov) throws VisADException {

    isPolarStereo = true;
    spacing = DxDy * 1000.0;
    aspectRatio = 1.0;
    range[0] = 0.0;
    range[1] = 0.0;
    range[2] = (double) Nx;
    range[3] = (double) Ny;

    c = PolarStereographic.makePolarStereographic( ref,
         La1*Data.DEGREES_TO_RADIANS, Lo1*Data.DEGREES_TO_RADIANS,
         Lov*Data.DEGREES_TO_RADIANS);
  }    

  private void doLambert( RealTupleType ref, int Nx, int Ny, double La1, 
     double Lo1, double DxDy, double Latin1, double Latin2, double Lov) 
              throws VisADException {

    isLambert = true;
    spacing = DxDy*1000.0;
    //double earth = 6371229.0;  // this is the GRADS value
    //double earth = 6367470.0;  // this is the GRIB document value
    double earth = 6371230.0;  // this is the one to use...
    aspectRatio = 1.0;
    range[0] = 0.0;
    range[1] = 0.0;
    range[2] = (double) Nx;
    range[3] = (double) Ny;

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



  /** convert from x,y to lat,lon
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

    if (isLambert || isPolarStereo) {
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

  /** convert from lat,lon to x,y
  *
  * @param tuples contains the lat,lon coordinates
  *
  */
  public double[][] fromReference(double[][] tuples) throws VisADException {
    if (tuples == null || tuples.length != 2) {
      throw new CoordinateSystemException("GRIBCoordinateSystem." +
             "toReference: tuples wrong dimension");
    }

    if (isLambert || isPolarStereo) {

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

  /** determine if the Coordinate System in question is an GRIBCoordinateSystem
  *
  */
  public boolean equals(Object cs) {
    return (cs instanceof GRIBCoordinateSystem);
  }

  /**
   *  return the bounding box for this projection
   */
  public Rectangle2D getDefaultMapArea() { 
    return new Rectangle2D.Double(range[0], range[1], range[2], range[3]); 
  }

  /** return the ratio of the grid spacing between rows and columns
  */
  public double getAspectRatio() {
    return aspectRatio;
  }

  /** find out if the specified grid number (GRIB code) is known
  */
  public static boolean isGridNumberKnown(int gridNumber) {
    final int[] knownGrids = {201,202,203,211,212,215,236};
    for (int i=0; i<knownGrids.length; i++) {
      if (knownGrids[i] == gridNumber) return (true);
    }
    return (false);
  }



  public static void main(String args[] ) {
    try {
      RealTupleType ref = new RealTupleType
           (RealType.Latitude, RealType.Longitude);

      GRIBCoordinateSystem nc = new GRIBCoordinateSystem(211);
      System.out.println("isSerializable? "+
                  visad.util.DataUtility.isSerializable(nc,true));

      double[][] latlon = new double[2][1];
      double[][] xy = new double[2][1];

      xy[0][0] = 92.;
      xy[1][0] = 64.;

      latlon = nc.toReference(xy);
      System.out.println(" (93,65) lat="+latlon[0][0]+" lon="+latlon[1][0]);

      // first, the (1,1) point...
      latlon[0][0] = 12.190;        // lat
      latlon[1][0] = -133.459;      // lon

      xy = nc.fromReference(latlon);
      System.out.println(" at (1,1) x="+xy[0][0]+" y="+xy[1][0]);
      //now the upper right..
      latlon[0][0] = 57.290;       // lat
      latlon[1][0] = -49.385;      // lon

      xy = nc.fromReference(latlon);
      System.out.println(" max row/col x="+xy[0][0]+" y="+xy[1][0]);

      //now the given point at 35n, 95w

      latlon[0][0] = 35.;        // lat
      latlon[1][0] = -95.;       // lon

      xy = nc.fromReference(latlon);
      System.out.println(" at 35N/95W x="+xy[0][0]+" y="+xy[1][0]);


      nc = new GRIBCoordinateSystem(202);
      System.out.println("PolarStereo.......isSerializable? "+
                  visad.util.DataUtility.isSerializable(nc,true));

      latlon = new double[2][1];
      xy = new double[2][1];

      xy[0][0] = 64.;
      xy[1][0] = 42.;

      latlon = nc.toReference(xy);
      System.out.println(" (65,42) lat="+latlon[0][0]+" lon="+latlon[1][0]);

      // first, the (1,1) point...
      latlon[0][0] = 7.838;        // lat
      latlon[1][0] = -1141.028;      // lon

      xy = nc.fromReference(latlon);
      System.out.println(" at (1,1) x="+xy[0][0]+" y="+xy[1][0]);

      //now the upper right..
      latlon[0][0] = 35.617;       // lat
      latlon[1][0] = -18.576;      // lon

      xy = nc.fromReference(latlon);
      System.out.println(" max row/col x="+xy[0][0]+" y="+xy[1][0]);

      //now the given point at 35n, 95w

      latlon[0][0] = 35.;        // lat
      latlon[1][0] = -95.;       // lon

      xy = nc.fromReference(latlon);
      System.out.println(" at 35N/95W x="+xy[0][0]+" y="+xy[1][0]);
    } catch (Exception e) {e.printStackTrace();}

  }

}
