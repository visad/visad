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

  public LinearVectorPointMethod(double[][] lonlat_s)
         throws VisADException
  {
    scale_1 = 1;
    scale_2 = (1+Math.sin(lonlat_s[1][0]))/(1+Math.sin(lonlat_s[1][1]));
    scale_3 = (1+Math.sin(lonlat_s[1][0]))/(1+Math.sin(lonlat_s[1][2]));

    CoordinateSystem cs = new PolarStereographic(lonlat_s[0][0], lonlat_s[1][0]);
    double[][] verts_xy = cs.fromReference(lonlat_s);

    double[][] del_xy = new double[2][lonlat_s[0].length];
    double[] centroid_xy = triangleCentroid(verts_xy);

    double[][] values = new double[2][1];
    values[0][0] = centroid_xy[0];
    values[1][0] = centroid_xy[1];

    double[][] centroid_ll = cs.toReference(values);

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

  public double[][] getKinematics( double[][] uv_wind )
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

    return x.getValues();
  }

  public static void main(String args[])
         throws VisADException
  {
    double[][] lonlat_s = new double[2][3];
    lonlat_s[0][0] = -100*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][0] =   40*Data.DEGREES_TO_RADIANS;

    lonlat_s[0][1] = -104*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][1] =   45*Data.DEGREES_TO_RADIANS;

    lonlat_s[0][2] =  -96*Data.DEGREES_TO_RADIANS;
    lonlat_s[1][2] =   45*Data.DEGREES_TO_RADIANS;

    LinearVectorPointMethod lvpm = new LinearVectorPointMethod(lonlat_s);

    double[][] uv_wind = new double[2][3];

    uv_wind[0][0] = 20;
    uv_wind[1][0] = 0;
    uv_wind[0][1] = 20;
    uv_wind[1][1] = 0;
    uv_wind[0][2] = -20;
    uv_wind[1][2] = 0;

    double[][] div_vort = lvpm.getKinematics(uv_wind);

    for ( int ii = 0; ii < div_vort[0].length; ii++ ) {
      System.out.println(div_vort[0][ii]);
    }
  }

  public static double[] triangleCentroid(double[][] verts_xy)
  {
    double[] centroid_xy = new double[2];
    double[] midpoint_12 = new double[2];
    double[] midpoint_13 = new double[2];
    double[] midpoint_23 = new double[2];

    double slope_12_3;
    double slope_13_2;
    double slope_23_1;
    double yintercept_12_3;
    double yintercept_13_2;
    double yintercept_23_1;

    midpoint_12[0] = (verts_xy[0][1] - verts_xy[0][0])/2 + verts_xy[0][0];
    midpoint_12[1] = (verts_xy[1][1] - verts_xy[1][0])/2 + verts_xy[1][0];

    midpoint_13[0] = (verts_xy[0][2] - verts_xy[0][0])/2 + verts_xy[0][0];
    midpoint_13[1] = (verts_xy[1][2] - verts_xy[1][0])/2 + verts_xy[1][0];

    midpoint_23[0] = (verts_xy[0][2] - verts_xy[0][1])/2 + verts_xy[0][1];
    midpoint_23[1] = (verts_xy[1][2] - verts_xy[1][1])/2 + verts_xy[1][1];

    slope_12_3 = (verts_xy[1][2] - midpoint_12[1])/(verts_xy[0][2] - midpoint_12[0]);
    slope_13_2 = (verts_xy[1][1] - midpoint_13[1])/(verts_xy[0][1] - midpoint_13[0]);

    yintercept_12_3 = verts_xy[1][2] - slope_12_3*verts_xy[0][2];
    yintercept_13_2 = verts_xy[1][1] - slope_13_2*verts_xy[0][1];

    centroid_xy[0] = (yintercept_12_3 - yintercept_13_2)/(slope_13_2 - slope_12_3);
    centroid_xy[1] = slope_12_3*centroid_xy[0] + yintercept_12_3;

    return centroid_xy;
  }
}
