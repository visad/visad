//
// Vis5DCooridinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.vis5d;

import visad.*;
import visad.georef.MapProjection;
import java.awt.geom.Rectangle2D;

/**
   Vis5DCoordinateSystem is the VisAD class for coordinate
   systems for ( row, col ).<P>
*/

public class Vis5DCoordinateSystem extends MapProjection
{
  private static final int  PROJ_GENERIC      =   0;
  private static final int  PROJ_LINEAR       =   1;
  private static final int  PROJ_CYLINDRICAL  =  20;
  private static final int  PROJ_SPHERICAL    =  21;
  private static final int  PROJ_LAMBERT      =   2;
  private static final int  PROJ_STEREO       =   3;
  private static final int  PROJ_ROTATED      =   4;

  private static final double  RADIUS = 6371.23;  /* KM */

  /*- negate Vis5D/McIDAS longitude west positive
      convention */
  private static final double WEST_POSITIVE = -1.0;

  private int    Projection;
  int     REVERSE_POLES = 1;
  double  NorthBound;
  double  SouthBound;
  double  WestBound;
  double  EastBound;
  double  RowInc;
  double  ColInc;
  double  Lat1;
  double  Lat2;
  double  PoleRow;
  double  PoleCol;
  double  CentralLat;
  double  CentralLon;
  double  CentralRow;
  double  CentralCol;
  double  Rotation;             /* radians */
  double  Cone;
  double  Hemisphere;
  double  ConeFactor;
  double  CosCentralLat;
  double  SinCentralLat;
  double  StereoScale;
  double  InvScale;
  double  CylinderScale;
  double  Nr;
  double  Nc;
  double[] projargs;

  private static Unit[] coordinate_system_units =
    {null, null};

