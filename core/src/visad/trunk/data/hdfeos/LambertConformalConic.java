//
// LambertAzimuthalEqualArea.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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
   LambertConformalConic is the VisAD class for coordinate
   systems for ( X_map, Y_map ).<P>
*/

public class LambertConformalConic extends CoordinateSystem
{
  double r_major;
  double es;
  double e;
  double r_minor;
  double lon_center;
  double lat_center;
  double ns;
  double f0;
  double rh;
  double false_easting;
  double false_northing;

  private static Unit[] coordinate_system_units =
   {SI.meter, SI.meter};

  public LambertConformalConic( RealTupleType reference,  //- earth Reference
                                double r_major,           //- semimajor axis
                                double r_minor,           //- semiminor axis
                                double s_lat1,            //- 1st standard latitude
                                double s_lat2,            //- 2nd standard latitude
                                double lon_center,        //- longitude of grid origin
                                double lat_center,        //- latitude of grid origin
                                double false_easting,     //- x offset
                                double false_northing     //- y offset
                                                        )
  throws VisADException
  {
    super( reference, coordinate_system_units );

    this.r_major = r_major;
    this.r_minor = r_minor;
    this.lon_center = lon_center;
    this.lat_center = lat_center;
    this.false_easting = false_easting;
    this.false_northing = false_northing;

    if (Math.abs(s_lat1+s_lat2) < GctpFunction.EPSLN)
    {
      throw new GctpException(
         "Equal latitudes for st. paralles on opposite sides of equator");
    }

    double sin_po;
    double cos_po;
    double con;
    double ms1, ms2;
    double ts0, ts1, ts2;
    double temp = r_minor/r_major;
    es = 1d - temp*temp;
    e = Math.sqrt(es);

    sin_po = Math.sin(s_lat1);
    cos_po = Math.cos(s_lat1);
    con = sin_po;
    ms1 = GctpFunction.msfnz(e, sin_po, cos_po);
    ts1 = GctpFunction.tsfnz(e, s_lat1, sin_po);

    sin_po = Math.sin(s_lat2);
    cos_po = Math.cos(s_lat2);
    ms2 = GctpFunction.msfnz(e, sin_po, cos_po);
    ts2 = GctpFunction.tsfnz(e, s_lat2, sin_po);
    sin_po = Math.sin(lat_center);
    ts0 = GctpFunction.tsfnz(e, lat_center, sin_po);

    if (Math.abs(s_lat1 - s_lat2) > GctpFunction.EPSLN) {
      ns = Math.log (ms1/ms2)/ Math.log (ts1/ts2);
    }
    else {
      ns = con;
    }
    f0 = ms1 / (ns * Math.pow(ts1,ns));
    rh = r_major * f0 * Math.pow(ts0,ns);
  }

  public double[][] toReference(double[][] tuples)
                    throws VisADException
  {
     double rh1;
     double con;
     double ts;
     double theta;
     double x;
     double y;
     double lon;
     double lat;
     long flag;

     int n_tuples = tuples[0].length;
     int tuple_dim = tuples.length;

     if ( tuple_dim != 2) {
       throw new VisADException("LambertConformalConic: tuple dim != 2");
     }

     double t_tuples[][] = new double[2][n_tuples];

     for ( int ii = 0; ii < n_tuples; ii++ ) {
       y = tuples[1][ii];
       x = tuples[0][ii];
       flag = 0;
       x -= false_easting;
       y = rh - y + false_northing;
       if ( ns > 0 ) {
         rh1 = Math.sqrt(x*x + y*y);
         con = 1d;
       }
       else {
         rh1 = -Math.sqrt(x*x + y*y);
         con = -1d;
       }
       theta = 0d;
       if (rh1 != 0) {
         theta = Math.atan2((con*x),(con*y));
       }
       if ((rh1 != 0) || (ns > 0.0)) {
         con = 1.0/ns;
         ts = Math.pow((rh1/(r_major*f0)),con);
         t_tuples[1][ii] = GctpFunction.phi2z(e, ts);
       }
       else {
         t_tuples[1][ii] = -GctpFunction.HALF_PI;
       }
       t_tuples[0][ii] = GctpFunction.adjust_lon(theta/ns + lon_center);
     }

     return t_tuples;
  }

  public double[][] fromReference(double[][] tuples)
                    throws VisADException
  {
    int n_tuples = tuples[0].length;
    int tuple_dim = tuples.length;

    if ( tuple_dim != 2) {
      throw new VisADException("LambertConformalConic: tuple dim != 2");
    }

    double con;
    double ts;
    double sinphi;
    double lat;
    double lon;
    double theta;
    double[] rh1 = new double[n_tuples];
    double t_tuples[][] = new double[2][n_tuples];

    for ( int ii = 0; ii < n_tuples; ii++ )
    {
      lon = tuples[0][ii];
      lat = tuples[1][ii];
      con = Math.abs(Math.abs(lat) - GctpFunction.HALF_PI);
      if ( con > GctpFunction.EPSLN ) {
        sinphi = Math.sin(lat);
        ts = GctpFunction.tsfnz(e, lat, sinphi);
        rh1[ii] = r_major*f0*Math.pow(ts,ns);
      }
      else {
        con = lat*ns;
        if ( con <= 0 ) {
          t_tuples[0][ii] = Double.NaN;
          t_tuples[1][ii] = Double.NaN;
        }
        rh1[ii] = 0d;
      }

      theta = ns*GctpFunction.adjust_lon(lon - lon_center);
      t_tuples[0][ii] = rh1[ii]*Math.sin(theta) + false_easting;
      t_tuples[1][ii] = rh - rh1[ii]*Math.cos(theta) + false_northing;
    }
    return t_tuples;
  }

  public boolean equals(Object cs) {
    return ( cs instanceof LambertConformalConic );
  }

  public static void main(String args[]) throws VisADException
  {
     CoordinateSystem cs_lamcc = null;
     RealType real1;
     RealType real2;
     double[][] values_in = { {-2.3292989, -1.6580627, -1.6580627, -1.6580627},
                              { 0.2127555, 0.4363323, 0.6981317, 0.8726646} };

     real1 = RealType.getRealType("Theta", SI.radian);
     real2 = RealType.getRealType("radius", SI.meter);
     RealType reals[] = {RealType.Longitude,RealType.Latitude};

  //-double r_major = 6378206d;
  //-double r_minor = 6356584d;
     double r_major = 6367470d;
     double r_minor = 6367470d;
  //-double r_major = 6378160d;
  //-double r_minor = 6356775d;
     double s_lat1 = 23*Data.DEGREES_TO_RADIANS;
     double s_lat2 = 27*Data.DEGREES_TO_RADIANS;
     double center_lat = 25*Data.DEGREES_TO_RADIANS;
     double center_lon = -95*Data.DEGREES_TO_RADIANS;
     double false_easting = 0;
     double false_northing = 0;

     RealTupleType Reference = new RealTupleType(reals);

     cs_lamcc = new LambertConformalConic( Reference, r_major, r_minor,
                                           s_lat1, s_lat2,
                                           center_lon, center_lat,
                                           false_easting, false_northing );

     double[][] values_out = cs_lamcc.fromReference( values_in );

     for ( int i=0; i<values_out[0].length; i++)
     {
       System.out.println(values_out[0][i]+",  "+values_out[1][i]);
     }
     System.out.println(" ");

     double[][] values_inR = cs_lamcc.toReference( values_out );
     for ( int i=0; i<values_inR[0].length; i++)
     {
       System.out.println(values_inR[0][i]+",  "+values_inR[1][i]);
     }
  }
}
