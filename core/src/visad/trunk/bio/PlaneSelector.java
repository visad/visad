//
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

import java.awt.event.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * PlaneSelector maintains a data structure that can be
 * manipulated by the user to specify an arbitrary plane.
 */
public class PlaneSelector implements DisplayListener {

  // -- CONSTANTS --

  /** Error range when detecting colocated points. */
  protected static final double EPS = 1.0e-10;


  // -- FIELDS --

  /** Associated display. */
  protected DisplayImpl display;

  /** Data references for the endpoints and linked plane. */
  protected DataReferenceImpl[] refs = new DataReferenceImpl[5];

  /** Data renderers for the endpoints and linked plane. */
  protected DataRenderer[] renderers = new DataRenderer[5];

  /** Computation cell for linking plane with endpoints. */
  protected CellImpl cell;

  /** Data type for the plane. */
  protected RealType xtype, ytype, ztype;

  /** Bounding box for the plane. */
  protected float lox, loy, loz, hix, hiy, hiz;

  /** Whether to snap endpoints to plane's bounding box. */
  protected boolean snap;

  /** Flag for whether selection plane is visible. */
  protected boolean visible;

  /** Flag for endpoint mode versus rotatable mode. */
  protected boolean endpointMode;

  /** Position of plane selector for each timestep. */
  protected double[][][] pos;

  /** Number of timesteps. */
  protected int numIndices;

  /** Current timestep value. */
  protected int index;

  /** Set representing the plane's 2-D slice through the bounding box. */
  protected Gridded3DSet lines;

