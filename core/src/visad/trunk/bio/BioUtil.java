//
// BioUtil.java
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

package visad.bio;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;
import visad.data.DefaultFamily;

/** BioUtil provides a collection of general utilities used by VisBio. */
public class BioUtil {

  /** Loader for opening data series. */
  private static DefaultFamily loader = new DefaultFamily("bio_loader");


  // -- Used in MeasureList and MeasurePool --

  /** Converts the given pixel coordinates to domain coordinates. */
  public static double[] pixelToDomain(DisplayImpl d, int x, int y) {
    return cursorToDomain(d, pixelToCursor(d, x, y));
  }

  /** Converts the given pixel coordinates to cursor coordinates. */
  public static double[] pixelToCursor(DisplayImpl d, int x, int y) {
    MouseBehavior mb = d.getDisplayRenderer().getMouseBehavior();
    VisADRay ray = mb.findRay(x, y);
    return ray.position;
  }

  /** Converts the given cursor coordinates to domain coordinates. */
  public static double[] cursorToDomain(DisplayImpl d, double[] cursor) {
    // locate x, y and z mappings
    Vector maps = d.getMapVector();
    int numMaps = maps.size();
    ScalarMap map_x = null, map_y = null, map_z = null;
    for (int i=0; i<numMaps; i++) {
      if (map_x != null && map_y != null && map_z != null) break;
      ScalarMap map = (ScalarMap) maps.elementAt(i);
      DisplayRealType drt = map.getDisplayScalar();
      if (drt.equals(Display.XAxis)) map_x = map;
      else if (drt.equals(Display.YAxis)) map_y = map;
      else if (drt.equals(Display.ZAxis)) map_z = map;
    }

    // adjust for scale
    double[] scale_offset = new double[2];
    double[] dummy = new double[2];
    double[] values = new double[3];
    if (map_x == null) values[0] = Double.NaN;
    else {
      map_x.getScale(scale_offset, dummy, dummy);
      values[0] = (cursor[0] - scale_offset[1]) / scale_offset[0];
    }
    if (map_y == null) values[1] = Double.NaN;
    else {
      map_y.getScale(scale_offset, dummy, dummy);
      values[1] = (cursor[1] - scale_offset[1]) / scale_offset[0];
    }
    if (map_z == null) values[2] = Double.NaN;
    else {
      map_z.getScale(scale_offset, dummy, dummy);
      values[2] = (cursor[2] - scale_offset[1]) / scale_offset[0];
    }

    return values;
  }


  // -- Used in MeasurePool --

  /**
   * Computes the minimum distance between the point v and the line a-b.
   *
   * @param a Coordinates of the line's first endpoint
   * @param b Coordinates of the line's second endpoint
   * @param v Coordinates of the standalone endpoint
   * @param segment Whether distance computation should be
   *                constrained to the given line segment
   */
  public static double getDistance(double[] a, double[] b, double[] v,
    boolean segment)
  {
    int len = a.length;
     
    // vectors
    double[] ab = new double[len];
    double[] va = new double[len];
    for (int i=0; i<len; i++) {
      ab[i] = a[i] - b[i];
      va[i] = v[i] - a[i];
    }

    // project v onto (a, b)
    double numer = 0;
    double denom = 0;
    for (int i=0; i<len; i++) {
      numer += va[i] * ab[i];
      denom += ab[i] * ab[i];
    }
    double c = numer / denom;
    double[] p = new double[len];
    for (int i=0; i<len; i++) p[i] = c * ab[i] + a[i];

    // determine which point (a, b or p) to use in distance computation
    int flag = 0;
    if (segment) {
      for (int i=0; i<len; i++) {
        if (p[i] > a[i] && p[i] > b[i]) flag = a[i] > b[i] ? 1 : 2;
        else if (p[i] < a[i] && p[i] < b[i]) flag = a[i] < b[i] ? 1 : 2;
        else continue;
        break;
      }
    }

    double sum = 0;
    for (int i=0; i<len; i++) {
      double q;
      if (flag == 0) q = p[i] - v[i]; // use p
      else if (flag == 1) q = a[i] - v[i]; // use a
      else q = b[i] - v[i]; // flag == 2, use b
      sum += q * q;
    }

    return Math.sqrt(sum);
  }


