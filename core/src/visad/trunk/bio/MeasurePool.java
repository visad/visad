//
// MeasurePool.java
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

import java.awt.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/**
 * MeasurePool maintains a pool of manipulable points and a set of
 * connecting lines, for interactive data measurement in the given display.
 */
public class MeasurePool implements DisplayListener {

  // -- CONSTANTS --

  /** Number of extra points to add when number of points is expanded. */
  private static final int BUFFER_SIZE = 15;

  /** Maximum pixel distance for picking. */
  private static final int PICKING_THRESHOLD = 10;


  // -- FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Associated selection box. */
  private SelectionBox box;

  /** Internal list of free PoolPoints. */
  private Vector free;

  /** Internal list of MeasureThings using PoolPoints. */
  private Vector things;

  /** Data reference for colored line segments. */
  private DataReferenceImpl lines;

  /** Data renderer for colored line segments. */
  private DataRenderer line_renderer;

  /** Cell for updating connecting lines when an endpoint changes. */
  private CellImpl cell;

  /** Dimensionality of measurement pool's display. */
  private int dim;

  /** Image slice value. */
  private int slice;


  // -- CONSTRUCTOR --

  /** Constructs a pool of measurements. */
  public MeasurePool(BioVisAD biovis, DisplayImpl display, int dim) {
    bio = biovis;
    this.display = display;
    this.dim = dim;
    box = new SelectionBox(display);
    free = new Vector();
    things = new Vector();
    try { lines = new DataReferenceImpl("bio_colored_lines"); }
    catch (VisADException exc) { exc.printStackTrace(); }

    cell = new CellImpl() {
      public void doAction() { refresh(); }
    };

    display.addDisplayListener(this);
    expand(BUFFER_SIZE, false);
  }


  // -- API METHODS --

  /** Adds all references to the associated display. */
  public void init() throws VisADException, RemoteException {
    int total = free.size();
    for (int i=0; i<total; i++) ((PoolPoint) free.elementAt(i)).init();
    box.init();
    line_renderer = display.getDisplayRenderer().makeDefaultRenderer();
    line_renderer.suppressExceptions(true);
    line_renderer.toggle(false);
    display.addReferences(line_renderer, lines);
  }

  /** Grants the given measurement object use of a number of pool points. */
  public PoolPoint[] lease(MeasureThing thing) {
    if (things.contains(thing)) return null;
    int num = thing.getLength();
    expand(num, true);
    PoolPoint[] pts = new PoolPoint[num];
    for (int i=0; i<num; i++) {
      pts[i] = (PoolPoint) free.lastElement();
      free.remove(free.size() - 1);
    }
    things.add(thing);
    return pts;
  }

  /**
   * Returns the given measurement object's pool points
   * to the measurement pool.
   */
  public void release(MeasureThing thing) {
    if (!things.contains(thing)) return;
    select(null);
    purge(thing);
  }

  /** Returns all pool points to the measurement pool. */
  public void releaseAll() {
    select(null);
    while (!things.isEmpty()) purge((MeasureThing) things.lastElement());
  }

  /** Creates measurement pool objects to match the given measurements. */
  public void set(Measurement[] m) {
    // release old leases
    releaseAll();

    // register new leases
    expand(m.length, true);
    for (int i=0; i<m.length; i++) new MeasureThing(bio, this, m[i]);
    refresh();
  }

  /** Adds a measurement pool object to match the given measurement. */
  public void add(Measurement m) {
    expand(1, true);
    new MeasureThing(bio, this, m);
  }

  /** Sets the current image slice value. */
  public void setSlice(int slice) {
    if (this.slice == slice) return;
    this.slice = slice;
    cell.disableAction();
    int size = things.size();
    select(null);
    for (int i=0; i<size; i++) ((MeasureThing) things.elementAt(i)).refresh();
    cell.enableAction();
  }

