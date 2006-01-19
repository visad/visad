//
// PolarStereographic.java
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

/**
   PolarStereographic is the VisAD class for coordinate
   systems for ( X_map, Y_map ).<P>
*/

public class PolarStereographic extends CoordinateSystem 
{
  double r_major;                // major axis
  double r_minor;                // minor axis
  double es;                     // eccentricity squared
  double e;                      // eccentricity
  double e4;                     // e4 calculated from eccentricity
  double center_lon;             // center longitude
  double center_lat;             // center latitude
  double fac;                    // sign variable
  double ind;                    // flag variable
  double mcs;                    // small m
  double tcs;                    // small t
  double false_northing;         // y offset in meters
  double false_easting;          // x offset in meters


  private static Unit[] coordinate_system_units =
    {null, null};

  public PolarStereographic( double lon_center, 
                             double lat_center 
                                               )
         throws VisADException
  {
    this(RealTupleType.SpatialEarth2DTuple, 6371230, 6371230,
         lon_center, lat_center, 0, 0); 
  }

  public PolarStereographic( double r_major,
                             double r_minor,
                             double lon_center,
                             double lat_center 
                                               )
         throws VisADException
  {
     this(RealTupleType.SpatialEarth2DTuple, 
          r_major, r_minor, lon_center, lat_center, 0, 0);
  }

  public PolarStereographic( RealTupleType reference,
                             double r_major,
                             double r_minor,
                             double lon_center,
                             double lat_center
                                                     )
         throws VisADException
  {
    this(reference, r_major, r_minor, lon_center, lat_center, 0, 0);
  }

  /*-  GRIB/AWIPS friendly static methods. 
       Note: Lon/Lat must be in unit radians
   ---------------------------------------------*/

  public static PolarStereographic 
         makePolarStereographic( RealTupleType reference,  //- Earth Reference
                                 double La1,               //- Latitude of first grid point
                                 double Lo1,               //- Longitude of first grid point
                                 double Lov                //- The orientation of grid
                                                     )
         throws VisADException
  {
    return makePolarStereographic(reference, 6371230, 6371230, La1, Lo1, Lov, 
                                  60*Data.DEGREES_TO_RADIANS);
  }
                                         
  public static PolarStereographic
         makePolarStereographic( RealTupleType reference,  //- Earth Reference
                                 double r_major,           //- Earth major axis
                                 double r_minor,           //- Earth minor axis
                                 double La1,               //- Latitude of first grid point
                                 double Lo1,               //- Longitude of first grid point
                                 double Lov,               //- The orientation of grid
                                 double lat_center         //- Latitude of true scale
                                                          )
         throws VisADException
  {
    PolarStereographic ps =
      new PolarStereographic(reference, r_major, r_minor, Lov, lat_center, 0, 0);

    double[][] values =
      ps.fromReference(new double[][] {{Lo1}, {La1}});
    
    double false_easting = values[0][0];
    double false_northing = values[1][0];

    return 
      new PolarStereographic(reference,
                             r_major, r_minor, Lov, lat_center,
                             -false_easting, -false_northing);
  }

  public PolarStereographic( RealTupleType reference,   //- Earth Reference
                             double r_major,            //- Earth major axis
                             double r_minor,            //- Earth minor axis
                             double lon_center,         //- Longitude down below pole of map
                             double lat_center,         //- Latitude of true scale
                             double false_easting,      //- x_axis offset
                             double false_northing      //- y_axis offset
                                                     )
  throws VisADException
  {
    super( reference, coordinate_system_units );

    this.r_major = r_major;
    this.r_minor = r_minor;
    this.center_lon = lon_center;
    this.center_lat = lat_center;
    this.false_easting = false_easting;
    this.false_northing = false_northing;

    double temp;                            // temporary variable
    double con1;                            // temporary angle
    double sinphi;                          // sin value
    double cosphi;                          // cos value
    double[] dum_1 = new double[1];
    double[] dum_2 = new double[1];
    double[] dum_3 = new double[1];

    temp = r_minor / r_major;
    es = 1.0 - temp*temp;
    e = Math.sqrt(es);
    e4 = GctpFunction.e4fn(e);

    if ( lat_center < 0) {
      fac = -1.0;
    }
    else {
      fac = 1.0;
    }

    ind = 0;
    if (Math.abs(Math.abs(lat_center) - GctpFunction.HALF_PI) > GctpFunction.EPSLN )
    {
      ind = 1;
      con1 = fac * center_lat;
      dum_1[0] = con1;
      GctpFunction.sincos(dum_1, dum_2, dum_3);
      sinphi = dum_2[0];
      cosphi = dum_3[0];
      mcs = GctpFunction.msfnz(e,sinphi,cosphi);
      tcs = GctpFunction.tsfnz(e,con1,sinphi);
    }
  }

