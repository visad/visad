//
// LinePool.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

/** LinePool maintains a collection of MeasureLine objects. */
public class LinePool implements DisplayListener {

  /** Maximum pixel distance for picking. */
  private static final int PICKING_THRESHOLD = 10;

  /** Internal list of MeasureLine objects. */
  private Vector lines;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Associated measurement toolbar. */
  private MeasureToolbar toolbar;

  /** Associated selection box. */
  private SelectionBox box;

  /** Number of lines in a block. */
  private int blockSize;

  /** Total number of lines. */
  private int size;

  /** Number of lines allocated. */
  private int used;

  /** Constructs a pool of lines. */
  public LinePool(DisplayImpl display, MeasureToolbar toolbar, int blockSize) {
    lines = new Vector();
    this.display = display;
    this.toolbar = toolbar;
    this.blockSize = blockSize;
    size = 0;
    used = 0;
    display.addDisplayListener(this);
    try {
      box = new SelectionBox();
      box.addToDisplay(display);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int numLines) {
    if (size == 0) {
      System.out.println("LinePool.expand: warning: " +
        "Cannot expand from zero without domain type");
      return;
    }
    MeasureLine line = (MeasureLine) lines.elementAt(0);
    RealTupleType domain = line.getDomain();
    expand(numLines, domain);
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int numLines, RealTupleType domain) {
    if (numLines <= size) return;
    int n = numLines - size;
    if (n % blockSize > 0) n += blockSize - n % blockSize;
    MeasureLine[] l = new MeasureLine[n];
    try {
      for (int i=0; i<n; i++) {
        l[i] = new MeasureLine();
        l[i].setType(domain);
        l[i].hide();
        lines.add(l[i]);
      }
      synchronized (this) {
        display.disableAction();
        for (int i=0; i<l.length; i++) {
          try {
            l[i].addToDisplay(display);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
        display.enableAction();
      }
      size += l.length;
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /**
   * Sets the endpoint values for all lines in the
   * line pool to match the given measurements.
   */
  public void setLines(Measurement[] m) {
    int numLines = m.length;

    // set each reference accordingly
    expand(numLines);
    for (int i=0; i<numLines; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.setMeasurement(m[i]);
    }

    // hide extra references
    for (int i=numLines; i<size; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.hide();
    }

    used = numLines;
  }

  /** Sets a line in the line pool to match the given measurement. */
  public void addLine(Measurement m) {
    expand(used + 1);
    MeasureLine line = (MeasureLine) lines.elementAt(used);
    line.setMeasurement(m);
    used++;
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
      x == cursor_x && y == cursor_y && used > 0)
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
      for (int i=0; i<used; i++) {
        MeasureLine line = (MeasureLine) lines.elementAt(i);
        double[][] vals = line.getMeasurement().doubleValues();
        int dim = vals.length;
        double[] ep1 = new double[dim];
        double[] ep2 = new double[dim];
        for (int j=0; j<dim; j++) {
          ep1[j] = vals[j][0];
          ep2[j] = vals[j][1];
        }
        double dist = distance(vals[0][0], vals[1][0],
          vals[0][1], vals[1][1], coords[0], coords[1]);
        if (dist < mindist) {
          mindist = dist;
          index = i;
        }
      }

      // highlight picked line
      if (mindist > threshold) {
        box.select(null);
        if (toolbar != null) toolbar.select(null);
      }
      else {
        MeasureLine line = (MeasureLine) lines.elementAt(index);
        box.select(line);
        if (toolbar != null) toolbar.select(line);
      }
    }
  }

  /** Converts the given pixel coordinates to cursor coordinates. */
  private double[] pixelToCursor(int x, int y) {
    MouseBehavior mb = display.getDisplayRenderer().getMouseBehavior();
    VisADRay ray = mb.findRay(x, y);
    return new double[] {ray.position[0], ray.position[1]};
  }

  /** Converts the given cursor coordinates to domain coordinates. */
  private double[] cursorToDomain(double[] cursor) {
    // locate x and y mappings
    Vector maps = display.getMapVector();
    int numMaps = maps.size();
    ScalarMap map_x = null, map_y = null;
    for (int i=0; i<numMaps && (map_x == null || map_y == null); i++) {
      ScalarMap map = (ScalarMap) maps.elementAt(i);
      if (map.getDisplayScalar().equals(Display.XAxis)) map_x = map;
      else if (map.getDisplayScalar().equals(Display.YAxis)) map_y = map;
    }
    if (map_x == null || map_y == null) return null;

    // adjust for scale
    double[] scale_offset = new double[2];
    double[] dummy = new double[2];
    double[] values = new double[2];
    map_x.getScale(scale_offset, dummy, dummy);
    values[0] = (cursor[0] - scale_offset[1]) / scale_offset[0];
    map_y.getScale(scale_offset, dummy, dummy);
    values[1] = (cursor[1] - scale_offset[1]) / scale_offset[0];

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