  /** Refreshes the connecting lines of the measurements in the pool. */
  public void refresh() {
    if (line_renderer == null) return;

    // redraw line segments when endpoints change
    Vector strips = new Vector();
    Vector colors = new Vector();

    // compute list of line strips
    int size = things.size();
    for (int i=0; i<size; i++) {
      MeasureThing thing = (MeasureThing) things.elementAt(i);
      Color color = thing.getColor();

      // create line strip
      RealTuple[] values = thing.getValues();
      for (int j=0; j<values.length; j++) if (values[j] == null) return;
      if (dim == 2) {
        boolean okay = false;
        for (int j=0; j<values.length; j++) {
          double[] s = values[j].getValues();
          if (s[2] == slice) okay = true;
        }
        if (!okay) continue;
      }

      if (values.length == 1) {
        // point - no line segments needed
        continue;
      }
      else if (values.length == 2) {
        // line - one segment needed
        double[] s0 = values[0].getValues();
        double[] s1 = values[1].getValues();
        try {
          GriddedSet set;
          if (dim == 2) {
            float[][] samples = {
              {(float) s0[0], (float) s1[0]},
              {(float) s0[1], (float) s1[1]}
            };
            set = new Gridded2DSet(bio.domain2, samples, 2);
          }
          else { // dim == 3
            float[][] samples = {
              {(float) s0[0], (float) s1[0]},
              {(float) s0[1], (float) s1[1]},
              {(float) s0[2], (float) s1[2]}
            };
            set = new Gridded3DSet(bio.domain3, samples, 2);
          }
          strips.add(set);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
      }
      else {
        // multi-vertex shape - line strip loop needed
        float[][] samples = new float[dim][values.length + 1];
        for (int j=0; j<values.length; j++) {
          double[] s = values[j].getValues();
          for (int k=0; k<dim; k++) samples[k][j] = (float) s[k];
        }
        double[] s = values[0].getValues();
        for (int k=0; k<dim; k++) samples[k][values.length] = (float) s[k];
        try {
          GriddedSet set;
          if (dim == 2) {
            set =
              new Gridded2DSet(bio.domain2, samples, values.length + 1);
          }
          else { // dim == 3
            set =
              new Gridded3DSet(bio.domain3, samples, values.length + 1);
          }
          strips.add(set);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
      }
      if (values.length > 1) {
        for (int k=0; k<values.length; k++) colors.add(color);
      }

      // check for any needed X's
      if (dim == 2) {
        double x_width = 0.05 *
          (bio.xRange < bio.yRange ? bio.xRange : bio.yRange);
        for (int j=0; j<values.length; j++) {
          double[] s = values[j].getValues();
          if (s[2] == slice) continue;
          float[][] samples1 = {
            {(float) (s[0] - x_width), (float) (s[0] + x_width)},
            {(float) (s[1] - x_width), (float) (s[1] + x_width)}
          };
          float[][] samples2 = {
            {(float) (s[0] - x_width), (float) (s[0] + x_width)},
            {(float) (s[1] + x_width), (float) (s[1] - x_width)}
          };
          try {
            strips.add(new Gridded2DSet(bio.domain2, samples1, 2));
            strips.add(new Gridded2DSet(bio.domain2, samples2, 2));
            for (int k=0; k<4; k++) colors.add(color);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
        }
      }
    }

    size = strips.size();
    if (size == 0) line_renderer.toggle(false);
    else {
      // compile line strips into UnionSet
      GriddedSet[] sets = new GriddedSet[size];
      strips.copyInto(sets);
      Color[] line_colors = new Color[colors.size()];
      colors.copyInto(line_colors);
      try {
        RealTupleType domain = dim == 2 ? bio.domain2 : bio.domain3;
        UnionSet set = new UnionSet(domain, sets);
        FunctionType function =
          new FunctionType(domain, bio.colorRange);
        FlatField field = new FlatField(function, set);

        // assign color values to line segments
        double[][] samples = new double[3][line_colors.length];
        for (int j=0; j<line_colors.length; j++) {
          samples[0][j] = line_colors[j].getRed();
          samples[1][j] = line_colors[j].getGreen();
          samples[2][j] = line_colors[j].getBlue();
        }
        field.setSamples(samples);

        lines.setData(field);
        line_renderer.toggle(true);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Gets the current image slice value. */
  public int getSlice() { return slice; }

  /** Gets the display's dimensionality. */
  public int getDimension() { return dim; }


  // -- INTERNAL API METHODS --

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
      x == cursor_x && y == cursor_y)
    {
      // get domain coordinates of mouse click
      double[] coords = pixelToDomain(x, y);

      // compute maximum distance threshold
      double[] e1 = pixelToDomain(0, 0);
      double[] e2 = pixelToDomain(PICKING_THRESHOLD, 0);
      double threshold = e2[0] - e1[0];

      // find closest object
      int index = -1;
      double mindist = Double.MAX_VALUE;
      int size = things.size();
      for (int i=0; i<size; i++) {
        // skip multi-vertex measurements
        MeasureThing thing = (MeasureThing) things.elementAt(i);
        int len = thing.getLength();
        if (len > 2) continue;

        // skip measurements not on this slice
        double[][] values = thing.getMeasurement().doubleValues();
        boolean okay = false;
        for (int j=0; j<len; j++) {
          if (values[2][j] == slice) {
            okay = true;
            break;
          }
        }
        if (!okay) continue;

        // compute distance
        double dist;
        if (len == 1) {
          // compute point distance
          double dx = values[0][0] - coords[0];
          double dy = values[1][0] - coords[1];
          dist = Math.sqrt(dx * dx + dy * dy);
        }
        else {
          // compute line distance
          dist = distance(values[0][0], values[1][0],
            values[0][1], values[1][1], coords[0], coords[1]);
        }
        if (dist < mindist) {
          mindist = dist;
          index = i;
        }
      }

      // highlight picked line or point
      if (mindist > threshold) select(null);
      else {
        MeasureThing thing = (MeasureThing) things.elementAt(index);
        select(thing);
      }
    }
  }


  // -- HELPER METHODS --

  /** Ensures the pool has at least the given number of free points. */
  private void expand(int size, boolean init) {
    // compute number of PoolPoints to add
    int total = free.size();
    if (size <= total) return;
    int n = size - total + BUFFER_SIZE;

    // add new PoolPoints to display
    cell.disableAction();
    display.disableAction();
    for (int i=0; i<n; i++) {
      PoolPoint pt = new PoolPoint(display, "p" + (total + n));
      try {
        cell.addReference(pt.ref);
        if (init) pt.init();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
      free.add(pt);
    }
    display.enableAction();
    cell.enableAction();
  }

  /** Purges a measurement object from the pool. */
  private void purge(MeasureThing thing) {
    thing.destroy();
    PoolPoint[] pts = thing.getPoints();
    for (int i=0; i<pts.length; i++) {
      pts[i].toggle(false);
      free.add(pts[i]);
    }
    things.remove(thing);
  }

  /** Updates various aspects of the GUI when a measurement object changes. */
  private void updateStuff(MeasureThing thing) {
    if (thing == null) return;
    RealTuple[] values = thing.getMeasurement().getValues();
    boolean same = true;
    boolean match = false;
    double value = 0;
    try {
      for (int i=0; i<values.length; i++) {
        Real[] reals = values[i].getRealComponents();
        double v = reals[reals.length - 1].getValue();
        if (v == slice) match = true;
        if (i != 0 && v != value) same = false;
        value = v;
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    if (!match) select(null);
    bio.toolMeasure.setStandardEnabled(same);
  }

  /**
   * Called to indicate that the given measurement object's values
   * have changed.
   */
  void valuesChanged(MeasureThing thing) {
    MeasureThing sel = box.getSelection();
    if (thing != box.getSelection()) return;
    updateStuff(thing);
  }

  /** Deselects any selected measurements. */
  void select(MeasureThing thing) {
    bio.toolMeasure.select(thing);
    box.select(thing);
    updateStuff(thing);
  }

  /** Converts the given pixel coordinates to domain coordinates. */
  double[] pixelToDomain(int x, int y) {
    return cursorToDomain(pixelToCursor(x, y));
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
