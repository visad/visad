//
// CursorUtil.java
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

package visad.util;

import java.util.Vector;
import visad.*;

/**
 * Utility methods for cursor-related functions, including converting between
 * pixel, cursor and domain coordinates, and evaluating functions at a given
 * cursor location.
 */
public class CursorUtil {

  /**
   * Gets scale values (multiplier and offset) for the X, Y and Z maps
   * corresponding to the given RealTypes (or the first ScalarMaps to
   * X, Y and Z if types is null). If no mapping to a spatial axis is
   * found, that component of the array will be null.
   *
   * @return Scale array of size [3][2], with the first dimension
   * corresponding to X, Y or Z, and the second giving multiplier and offset.
   * For example, cursor_x = scale[0][0] * domain_x + scale[0][1].
   */
  public static double[][] getScaleValues(DisplayImpl d, RealType[] types) {
    // locate x, y and z mappings
    Vector maps = d.getMapVector();
    int numMaps = maps.size();
    ScalarMap mapX = null, mapY = null, mapZ = null;
    for (int i=0; i<numMaps; i++) {
      if (mapX != null && mapY != null && mapZ != null) break;
      ScalarMap map = (ScalarMap) maps.elementAt(i);
      if (types == null) {
        DisplayRealType drt = map.getDisplayScalar();
        if (drt.equals(Display.XAxis)) mapX = map;
        else if (drt.equals(Display.YAxis)) mapY = map;
        else if (drt.equals(Display.ZAxis)) mapZ = map;
      }
      else {
        ScalarType st = map.getScalar();
        if (st.equals(types[0])) mapX = map;
        if (st.equals(types[1])) mapY = map;
        if (st.equals(types[2])) mapZ = map;
      }
    }

    // get scale values
    double[][] scale = new double[3][];
    double[] dummy = new double[2];
    if (mapX == null) scale[0] = null;
    else {
      scale[0] = new double[2];
      mapX.getScale(scale[0], dummy, dummy);
    }
    if (mapY == null) scale[1] = null;
    else {
      scale[1] = new double[2];
      mapY.getScale(scale[1], dummy, dummy);
    }
    if (mapZ == null) scale[2] = null;
    else {
      scale[2] = new double[2];
      mapZ.getScale(scale[2], dummy, dummy);
    }
    return scale;
  }

  /** Converts the given cursor coordinates to domain coordinates. */
  public static double[] cursorToDomain(DisplayImpl d,
    RealType[] types, double[] cursor)
  {
    if (d == null) return null;
    double[][] scale = getScaleValues(d, types);
    double[] domain = new double[3];
    for (int i=0; i<3; i++) {
      domain[i] = scale[i] == null ? 0 :
        (cursor[i] - scale[i][1]) / scale[i][0];
    }
    return domain;
  }

  /** Converts the given domain coordinates to cursor coordinates. */
  public static double[] domainToCursor(DisplayImpl d,
    RealType[] types, double[] domain)
  {
    if (d == null) return null;
    double[][] scale = getScaleValues(d, types);
    double[] cursor = new double[3];
    for (int i=0; i<3; i++) {
      cursor[i] = scale[i] == null ? 0 :
        scale[i][0] * domain[i] + scale[i][1];
    }
    return cursor;
  }

  /** Converts the given cursor coordinates to domain coordinates. */
  public static double[] cursorToDomain(DisplayImpl d, double[] cursor) {
    return cursorToDomain(d, null, cursor);
  }

  /** Converts the given domain coordinates to cursor coordinates. */
  public static double[] domainToCursor(DisplayImpl d, double[] domain) {
    return domainToCursor(d, null, domain);
  }

  /** Converts the given pixel coordinates to cursor coordinates. */
  public static double[] pixelToCursor(DisplayImpl d, int x, int y) {
    if (d == null) return null;
    MouseBehavior mb = d.getDisplayRenderer().getMouseBehavior();
    VisADRay ray = mb.findRay(x, y);
    return ray.position;
  }

  /** Converts the given cursor coordinates to pixel coordinates. */
  public static int[] cursorToPixel(DisplayImpl d, double[] cursor) {
    if (d == null) return null;
    MouseBehavior mb = d.getDisplayRenderer().getMouseBehavior();
    return mb.getScreenCoords(cursor);
  }

  /** Converts the given pixel coordinates to domain coordinates. */
  public static double[] pixelToDomain(DisplayImpl d, int x, int y) {
    return cursorToDomain(d, pixelToCursor(d, x, y));
  }

  /** Converts the given domain coordinates to pixel coordinates. */
  public static int[] domainToPixel(DisplayImpl d, double[] domain) {
    return cursorToPixel(d, domainToCursor(d, domain));
  }

}
