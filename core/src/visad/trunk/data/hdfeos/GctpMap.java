//
// GctpMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

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

package visad.data.hdfeos;

import visad.*;

public class GctpMap
{

   double[] projparms;
   int projcode;
   int zonecode;
   int sphrcode;
   int xdimsize;
   int ydimsize;
   double[] uprLeft;
   double[] lwrRight;

   static RealType[] map_coords = { RealType.XAxis, RealType.YAxis };
   

  public GctpMap(  int projcode,
                   int zonecode,
                   int sphrcode,
                   int xdimsize,
                   int ydimsize,
                   double[] projparms,
                   double[] uprLeft,
                   double[] lwrRight )
  {

    this.projcode = projcode;
    this.projparms = projparms;
    this.zonecode = zonecode;
    this.sphrcode = sphrcode;
    this.xdimsize = xdimsize;
    this.ydimsize = ydimsize;
    this.uprLeft = uprLeft;
    this.lwrRight = lwrRight;
  }

  public CoordinateSystem getVisADCoordinateSystem() throws VisADException 
  {

    CoordinateSystem coord_sys = null;
    RealTupleType Reference = null;  

    double[] r_major = new double[1];
    double[] r_minor = new double[1];
    double[] radius =  new double[1];
    double[] center_long = new double[1];
    double[] center_lat = new double[1];
    double[] lat_1 = new double[1];
    int stat;

    double false_easting = projparms[6];
    double false_northing = projparms[7];

    RealType r_lat = RealType.Latitude;
    RealType r_lon = RealType.Longitude;
    RealType[] components = { r_lat, r_lon };
    Reference = new RealTupleType( components );

    GctpFunction.sphdz( sphrcode, projparms, r_major, r_minor, radius );

    switch  ( projcode ) { 

      case GctpFunction.LAMAZ:
        stat = GctpFunction.paksz( projparms[4], center_long );
        if ( stat != 0 ) {
           // error?
        }
        stat = GctpFunction.paksz( projparms[5], center_lat );
        if ( stat != 0 ) {
           // error?
        }

        coord_sys = new LambertAzimuthalEqualArea( Reference, radius[0], center_long[0],
                                             center_lat[0], false_easting, false_northing );

      case GctpFunction.PS:

        stat = GctpFunction.paksz( projparms[4], center_long );
        if ( stat !=0 ) {
          // error
        }
        stat = GctpFunction.paksz( projparms[5], lat_1 );
        if ( stat !=0 ) {
          // error
        }


        coord_sys = new PolarStereographic( Reference, r_major[0],
                                                       r_minor[0],
                                                       center_long[0],
                                                       lat_1[0],
                                                       false_easting, false_northing );

      case GctpFunction.LAMCC:

      default:

    }

    return coord_sys;
  }

 public Set getVisADSet() throws VisADException
 {

   int length1 = xdimsize;
   int length2 = ydimsize;

   MathType M_type = getVisADMathType();

   Set VisADset = new Linear2DSet( M_type, uprLeft[0], lwrRight[0], length1,
                                           lwrRight[1], uprLeft[1] , length2 );

   return VisADset;
 }

 public MathType getVisADMathType() throws VisADException
 {

   CoordinateSystem coord_sys = getVisADCoordinateSystem();

   MathType M_type = (MathType) new RealTupleType( map_coords, coord_sys, null );

   return M_type;

 }

}