  public double[][] toReference(double[][] tuples) 
         throws VisADException 
  {
    double x;
    double y;
    double rh;                      // height above ellipsiod
    double ts;                      // small value t
    double temp;                    // temporary variable
    long   flag;                    // error flag
    double lon;
    double lat;

    int n_tuples = tuples[0].length;
    int tuple_dim = tuples.length;

    if ( tuple_dim != 2) {
      throw new VisADException("PolarStereographic: tuple dim != 2");
    }

    double t_tuples[][] = new double[2][n_tuples];

    for ( int ii = 0; ii < n_tuples; ii++ ) 
    {
      x = (tuples[0][ii] - false_easting)*fac;
      y = (tuples[1][ii] - false_northing)*fac;
      rh = Math.sqrt(x * x + y * y);

      if (ind != 0) {
        ts = rh * tcs/(r_major * mcs);
      }
      else {
        ts = rh * e4 / (r_major * 2.0);
      }

      lat = GctpFunction.phi2z(e,ts);
      if ( lat == Double.NaN ) {
      }
      else {
        lat = lat*fac;
      }

      if (rh == 0) {
        lon = fac * center_lon;
      }
      else {
        temp = Math.atan2(x, -y);
        lon = GctpFunction.adjust_lon(fac *temp + center_lon);
      }

      t_tuples[0][ii] = lon;
      t_tuples[1][ii] = lat;
    }
    return t_tuples;
  }

  public double[][] fromReference(double[][] tuples) 
         throws VisADException 
  {
    int n_tuples = tuples[0].length;
    int tuple_dim = tuples.length;
    double con1;                    // adjusted longitude
    double con2;                    // adjusted latitude
    double rh;                      // height above ellipsoid
    double sinphi;                  // sin value
    double ts;                      // value of small t
    double x;
    double y;
    double lat;
    double lon;

    if ( tuple_dim != 2) {
      throw new VisADException("PolarStereographic: tuple dim != 2");
    }

    double t_tuples[][] = new double[2][n_tuples];

    for ( int ii = 0; ii < n_tuples; ii++ ) 
    {
      lon = tuples[0][ii];
      lat = tuples[1][ii];

      con1 = fac * GctpFunction.adjust_lon(lon - center_lon);
      con2 = fac * lat;
      sinphi = Math.sin(con2);
      ts = GctpFunction.tsfnz(e,con2,sinphi);
      if (ind != 0) {
        rh = r_major * mcs * ts / tcs;
      }
      else {
        rh = 2.0 * r_major * ts / e4;
      }

      t_tuples[0][ii] = fac * rh * Math.sin(con1) + false_easting;
      t_tuples[1][ii] = -fac * rh * Math.cos(con1) + false_northing;
    }
    return t_tuples;
  }

  public boolean equals(Object cs) {
    return (cs instanceof PolarStereographic);
  }

  public static void main(String args[]) throws VisADException 
  {
    double r_major = 6371230; 
    double r_minor = 6371230;
    double center_lat = 40*Data.DEGREES_TO_RADIANS;
    double center_lon = -100*Data.DEGREES_TO_RADIANS;
    double false_easting = 0;
    double false_northing = 0;

    double[][] values_in = { {-2.3292989, -1.6580627, -1.6580627, -1.6580627, center_lon},
                             { 0.2127555, 0.4363323, 0.6981317, 0.8726646, 90*Data.DEGREES_TO_RADIANS} };

    RealType[] reals = {RealType.Longitude, RealType.Latitude};
    RealTupleType reference = new RealTupleType(reals);

    CoordinateSystem cs = new PolarStereographic( reference,
                                                  r_major,
                                                  r_minor,
                                                  center_lon,
                                                  center_lat,
                                                  false_easting,
                                                  false_northing );

    double[][] values = cs.fromReference(values_in);

    for ( int i=0; i<values_in[0].length; i++) {
       System.out.println(values_in[0][i]+",  "+values_in[1][i]);
    }

    System.out.println("--------------------------\n");

    for ( int i=0; i<values[0].length; i++) {
       System.out.println(values[0][i]+",  "+values[1][i]);
    }

    double[][] values_R = cs.toReference(values);

    System.out.println("--------------------------\n");

    for ( int i=0; i<values_R[0].length; i++) {
      System.out.println(values_R[0][i]+",  "+values_R[1][i]);
    }


    PolarStereographic ps = 
      makePolarStereographic(reference, -20.826*Data.DEGREES_TO_RADIANS,
                             -150.000*Data.DEGREES_TO_RADIANS,
                             -105.000*Data.DEGREES_TO_RADIANS);

    values = ps.fromReference(new double[][] {{-150*Data.DEGREES_TO_RADIANS},
                                              {-20.826*Data.DEGREES_TO_RADIANS}});
    System.out.println("x: "+values[0][0]);
    System.out.println("y: "+values[1][0]);

    values = ps.fromReference(new double[][] {{-105*Data.DEGREES_TO_RADIANS},
                                              {90*Data.DEGREES_TO_RADIANS}});
    System.out.println("x: "+values[0][0]);
    System.out.println("y: "+values[1][0]);
  }
}
