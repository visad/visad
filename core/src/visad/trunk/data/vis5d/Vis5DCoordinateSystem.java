//
// Vis5DCooridinateSystem.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.units.Parser;
import visad.data.units.ParseException;

/**
   Vis5DCoordinateSystem is the VisAD class for coordinate
   systems for ( row, col ).<P>
*/

public class Vis5DCoordinateSystem extends CoordinateSystem
{
  private static final int  PROJ_GENERIC      =   0;
  private static final int  PROJ_LINEAR       =   1;
  private static final int  PROJ_CYLINDRICAL  =  20;
  private static final int  PROJ_SPHERICAL    =  21;
  private static final int  PROJ_LAMBERT      =   2;
  private static final int  PROJ_STEREO       =   3;
  private static final int  PROJ_ROTATED      =   4;

  private static final double  RADIUS = 6371.23;  /* KM */

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

          latlon[0][kk] = NorthBound - rowcol[0][kk] * (NorthBound-SouthBound)
                    / (double) (Nr-1);
          latlon[1][kk] = WestBound - rowcol[1][kk] * (WestBound-EastBound)
                    / (double) (Nc-1);
        }
        break;
      case PROJ_LAMBERT:
         {
           double xldif, xedif, xrlon, radius, lon, lat;
           for (int kk = 0; kk < length; kk++) {

             xldif = Hemisphere * (rowcol[0][kk]-PoleRow) / ConeFactor;
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
             latlon[1][kk] = lon;
           }
         }
         break;
      case PROJ_STEREO:
         {
            double xrow, xcol, rho, c, cc, sc, lon, lat;
            for ( int kk = 0; kk < length; kk++) {
              xrow = CentralRow - rowcol[0][kk] - 1;
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
            latlon[1][kk] = lon;
           }
         }
         break;
      case PROJ_ROTATED:
         {
           for (int kk = 0; kk < length; kk++) {
             latlon[0][kk] = NorthBound - rowcol[0][kk]
                     * (NorthBound-SouthBound) / (double) (Nr-1);
             latlon[1][kk] = WestBound - rowcol[0][kk]
                     * (WestBound-EastBound) / (double) (Nc-1);
             pandg_back(latlon, CentralLat, CentralLon, Rotation);
           }
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
           rowcol[1][kk] = (WestBound - latlon[1][kk])/ColInc;
         }
         break;
      case PROJ_LAMBERT:
         {
            double rlon, rlat, r, lat, lon;
            for (int kk = 0; kk < length; kk++) {
              lat = latlon[0][kk];
              lon = latlon[1][kk];

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
              rowcol[1][kk] = PoleCol - r * Math.sin(rlon);
            }
         }
         break;
      case PROJ_STEREO:
         {
            double rlat, rlon, clon, clat, k, lat, lon;
            
            for (int kk = 0; kk < length; kk++) {
              lat = latlon[0][kk];
              lon = latlon[1][kk];

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
            }
         }
         break;
      case PROJ_ROTATED:
         {
            pandg_for(latlon, CentralLat, CentralLon, Rotation);
            for (int kk = 0; kk < length; kk++) {
              rowcol[0][kk] = (NorthBound - latlon[0][kk])/RowInc;
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
      g1 = -Data.DEGREES_TO_RADIANS * latlon[1][kk];
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
    return (cs instanceof Vis5DCoordinateSystem);
  }

  public static void main(String args[]) throws VisADException 
  {

  }
}

class Vis5DVertCoordinateSystem extends CoordinateSystem
{
  private static final int EQUAL_SPACE    = 0;
  private static final int UNEQUAL_SPACE  = 1;

  private final int vert_sys;
  private int n_levels;
  private double[] vert_args;
  private SampledSet vertSet;
  RealType vert_type;
  Unit  vert_unit = null;
  
  private static Unit[] coordinate_system_units =
    {null};

