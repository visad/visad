//
// PolarStereographic.java
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

/**
   LambertAzimuthalEqualArea is the VisAD class for cordinate
   systems for ( X_map, Y_map ).<P>
*/

public class PolarStereographic extends CoordinateSystem {

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
    {SI.radian, SI.radian};

  public PolarStereographic( RealTupleType reference, 
                             double r_major, 
                             double r_minor, 
                             double lon_center, 
                             double lat_center,
                             double false_easting, 
                             double false_northing 
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
    Double dum_1 = null;
    Double dum_2 = null;

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
      GctpFunction.sincos(con1, dum_1, dum_2);
      sinphi = dum_1.doubleValue();
      cosphi = dum_2.doubleValue();
      mcs = GctpFunction.msfnz(e,sinphi,cosphi);
      tcs = GctpFunction.tsfnz(e,con1,sinphi);
    }

  }

  public double[][] toReference(double[][] tuples) throws VisADException {

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
       throw new VisADException("LambertAzimuthalEqualArea: tuple dim != 2");
     }

     double t_tuples[][] = new double[2][n_tuples];

     for ( int ii = 0; ii < n_tuples; ii++ ) {

       x = (tuples[0][ii] - false_easting)*fac;
       y = (tuples[0][ii] - false_northing)*fac;
       rh = Math.sqrt(x * x + y * y);

       if (ind != 0) {
         ts = rh * tcs/(r_major * mcs);
       }
       else {
         ts = rh * e4 / (r_major * 2.0);
       }

       lat = GctpFunction.phi2z(e,ts);
       if ( lat == Double.NaN ) {
          // exit 
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

       t_tuples[0][ii] = lat;
       t_tuples[1][ii] = lon;
     }

     return t_tuples;
  }

  public double[][] fromReference(double[][] tuples) throws VisADException {

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
        throw new VisADException("LambertAzimuthalEqualArea: tuple dim != 2");
     }

     double t_tuples[][] = new double[2][n_tuples];

     for ( int ii = 0; ii < n_tuples; ii++ ) {

       lat = tuples[0][ii];
       lon = tuples[1][ii];

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
       t_tuples[0][ii] = -fac * rh * Math.cos(con1) + false_northing;;
     } 

     return t_tuples;
  }

  public boolean equals(Object cs) {
    return ( cs instanceof LambertAzimuthalEqualArea );
  }


  public static void main(String args[]) throws VisADException {


     CoordinateSystem coord_cs1 = null;
     RealType real1;
     RealType real2;
     double[][] value_in = { {0, .5236, 1.0472, 1.5708}, {1, 1, 1, 1}};
     double[][] value_out = new double[2][4];

     real1 = new RealType("Theta", SI.radian, null);
     real2 = new RealType("radius", SI.meter, null);
     RealType reals[] = {real1, real2};

     RealTupleType Reference = new RealTupleType(reals);

   //  coord_cs1 = new LambertAzimuthalEqualArea( Reference, null );

     RealTupleType tuple1 = new RealTupleType( reals, coord_cs1, null);

     value_out = tuple1.getCoordinateSystem().fromReference( value_in );

     for ( int i=0; i<value_out[0].length; i++) {
        System.out.println(value_out[0][i]+",  "+value_out[1][i]);
     }

     value_in = tuple1.getCoordinateSystem().toReference( value_out );

     for ( int i=0; i<value_in[0].length; i++) {
        System.out.println(value_in[0][i]+",  "+value_in[1][i]);
     }

     Unit kilometer = new ScaledUnit(1000, SI.meter);

  }
}