  /** List of PlaneListeners to notify when plane changes. */
  protected Vector listeners = new Vector();


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public PlaneSelector(DisplayImpl display) {
    this.display = display;

    // set up cell that links plane with endpoints
    cell = new CellImpl() {
      public void doAction() {
        // start the plane in a reasonable location if the data is missing
        int len = refs.length - 2;
        RealTuple[] tuple = new RealTuple[len];
        for (int i=0; i<len; i++) {
          tuple[i] = (RealTuple) refs[i + 2].getData();
          if (tuple[i] == null) {
            if (xtype == null) return;
            double vx = i < 2 ? lox : hix;
            double vy = i == 0 ? loy : hiy;
            double vz = i == 0 ? loz : hiz;
            setData(i, vx, vy, vz);
            return;
          }
        }

        int vcount = 0;
        lines = null;
        float[][] samples = null;
        RealTupleType type = (RealTupleType) tuple[0].getType();
        if (snap) {
          // snap values to nearest box edge (must touch two of three axes)
          double[] x = new double[len];
          double[] y = new double[len];
          double[] z = new double[len];
          for (int i=0; i<len; i++) {
            double[] values = tuple[i].getValues();
            double vx = values[0];
            double vy = values[1];
            double vz = values[2];
            boolean cx1 = vx < lox;
            boolean cx2 = vx > hix;
            boolean cy1 = vy < loy;
            boolean cy2 = vy > hiy;
            boolean cz1 = vz < loz;
            boolean cz2 = vz > hiz;
            if (cx1) vx = lox;
            else if (cx2) vx = hix;
            if (cy1) vy = loy;
            else if (cy2) vy = hiy;
            if (cz1) vz = loz;
            else if (cz2) vz = hiz;
            boolean changed = cx1 || cx2 || cy1 || cy2 || cz1 || cz2;
            double rx = Math.abs(hix - lox);
            double ry = Math.abs(hiy - loy);
            double rz = Math.abs(hiz - loz);
            double dx1 = Math.abs(vx - lox) / rx;
            double dx2 = Math.abs(vx - hix) / rx;
            double dy1 = Math.abs(vy - loy) / ry;
            double dy2 = Math.abs(vy - hiy) / ry;
            double dz1 = Math.abs(vz - loz) / rz;
            double dz2 = Math.abs(vz - hiz) / rz;
            boolean xm = dx1 == 0 || dx2 == 0;
            boolean ym = dy1 == 0 || dy2 == 0;
            boolean zm = dz1 == 0 || dz2 == 0;
            if (!xm && !ym || !xm && !zm || !ym && !zm) {
              boolean axisx = dx1 < dx2;
              boolean axisy = dy1 < dy2;
              boolean axisz = dz1 < dz2;
              double distx = axisx ? dx1 : dx2;
              double disty = axisy ? dy1 : dy2;
              double distz = axisz ? dz1 : dz2;
              boolean snapx = distx <= distz || distx <= disty;
              boolean snapy = disty <= distx || disty <= distz;
              boolean snapz = !snapx || !snapy;
              if (snapx) vx = axisx ? lox : hix;
              if (snapy) vy = axisy ? loy : hiy;
              if (snapz) vz = axisz ? loz : hiz;
              changed = true;
            }
            if (changed) {
              setData(i, vx, vy, vz);
              return;
            }
            x[i] = vx;
            y[i] = vy;
            z[i] = vz;
          }

          // solve plane equation for box edge intersections
          //
          // x = x0 + s*(x1 - x0) + t*(x2 - x0) = x0 + s*x10 + t*x20
          // y = y0 + s*(y1 - y0) + t*(y2 - y0) = y0 + s*y10 + t*y20
          // z = z0 + s*(z1 - z0) + t*(z2 - z0) = y0 + s*z10 + t*z20
          //
          // t = (x - x0 - s*x10) / x20
          // y = y0 + s*y10 + [(x - x0 - s*x10) / x20] * y20
          //   = y0 + s*y10 + y20 * (x - x0) / x20 - y20 * s * x10 / x20
          //   = y0 + y20 * (x - x0) / x20 + s * (y10 - y20 * x10 / x20)
          // s = [y - y0 - y20 * (x - x0) / x20] / (y10 - y20 * x10 / x20)
          //   = [x20 * (y - y0) - y20 * (x - x0)] / (x20 * y10 - y20 * x10)
          //
          double NaN = Double.NaN;
          double[][] p = { // this sequence ensures the hull is ordered properly
            {NaN, lox, lox, lox, lox, NaN, hix, NaN, hix, NaN, hix, hix},
            {loy, NaN, loy, NaN, hiy, hiy, hiy, hiy, NaN, loy, loy, NaN},
            {loz, loz, NaN, hiz, NaN, loz, NaN, hiz, hiz, hiz, NaN, loz}
          };
          boolean[] valid = new boolean[12];
          for (int i=0; i<12; i++) {
            double px0 = p[0][i] - x[0];
            double py0 = p[1][i] - y[0];
            double pz0 = p[2][i] - z[0];
            double x10 = x[1] - x[0];
            double y10 = y[1] - y[0];
            double z10 = z[1] - z[0];
            double x20 = x[2] - x[0];
            double y20 = y[2] - y[0];
            double z20 = z[2] - z[0];
            if (px0 != px0) {
              // solve for x
              double s = (y20 * pz0 - z20 * py0) / (y20 * z10 - z20 * y10);
              double t = y20 == 0 ?
                ((pz0 - s * z10) / z20) :
                ((py0 - s * y10) / y20);
              p[0][i] = x[0] + s * x10 + t * x20;
              valid[i] = p[0][i] >= lox && p[0][i] <= hix;
            }
            else if (py0 != py0) {
              // solve for y
              double s = (z20 * px0 - x20 * pz0) / (z20 * x10 - x20 * z10);
              double t = z20 == 0 ?
              ((px0 - s * x10) / x20) :
              ((pz0 - s * z10) / z20);
              p[1][i] = y[0] + s * y10 + t * y20;
              valid[i] = p[1][i] >= loy && p[1][i] <= hiy;
            }
            else { // pz0 != pz0
              // solve for z
              double s = (x20 * py0 - y20 * px0) / (x20 * y10 - y20 * x10);
              double t = x20 == 0 ?
                ((py0 - s * y10) / y20) :
                ((px0 - s * x10) / x20);
              p[2][i] = z[0] + s * z10 + t * z20;
              valid[i] = p[2][i] >= loz && p[2][i] <= hiz;
            }
            // invalidate duplicate points
            if (valid[i]) {
              for (int j=0; j<i; j++) {
                if (!valid[j]) continue;
                if (Math.abs(p[0][i] - p[0][j]) < EPS &&
                  Math.abs(p[1][i] - p[1][j]) < EPS &&
                  Math.abs(p[2][i] - p[2][j]) < EPS)
                {
                  valid[i] = false;
                  break;
                }
              }
            }
            if (valid[i]) vcount++;
          }

          // analyze x, y, z for valid box edge intersections
          // there could be as few as 3 or as many as 6
          if (vcount > 0) {
            try {
              // extract valid box edge intersection points
              float[] ux = new float[vcount];
              float[] uy = new float[vcount];
              float[] uz = new float[vcount];
              for (int i=0, c=0; i<12; i++) {
                if (valid[i]) {
                  ux[c] = (float) p[0][i];
                  uy[c] = (float) p[1][i];
                  uz[c] = (float) p[2][i];
                  c++;
                }
              }
              // sort points in convex hull order
              boolean[] hull = new boolean[vcount];
              samples = new float[3][vcount + 1];
              samples[0][0] = ux[0];
              samples[1][0] = uy[0];
              samples[2][0] = uz[0];
              hull[0] = true;
              for (int i=1; i<vcount; i++) {
                double sx = samples[0][i - 1];
                double sy = samples[1][i - 1];
                double sz = samples[2][i - 1];
                boolean xok = sx == lox || sx == hix;
                boolean yok = sy == loy || sy == hiy;
                boolean zok = sz == loz || sz == hiz;
                for (int j=1; j<vcount; j++) {
                  if (hull[j]) continue;
                  if (xok && sx == ux[j] ||
                    yok && sy == uy[j] ||
                    zok && sz == uz[j])
                  {
                    // found next point in hull
                    samples[0][i] = ux[j];
                    samples[1][i] = uy[j];
                    samples[2][i] = uz[j];
                    hull[j] = true;
                    break;
                  }
                }
              }
              samples[0][vcount] = samples[0][0];
              samples[1][vcount] = samples[1][0];
              samples[2][vcount] = samples[2][0];
              lines = new Gridded3DSet(type, samples,
                vcount + 1, null, null, null, false);
            }
            catch (VisADException exc) { exc.printStackTrace(); }
          }
        }
        else {
          // do not snap to bounding box
          vcount = 3;
          samples = new float[3][vcount + 1];
          double[] values = tuple[0].getValues();
          for (int j=0; j<3; j++) {
            samples[j][0] = samples[j][vcount] = (float) values[j];
            pos[index][0][j] = values[j];
          }
          for (int i=1; i<len; i++) {
            values = tuple[i].getValues();
            for (int j=0; j<3; j++) {
              samples[j][i] = (float) values[j];
              pos[index][i][j] = values[j];
            }
          }
          if (endpointMode) {
            // set all indices to match the current one
            for (int ndx=0; ndx<numIndices; ndx++) {
              if (ndx == index) continue;
              for (int i=0; i<3; i++) {
                for (int j=0; j<3; j++) pos[ndx][i][j] = pos[index][i][j];
              }
            }
          }
          try {
            lines = new Gridded3DSet(type, samples,
              vcount + 1, null, null, null, false);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
        }

        Gridded3DSet plane = null;
        try {
          if (vcount == 3) {
            // planar slice is a triangle
            float[][] samps = new float[3][4];
            for (int i=0; i<3; i++) {
              samps[i][0] = samples[i][0];
              samps[i][1] = (samples[i][0] + samples[i][1]) / 2;
              samps[i][2] = samples[i][2];
              samps[i][3] = samples[i][1];
            }
            plane = new Gridded3DSet(type, samps,
              2, 2, null, null, null, false);
          }
          else if (vcount == 4) {
            // planar slice is a quadrilateral
            float[][] samps = new float[3][4];
            for (int i=0; i<3; i++) {
              samps[i][0] = samples[i][0];
              samps[i][1] = samples[i][1];
              samps[i][2] = samples[i][3];
              samps[i][3] = samples[i][2];
            }
            plane = new Gridded3DSet(type, samps,
              2, 2, null, null, null, false);
          }
          else if (vcount == 5) {
            // planar slice is a pentagon
            float[][] samps = new float[3][6];
            for (int i=0; i<3; i++) {
              samps[i][0] = samples[i][0];
              samps[i][1] = samples[i][1];
              samps[i][2] = samples[i][4];
              samps[i][3] = (samples[i][1] + samples[i][2]) / 2;
              samps[i][4] = samples[i][3];
              samps[i][5] = samples[i][2];
            }
            plane = new Gridded3DSet(type, samps,
              2, 3, null, null, null, false);
          }
          else if (vcount == 6) {
            // planar slice is a hexagon
            float[][] samps = new float[3][6];
            for (int i=0; i<3; i++) {
              samps[i][0] = samples[i][0];
              samps[i][1] = samples[i][1];
              samps[i][2] = samples[i][5];
              samps[i][3] = samples[i][2];
              samps[i][4] = samples[i][4];
              samps[i][5] = samples[i][3];
            }
            plane = new Gridded3DSet(type, samps,
              2, 3, null, null, null, false);
          }
          refs[0].setData(lines);
          refs[1].setData(plane);
        }
        catch (VisADException exc) { exc.printStackTrace(); }
        catch (RemoteException exc) { exc.printStackTrace(); }
        notifyListeners();
      }
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
    for (int i=0; i<2; i++) renderers[i].toggle(visible);
    for (int i=2; i<renderers.length; i++) {
      renderers[i].toggle(visible && endpointMode);
    }
  }

  /**
   * Adds the plane selector to its display, bounding it with the given
   * minimum and maximum x, y and z values (typically your data's range).
   */
  public void init(RealType xtype, RealType ytype, RealType ztype, int numTime,
    float lox, float loy, float loz, float hix, float hiy, float hiz)
    throws VisADException, RemoteException
  {
    this.xtype = xtype;
    this.ytype = ytype;
    this.ztype = ztype;
    snap = lox == lox;
    visible = false;
    endpointMode = true;
    numIndices = numTime;
    pos = new double[numIndices][3][3];
    if (snap) {
      this.lox = lox;
      this.loy = loy;
      this.loz = loz;
      this.hix = hix;
      this.hiy = hiy;
      this.hiz = hiz;
      index = -1;
    }
    else {
      this.lox = this.loy = this.loz = 0;
      this.hix = this.hiy = this.hiz = 1;
      index = 0;
    }
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    for (int i=0; i<refs.length; i++) {
      ConstantMap[] maps;
      if (i == 0) {
        // thick plane outline
        renderers[i] = displayRenderer.makeDefaultRenderer();
        if (snap) {
          maps = new ConstantMap[] { // cyan
            new ConstantMap(0.0f, Display.Red),
            new ConstantMap(1.0f, Display.Green),
            new ConstantMap(1.0f, Display.Blue),
            new ConstantMap(3.0f, Display.LineWidth)
          };
        }
        else {
          maps = new ConstantMap[] { // red
            new ConstantMap(1.0f, Display.Red),
            new ConstantMap(0.0f, Display.Green),
            new ConstantMap(0.0f, Display.Blue),
            new ConstantMap(3.0f, Display.LineWidth)
          };
        }
      }
      else if (i == 1) {
        // semi-transparent plane
        renderers[i] = displayRenderer.makeDefaultRenderer();
        if (snap) {
          maps = new ConstantMap[] { // gray
            new ConstantMap(1.0f, Display.Red),
            new ConstantMap(1.0f, Display.Green),
            new ConstantMap(1.0f, Display.Blue),
            new ConstantMap(0.75f, Display.Alpha)
          };
        }
        else {
          maps = new ConstantMap[] { // red
            new ConstantMap(1.0f, Display.Red),
            new ConstantMap(0.0f, Display.Green),
            new ConstantMap(0.0f, Display.Blue),
            new ConstantMap(0.75f, Display.Alpha)
          };
        }
      }
      else {
        // manipulable plane definition points
        renderers[i] = new DirectManipulationRendererJ3D();
        renderers[i].setPickCrawlToCursor(false);
        maps = new ConstantMap[] { // yellow
          new ConstantMap(1.0f, Display.Red),
          new ConstantMap(1.0f, Display.Green),
          new ConstantMap(0.0f, Display.Blue),
          new ConstantMap(6.0f, Display.PointSize)
        };
      }
      renderers[i].suppressExceptions(true);
      renderers[i].toggle(false);
      display.addReferences(renderers[i], refs[i], maps);
    }
  }

  /** Extracts a field using the current plane, at the given resolution. */
  public Field extractSlice(FieldImpl field, int resx, int resy,
    int hix, int hiy) throws VisADException, RemoteException
  {
    if (lines == null) return null;

    // extract hull points from lines data object
    float[][] samples = lines.getSamples(false);
    float[] x = samples[0];
    float[] y = samples[1];
    float[] z = samples[2];
    int numpts = x.length - 1;

    // find longest line segment
    double longest = 0;
    int index = -1;
    for (int i=0; i<numpts; i++) {
      float dx = x[i] - x[i + 1];
      float dy = y[i] - y[i + 1];
      float dz = z[i] - z[i + 1];
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len > longest) {
        longest = len;
        index = i;
      }
    }
    int p1 = index;
    int p2 = (index + 1) % numpts;
    int p = 0;
    while (p == p1 || p == p2) p++;

    // compute first two corners
    float[] c1 = new float[3];
    float[] c2 = new float[3];
    float[] proj = new float[3];
    project(x, y, z,
      x[p1], y[p1], z[p1], x[p2], y[p2], z[p2], p, c1, c2, proj);

    // compute third corner
    float[] c3 = new float[3];
    float[] l = corner(proj, c1, new float[] {x[p], y[p], z[p]});
    project(x, y, z, c1[0], c1[1], c1[2], l[0], l[1], l[2], -1, c1, c3, proj);

    // compute fourth corner
    float[] c4 = corner(c1, c2, c3);

    // construct 3-D planar grid
    SetType type3 = (SetType) lines.getType();
    float[][] samp3 = {
      {c1[0], c2[0], c3[0], c4[0]},
      {c1[1], c2[1], c3[1], c4[1]},
      {c1[2], c2[2], c3[2], c4[2]}
    };
    Gridded3DSet box3 = new Gridded3DSet(type3, samp3,
      2, 2, null, null, null, false);
    int rx = resx - 1;
    int ry = resy - 1;
    int len = resx * resy;
    float[][] grid = new float[2][len];
    for (int j=0; j<resy; j++) {
      for (int i=0; i<resx; i++) {
        index = j * resx + i;
        grid[0][index] = (float) i / rx;
        grid[1][index] = (float) j / ry;
      }
    }
    Gridded3DSet set3 = new Gridded3DSet(type3, box3.gridToValue(grid),
      resx, resy, null, null, null, false);

    // extract 3-D slice data
    FieldImpl slice3 = (FieldImpl)
      field.resample(set3, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);

    // construct 2-D planar grid
    RealType[] rt = type3.getDomain().getRealComponents();
    RealTupleType type2 = new RealTupleType(new RealType[] {rt[0], rt[1]});
    float[][] samp2 = new float[2][resx * resy];
    for (int j=0; j<resy; j++) {
      for (int i=0; i<resx; i++) {
        index = resx * j + i;
        samp2[0][index] = (float) hix * i / resx;
        samp2[1][index] = (float) hiy * j / resy;
      }
    }
    Gridded2DSet set2 = new Gridded2DSet(type2,
      samp2, resx, resy, null, null, null, false);

    // convert slice data to 2-D
    FunctionType ftype3 = (FunctionType) slice3.getType();
    FunctionType ftype2 = new FunctionType(type2, ftype3.getRange());
    FlatField slice2 = new FlatField(ftype2, set2);
    slice2.setSamples(slice3.getValues(false), false);

    return slice2;
  }

  /** Adds a PlaneListener to be notified when plane changes. */
  public void addListener(PlaneListener l) { listeners.add(l); }

  /** Removes a PlaneListener. */
  public void removeListener(PlaneListener l) { listeners.remove(l); }

  /** Toggles the plane's mode between manipulable endpoints and rotatable. */
  public void setMode(boolean endpoints) {
    if (endpointMode == endpoints) return;
    endpointMode = endpoints;
    if (endpointMode) display.removeDisplayListener(this);
    else display.addDisplayListener(this);
    toggle(visible);
  }

  /** Sets the current timestep. */
  public void setIndex(int index) {
    if (this.index == index) return;
    this.index = index;
    // set endpoint values to match those at current index
    for (int i=0; i<3; i++) {
      setData(i, pos[index][i][0], pos[index][i][1], pos[index][i][2]);
    }
  }

  /** Gets whether the plane selector is visible. */
  public boolean isVisible() { return visible; }


  // -- INTERNAL API METHODS --

  private int mx, my;
  private boolean m_ctrl;

  /** Listens for mouse events in the display. */
  public void displayChanged(DisplayEvent e) {
    int id = e.getId();
    InputEvent event = e.getInputEvent();

    // ignore non-mouse display events
    if (event == null || !(event instanceof MouseEvent)) return;

    int x = e.getX();
    int y = e.getY();
    int mods = e.getModifiers();
    boolean right = (mods & InputEvent.BUTTON3_MASK) != 0;
    boolean ctrl = (mods & InputEvent.CTRL_MASK) != 0;

    // ignore non-right button events
    if (!right) return;

    if (id == DisplayEvent.MOUSE_PRESSED) {
      mx = x;
      my = y;
      m_ctrl = ctrl;
    }
    else if (id == DisplayEvent.MOUSE_DRAGGED) {
      // CTR - TODO - actually, this is obsolete--purge it.
      // instead, do "locking down" of individual endpoints.
    }
  }

  /** Writes the current program state to the given output stream. */
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

  /** Restores the current program state from the given input stream. */
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

  /** Moves the given reference point. */
  protected void setData(int i, double x, double y, double z) {
    try {
      refs[i + 2].setData(new RealTuple(new Real[] {
        new Real(xtype, x),
        new Real(ytype, y),
        new Real(ztype, z)
      }));
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

  /**
   * Projects all the points in (x, y, z) onto the line defined by (p1, p2).
   * The points that bound the line segment are stored in (min, max).
   * The projection of the pth point is stored in proj.
   */
  protected static void project(float[] x, float[] y, float[] z,
    float p1x, float p1y, float p1z, float p2x, float p2y, float p2z,
    int p, float[] min, float[] max, float[] proj)
  {
    int numpts = x.length - 1;
    float x21 = p2x - p1x;
    float y21 = p2y - p1y;
    float z21 = p2z - p1z;
    float maxdist = x21 * x21 + y21 * y21 + z21 * z21;
    min[0] = p1x;
    min[1] = p1y;
    min[2] = p1z;
    max[0] = p2x;
    max[1] = p2y;
    max[2] = p2z;

    // project all hull points onto line
    for (int p3=0; p3<numpts; p3++) {
      float x31 = x[p3] - p1x;
      float y31 = y[p3] - p1y;
      float z31 = z[p3] - p1z;
      float u = (x31 * x21 + y31 * y21 + z31 * z21) /
        (x21 * x21 + y21 * y21 + z21 * z21);
      float px = p1x + u * x21;
      float py = p1y + u * y21;
      float pz = p1z + u * z21;
      if (p3 == p) {
        proj[0] = px;
        proj[1] = py;
        proj[2] = pz;
      }

      float pminx = px - min[0];
      float pminy = py - min[1];
      float pminz = pz - min[2];
      float pdistmin = pminx * pminx + pminy * pminy + pminz * pminz;
      float pmaxx = px - max[0];
      float pmaxy = py - max[1];
      float pmaxz = pz - max[2];
      float pdistmax = pmaxx * pmaxx + pmaxy * pmaxy + pmaxz * pmaxz;

      if (pdistmin > maxdist || pdistmax > maxdist) {
        if (pdistmin > pdistmax) {
          maxdist = pdistmin;
          max[0] = px;
          max[1] = py;
          max[2] = pz;
        }
        else {
          maxdist = pdistmax;
          min[0] = px;
          min[1] = py;
          min[2] = pz;
        }
      }
    }
  }

  /** Computes the fourth corner of a rectangle, given the first three. */
  private static float[] corner(float[] c1, float[] c2, float[] c3) {
    float[] c4 = new float[c1.length];
    for (int i=0; i<c1.length; i++) c4[i] = c3[i] + c2[i] - c1[i];
    return c4;
  }

  /** Saves a data object to a binary file, for debugging. */
  protected static void save(String file, DataImpl data) {
    System.err.print("Saving " + file + "... ");
    java.io.File f = new java.io.File(file);
    if (f.exists()) f.delete();
    visad.data.visad.VisADForm saver = new visad.data.visad.VisADForm(true);
    try { saver.save(file, data, false); }
    catch (Exception exc) { exc.printStackTrace(); }
    System.err.println("done.");
  }

}