  // -- Used in AlignmentPlane, MeasureManager and MeasureToolPanel --

  /**
   * Gets the distance between the endpoints p and q, using
   * the given conversion values between pixels and microns.
   *
   * @param p Coordinates of the first endpoint
   * @param q Coordinates of the second endpoint
   * @param m Conversion values between microns and pixels
   */
  public static double getDistance(double[] p, double[] q, double[] m) {
    int len = p.length;
    double sum = 0;
    for (int i=0; i<len; i++) {
      double dist = m[i] * (q[i] - p[i]);
      sum += dist * dist;
    }
    return Math.sqrt(sum);
  }


  // -- Used in MeasurePool --

  /**
   * Determines whether the specified line segment
   * intersects the given rectangle.
   *
   * @param x1 X-coordinate of the top-left corner of the rectangle
   * @param y1 Y-coordinate of the top-left corner of the rectangle
   * @param x2 X-coordinate of the bottom-right corner of the rectangle
   * @param y2 Y-coordinate of the bottom-right corner of the rectangle
   * @param ax X-coordinate of the line's first endpoint
   * @param ay Y-coordinate of the line's first endpoint
   * @param bx X-coordinate of the line's second endpoint
   * @param by Y-coordinate of the line's second endpoint
   */
  public static boolean intersects(double x1, double y1,
    double x2, double y2, double ax, double ay, double bx, double by)
  {
    if (x1 > x2) {
      double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      double t = y1;
      y1 = y2;
      y2 = t;
    }

    if (contains(x1, y1, x2, y2, ax, ay) ||
      contains(x1, y1, x2, y2, bx, by))
    {
      // rectangle contains an endpoint
      return true;
    }

    if (ax == bx) {
      // vertical line
      if (ax < x1 || ax > x2) return false;
      return (ay <= y1 && by >= y1) || (ay <= y2 && by >= y2) ||
        (ay >= y1 && by <= y1) || (ay >= y2 && by <= y2);
    }
    if (ay == by) {
      // horizontal line
      if (ay < y1 || ay > y2) return false;
      return (ax <= x1 && bx >= x1) || (ax <= x2 && bx >= x2) ||
        (ax >= x1 && bx <= x1) || (ax >= x2 && bx <= x2);
    }

    double m = (by - ay) / (bx - ax);
    double b = ay - m * ax;

    double left_y = m * x1 + b;
    if (left_y >= y1 && left_y <= y2) return true;
    double right_y = m * x2 + b;
    if (right_y >= y1 && right_y <= y2) return true;
    double top_x = (y1 - b) / m;
    if (top_x >= x1 && top_x <= x2) return true;
    double bottom_x = (y2 - b) / m;
    if (bottom_x >= x1 && bottom_x <= x2) return true;

    return false;
  }

  /**
   * Determines whether the specified endpoint lies within the given rectangle.
   *
   * @param x1 X-coordinate of the top-left corner of the rectangle
   * @param y1 Y-coordinate of the top-left corner of the rectangle
   * @param x2 X-coordinate of the bottom-right corner of the rectangle
   * @param y2 Y-coordinate of the bottom-right corner of the rectangle
   * @param vx X-coordinate of the standalone endpoint
   * @param vy Y-coordinate of the standalone endpoint
   */
  public static boolean contains(double x1, double y1,
    double x2, double y2, double vx, double vy)
  {
    if (x1 > x2) {
      double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      double t = y1;
      y1 = y2;
      y2 = t;
    }
    return vx >= x1 && vx <= x2 && vy >= y1 && vy <= y2;
  }


