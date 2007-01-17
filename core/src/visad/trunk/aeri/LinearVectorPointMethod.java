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

package visad.aeri;

import visad.*;
import visad.matrix.*;
import visad.data.hdfeos.PolarStereographic;

import java.lang.reflect.*;

public class LinearVectorPointMethod
{
  JamaMatrix jm_A;
  double scale_1;
  double scale_2;
  double scale_3;
  double[][] centroid_ll;

  public LinearVectorPointMethod(double[][] lonlat_s)
         throws VisADException
  {
    if ( lonlat_s[0].length != 3 ) {
      throw new VisADException("number of points must equal 3");
    }

    scale_1 = 1;
    scale_2 = (1+Math.sin(lonlat_s[1][0]))/(1+Math.sin(lonlat_s[1][1]));
    scale_3 = (1+Math.sin(lonlat_s[1][0]))/(1+Math.sin(lonlat_s[1][2]));

    CoordinateSystem cs = new PolarStereographic(lonlat_s[0][0], lonlat_s[1][0]);
    double[][] verts_xy = cs.fromReference(lonlat_s);

    double[][] del_xy = new double[2][lonlat_s[0].length];
    double[] centroid_xy = triangleCentroid(verts_xy);

    centroid_ll = cs.toReference(new double[][] 
                                                { {centroid_xy[0]}, {centroid_xy[1]} });
    System.out.println("centroid lon: "+centroid_ll[0][0]*Data.RADIANS_TO_DEGREES);
    System.out.println("centroid lat: "+centroid_ll[1][0]*Data.RADIANS_TO_DEGREES);

    double scale_c = (1+Math.sin(lonlat_s[1][0]))/(1+Math.sin(centroid_ll[1][0]));
    double scale_c_squared = scale_c*scale_c;

    del_xy[0][0] = verts_xy[0][0] - centroid_xy[0];
    del_xy[1][0] = verts_xy[1][0] - centroid_xy[1];

    del_xy[0][1] = verts_xy[0][1] - centroid_xy[0];
    del_xy[1][1] = verts_xy[1][1] - centroid_xy[1];

    del_xy[0][2] = verts_xy[0][2] - centroid_xy[0];
    del_xy[1][2] = verts_xy[1][2] - centroid_xy[1];

    double[][] X_values = new double[6][6];

    X_values[0][0] = 1;
    X_values[0][1] = 0;
    X_values[0][2] = 1;
    X_values[0][3] = 0;
    X_values[0][4] = 1;
    X_values[0][5] = 0;

    X_values[1][0] = 0;
    X_values[1][1] = 1;
    X_values[1][2] = 0;
    X_values[1][3] = 1;
    X_values[1][4] = 0;
    X_values[1][5] = 1;

    X_values[2][0] = del_xy[0][0]/scale_c_squared;
    X_values[2][1] = -del_xy[1][0]/scale_c_squared;
    X_values[2][2] = del_xy[0][1]/scale_c_squared;
    X_values[2][3] = -del_xy[1][1]/scale_c_squared;
    X_values[2][4] = del_xy[0][2]/scale_c_squared;
    X_values[2][5] = -del_xy[1][2]/scale_c_squared;

    X_values[3][0] = del_xy[1][0]/scale_c_squared;
    X_values[3][1] = del_xy[0][0]/scale_c_squared;
    X_values[3][2] = del_xy[1][1]/scale_c_squared;
    X_values[3][3] = del_xy[0][1]/scale_c_squared;
    X_values[3][4] = del_xy[1][2]/scale_c_squared;
    X_values[3][5] = del_xy[0][2]/scale_c_squared;

    X_values[4][0] = del_xy[0][0]/scale_c_squared;
    X_values[4][1] = del_xy[1][0]/scale_c_squared;
    X_values[4][2] = del_xy[0][1]/scale_c_squared;
    X_values[4][3] = del_xy[1][1]/scale_c_squared;
    X_values[4][4] = del_xy[0][2]/scale_c_squared;
    X_values[4][5] = del_xy[1][2]/scale_c_squared;


    X_values[5][0] = -del_xy[1][0]/scale_c_squared;
    X_values[5][1] = del_xy[0][0]/scale_c_squared;
    X_values[5][2] = -del_xy[1][1]/scale_c_squared;
    X_values[5][3] = del_xy[0][1]/scale_c_squared;
    X_values[5][4] = -del_xy[1][2]/scale_c_squared;
    X_values[5][5] = del_xy[0][2]/scale_c_squared;

    try {
      jm_A = new JamaMatrix(X_values);
      jm_A = jm_A.transpose();
    }
    catch (IllegalAccessException e) {
    }
    catch (InstantiationException e) {
    }
    catch (InvocationTargetException e) {
    }
  }

  public double[][] getCentroid() 
  {
    return centroid_ll;
  }
    