  public Vis5DCoordinateSystem(int Projection,
                               double[] projargs,
                               double Nr,
                               double Nc)
       throws VisADException
  {
    super( RealTupleType.LatitudeLongitudeTuple, coordinate_system_units );

    this.Projection = Projection;
    this.Nr = Nr;
    this.Nc = Nc;
    this.projargs = projargs;
    switch ( Projection )
    {
      case PROJ_GENERIC:
      case PROJ_LINEAR:
      case PROJ_CYLINDRICAL:
      case PROJ_SPHERICAL:
         NorthBound = projargs[0];
         WestBound  = projargs[1];
         RowInc     = projargs[2];
         ColInc     = projargs[3];
         break;
      case PROJ_ROTATED:
         NorthBound = projargs[0];
         WestBound  = projargs[1];
         RowInc     = projargs[2];
         ColInc     = projargs[3];
         CentralLat = Data.DEGREES_TO_RADIANS * projargs[4];
         CentralLon = Data.DEGREES_TO_RADIANS * projargs[5];
         Rotation   = Data.DEGREES_TO_RADIANS * projargs[6];
         break;
      case PROJ_LAMBERT:
         Lat1       = projargs[0];
         Lat2       = projargs[1];
         PoleRow    = projargs[2];
         PoleCol    = projargs[3];
         CentralLon = projargs[4];
         ColInc     = projargs[5];
         break;
      case PROJ_STEREO:
         CentralLat = projargs[0];
         CentralLon = projargs[1];
         CentralRow = projargs[2];
         CentralCol = projargs[3];
         ColInc     = projargs[4];
         break;
      default:
         throw new VisADException("Projection unknown");
    }

   /*
    * Precompute useful values for coordinate transformations.
    */
   switch (Projection) {
      case PROJ_GENERIC:
      case PROJ_LINEAR:
         SouthBound = NorthBound - RowInc * (Nr-1);
         EastBound = WestBound - ColInc * (Nc-1);

         break;
      case PROJ_LAMBERT:
         double lat1, lat2;
         if (Lat1==Lat2) {
            /* polar stereographic? */
            if (Lat1>0.0) {
               lat1 = (90.0 - Lat1) * Data.DEGREES_TO_RADIANS;
            }
            else {
               lat1 = (90.0 + Lat1) * Data.DEGREES_TO_RADIANS;
            }
            Cone = Math.cos( lat1 );
            Hemisphere = 1.0;

         }
         else {
            /* general Lambert conformal */
            double a, b;
            if (sign(Lat1) != sign(Lat2)) {
              throw 
                new 
                  VisADException("Error: standard latitudes must have the same sign.\n");
            }
            if (Lat1<Lat2) {
              throw 
                new
                  VisADException("Error: Lat1 must be >= Lat2\n");
            }
            Hemisphere = 1.0;

            lat1 = (90.0 - Lat1) * Data.DEGREES_TO_RADIANS;
            lat2 = (90.0 - Lat2) * Data.DEGREES_TO_RADIANS;
            a = Math.log(Math.sin(lat1)) - Math.log(Math.sin(lat2));
            b = Math.log( Math.tan(lat1/2.0) ) - Math.log( Math.tan(lat2/2.0) );
            Cone = a / b;

         }
         /* Cone is in [-1,1] */
         ConeFactor = RADIUS * Math.sin(lat1)
                          / (ColInc * Cone
                             * Math.pow(Math.tan(lat1/2.0), Cone) );

         break;
      case PROJ_STEREO:
         CosCentralLat = Math.cos( CentralLat * Data.DEGREES_TO_RADIANS );
         SinCentralLat = Math.sin( CentralLat * Data.DEGREES_TO_RADIANS );
         StereoScale = (2.0 * RADIUS / ColInc);
         InvScale = 1.0 / StereoScale;

         break;
      case PROJ_ROTATED:
         SouthBound = NorthBound - RowInc * (Nr-1);
         EastBound = WestBound - ColInc * (Nc-1);

         break;
      case PROJ_CYLINDRICAL:
         if (REVERSE_POLES==-1){
            CylinderScale = 1.0 / (-1.0*(-90.0-NorthBound));
         }
         else{
            CylinderScale = 1.0 / (90.0-SouthBound);
         }
         SouthBound = NorthBound - RowInc * (Nr-1);
         EastBound = WestBound - ColInc * (Nc-1);

         break;
      case PROJ_SPHERICAL:
         SouthBound = NorthBound - RowInc * (Nr-1);
         EastBound = WestBound - ColInc * (Nc-1);

         break;
   }

   if (Projection != PROJ_GENERIC) {
     if (SouthBound < -90.0) {
       throw new VisADException("SouthBound less than -90.0");
     }
     if (NorthBound < SouthBound) {
       throw new VisADException("NorthBound less than SouthBound");
     }
     if (90.0 < NorthBound) {
       throw new VisADException("NorthBound greater than 90.0");
     }
   }
  }

  /**
   * Get the bounds for this image
   */
  public Rectangle2D getDefaultMapArea() 
  { 
      return new Rectangle2D.Double(0, 0, Nc, Nr);
  }

  /**
   * Get the Projection type
   */
  public int getProjection() { return Projection; }

  /**
   * Get the number of Rows
   */
  public double getRows() { return Nr; }

  /**
   * Get the number of Columns
   */
  public double getColumns() { return Nc; }

  /**
   * Get the projection args
   */
  public double[] getProjectionParams() { return projargs; }

  /**
   * Override from super class since toRef and fromRef use rowcol (yx) order
   * instead of colrow (xy) order.
   * @return false
   */
  public boolean isXYOrder() { return false; }