  public static Vis5DVertCoordinateSystem
         makeVis5DVertCoordinateSystem( int vert_sys,
                                        int n_levels,
                                        double[] vert_args )
         throws VisADException
  {
    Vis5DVertCoordinateSystem cs = null;
    RealTupleType reference = null;
    Unit u = null;

    switch(vert_sys)
    {
      case (0):
        reference = 
          new RealTupleType(RealType.getRealType("Height_2"));
        cs =
          new
          Vis5DVertCoordinateSystem(reference, EQUAL_SPACE, n_levels, vert_args);
        break;
      case (1):
        try {
          u = Parser.parse("km");
        }
        catch (ParseException e) {
        }
        reference = 
          new RealTupleType(RealType.getRealType("Height_2", u));
        cs =
          new
          Vis5DVertCoordinateSystem(reference, EQUAL_SPACE, n_levels, vert_args);
        break;
      case (2):
        try {
          u = Parser.parse("km");
        }
        catch (ParseException e) {
        }
        reference = 
          new RealTupleType(RealType.getRealType("Height_2", u));
        cs =
          new
          Vis5DVertCoordinateSystem(reference, UNEQUAL_SPACE, n_levels, vert_args);
        break;
      case (3):
        try {
          u = Parser.parse("mb");
        }
        catch (ParseException e) {
        }
        reference = 
          new RealTupleType(RealType.getRealType("Height_2", u));
        cs =
          new
          Vis5DVertCoordinateSystem(reference, UNEQUAL_SPACE, n_levels, vert_args);
        break;
      default:
        throw new VisADException("vert_sys unknown");
    }
    return cs;
  }

  public Vis5DVertCoordinateSystem(RealTupleType Reference,
                                   int vert_sys,
                                   int n_levels,
                                   double[] vert_args)
         throws VisADException
  {
    super( Reference, coordinate_system_units );

    this.vert_sys = vert_sys;
    this.vert_args = vert_args;
    this.n_levels = n_levels;

    switch ( vert_sys )
    {
      case (0):
        vert_type = RealType.getRealType("Height");
        break;
      case (1):
      case (2):
        try {
          vert_unit = Parser.parse("km");
        }
        catch (ParseException e) {
        }
        vert_type = RealType.getRealType("Height", vert_unit);
        break;
      case (3):
        try {
          vert_unit = Parser.parse("mb");
        }
        catch (ParseException e) {
        }
        vert_type = RealType.getRealType("Height", vert_unit);
        break;
      default:
        throw new VisADException("vert_sys unknown");
    }

    switch ( vert_sys )
    {
      case EQUAL_SPACE:
        double first = vert_args[0];
        double last = first + vert_args[1]*(n_levels-1);
        vertSet = new Linear1DSet(getReference(), first, last, n_levels);
      case UNEQUAL_SPACE:
        double[][] values = new double[1][n_levels];
        System.arraycopy(vert_args, 0, values[0], 0, n_levels);
        vertSet = 
          new Gridded1DSet(getReference(), Set.doubleToFloat(values), n_levels);
        break;
      default:
         throw new VisADException("vert_sys unknown");
    }
  }

  public double[][] toReference(double[][] cs_tuple)
         throws VisADException
  {
    int[] indexes = new int[cs_tuple[0].length];

    for (int kk = 0; kk < indexes.length; kk++) {
      indexes[kk] = (int) cs_tuple[0][kk];
    }

    float[][] ref_tuple =
      vertSet.indexToValue(indexes);

    return Set.floatToDouble(ref_tuple);
  }

  public double[][] fromReference(double[][] ref_tuple)
         throws VisADException
  {
    int[] indexes =
      vertSet.valueToIndex(Set.doubleToFloat(ref_tuple));

    double[][] cs_tuple = new double[1][indexes.length];

    for (int kk = 0; kk < indexes.length; kk++) {
      cs_tuple[0][kk] = (double) indexes[kk];
    }

    return cs_tuple;
  }

  public SampledSet getVerticalSet() {
    return vertSet;
  }

  public boolean equals(Object cs)
  {
    return (cs instanceof Vis5DCoordinateSystem);
  }

  public static void main(String args[]) throws VisADException 
  {

  }
}
