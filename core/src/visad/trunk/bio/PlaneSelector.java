// {
// PlaneSelector.java
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
import java.io.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * PlaneSelector maintains a data structure of three endpoints that
 * can be manipulated by the user to specify an arbitrary plane in 3-D.
 */
public class PlaneSelector {

  // -- FIELDS --

  /** Associated display. */
  protected DisplayImpl display;

  /** Data references for the endpoints and linked plane. */
  protected DataReferenceImpl[] refs = new DataReferenceImpl[5];

  /** Data renderers for the endpoints and linked plane. */
  protected DataRenderer[] renderers = new DataRenderer[5];

  /** Computation cell for linking plane with endpoints. */
  protected CellImpl cell;

  /** Real type mappings for the plane. */
  protected RealType xtype, ytype, ztype, rtype, gtype, btype;

  /** Math type for domain and range tuples. */
  protected RealTupleType domain, range;

  /** Starting coordinates for plane's endpoints. */
  protected double x1, y1, z1, x2, y2, z2, x3, y3, z3;

  /** Flag for whether selection plane is visible. */
  protected boolean visible;

  /** Color values for plane outline points. */
  protected double[][] lineValues;
  
  /** Perimeter lines for the plane. */
  protected FlatField lines;

  /** Semi-transparent planar slice. */
  protected Gridded3DSet plane;

