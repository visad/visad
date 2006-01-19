//
// GctpMap.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.hdfeos;

import visad.*;

public class GctpMap
{
  double[] projparms;
  public int projcode;
  Unit[] setUnits = null;
  int zonecode;
  int sphrcode;
  int xdimsize;
  int ydimsize;
  double[] uprLeft;
  double[] lwrRight;

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

  public CoordinateSystem getVisADCoordinateSystem()
         throws VisADException
  {
    CoordinateSystem coord_sys = null;
    RealTupleType Reference;

    double[] r_major = new double[1];
    double[] r_minor = new double[1];
    double[] radius =  new double[1];
    double[] center_lon = new double[1];
    double[] center_lat = new double[1];
    double[] lat_1 = new double[1];
    double[] lat_2 = new double[1];
    int stat;

    double false_easting = projparms[6];
    double false_northing = projparms[7];

    RealType r_lat = RealType.Latitude;
    RealType r_lon = RealType.Longitude;
    RealType[] components = { r_lon, r_lat };
    Reference = new RealTupleType( components );

    GctpFunction.sphdz( sphrcode, projparms, r_major, r_minor, radius );

    switch  ( projcode )
    {
      case GctpFunction.LAMAZ:
        stat = GctpFunction.paksz( projparms[4], center_lon );
        if ( stat != 0 ) {
           // error?
        }
        stat = GctpFunction.paksz( projparms[5], center_lat );
        if ( stat != 0 ) {
           // error?
        }

        coord_sys = new LambertAzimuthalEqualArea( Reference,
                                                   radius[0],
                                                   center_lon[0],
                                                   center_lat[0],
                                                   false_easting,
                                                   false_northing );
        break;
      case GctpFunction.PS:

        stat = GctpFunction.paksz( projparms[4], center_lon );
        if ( stat !=0 ) {
          // error
        }
        stat = GctpFunction.paksz( projparms[5], lat_1 );
        if ( stat !=0 ) {
          // error
        }

        coord_sys = new PolarStereographic( Reference,
                                            r_major[0],
                                            r_minor[0],
                                            center_lon[0],
                                            lat_1[0],
                                            false_easting,
                                            false_northing );
        break;
      case GctpFunction.LAMCC:

        coord_sys = new LambertConformalConic( Reference,
                                               r_major[0],
                                               r_minor[0],
                                               lat_1[0],
                                               lat_2[0],
                                               center_lon[0],
                                               center_lat[0],
                                               false_easting,
                                               false_northing );
        break;
      case GctpFunction.GEO:

        uprLeft[0] = uprLeft[0]*1E-06;
        lwrRight[0] = lwrRight[0]*1E-06;
        uprLeft[1] = uprLeft[1]*1E-06;
        lwrRight[1] = lwrRight[1]*1E-06;
        setUnits = new Unit[2];
        setUnits[0] = CommonUnit.degree;
        setUnits[1] = CommonUnit.degree;

      default:

    }
    return coord_sys;
  }

  public Unit[] getUnits()
  {
    return setUnits;
  }

  public Set getVisADSet( MathType map )
         throws VisADException
  {
    int length1 = xdimsize;
    int length2 = ydimsize;
    Set VisADset;

    if ( projcode == GctpFunction.GEO ) {
      VisADset = new LinearLatLonSet( map, uprLeft[0], lwrRight[0], length1,
                                           lwrRight[1], uprLeft[1] , length2 );
    }
    else {
      VisADset = new Linear2DSet( map, uprLeft[0], lwrRight[0], length1,
                                       lwrRight[1], uprLeft[1] , length2 );
    }
    return VisADset;
  }
}
