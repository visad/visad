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
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.*;
import javax.swing.JOptionPane;
import visad.*;
import visad.bom.RubberBandBoxRendererJ3D;
import visad.java3d.DisplayImplJ3D;

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

  /** Maximum number of measurement pools. */
  static final int MAX_POOLS = 2;


  // -- STATIC FIELDS --

  /** Measurement pool counter. */
  private static int numPools = 0;


  // -- GENERAL FIELDS --

  /** BioVisAD frame. */
  private BioVisAD bio;

  /** Id of this measurement pool. */
  private int pid;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Dimensionality of measurement pool's display. */
  private int dim;

  /** Currently linked list of measurements. */
  private MeasureList list;

  /** Current image slice value. */
  private int slice;


  // -- POOL ELEMENT FIELDS --

  /** Internal stack of free PoolPoints. */
  private Stack free;

  /** Internal list of used PoolPoints. */
  private Vector used;

  /**
   * Cell for updating linked measurement endpoints, solid lines,
   * dashed lines and colored endpoints when a pool point changes.
   */
  private CellImpl lineCell;

  /** Cell for handling rubber band box selections. */
  private CellImpl boxCell;


  // -- SELECTION FIELDS --

  /** List of selected lines. */
  private Vector selLines;

  /** List of selected markers. */
  private Vector selPoints;


  // -- SPECIAL DATA REFERENCES --

  /** Data reference for colored line segments. */
  private DataReferenceImpl solidLines;

  /** Data renderer for colored line segments. */
  private DataRenderer lineRenderer;

  /** Data reference for dashed line segments. */
  private DataReferenceImpl dashedLines;

  /** Data renderer for dashed line segments. */
  private DataRenderer dashedRenderer;

  /** Data reference for colored endpoints. */
  private DataReferenceImpl coloredPoints;

  /** Data renderer for colored endpoints. */
  private DataRenderer pointRenderer;

  /** Data reference for rubber band box. */
  private DataReferenceImpl rubberBand;

  /** Data renderer for rubber band box. */
  private DataRenderer boxRenderer;


  // -- CONSTRUCTOR --

  /** Constructs a pool of measurements. */
  public MeasurePool(BioVisAD biovis, DisplayImpl display, int dimension) {
    bio = biovis;
    pid = numPools++;
    this.display = display;
    dim = dimension;
    free = new Stack();
    used = new Vector();
    try {
      solidLines = new DataReferenceImpl("bio_solid_lines");
      dashedLines = new DataReferenceImpl("bio_dashed_lines");
      coloredPoints = new DataReferenceImpl("bio_colored_points");
      rubberBand = new DataReferenceImpl("bio_rubber_band");
    }
    catch (VisADException exc) { exc.printStackTrace(); }

    selLines = new Vector();
    selPoints = new Vector();

    lineCell = new CellImpl() {
      public void doAction() {
        // redraw colored lines and points when endpoints change
        if (lineRenderer == null) return;
        Vector solidStrips = new Vector();
        Vector dashedStrips = new Vector();
        Vector pointStrips = new Vector();
        Vector solidColors = new Vector();
        Vector dashedColors = new Vector();
        Vector pointColors = new Vector();
        boolean sel = hasSelection();

        // compute list of line strips
        Vector lines = list.getLines();
        int lsize = lines.size();
        for (int i=0; i<lsize; i++) {
          MeasureLine line = (MeasureLine) lines.elementAt(i);

          // ensure at least one endpoint is on this slice
          if (dim == 2) {
            boolean b1 = line.ep1.z == slice;
            boolean b2 = line.ep2.z == slice;
            boolean t1 = b1 && (!sel || line.ep1.selected > 0);
            boolean t2 = b2 && (!sel || line.ep2.selected > 0);
            if (line.ep1.pt[pid] != null) line.ep1.pt[pid].toggle(t1);
            if (line.ep2.pt[pid] != null) line.ep2.pt[pid].toggle(t2);
            if (!b1 && !b2) continue;
          }

          // create gridded set from this line
          GriddedSet set = doSet(new MeasurePoint[] {line.ep1, line.ep2});
          if (line.selected) {
            dashedStrips.add(set);
            dashedColors.add(line.color);
            dashedColors.add(line.color);
          }
          else {
            solidStrips.add(set);
            solidColors.add(line.color);
            solidColors.add(line.color);
          }

          // check for any needed X's
          if (dim == 2) doXs(line, solidStrips, solidColors);
        }

        // compute list of point strips
        Vector points = list.getPoints();
        int psize = points.size();
        for (int i=0; i<psize; i++) {
          MeasurePoint point = (MeasurePoint) points.elementAt(i);

          // ensure endpoint is on this slice
          if (dim == 2 && point.z != slice) continue;

          pointStrips.add(point);
          pointColors.add(point.selected > 0 ? Color.yellow : point.color);
        }

        doLines(solidStrips, solidColors, solidLines, lineRenderer);
        doLines(dashedStrips, dashedColors, dashedLines, dashedRenderer);
        doPoints(pointStrips, pointColors, coloredPoints, pointRenderer);
      }
    };

    boxCell = new CellImpl() {
      public void doAction() {
        // select measurements within the rubber band box
        if (boxRenderer == null) return;
        GriddedSet set = (GriddedSet) rubberBand.getData();
        float[][] samples = null;
        boolean stdPts = false;
        try { samples = set.getSamples(); }
        catch (VisADException exc) { exc.printStackTrace(); }
        if (samples == null) return;
        double x1 = (double) samples[0][0];
        double y1 = (double) samples[1][0];
        double x2 = (double) samples[0][1];
        double y2 = (double) samples[1][1];

        if (bio.toolMeasure.getMerge()) {
          // merge all endpoints lying within rectangle bounds
          double sum_x = 0, sum_y = 0;
          Vector mpts = new Vector();
          Vector points = list.getPoints();
          int psize = points.size();
          for (int i=0; i<psize; i++) {
            MeasurePoint point = (MeasurePoint) points.elementAt(i);

            // skip points that are not on this slice
            if (point.z != slice) continue;

            // check for standard point
            if (point.stdId >= 0) {
              stdPts = true;
              continue;
            }

            // check for containment
            if (BioUtil.contains(x1, y1, x2, y2, point.x, point.y)) {
              sum_x += point.x;
              sum_y += point.y;
              mpts.add(point);
            }
          }

          // warn user if standard points are involved
          if (stdPts) {
            JOptionPane.showMessageDialog(bio, "Some points within the " +
              "merge box are standard and will not be merged.", "Warning",
              JOptionPane.WARNING_MESSAGE);
          }

          int num_pts = mpts.size();
          if (num_pts == 0) {
            bio.toolMeasure.setMerge(false);
            return;
          }

          MeasurePoint first = (MeasurePoint) mpts.firstElement();
          MeasurePoint merged = new MeasurePoint(sum_x / num_pts,
            sum_y / num_pts, first.z, first.color, first.group);

          // replace line endpoints with merged point
          for (int i=0; i<num_pts; i++) {
            MeasurePoint point = (MeasurePoint) mpts.elementAt(i);

            // point is a marker; remove from measurement list
            if (point.lines.isEmpty()) {
              list.removeMarker(point, false);
              continue;
            }

            // point is endpoint for one or more lines
            int j = 0;
            while (j < point.lines.size()) {
              MeasureLine line = (MeasureLine) point.lines.elementAt(j);
              if (line.ep1 == point || line.ep2 == point) {
                list.removeLine(line, false);
                if (line.ep1 == point) line.ep1 = merged;
                if (line.ep2 == point) line.ep2 = merged;
                if (line.ep1 != line.ep2) {
                  line.ep1.lines.add(line);
                  line.ep2.lines.add(line);
                  list.addLine(line, false);
                }
              }
              else j++;
            }
          }
          merged.refreshColor();

          if (merged.lines.isEmpty()) {
            // add merged point back as a marker
            list.addMarker(merged, false);
          }
          list.refreshPools(true);
          bio.toolMeasure.setMerge(false);
        }
        else {
          // select all measurements lying within rectangle bounds
          Vector lines = list.getLines();
          int lsize = lines.size();
          for (int i=0; i<lsize; i++) {
            MeasureLine line = (MeasureLine) lines.elementAt(i);

            // skip lines not on this slice
            if (line.ep1.z != slice && line.ep2.z != slice) continue;

            // check for intersection
            if (BioUtil.intersects(x1, y1, x2, y2,
              line.ep1.x, line.ep1.y, line.ep2.x, line.ep2.y))
            {
              select(line);
            }
          }
          Vector points = list.getPoints();
          int psize = points.size();
          for (int i=0; i<psize; i++) {
            MeasurePoint point = (MeasurePoint) points.elementAt(i);

            // skip points that are part of a line, or not on this slice
            if (!point.lines.isEmpty() || point.z != slice) continue;

            // check for containment
            if (BioUtil.contains(x1, y1, x2, y2, point.x, point.y)) {
              select(point);
            }
          }
          bio.toolMeasure.updateSelection();
          list.refreshPools(true);
        }
      }
    };
    try { boxCell.addReference(rubberBand); }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }

    display.addDisplayListener(this);
    expand(BUFFER_SIZE, false);
  }


  // -- API METHODS --

  /** Adds references to the associated display. */
  public void init() throws VisADException, RemoteException {
    // solid line set
    lineRenderer = display.getDisplayRenderer().makeDefaultRenderer();
    lineRenderer.suppressExceptions(true);
    lineRenderer.toggle(false);
    display.addReferences(lineRenderer, solidLines);

    // dashed line set
    dashedRenderer = display.getDisplayRenderer().makeDefaultRenderer();
    dashedRenderer.suppressExceptions(true);
    dashedRenderer.toggle(false);
    display.addReferences(dashedRenderer, dashedLines, new ConstantMap[] {
      new ConstantMap(GraphicsModeControl.DASH_STYLE, Display.LineStyle)
    });

    // enlarged, colored endpoint set
    pointRenderer = display.getDisplayRenderer().makeDefaultRenderer();
    pointRenderer.suppressExceptions(true);
    pointRenderer.toggle(false);
    display.addReferences(pointRenderer, coloredPoints, new ConstantMap[] {
      new ConstantMap(5.0f, Display.PointSize)
    });

    // rubber band box
    if (dim == 2 && display instanceof DisplayImplJ3D) {
      rubberBand.setData(new Gridded2DSet(bio.sm.domain2, null, 1));
      int m = InputEvent.SHIFT_MASK;
      boxRenderer = new RubberBandBoxRendererJ3D(
        bio.sm.dtypes[0], bio.sm.dtypes[1], m, m);
      display.addReferences(boxRenderer, rubberBand);
    }

    // direct manipulation endpoints
    int total = free.size();
    for (int i=0; i<total; i++) ((PoolPoint) free.elementAt(i)).init();
  }

  /** Grants the given endpoint use of a pool point. */
  public PoolPoint lease(MeasurePoint point) {
    expand(1, true);
    PoolPoint pt = (PoolPoint) free.pop();
    pt.point = point;
    used.add(pt);
    point.pt[pid] = pt;
    pt.refresh();

    return pt;
  }

  /** Returns the given endpoint's pool point to the measurement pool. */
  public void release(MeasurePoint point) { purge(point.pt[pid]); }

  /** Returns all pool points to the measurement pool. */
  public void releaseAll() {
    while (!used.isEmpty()) purge((PoolPoint) used.lastElement());
  }

  /** Creates measurement pool objects to match the given measurements. */
  public void set(MeasureList list) {
    if (this.list == list) return;
    if (this.list != null) this.list.setCurrent(false);
    this.list = list;
    list.setCurrent(true);
    deselectAll();
    releaseAll();
    refresh(true);
  }

  /** Sets the current image slice value. */
  public void setSlice(int slice) {
    if (this.slice == slice) return;
    this.slice = slice;
    deselectAll();
    releaseAll();
    bio.toolMeasure.updateSelection();
    list.refreshPools(true);
  }

  /** Refreshes the measurement endpoints in the pool. */
  public synchronized void refresh(boolean reconstruct) {
    if (list == null) return;

    // update endpoints
    Vector points = list.getPoints();
    int size = points.size();
    display.disableAction();
    for (int i=0; i<size; i++) {
      MeasurePoint point = (MeasurePoint) points.elementAt(i);
      if (point.pt[pid] == null) point.pt[pid] = lease(point);
      point.pt[pid].toggle(dim == 3 || point.z == slice);
    }
    display.enableAction();

    if (reconstruct) {
      try { lineCell.doAction(); }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
  }

  /** Gets the current image slice value. */
  public int getSlice() { return slice; }

  /** Gets the display's dimensionality. */
  public int getDimension() { return dim; }

  /** Gets whether the measurement pool has any selected measurements. */
  public boolean hasSelection() {
    return !selLines.isEmpty() || !selPoints.isEmpty();
  }

  /** Gets whether the measurement pool has a single selected measurement. */
  public boolean hasSingleSelection() {
    return selLines.size() + selPoints.size() == 1;
  }

  /** Gets the list of selected measurements in array form. */
  public MeasureThing[] getSelection() {
    int lsize = selLines.size();
    int psize = selPoints.size();
    MeasureThing[] things = new MeasureThing[lsize + psize];
    for (int i=0; i<lsize; i++) {
      things[i] = (MeasureLine) selLines.elementAt(i);
    }
    for (int i=0; i<psize; i++) {
      things[i + lsize] = (MeasurePoint) selPoints.elementAt(i);
    }
    return things;
  }

  /** Gets whether all measurements in the current selection are standard. */
  public boolean isSelectionStandard() {
    int lsize = selLines.size();
    int psize = selPoints.size();
    if (lsize == 0 && psize == 0) return false;
    for (int i=0; i<lsize; i++) {
      MeasureLine line = (MeasureLine) selLines.elementAt(i);
      if (line.stdId == -1) return false;
    }
    for (int i=0; i<psize; i++) {
      MeasurePoint point = (MeasurePoint) selPoints.elementAt(i);
      if (point.stdId == -1) return false;
    }
    return true;
  }

  /**
   * Gets the color corresponding to the current selection.  If the
   * selection consists of more than one color, the first color is returned.
   */
  public Color getSelectionColor() {
    int lsize = selLines.size();
    int psize = selPoints.size();
    if (lsize == 0 && psize == 0) return null;
    return lsize > 0 ?
      ((MeasureLine) selLines.firstElement()).color :
      ((MeasurePoint) selPoints.firstElement()).color;
  }

  /**
   * Gets the group corresponding to the current selection.  If the
   * selection consists of more than one group, the first group is returned.
   */
  public MeasureGroup getSelectionGroup() {
    int lsize = selLines.size();
    int psize = selPoints.size();
    if (lsize == 0 && psize == 0) return null;
    return lsize > 0 ?
      ((MeasureLine) selLines.firstElement()).group :
      ((MeasurePoint) selPoints.firstElement()).group;
  }


  // -- INTERNAL API METHODS --

  private int mx, my;
  private boolean m_shift;

  /** Listens for mouse events in the display. */
  public void displayChanged(DisplayEvent e) {
    int id = e.getId();
    InputEvent event = e.getInputEvent();

    // ignore non-mouse display events
    if (event == null || !(event instanceof MouseEvent)) return;

    int x = e.getX();
    int y = e.getY();
    int mods = e.getModifiers();
    boolean left = (mods & InputEvent.BUTTON1_MASK) != 0;
    boolean shift = (mods & InputEvent.SHIFT_MASK) != 0;
    boolean ctrl = (mods & InputEvent.CTRL_MASK) != 0;

    // ignore CTRL events and non-left button events
    if (ctrl || !left) return;

    if (id == DisplayEvent.MOUSE_PRESSED) {
      mx = x;
      my = y;
      m_shift = shift;
    }
    else if (id == DisplayEvent.MOUSE_RELEASED) {
      if (x == mx && y == my) {
        if (list == null || dim != 2) return;
        Vector lines = list.getLines();
        Vector points = list.getPoints();

        // get domain coordinates of mouse click
        double[] coords = BioUtil.pixelToDomain(display, x, y);

        // compute maximum distance threshold
        double[] e1 = BioUtil.pixelToDomain(display, 0, 0);
        double[] e2 = BioUtil.pixelToDomain(display, PICKING_THRESHOLD, 0);
        double threshold = e2[0] - e1[0];

        // find closest measurement
        int index = -1;
        double mindist = Double.POSITIVE_INFINITY;
        int lsize = lines.size();
        boolean isLine = true;
        for (int i=0; i<lsize; i++) {
          MeasureLine line = (MeasureLine) lines.elementAt(i);

          // skip lines not on this slice
          if (line.ep1.z != slice && line.ep2.z != slice) continue;

          // compute distance
          double dist = BioUtil.getDistance(line.ep1.x, line.ep1.y,
            line.ep2.x, line.ep2.y, coords[0], coords[1]);
          if (dist < mindist) {
            mindist = dist;
            index = i;
          }
        }
        int psize = points.size();
        for (int i=0; i<psize; i++) {
          MeasurePoint point = (MeasurePoint) points.elementAt(i);

          // skip points that are part of a line, or not on this slice
          if (!point.lines.isEmpty() || point.z != slice) continue;

          // compute distance
          double dx = point.x - coords[0];
          double dy = point.y - coords[1];
          double dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < mindist) {
            mindist = dist;
            index = i;
            isLine = false;
          }
        }

        if (m_shift) {
          // add or remove picked line or point from the selection
          if (mindist <= threshold && index >= 0) {
            if (isLine) {
              MeasureLine line = (MeasureLine) lines.elementAt(index);
              if (selLines.contains(line)) deselect(line);
              else select(line);
            }
            else {
              MeasurePoint point = (MeasurePoint) points.elementAt(index);
              if (selPoints.contains(point)) deselect(point);
              else select(point);
            }
            bio.toolMeasure.updateSelection();
            list.refreshPools(true);
          }
        }
        else {
          // set picked line or point as the selection
          deselectAll();
          if (mindist <= threshold && index >= 0) {
            if (isLine) select((MeasureLine) lines.elementAt(index));
            else select((MeasurePoint) points.elementAt(index));
          }
          bio.toolMeasure.updateSelection();
          list.refreshPools(true);
        }
      }
    }
  }

  /** If a single measurement is selected, return its linked pool points. */
  PoolPoint[] getSelectionPts() {
    int lsize = selLines.size();
    int psize = selPoints.size();
    PoolPoint[] pts;
    if (lsize + psize != 1) pts = new PoolPoint[0];
    else if (lsize > 0) {
      pts = new PoolPoint[2];
      MeasureLine line = (MeasureLine) selLines.firstElement();
      pts[0] = line.ep1.pt[pid];
      pts[1] = line.ep2.pt[pid];
    }
    else {
      pts = new PoolPoint[1];
      MeasurePoint point = (MeasurePoint) selPoints.firstElement();
      pts[0] = point.pt[pid];
    }
    return pts;
  }

  /** Deselects the given line. */
  void deselect(MeasureLine line) {
    if (!selLines.contains(line)) return;
    line.selected = false;
    line.ep1.selected--;
    line.ep2.selected--;
    selLines.remove(line);
    if (!hasSelection()) list.setEndpointsEnabled(true);
    else {
      if (line.ep1.selected == 0) list.setEndpointEnabled(line.ep1, false);
      if (line.ep2.selected == 0) list.setEndpointEnabled(line.ep2, false);
    }
  }

  /** Deselects the given point. */
  void deselect(MeasurePoint point) {
    if (!selPoints.contains(point)) return;
    point.selected--;
    selPoints.remove(point);
    if (!hasSelection()) list.setEndpointsEnabled(true);
    else if (point.selected == 0) list.setEndpointEnabled(point, false);
  }


  // -- HELPER METHODS --

  /** Ensures the pool has at least the given number of free points. */
  private void expand(int size, boolean init) {
    // compute number of PoolPoints to add
    int total = free.size();
    if (size <= total) return;
    int count = total + used.size();
    int n = size - total + BUFFER_SIZE;

    // add new PoolPoints to display
    lineCell.disableAction();
    display.disableAction();
    for (int i=0; i<n; i++) {
      PoolPoint pt = new PoolPoint(bio, display, "p" + (count + i), dim);
      try {
        lineCell.addReference(pt.ref);
        if (init) pt.init();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
      free.push(pt);
    }
    display.enableAction();
    lineCell.enableAction();
  }

  /** Purges a point from the pool. */
  private void purge(PoolPoint pt) {
    if (!used.contains(pt)) return;
    pt.point.pt[pid] = null;
    pt.point = null;
    used.remove(pt);
    free.push(pt);
    pt.refresh();
  }

  /** Converts a set of measurement endpoints into a VisAD set. */
  private GriddedSet doSet(MeasurePoint[] points) {
    float[][] samples = new float[dim][points.length];
    GriddedSet set = null;
    try {
      if (dim == 2) {
        for (int i=0; i<points.length; i++) {
          samples[0][i] = (float) points[i].x;
          samples[1][i] = (float) points[i].y;
        }
        set = new Gridded2DSet(bio.sm.domain2, samples,
          points.length, null, null, null, false);
      }
      else {
        for (int i=0; i<points.length; i++) {
          samples[0][i] = (float) points[i].x;
          samples[1][i] = (float) points[i].y;
          samples[2][i] = (float) points[i].z;
        }
        set = new Gridded3DSet(bio.sm.domain3, samples,
          points.length, null, null, null, false);
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    return set;
  }

  /** Checks a line to see if an X must be placed over either endpoint. */
  private void doXs(MeasureLine line, Vector strips, Vector colors) {
    double xRange = Math.abs(bio.sm.max_x - bio.sm.min_x);
    double yRange = Math.abs(bio.sm.max_y - bio.sm.min_y);
    double x_width = 0.05 * (xRange < yRange ? xRange : yRange);
    for (int j=0; j<2; j++) {
      double x, y, z;
      if (j == 0) {
        x = line.ep1.x;
        y = line.ep1.y;
        z = line.ep1.z;
      }
      else {
        x = line.ep2.x;
        y = line.ep2.y;
        z = line.ep2.z;
      }
      if (z == slice) continue;
      float[][] samples1 = {
        {(float) (x - x_width), (float) (x + x_width)},
        {(float) (y - x_width), (float) (y + x_width)}
      };
      float[][] samples2 = {
        {(float) (x - x_width), (float) (x + x_width)},
        {(float) (y + x_width), (float) (y - x_width)}
      };
      try {
        strips.add(new Gridded2DSet(bio.sm.domain2,
          samples1, 2, null, null, null, false));
        strips.add(new Gridded2DSet(bio.sm.domain2,
          samples2, 2, null, null, null, false));
        for (int k=0; k<4; k++) colors.add(Color.white);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
    }
  }

  /**
   * Converts a set of gridded sets with matching colors
   * into a field and updates the data reference accordingly.
   */
  private void doLines(Vector strips, Vector colors,
    DataReferenceImpl ref, DataRenderer renderer)
  {
    int size = strips.size();
    if (size == 0) {
      renderer.toggle(false);
      return;
    }

    // compile strips into UnionSet
    GriddedSet[] sets = new GriddedSet[size];
    strips.copyInto(sets);
    try {
      RealTupleType domain = dim == 2 ? bio.sm.domain2 : bio.sm.domain3;
      UnionSet set = new UnionSet(domain, sets);
      FunctionType function = new FunctionType(domain, bio.sm.colorRange);
      FlatField field = new FlatField(function, set);

      // assign color values to segment endpoints
      int colorSize = colors.size();
      double[][] samples = new double[3][colorSize];
      for (int j=0; j<colorSize; j++) {
        Color color = (Color) colors.elementAt(j);
        samples[0][j] = color.getRed();
        samples[1][j] = color.getGreen();
        samples[2][j] = color.getBlue();
      }
      field.setSamples(samples, false);

      ref.setData(field);
      renderer.toggle(true);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /**
   * Converts a set of measurement points with matching colors
   * into a field and updates the data reference accordingly.
   */
  private void doPoints(Vector strips, Vector colors,
    DataReferenceImpl ref, DataRenderer renderer)
  {
    int size = strips.size();
    if (size == 0) {
      renderer.toggle(false);
      return;
    }

    try {
      RealType index = RealType.getRealType("point_index");
      RealType[] range_types;
      if (dim == 2) {
        range_types = new RealType[] {
          bio.sm.dtypes[0],
          bio.sm.dtypes[1],
          SliceManager.RED_TYPE,
          SliceManager.GREEN_TYPE,
          SliceManager.BLUE_TYPE
        };
      }
      else {
        range_types = new RealType[] {
          bio.sm.dtypes[0],
          bio.sm.dtypes[1],
          bio.sm.dtypes[2],
          SliceManager.RED_TYPE,
          SliceManager.GREEN_TYPE,
          SliceManager.BLUE_TYPE
        };
      }
      RealTupleType range = new RealTupleType(range_types);
      FunctionType function = new FunctionType(index, range);
      FlatField field = new FlatField(function, new Integer1DSet(size));

      // assign values to data samples
      double[][] samples = new double[dim + 3][size];
      if (dim == 2) {
        for (int j=0; j<size; j++) {
          MeasurePoint point = (MeasurePoint) strips.elementAt(j);
          Color color = (Color) colors.elementAt(j);
          samples[0][j] = point.x;
          samples[1][j] = point.y;
          samples[2][j] = color.getRed();
          samples[3][j] = color.getGreen();
          samples[4][j] = color.getBlue();
        }
      }
      else {
        for (int j=0; j<size; j++) {
          MeasurePoint point = (MeasurePoint) strips.elementAt(j);
          Color color = (Color) colors.elementAt(j);
          samples[0][j] = point.x;
          samples[1][j] = point.y;
          samples[2][j] = point.z;
          samples[3][j] = color.getRed();
          samples[4][j] = color.getGreen();
          samples[5][j] = color.getBlue();
        }
      }
      field.setSamples(samples, false);
      ref.setData(field);
      renderer.toggle(true);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Selects the given line. */
  private void select(MeasureLine line) {
    if (selLines.contains(line)) return;
    if (!hasSelection()) list.setEndpointsEnabled(false);
    line.selected = true;
    line.ep1.selected++;
    line.ep2.selected++;
    selLines.add(line);
    list.setEndpointEnabled(line.ep1, true);
    list.setEndpointEnabled(line.ep2, true);
  }

  /** Selects the given marker. */
  private void select(MeasurePoint point) {
    if (selPoints.contains(point)) return;
    if (!hasSelection()) list.setEndpointsEnabled(false);
    point.selected++;
    selPoints.add(point);
    list.setEndpointEnabled(point, true);
  }

  /** Deselects all measurements. */
  private void deselectAll() {
    int lsize = selLines.size();
    for (int i=0; i<lsize; i++) {
      MeasureLine line = (MeasureLine) selLines.elementAt(i);
      line.selected = false;
      line.ep1.selected--;
      line.ep2.selected--;
    }
    selLines.removeAllElements();
    int psize = selPoints.size();
    for (int i=0; i<psize; i++) {
      MeasurePoint point = (MeasurePoint) selPoints.elementAt(i);
      point.selected--;
    }
    selPoints.removeAllElements();
    list.setEndpointsEnabled(true);
  }

}