  public double[][] toReference(double[][] rowcol)
         throws VisADException
  {
    int length = rowcol[0].length;
    double[][] latlon = new double[2][length];

    switch (Projection) {
      case PROJ_GENERIC:
      case PROJ_LINEAR:
      case PROJ_CYLINDRICAL:
      case PROJ_SPHERICAL:
        for (int kk = 0; kk < length; kk++) {

       //-latlon[0][kk] = NorthBound - rowcol[0][kk] * (NorthBound-SouthBound)
          latlon[0][kk] = NorthBound - (Nr-1-rowcol[0][kk]) * (NorthBound-SouthBound)
                    / (double) (Nr-1);
          latlon[1][kk] = WestBound - rowcol[1][kk] * (WestBound-EastBound)
                    / (double) (Nc-1);
          latlon[1][kk] *= WEST_POSITIVE;
        }
        break;
      case PROJ_LAMBERT:
         {
           double xldif, xedif, xrlon, radius, lon, lat;
           for (int kk = 0; kk < length; kk++) {

          //-xldif = Hemisphere * (rowcol[0][kk]-PoleRow) / ConeFactor;
             xldif = Hemisphere * ((Nr-1-rowcol[0][kk])-PoleRow) / ConeFactor;
             xedif = (PoleCol-rowcol[1][kk]) / ConeFactor;
             if (xldif==0.0 && xedif==0.0)
               xrlon = 0.0;
             else
               xrlon = Math.atan2( xedif, xldif );
             lon = xrlon / Cone * Data.RADIANS_TO_DEGREES + CentralLon;
             if (lon > 180.0)
                lon -= 360.0;

             radius = Math.sqrt( xldif*xldif + xedif*xedif );
             if (radius < 0.0001)
               lat = 90.0 * Hemisphere;   /* +/-90 */
             else
               lat = Hemisphere
                      * (90.0 - 2.0*Math.atan(Math.exp(Math.log(radius)/Cone))*
                         Data.RADIANS_TO_DEGREES);

             latlon[0][kk] = lat;
             latlon[1][kk] = WEST_POSITIVE*lon;
           }
         }
         break;
      case PROJ_STEREO:
         {
            double xrow, xcol, rho, c, cc, sc, lon, lat;
            for ( int kk = 0; kk < length; kk++) {
           //-xrow = CentralRow - rowcol[0][kk] - 1;
              xrow = CentralRow - (Nr-1-rowcol[0][kk]) - 1;
              xcol = CentralCol - rowcol[1][kk] - 1;
              rho = xrow*xrow + xcol*xcol;
              if (rho<1.0e-20) {
                lat = CentralLat;
                lon = CentralLon;
              }
              else {
                rho = Math.sqrt( rho );
                c = 2.0 * Math.atan( rho * InvScale);
                cc = Math.cos(c);
                sc = Math.sin(c);
                lat = Data.RADIANS_TO_DEGREES
                     * Math.asin( cc*SinCentralLat
                            + xrow*sc*CosCentralLat / rho );
                lon = CentralLon + Data.RADIANS_TO_DEGREES * Math.atan2( xcol * sc,
                         (rho * CosCentralLat * cc
                      - xrow * SinCentralLat * sc) );
                if (lon < -180.0)  lon += 360.0;
                else if (lon > 180.0)  lon -= 360.0;
            }
            latlon[0][kk] = lat;
            latlon[1][kk] = WEST_POSITIVE*lon;
           }
         }
         break;
      case PROJ_ROTATED:
         {
           for (int kk = 0; kk < length; kk++) {
          //-latlon[0][kk] = NorthBound - rowcol[0][kk]
             latlon[0][kk] = NorthBound - (Nr-1-rowcol[0][kk])
                     * (NorthBound-SouthBound) / (double) (Nr-1);
             latlon[1][kk] = WestBound - rowcol[1][kk]
                     * (WestBound-EastBound) / (double) (Nc-1);
           }
           pandg_back(latlon, CentralLat, CentralLon, Rotation);
         }
         break;
      default:
         throw new VisADException("projection unknown");
   }

   return latlon;
  }

