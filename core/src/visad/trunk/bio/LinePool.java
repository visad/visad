//
// LinePool.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** LinePool maintains a pool of lines and points in the given display. */
public class LinePool implements DisplayListener {

  /** Minimum number of lines in the line pool. */
  static final int MINIMUM_SIZE = 16;

  /** Maximum pixel distance for picking. */
  private static final int PICKING_THRESHOLD = 10;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Associated measurement toolbar. */
  private MeasureToolbar toolbar;

  /** Associated selection box. */
  private SelectionBox box;

  /** Dimensionality of line pool's display. */
  private int dim;

  /** Current slice of lines in pool. */
  private int slice;

  /** Number of lines/points in a block. */
  private int blockSize;

  /** Total number of lines/points. */
  private int size;

  /** Internal list of MeasureLine objects. */
  private Vector lines;

  /** Number of lines allocated. */
  private int lnUsed;

  /** Line ID counter. */
  int maxLnId = 0;

  /** Internal list of MeasurePoint objects. */
  private Vector points;

  /** Number of points allocated. */
  private int ptUsed;

  /** Point ID counter. */
  int maxPtId = 0;


  /** Constructs a pool of lines. */
  public LinePool(DisplayImpl display, MeasureToolbar toolbar,
    int dim, int slice, int blockSize)
  {
    lines = new Vector();
    points = new Vector();
    this.display = display;
    this.toolbar = toolbar;
    this.dim = dim;
    this.slice = slice;
    this.blockSize = blockSize;
    size = 0;
    lnUsed = 0;
    ptUsed = 0;
    display.addDisplayListener(this);
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int size) { expand(size, true); }

  /** Ensures the line pool is at least the given size. */
  public void expand(int size, boolean handleDisplay) {
    if (this.size == 0) {
      System.err.println("LinePool.expand: warning: " +
        "Cannot expand from zero without domain type");
      return;
    }
    MeasureLine line = (MeasureLine) lines.elementAt(0);
    RealTupleType domain = line.getDomain();
    expand(size, domain, handleDisplay);
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int size, RealTupleType domain) {
    expand(size, domain, true);
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int size, RealTupleType domain, boolean handleDisplay) {
    if (size <= this.size) return;
    int n = size - this.size;
    if (n % blockSize > 0) n += blockSize - n % blockSize;
    MeasureLine[] l = new MeasureLine[n];
    MeasurePoint[] p = new MeasurePoint[n];
    try {
      for (int i=0; i<n; i++) {
        l[i] = new MeasureLine(dim, this);
        l[i].setType(domain);
        l[i].hide();
        lines.add(l[i]);
        p[i] = new MeasurePoint(dim, this);
        p[i].setType(domain);
        p[i].hide();
        points.add(p[i]);
      }
      synchronized (this) {
        if (handleDisplay) {
          display.disableAction();
          if (box == null) {
            box = new SelectionBox();
            box.setDisplay(display);
          }
        }
        for (int i=0; i<n; i++) {
          try {
            l[i].setDisplay(display);
            p[i].setDisplay(display);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
        if (handleDisplay) display.enableAction();
      }
      this.size += n;
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /**
   * Sets the endpoint values for all lines in the
   * line pool to match the given measurements.
   */
  public void set(Measurement[] m) {
    int size = m.length;

    // deselect
    if (box != null) box.select(null);
    if (toolbar != null) toolbar.select(null);

    // set each reference accordingly
    expand(size);
    lnUsed = 0;
    ptUsed = 0;
    for (int i=0; i<size; i++) {
      if (m[i].isPoint()) {
        // measurement is a point
        MeasurePoint point = (MeasurePoint) points.elementAt(ptUsed++);
        point.setMeasurement(m[i], slice);
      }
      else {
        // measurement is a line
        MeasureLine line = (MeasureLine) lines.elementAt(lnUsed++);
        line.setMeasurement(m[i], slice);
      }
    }

    // hide extra points
    for (int i=ptUsed; i<this.size; i++) {
      MeasurePoint point = (MeasurePoint) points.elementAt(i);
      point.hide();
    }

    // hide extra lines
    for (int i=lnUsed; i<this.size; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.hide();
    }
  }

  /** Sets a line in the line pool to match the given measurement. */
  public void add(Measurement m) {
    if (m.isPoint()) {
      // measurement is a point
      expand(ptUsed + 1);
      MeasurePoint point = (MeasurePoint) points.elementAt(ptUsed);
      point.setMeasurement(m, slice);
      ptUsed++;
    }
    else {
      // measurement is a line
      expand(lnUsed + 1);
      MeasureLine line = (MeasureLine) lines.elementAt(lnUsed);
      line.setMeasurement(m, slice);
      lnUsed++;
    }
  }

  private int cursor_x, cursor_y;

  /** Listens for left mouse clicks in the display. */
  public void displayChanged(DisplayEvent e) {
    int id = e.getId();
    int x = e.getX();
    int y = e.getY();
    if (id == DisplayEvent.MOUSE_PRESSED_LEFT) {
      cursor_x = x;
      cursor_y = y;
    }
    else if (id == DisplayEvent.MOUSE_RELEASED_LEFT &&
      x == cursor_x && y == cursor_y && (ptUsed > 0 || lnUsed > 0))
    {
      // get domain coordinates of mouse click
      double[] coords = cursorToDomain(pixelToCursor(x, y));

      // compute maximum distance threshold
      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      int w = size.width;
      int h = size.height;
      double[] e1 = cursorToDomain(pixelToCursor(0, 0));
      double[] e2 = cursorToDomain(pixelToCursor(PICKING_THRESHOLD, 0));
      double threshold = e2[0] - e1[0];

      // find closest line
      int index = -1;
      double mindist = Double.MAX_VALUE;
      MouseBehavior mb = display.getMouseBehavior();
      for (int i=0; i<lnUsed; i++) {
        MeasureLine line = (MeasureLine) lines.elementAt(i);
        double[][] vals = line.getMeasurement().doubleValues();
        double dist = distance(vals[0][0], vals[1][0],
          vals[0][1], vals[1][1], coords[0], coords[1]);
        if (dist < mindist) {
          mindist = dist;
          index = i;
        }
      }

      // find closest point
      boolean pt = false;
      for (int i=0; i<ptUsed; i++) {
        MeasurePoint point = (MeasurePoint) points.elementAt(i);
        double[][] vals = point.getMeasurement().doubleValues();
        double dx = vals[0][0] - coords[0];
        double dy = vals[1][0] - coords[1];
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < mindist) {
          pt = true;
          mindist = dist;
          index = i;
        }
      }

      // highlight picked line or point
      if (mindist > threshold) {
        if (box != null) box.select(null);
        if (toolbar != null) toolbar.select(null);
      }
      else if (pt) {
        MeasurePoint point = (MeasurePoint) points.elementAt(index);
        if (box != null) box.select(point);
        if (toolbar != null) toolbar.select(point);
      }
      else {
        MeasureLine line = (MeasureLine) lines.elementAt(index);
        if (box != null) box.select(line);
        if (toolbar != null) toolbar.select(line);
      }
    }
  }

  /** Converts the given pixel coordinates to cursor coordinates. */
  private double[] pixelToCursor(int x, int y) {
    MouseBehavior mb = display.getDisplayRenderer().getMouseBehavior();
    VisADRay ray = mb.findRay(x, y);
    return ray.position;
  }

  /** Converts the given cursor coordinates to domain coordinates. */
  private double[] cursorToDomain(double[] cursor) {
    // locate x, y and z mappings
    Vector maps = display.getMapVector();
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
  private double distance(double ax, double ay,
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

}