  // -- Used in ExportDialog and SliceManager --

  /**
   * Loads the data from the given file, and ensures that the
   * resulting data object is of the proper form, converting
   * image data into single-slice stack data if specified.
   */
  public static FieldImpl loadData(File file, boolean makeStack)
    throws VisADException, RemoteException
  {
    // load data from file
    Data data = loader.open(file.getPath());

    // convert data to field
    FieldImpl f = null;
    if (data instanceof FieldImpl) f = (FieldImpl) data;
    else if (data instanceof Tuple) {
      Tuple tuple = (Tuple) data;
      Data[] d = tuple.getComponents();
      for (int i=0; i<d.length; i++) {
        if (d[i] instanceof FieldImpl) {
          f = (FieldImpl) d[i];
          break;
        }
      }
    }
    return makeStack && f instanceof FlatField ?
      makeStack(new FlatField[] {(FlatField) f}) : f;
  }


  // -- Used in ExportDialog --

  /** Converts an array of images to an image stack. */
  public static FieldImpl makeStack(FlatField[] f)
    throws VisADException, RemoteException
  {
    FunctionType func = new FunctionType(
      SliceManager.SLICE_TYPE, f[0].getType());
    FieldImpl stack = new FieldImpl(func, new Integer1DSet(f.length));
    for (int i=0; i<f.length; i++) stack.setSample(i, f[i], false);
    return stack;
  }


  // -- Used in VisBio and SliceManager --

  /**
   * Ensures the color table is of the proper type (RGB or RGBA).
   *
   * If the alpha is not required but the table has an alpha channel,
   * a new table is returned with the alpha channel stripped out.
   *
   * If alpha is required but the table does not have an alpha channel,
   * a new table is returned with an alpha channel matching the provided
   * one (or all 1s if the provided alpha channel is null or invalid).
   */
  public static float[][] adjustColorTable(float[][] table,
    float[] alpha, boolean doAlpha)
  {
    if (table == null || table[0] == null) return null;
    if (table.length == 3) {
      if (!doAlpha) return table;
      int len = table[0].length;
      if (alpha == null || alpha.length != len) {
        alpha = new float[len];
        Arrays.fill(alpha, 1.0f);
      }
      return new float[][] {table[0], table[1], table[2], alpha};
    }
    else { // table.length == 4
      if (doAlpha) return table;
      return new float[][] {table[0], table[1], table[2]};
    }
  }


  // -- Used in SliceManager --

  /** Tests whether two color tables are identical in content. */
  public static boolean tablesEqual(float[][] t1, float[][] t2) {
    if (t1 == null && t2 == null) return true;
    if (t1 == null || t2 == null) return false;
    if (t1.length != t2.length) return false;
    if (t1.length == 0) return true;
    int len = t1[0].length;
    if (len != t2[0].length) return false;
    for (int i=0; i<t1.length; i++) {
      for (int j=0; j<len; j++) {
        if (t1[i][j] != t2[i][j]) return false;
      }
    }
    return true;
  }


  // -- Used in OrthonormalCoordinateSystem --

  /** Projects point p onto line p1-p2. */
  public static double[] project(double[] p1, double[] p2, double[] p) {
    int len = p.length;
    double[] v21 = new double[len];
    double[] vp1 = new double[len];
    double numer = 0, denom = 0;
    for (int i=0; i<len; i++) {
      v21[i] = p2[i] - p1[i];
      vp1[i] = p[i] - p1[i];
      numer += v21[i] * vp1[i];
      denom += v21[i] * v21[i];
    }
    double u = numer / denom;
    double[] q = new double[len];
    for (int i=0; i<len; i++) q[i] = p1[i] + u * v21[i];
    return q;
  }