  /** List of PlaneListeners to notify when plane changes. */
  protected Vector listeners = new Vector();


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public PlaneSelector(DisplayImpl display) {
    this.display = display;

    // cell links plane with endpoints
    cell = new CellImpl() {
      public void doAction() { refresh(); }
    };

    // construct data references
    cell.disableAction();
    try {
      for (int i=0; i<refs.length; i++) {
        if (i > 1) {
          refs[i] = new DataReferenceImpl("bio_plane" + i);
          cell.addReference(refs[i]);
        }
        else {
          refs[i] = new DataReferenceImpl(i == 0 ? "bio_edges" : "bio_plane");
        }
      }
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    cell.enableAction();
  }


  // -- API METHODS --

  /** Toggles the plane selector's visibility. */
  public void toggle(boolean visible) {
    this.visible = visible;
    for (int i=0; i<renderers.length; i++) renderers[i].toggle(visible);
  }

  /**
   * Adds the plane selector to its display, with the given colors
   * and starting endpoint coordinates.
   */
  public void init(RealType xtype, RealType ytype, RealType ztype,
    RealType rtype, RealType gtype, RealType btype,
    Color[] lineColors, Color planeColor, double x1, double y1, double z1,
    double x2, double y2, double z2, double x3, double y3, double z3)
    throws VisADException, RemoteException
  {
    this.xtype = xtype;
    this.ytype = ytype;
    this.ztype = ztype;
    this.rtype = rtype;
    this.gtype = gtype;
    this.btype = btype;
    this.domain = new RealTupleType(xtype, ytype, ztype);
    this.range = new RealTupleType(rtype, gtype, btype);
    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;
    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    this.x3 = x3;
    this.y3 = y3;
    this.z3 = z3;
    visible = false;
    lineValues = new double[3][4];
    int len = lineColors.length;
    for (int i=0; i<4; i++) {
      lineValues[0][i] = lineColors[i % len].getRed();
      lineValues[1][i] = lineColors[i % len].getGreen();
      lineValues[2][i] = lineColors[i % len].getBlue();
    }
    float plane_r = planeColor.getRed() / 255.0f;
    float plane_g = planeColor.getGreen() / 255.0f;
    float plane_b = planeColor.getBlue() / 255.0f;
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    for (int i=0; i<refs.length; i++) {
      ConstantMap[] maps;
      if (i == 0) {
        // thick plane outline
        renderers[i] = displayRenderer.makeDefaultRenderer();
        maps = new ConstantMap[] {new ConstantMap(3.0, Display.LineWidth)};
      }
      else if (i == 1) {
        // semi-transparent plane
        renderers[i] = displayRenderer.makeDefaultRenderer();
        maps = new ConstantMap[] {
          new ConstantMap(plane_r, Display.Red),
          new ConstantMap(plane_g, Display.Green),
          new ConstantMap(plane_b, Display.Blue),
          new ConstantMap(0.75, Display.Alpha)
        };
      }
      else {
        // manipulable plane definition points
        renderers[i] = new DirectManipulationRendererJ3D();
        renderers[i].setPickCrawlToCursor(false);
        maps = new ConstantMap[] {
          new ConstantMap(lineValues[0][i - 2] / 255.0, Display.Red),
          new ConstantMap(lineValues[1][i - 2] / 255.0, Display.Green),
          new ConstantMap(lineValues[2][i - 2] / 255.0, Display.Blue),
          new ConstantMap(6.0, Display.PointSize)
        };
      }
      renderers[i].suppressExceptions(true);
      renderers[i].toggle(false);
      display.addReferences(renderers[i], refs[i], maps);
    }
  }

  /** Adds a PlaneListener to be notified when plane changes. */
  public void addListener(PlaneListener l) { listeners.add(l); }

  /** Removes a PlaneListener. */
  public void removeListener(PlaneListener l) { listeners.remove(l); }

  /** Gets whether the plane selector is visible. */
  public boolean isVisible() { return visible; }


  // -- INTERNAL API METHODS --

  /** Writes the plane selector state to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    for (int i=2; i<refs.length; i++) {
      RealTuple tuple = (RealTuple) refs[i].getData();
      Real[] r = tuple == null ?
        new Real[] {null, null, null} : tuple.getRealComponents();
      for (int j=0; j<3; j++) {
        double value = r[j] == null ? Double.NaN : r[j].getValue();
        fout.println(value);
      }
    }
  }

  /** Restores the plane selector state from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
    for (int i=0; i<refs.length-2; i++) {
      try {
        double x = Double.parseDouble(fin.readLine().trim());
        double y = Double.parseDouble(fin.readLine().trim());
        double z = Double.parseDouble(fin.readLine().trim());
        setData(i, x, y, z);
      }
      catch (NumberFormatException exc) { }
    }
  }


  // -- HELPER METHODS --

  /** Refreshes the plane data from its endpoint locations. */
  protected boolean refresh() {
    // start the plane in a reasonable location if the data is missing
    int len = refs.length - 2;
    RealTuple[] tuple = new RealTuple[len];
    for (int i=0; i<len; i++) {
      tuple[i] = (RealTuple) refs[i + 2].getData();
      if (tuple[i] == null) {
        if (xtype == null) return true;
        double vx = i == 0 ? x1 : (i == 1 ? x2 : x3);
        double vy = i == 0 ? y1 : (i == 1 ? y2 : y3);
        double vz = i == 0 ? z1 : (i == 1 ? z2 : z3);
        setData(i, vx, vy, vz);
        return false;
      }
    }
    lines = null;
    plane = null;
    try {
      if (!computePlane(tuple)) return false;
      refs[0].setData(lines);
      refs[1].setData(plane);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
    notifyListeners();
    return true;
  }

  /** Computes the appropriate plane from the current endpoints. */
  protected boolean computePlane(RealTuple[] tuple)
    throws VisADException, RemoteException
  {
    int len = tuple.length;
    float[][] samples = new float[3][len + 1];
    for (int i=0; i<len; i++) {
      double[] values = tuple[i].getValues();
      for (int j=0; j<3; j++) samples[j][i] = (float) values[j];
    }
    for (int j=0; j<3; j++) samples[j][len] = samples[j][0];
    RealTupleType type = (RealTupleType) tuple[0].getType();
    float[][] samps = new float[3][4];
    for (int i=0; i<3; i++) {
      samps[i][0] = samples[i][0];
      samps[i][1] = (samples[i][0] + samples[i][1]) / 2;
      samps[i][2] = samples[i][2];
      samps[i][3] = samples[i][1];
    }
    FunctionType lineType = new FunctionType(type, range);
    Gridded3DSet lineSet =
      new Gridded3DSet(type, samples, 4, null, null, null, false);
    lines = new FlatField(lineType, lineSet);
    lines.setSamples(lineValues);
    plane = new Gridded3DSet(type, samps, 2, 2, null, null, null, false);
    return true;
  }

  /** Moves the given reference point. */
  protected void setData(int i, double x, double y, double z) {
    try {
      refs[i + 2].setData(new RealTuple(domain, new double[] {x, y, z}));
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Notifies all PlaneListeners that plane has changed. */
  protected void notifyListeners() {
    int size = listeners.size();
    for (int i=0; i<size; i++) {
      PlaneListener l = (PlaneListener) listeners.elementAt(i);
      l.planeChanged();
    }
  }

}
