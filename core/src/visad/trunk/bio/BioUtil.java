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
   */
  public static double distance(double ax, double ay,
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
   */
  public static double getDistance(double x1, double y1, double z1,
    double x2, double y2, double z2, double mx, double my, double sd)
  {
    double distx = mx * (x2 - x1);
    double disty = my * (y2 - y1);
    double distz = sd * (z2 - z1);
    return Math.sqrt(distx * distx + disty * disty + distz * distz);
  }

}