  /** Normalizes the given vector. */
  public static double[] normalize(double[] v) {
    int len = v.length;
    double factor = 0;
    for (int i=0; i<len; i++) factor += v[i] * v[i];
    factor = Math.sqrt(factor);
    double[] n = new double[len];
    for (int i=0; i<len; i++) n[i] = v[i] / factor;
    return n;
  }

  /** Computes the cross-product of the two given vectors. */
  public static double[] cross(double[] p1, double[] p2) {
    int len = p1.length;
    double[] q = new double[len];
    for (int i=0; i<len; i++) {
      int ndx1 = i;
      int ndx2 = (i + 1) % len;
      q[i] = p1[ndx1] * p2[ndx2] - p1[ndx2] * p2[ndx1];
    }
    return q;
  }


  // -- Used in ArbitrarySlice --

  /**
   * Projects all the points in (x, y, z) onto the line defined by (p1, p2).
   * The points that bound the line segment are stored in (min, max).
   * The projection of the pth point is stored in proj.
   */
  public static void project(float[] x, float[] y, float[] z,
    float p1x, float p1y, float p1z, float p2x, float p2y, float p2z,
    int p, float[] min, float[] max, float[] proj)
  {
    int numpts = x.length - 1;
    float x21 = p2x - p1x;
    float y21 = p2y - p1y;
    float z21 = p2z - p1z;
    float maxdist = x21 * x21 + y21 * y21 + z21 * z21;
    min[0] = p1x;
    min[1] = p1y;
    min[2] = p1z;
    max[0] = p2x;
    max[1] = p2y;
    max[2] = p2z;

    // project all hull points onto line
    for (int p3=0; p3<numpts; p3++) {
      float x31 = x[p3] - p1x;
      float y31 = y[p3] - p1y;
      float z31 = z[p3] - p1z;
      float u = (x31 * x21 + y31 * y21 + z31 * z21) /
        (x21 * x21 + y21 * y21 + z21 * z21);
      float px = p1x + u * x21;
      float py = p1y + u * y21;
      float pz = p1z + u * z21;
      if (p3 == p) {
        proj[0] = px;
        proj[1] = py;
        proj[2] = pz;
      }

      float pminx = px - min[0];
      float pminy = py - min[1];
      float pminz = pz - min[2];
      float pdistmin = pminx * pminx + pminy * pminy + pminz * pminz;
      float pmaxx = px - max[0];
      float pmaxy = py - max[1];
      float pmaxz = pz - max[2];
      float pdistmax = pmaxx * pmaxx + pmaxy * pmaxy + pmaxz * pmaxz;

      if (pdistmin > maxdist || pdistmax > maxdist) {
        if (pdistmin > pdistmax) {
          maxdist = pdistmin;
          max[0] = px;
          max[1] = py;
          max[2] = pz;
        }
        else {
          maxdist = pdistmax;
          min[0] = px;
          min[1] = py;
          min[2] = pz;
        }
      }
    }
  }

  /** Computes the fourth corner of a rectangle, given the first three. */
  public static float[] corner(float[] c1, float[] c2, float[] c3) {
    float[] c4 = new float[c1.length];
    for (int i=0; i<c1.length; i++) c4[i] = c3[i] + c2[i] - c1[i];
    return c4;
  }


  // -- Used in OrthonormalCoordinateSystem --

  /** Computes the fourth corner of a rectangle, given the first three. */
  public static double[] corner(double[] c1, double[] c2, double[] c3) {
    double[] c4 = new double[c1.length];
    for (int i=0; i<c1.length; i++) c4[i] = c3[i] + c2[i] - c1[i];
    return c4;
  }

  // -- Miscellaneous utility methods --
  
  /** Dumps information about the given RealTuple to the screen. */
  public static void dump(RealTuple tuple) {
    Data[] comps = tuple.getComponents();
    for (int i=0; i<comps.length; i++) {
      Real real = (Real) comps[i];
      System.out.println("#" + i +
        ": type=" + real.getType() + "; value=" + real.getValue());
    }
  }

}