  public double[][] fromReference(double[][] latlon)
         throws VisADException
  {
   int length = latlon[0].length;
   double[][] rowcol = new double[2][length];

   switch (Projection) {
      case PROJ_GENERIC:
      case PROJ_LINEAR:
      case PROJ_CYLINDRICAL:
      case PROJ_SPHERICAL:
         for ( int kk = 0; kk < length; kk++ ) {
           rowcol[0][kk] = (NorthBound - latlon[0][kk])/RowInc;
           rowcol[0][kk] = (Nr-1) - rowcol[0][kk];
           rowcol[1][kk] = (WestBound - latlon[1][kk]*WEST_POSITIVE)/ColInc;
         }
         break;
      case PROJ_LAMBERT:
         {
            double rlon, rlat, r, lat, lon;
            for (int kk = 0; kk < length; kk++) {
              lat = latlon[0][kk];
              lon = latlon[1][kk]*WEST_POSITIVE;

              rlon = lon - CentralLon;
              rlon = rlon * Cone * Data.DEGREES_TO_RADIANS;

              if (lat < -85.0) {
                /* infinity */
                r = 10000.0;
              }
              else {
                rlat = (90.0 - Hemisphere * lat) * Data.DEGREES_TO_RADIANS * 0.5;
                r = ConeFactor * Math.pow(Math.tan(rlat), Cone);
              }
              rowcol[0][kk] = PoleRow + r * Math.cos(rlon);
              rowcol[0][kk] = (Nr-1) - rowcol[0][kk];
              rowcol[1][kk] = PoleCol - r * Math.sin(rlon);
            }
         }
         break;
      case PROJ_STEREO:
         {
            double rlat, rlon, clon, clat, k, lat, lon;
            
            for (int kk = 0; kk < length; kk++) {
              lat = latlon[0][kk];
              lon = latlon[1][kk]*WEST_POSITIVE;

              rlat = Data.DEGREES_TO_RADIANS * lat;
              rlon = Data.DEGREES_TO_RADIANS * (CentralLon - lon);
              clon = Math.cos(rlon);
              clat = Math.cos(rlat);
              k = StereoScale
                / (1.0 + SinCentralLat*Math.sin(rlat)
                       + CosCentralLat*clat*clon);
              rowcol[1][kk] = (CentralCol-1) + k * clat * Math.sin(rlon);
              rowcol[0][kk] = (CentralRow-1)
                   - k * (CosCentralLat * Math.sin(rlat)
                       - SinCentralLat * clat * clon);
              rowcol[0][kk] = (Nr-1) - rowcol[0][kk];
            }
         }
         break;
      case PROJ_ROTATED:
         {
            pandg_for(latlon, CentralLat, CentralLon, Rotation);
            for (int kk = 0; kk < length; kk++) {
              rowcol[0][kk] = (NorthBound - latlon[0][kk])/RowInc;
              rowcol[0][kk] = (Nr-1) - rowcol[0][kk];
              rowcol[1][kk] = (WestBound - latlon[1][kk])/ColInc;
            }
         }
         break;
      default:
         throw new VisADException("Projection unknown");
   }
   return rowcol;
  }

  /*
    Pete and Greg parameters:
      Pete rotated sphere lat 0, lon 0 -> Earth lat a, lon b
      r = East angle between North half of lon = 0 line on Pete rotated
          sphere and lon = b on Earth

    coordinates:
      lat p1, lon g1 on Earth
      lat pr, lon gr on Pete rotated sphere
  */

