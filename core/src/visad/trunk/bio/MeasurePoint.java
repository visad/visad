//
// MeasurePoint.java
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

import java.awt.Color;
import java.util.Vector;

/** An endpoint in the list of measurements. */
public class MeasurePoint extends MeasureThing {

  // -- FIELDS --

  /** Coordinates of this endpoint. */
  double x, y, z;

  /** Preferred color of this endpoint. */
  Color preferredColor;

  /** The number of times this endpoint is selected. */
  int selected;

  /** List of measurement lines that use this endpoint. */
  Vector lines;

  /** Linked pool points from measurement pools. */
  PoolPoint[] pt;


  // -- CONSTRUCTORS --

  /** Constructs an endpoint with the given coordinates. */
  public MeasurePoint(double x, double y, double z) {
    this(x, y, z, Color.white, VisBio.noneGroup);
  }

  /**
   * Constructs an endpoint with the given coordinates,
   * color, group and selection status.
   */
  public MeasurePoint(double x, double y, double z,
    Color color, MeasureGroup group)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.preferredColor = color;
    this.color = color;
    this.group = group;
    lines = new Vector();
    pt = new PoolPoint[MeasurePool.MAX_POOLS];
  }

  /**
   * Constructs an endpoint cloned from the given endpoint,
   * but with a (possibly) different Z value.
   */
  public MeasurePoint(MeasurePoint point, double z) {
    x = point.x;
    y = point.y;
    this.z = z;
    preferredColor = point.preferredColor;
    color = point.color;
    group = point.group;
    lines = new Vector();
    pt = new PoolPoint[MeasurePool.MAX_POOLS];
    stdType = point.stdType;
    stdId = point.stdId;
  }


  // -- API METHODS --

  /** Sets the point's preferred color to the given one. */
  public void setColor(Color color) {
    preferredColor = color;
    refreshColor();
  }

  /** Sets the coordinates of the endpoint to match those given. */
  public void setCoordinates(PoolPoint p, double x, double y, double z) {
    if (this.x == x && this.y == y && this.z == z) return;
    this.x = x;
    this.y = y;
    this.z = z;
    for (int i=0; i<pt.length; i++) {
      if (pt[i] != null && pt[i] != p) pt[i].refresh();
    }
  }

  /** Sets the point's color to match its associated lines. */
  public void refreshColor() {
    if (lines.isEmpty()) color = preferredColor;
    else {
      int size = lines.size();
      Color c = ((MeasureLine) lines.elementAt(0)).color;
      for (int i=1; i<size; i++) {
        MeasureLine line = (MeasureLine) lines.elementAt(i);
        if (line.color != c) {
          color = Color.white;
          return;
        }
      }
      color = c;
    }
  }

}
