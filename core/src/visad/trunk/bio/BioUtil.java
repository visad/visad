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

import java.util.Vector;
import visad.*;

/** BioUtil provides a collection of general utilities used by BioVisAD. */
public class BioUtil {

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

  /**
   * Computes the minimum distance between the point (vx, vy)
   * and the line (ax, ay)-(bx, by).
   *
   * @param ax X-coordinate of the line's first endpoint
   * @param ay Y-coordinate of the line's first endpoint
   * @param bx X-coordinate of the line's second endpoint
   * @param by Y-coordinate of the line's second endpoint
   * @param vx X-coordinate of the standalone endpoint
   * @param vy Y-coordinate of the standalone endpoint
   */
  public static double getDistance(double ax, double ay,
    double bx, double by, double vx, double vy)
  {
    // vectors
    double abx = ax - bx;
    double aby = ay - by;
    double vax = vx - ax;
    double vay = vy - ay;

    // project v onto (a, b)
    double c = (vax * abx + vay * aby) / (abx * abx + aby * aby);
    double px = c * abx + ax;
    double py = c * aby + ay;

    // determine which point (a, b or p) to use in distance computation
    int flag = 0;
    if (px > ax && px > bx) flag = ax > bx ? 1 : 2;
    else if (px < ax && px < bx) flag = ax < bx ? 1 : 2;
    else if (py > ay && py > by) flag = ay > by ? 1 : 2;
    else if (py < ay && py < by) flag = ay < by ? 1 : 2;

    double x, y;
    if (flag == 0) { // use p
      x = px - vx;
      y = py - vy;
    }
    else if (flag == 1) { // use a
      x = ax - vx;
      y = ay - vy;
    }
    else { // flag == 2, use b
      x = bx - vx;
      y = by - vy;
    }

    return Math.sqrt(x * x + y * y);
  }

  /**
   * Gets the distance between the specified endpoints, using
   * the given conversion values between pixels and microns,
   * and distance between measurement slices.
   *
   * @param x1 X-coordinate of the first endpoint
   * @param y1 Y-coordinate of the first endpoint
   * @param z1 Z-coordinate of the first endpoint
   * @param x2 X-coordinate of the second endpoint
   * @param y2 Y-coordinate of the second endpoint
   * @param z2 Z-coordinate of the second endpoint
   * @param mx Microns per pixel along X-axis
   * @param my Microns per pixel along Y-axis
   * @param sd Micron distance between Z-slices
   */
  public static double getDistance(double x1, double y1, double z1,
    double x2, double y2, double z2, double mx, double my, double sd)
  {
    double distx = mx * (x2 - x1);
    double disty = my * (y2 - y1);
    double distz = sd * (z2 - z1);
    return Math.sqrt(distx * distx + disty * disty + distz * distz);
  }

  /**
   * Determines whether the specified line intersects the given rectangle.
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

}