  public double[] getKinematics( double[][] uv_wind )
         throws VisADException
  {
    double[][] values = new double[1][6];

    values[0][0] = uv_wind[0][0]/scale_1;
    values[0][1] = uv_wind[1][0]/scale_1;
    values[0][2] = uv_wind[0][1]/scale_2;
    values[0][3] = uv_wind[1][1]/scale_2;
    values[0][4] = uv_wind[0][2]/scale_3;
    values[0][5] = uv_wind[1][2]/scale_3;

    JamaMatrix x = null;
    try {
      JamaMatrix jm_b = new JamaMatrix(values);
      x = jm_A.solve(jm_b.transpose());
    }
    catch (IllegalAccessException e) {
    }
    catch (InstantiationException e) {
    }
    catch (InvocationTargetException e) {
    }

    return (x.getValues())[0];
  }

  public static void main(String args[])
         throws VisADException
  {
    double[][] lonlat_s = new double[2][3];
    lonlat_s[0][0] = -97.485*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][0] =   36.605*Data.DEGREES_TO_RADIANS;

    lonlat_s[0][1] = -99.204*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][1] =   36.072*Data.DEGREES_TO_RADIANS;

    lonlat_s[0][2] =  -97.522*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][2] =   34.984*Data.DEGREES_TO_RADIANS;

    LinearVectorPointMethod lvpm = new LinearVectorPointMethod(lonlat_s);

    double[][] uv_wind = new double[2][3];

    uv_wind[0][0] = 6.5;
    uv_wind[1][0] = 19.8;
    uv_wind[0][1] = 11.0;
    uv_wind[1][1] = 9.2;
    uv_wind[0][2] = 8.2;
    uv_wind[1][2] = 11.7;

    double[] div_vort = lvpm.getKinematics(uv_wind);

    for ( int ii = 0; ii < div_vort.length; ii++ ) {
      System.out.println(div_vort[ii]);
    }
    System.out.println(+(5.0*Double.NaN));
  }

  private static double[] triangleCentroid(double[][] verts)
  {
    double[] centroid_xy = new double[2];
    double[][] midpoint_12 = new double[2][1];
    double[][] midpoint_13 = new double[2][1];

    double[][] verts_xy = new double[2][3];
    verts_xy[0][0] = verts[0][0];
    verts_xy[0][1] = verts[0][1];
    verts_xy[0][2] = verts[0][2];
    verts_xy[1][0] = verts[1][0];
    verts_xy[1][1] = verts[1][1];
    verts_xy[1][2] = verts[1][2];

    double slope_12_3;
    double slope_13_2;
    double slope_23_1;
    double yintercept_12_3;
    double yintercept_13_2;
    double yintercept_23_1;

    boolean rotate = false;
    double rot_angle;

    midpoint_12[0][0] = (verts_xy[0][1] - verts_xy[0][0])/2 + verts_xy[0][0];
    midpoint_12[1][0] = (verts_xy[1][1] - verts_xy[1][0])/2 + verts_xy[1][0];

    midpoint_13[0][0] = (verts_xy[0][2] - verts_xy[0][0])/2 + verts_xy[0][0];
    midpoint_13[1][0] = (verts_xy[1][2] - verts_xy[1][0])/2 + verts_xy[1][0];

    slope_12_3 = (verts_xy[1][2] - midpoint_12[1][0])/(verts_xy[0][2] - midpoint_12[0][0]);
    slope_13_2 = (verts_xy[1][1] - midpoint_13[1][0])/(verts_xy[0][1] - midpoint_13[0][0]);

    if (Double.isInfinite(slope_12_3) || Double.isInfinite(slope_13_2)) 
    {
      System.out.println("infinite slope");
      rotate_clockwise(verts_xy, 90*Data.DEGREES_TO_RADIANS);
      rotate_clockwise(midpoint_12, 90*Data.DEGREES_TO_RADIANS);
      rotate_clockwise(midpoint_13, 90*Data.DEGREES_TO_RADIANS);
      rotate = true;
    } 

    yintercept_12_3 = verts_xy[1][2] - slope_12_3*verts_xy[0][2];
    yintercept_13_2 = verts_xy[1][1] - slope_13_2*verts_xy[0][1];

    centroid_xy[0] = (yintercept_12_3 - yintercept_13_2)/(slope_13_2 - slope_12_3);
    centroid_xy[1] = slope_12_3*centroid_xy[0] + yintercept_12_3;

    if ( rotate == true ) {
      //-rotate centroid back
      double[][] xy = new double[2][1];
      xy[0][0] = centroid_xy[0];
      xy[1][0] = centroid_xy[1];
      rotate_clockwise(xy, -90*Data.DEGREES_TO_RADIANS);
      centroid_xy[0] = xy[0][0];
      centroid_xy[1] = xy[1][0];
    }
    return centroid_xy;
  }

  private static void rotate_clockwise( double[][] points, double rot_angle )
  {
    for ( int ii = 0; ii < points[0].length; ii++ ) {
      double x = points[0][ii];
      double y = points[1][ii];
      double angle = Math.atan2(y,x);
      double r = Math.sqrt(x*x + y*y);
      points[0][ii] = r*Math.cos(angle - rot_angle);
      points[1][ii] = r*Math.sin(angle - rot_angle);
    }
  }
}
