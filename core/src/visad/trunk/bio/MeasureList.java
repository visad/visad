//
// MeasureList.java
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

/** MeasureList maintains a list of measurements between points in a field. */
public class MeasureList {

  // -- FIELDS --

  /** VisBio frame. */
  private VisBio bio;

  /** List of measurement points. */
  private Vector points;

  /** List of measurement lines. */
  private Vector lines;

  /** Whether this collection of measurements is currently being displayed. */
  private boolean isCurrent;


  // -- CONSTRUCTOR --

  /** Constructs a list of measurements. */
  public MeasureList(VisBio biovis) throws VisADException, RemoteException {
    bio = biovis;
    points = new Vector();
    lines = new Vector();
  } 


  // -- API METHODS --

  /** Adds a measurement line to the measurement list. */
  public void addLine() {
    // generate random acceptable endpoint start locations;
    // endpoints bisect a circle on the given Z-slice value
    Dimension size = bio.display2.getComponent().getSize();
    double[] e1 = BioUtil.pixelToDomain(bio.display2, 0, 0);
    double[] e2 = BioUtil.pixelToDomain(bio.display2, size.width, size.height);
    int slice = bio.sm.getSlice();
    double cx = (e1[0] + e2[0]) / 2;
    double cy = (e1[1] + e2[1]) / 2;
    double rx = Math.abs(e2[0] - cx);
    double ry = Math.abs(e2[1] - cy);
    double r = 0.75 * (rx < ry ? rx : ry);
    double theta = 2 * Math.PI * Math.random();
    double inc = Math.PI;
    double t1 = theta;
    double x1 = r * Math.cos(t1) + cx;
    double y1 = r * Math.sin(t1) + cy;
    double t2 = theta + Math.PI;
    double x2 = r * Math.cos(t2) + cx;
    double y2 = r * Math.sin(t2) + cy;

    // create two new endpoints and link them
    MeasurePoint ep1 = new MeasurePoint(x1, y1, slice);
    MeasurePoint ep2 = new MeasurePoint(x2, y2, slice);
    MeasureLine line = new MeasureLine(ep1, ep2,
      Color.white, VisBio.noneGroup, false);
    addLine(line, true);
  }

  /** Adds a measurement marker to the measurement list. */
  public void addMarker() {
    // generate random acceptable endpoint start location
    // endpoint lies within central 75% of display
    Dimension size = bio.display2.getComponent().getSize();
    double[] e1 = BioUtil.pixelToDomain(bio.display2, 0, 0);
    double[] e2 = BioUtil.pixelToDomain(bio.display2, size.width, size.height);
    int slice = bio.sm.getSlice();
    double w = 0.75 * (e2[0] - e1[0]);
    double h = 0.75 * (e2[1] - e1[1]);
    double cx = (e1[0] + e2[0]) / 2;
    double cy = (e1[1] + e2[1]) / 2;
    double x = cx + w * (Math.random() - 0.5);
    double y = cy + h * (Math.random() - 0.5);

    // create one new endpoint
    MeasurePoint point = new MeasurePoint(x, y, slice,
      Color.white, VisBio.noneGroup);
    addMarker(point, true);
  }

  /** Adds the given measurement line to the measurement list. */
  public void addLine(MeasureLine line, boolean updatePools) {
    if (!points.contains(line.ep1)) points.add(line.ep1);
    if (!points.contains(line.ep2)) points.add(line.ep2);
    lines.add(line);
    if (updatePools) refreshPools(false);
    boolean selection = bio.mm.pool2.hasSelection();
    setEndpointEnabled(line.ep1, !selection || line.ep1.selected > 0);
    setEndpointEnabled(line.ep2, !selection || line.ep2.selected > 0);
  }

  /** Adds the given measurement marker to the measurement list. */
  public void addMarker(MeasurePoint point, boolean updatePools) {
    points.add(point);
    if (updatePools) refreshPools(false);
    setEndpointEnabled(point,
      !bio.mm.pool2.hasSelection() || point.selected > 0);
  }

  /** Removes the given measurement line from the measurement list. */
  public void removeLine(MeasureLine line, boolean updatePools) {
    if (!lines.contains(line)) return;
    remove(line);
    if (updatePools) refreshPools(true);
  }

  /** Removes the given measurement marker from the measurement list. */
  public void removeMarker(MeasurePoint point, boolean updatePools) {
    if (!points.contains(point)) return;
    remove(point);
    if (updatePools) refreshPools(true);
  }

  /** Removes selected measurements from the measurement list. */
  public void removeSelected() { remove(false); }

  /** Removes all measurements from the measurement list. */
  public void removeAll() { remove(true); }

  /** Sets whether this list is currently linked to the measurement pools. */
  public void setCurrent(boolean current) { isCurrent = current; }

  /** Gets the list of measurement endpoints. */
  public Vector getPoints() { return points; }

  /** Gets the list of measurement lines. */
  public Vector getLines() { return lines; }

  /** Gets whether this list has any measurements. */
  public boolean hasMeasurements() {
    return !points.isEmpty() || !lines.isEmpty();
  }


  // -- INTERNAL API METHODS --

  /** Refreshes the associated measurement pools if this list is current. */
  void refreshPools(boolean reconstruct) {
    if (isCurrent) {
      bio.mm.pool2.refresh(reconstruct);
      if (bio.mm.pool3 != null) bio.mm.pool3.refresh(reconstruct);
    }
    bio.mm.changed = true;
  }

  /** Toggles direct manipulation endpoints. */
  void setEndpointsEnabled(boolean enabled) {
    int psize = points.size();
    for (int i=0; i<psize; i++) {
      MeasurePoint point = (MeasurePoint) points.elementAt(i);
      setEndpointEnabled(point, enabled);
    }
  }

  /** Toggles a direct manipulation endpoint. */
  void setEndpointEnabled(MeasurePoint point, boolean enabled) {
    for (int i=0; i<point.pt.length; i++) {
      if (point.pt[i] != null) point.pt[i].toggle(enabled);
    }
  }


  // -- HELPER METHODS --

  /** Removes measurements from the measurement pool. */
  private void remove(boolean all) {
    int i = 0;
    while (i < lines.size()) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      if (all || line.selected) remove(line);
      else i++;
    }
    i = 0;
    while (i < points.size()) {
      MeasurePoint point = (MeasurePoint) points.elementAt(i);
      if (all || point.selected > 0) remove(point);
      else i++;
    }

    refreshPools(true);
  }

  /** Removes the given measurement line from the measurement pool. */
  private void remove(MeasureLine line) {
    line.ep1.lines.remove(line);
    line.ep2.lines.remove(line);
    if (line.ep1.lines.isEmpty()) remove(line.ep1);
    if (line.ep2.lines.isEmpty()) remove(line.ep2);
    bio.mm.pool2.deselect(line);
    if (bio.mm.pool3 != null) bio.mm.pool3.deselect(line);
    lines.remove(line);
  }

  /** Removes the given measurement endpoint from the measurement pool. */
  private void remove(MeasurePoint point) {
    bio.mm.pool2.deselect(point);
    bio.mm.pool2.release(point);
    if (bio.mm.pool3 != null) {
      bio.mm.pool3.deselect(point);
      bio.mm.pool3.release(point);
    }
    points.remove(point);
  }

}