  /* Pete rotated sphere to Earth */
  private static void pandg_back( double[][] latlon, double a, double b, double r )
  {
    double pr, gr, pm, gm;

    /* NOTE - longitude sign switches - b too! */

    for (int kk = 0; kk < latlon[0].length; kk++) { 

      pr = Data.DEGREES_TO_RADIANS * latlon[0][kk];
      gr = -Data.DEGREES_TO_RADIANS * latlon[1][kk];
      pm = Math.asin( Math.cos(pr) * Math.cos (gr) );
      gm = Math.atan2(Math.cos(pr) * Math.sin (gr), -Math.sin(pr) );

      latlon[0][kk] =
         Data.RADIANS_TO_DEGREES *
           Math.asin( Math.sin(a) * Math.sin(pm) - Math.cos(a) * Math.cos(pm) * Math.cos(gm - r) );
      latlon[1][kk] =
        -Data.RADIANS_TO_DEGREES * (-b + Math.atan2(Math.cos(pm) * Math.sin(gm - r),
           Math.sin(a) * Math.cos(pm) * Math.cos(gm - r) + Math.cos(a) * Math.sin(pm)));
      latlon[1][kk] *= WEST_POSITIVE;
    }
    return;
  }

/* Earth to Pete rotated sphere */
  private static void pandg_for( double[][] latlon, double a, double b, double r )
  {
    double p1, g1, p, g;

    /* NOTE - longitude sign switches - b too! */
   
    for (int kk = 0; kk < latlon[0].length; kk++) {

      p1 = Data.DEGREES_TO_RADIANS * latlon[0][kk];
      g1 = -Data.DEGREES_TO_RADIANS * latlon[1][kk]*WEST_POSITIVE;
      p = Math.asin( Math.sin(a) * Math.sin(p1) + Math.cos(a) * Math.cos(p1) * Math.cos(g1 + b) );
      g = r + Math.atan2(Math.cos(p1) * Math.sin (g1 + b),
              Math.sin(a) * Math.cos(p1) * Math.cos(g1 + b) - Math.cos(a) * Math.sin(p1) );

      latlon[0][kk] =
        Data.RADIANS_TO_DEGREES * Math.asin( -Math.cos(p) * Math.cos(g) );
      latlon[1][kk] =
       -Data.RADIANS_TO_DEGREES * Math.atan2(Math.cos(p) * Math.sin(g), Math.sin(p) );
    }
    return;
  }

  private static boolean sign(double dub) 
  {
    if ( dub < 0.0 ) {
      return false;
    }
    else {
      return true;
    }
  }

  public boolean equals(Object cs) 
  {
    if ( !(cs instanceof Vis5DCoordinateSystem)) return false;
    Vis5DCoordinateSystem that = (Vis5DCoordinateSystem) cs;
    return (this.Projection == that.Projection &&
            Double.doubleToLongBits(this.Nr) == 
                Double.doubleToLongBits(that.Nr) &&
            Double.doubleToLongBits(this.Nc) == 
                Double.doubleToLongBits(that.Nc) &&
            java.util.Arrays.equals(this.projargs, that.projargs));
  }

  public static void main(String args[]) throws VisADException
  {
    int proj = 3;
    double[] projargs =
     {90, 100, 50, 50, 100};

    Vis5DCoordinateSystem v5dcs =
      new Vis5DCoordinateSystem(proj, projargs, 100, 100);

    double[][] latlon =
     {{89, 42, 60}, {-100, -100, -180}};

    double[][] rowcol = v5dcs.fromReference(latlon);
 // System.out.println(rowcol[0][0]+", "+rowcol[1][0]+" : "+rowcol[0][2]+", "+rowcol[1][2]);

    double[][] latlon_t = v5dcs.toReference(rowcol);
    System.out.println(latlon_t[0][0]+", "+latlon_t[1][0]+" : "+latlon_t[0][2]+", "+latlon_t[1][2]);

    proj = 2;
    double[] projargs_lam =
     {60, 30, 0, 50, 100, 100};
    
    v5dcs =
      new Vis5DCoordinateSystem(proj, projargs_lam, 100, 100);

    double[][] latlon2 =
     {{90, 40, 50}, {-100, -100, -180}};
    rowcol = v5dcs.fromReference(latlon2);
 // System.out.println(rowcol[0][0]+", "+rowcol[1][0]+" : "+rowcol[0][2]+", "+rowcol[1][2]);
    latlon_t = v5dcs.toReference(rowcol);
    System.out.println(latlon_t[0][0]+", "+latlon_t[1][0]+" : "+latlon_t[0][2]+", "+latlon_t[1][2]);
  }
}
