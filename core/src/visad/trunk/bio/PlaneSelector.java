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

import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * PlaneSelector maintains a data structure that can be
 * manipulated by the user to specify an arbitrary plane.
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

  /** Data type for the plane. */
  protected RealType xtype, ytype, ztype;

  /** Bounding box for the plane. */
  protected float lox, loy, loz, hix, hiy, hiz;

  /** Set representing the plane's 2-D slice through the bounding box. */
  protected Gridded3DSet plane;

  /** List of PlaneListeners to notify when plane changes. */
  protected Vector listeners = new Vector();


  // -- CONSTRUCTOR --

  /** Constructs a selection box. */
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
            double vy = i < 2 ? loy : hiy;
            double vz = i == 0 ? loz : hiz;
            setData(i, vx, vy, vz);
            return;
          }
        }

        // snap values to nearest box edge (must touch two of three axes)
        RealTupleType type = (RealTupleType) tuple[0].getType();
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
        int vcount = 0;
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
              if (p[0][i] == p[0][j] &&
                p[1][i] == p[1][j] &&
                p[2][i] == p[2][j])
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
        try {
          Gridded3DSet lines = null;
          plane = null;
          if (vcount > 0) {
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
            float[][] samples = new float[3][vcount + 1];
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
            lines = new Gridded3DSet(type, samples, vcount + 1);

            if (vcount == 3) {
              // planar slice is a triangle
              float[][] samps = new float[3][4];
              for (int i=0; i<3; i++) {
                samps[i][0] = samples[i][0];
                samps[i][1] = (samples[i][0] + samples[i][1]) / 2;
                samps[i][2] = samples[i][2];
                samps[i][3] = samples[i][1];
              }
              plane = new Gridded3DSet(type, samps, 2, 2);
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
              plane = new Gridded3DSet(type, samps, 2, 2);
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
              plane = new Gridded3DSet(type, samps, 2, 3);
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
              plane = new Gridded3DSet(type, samps, 2, 3);
            }
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
    for (int i=0; i<renderers.length; i++) renderers[i].toggle(visible);
  }

  /**
   * Adds the plane selector to its display, bounding it with the given
   * minimum and maximum x, y and z values (typically your data's range).
   */
  public void init(RealType xtype, RealType ytype, RealType ztype,
    float lox, float loy, float loz, float hix, float hiy, float hiz)
    throws VisADException, RemoteException
  {
    this.xtype = xtype;
    this.ytype = ytype;
    this.ztype = ztype;
    this.lox = lox;
    this.loy = loy;
    this.loz = loz;
    this.hix = hix;
    this.hiy = hiy;
    this.hiz = hiz;
    DisplayRenderer displayRenderer = display.getDisplayRenderer();
    for (int i=0; i<refs.length; i++) {
      ConstantMap[] maps;
      if (i == 0) {
        // thick plane outline
        renderers[i] = displayRenderer.makeDefaultRenderer();
        maps = new ConstantMap[] {
          new ConstantMap(0.0f, Display.Red),
          new ConstantMap(1.0f, Display.Green),
          new ConstantMap(1.0f, Display.Blue),
          new ConstantMap(3.0f, Display.LineWidth)
        };
      }
      else if (i == 1) {
        // semi-transparent plane
        renderers[i] = displayRenderer.makeDefaultRenderer();
        maps = new ConstantMap[] {
          new ConstantMap(1.0f, Display.Red),
          new ConstantMap(1.0f, Display.Green),
          new ConstantMap(1.0f, Display.Blue),
          new ConstantMap(0.75f, Display.Alpha)
        };
      }
      else {
        // manipulable plane definition points
        renderers[i] = new DirectManipulationRendererJ3D();
        renderers[i].setPickCrawlToCursor(false);
        maps = new ConstantMap[] {
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
  public Field extractSlice(FieldImpl field, int resx, int resy)
    throws VisADException, RemoteException
  {
    if (plane == null) return null;
    int rx = resx - 1;
    int ry = resy - 1;
    int[] lengths = plane.getLengths();
    int lx = lengths[0] - 1;
    int ly = lengths[1] - 1;
    int len = resx * resy;
    float[][] grid = new float[2][len];
    for (int y=0; y<resy; y++) {
      for (int x=0; x<resx; x++) {
        int index = y * resx + x;
        grid[0][index] = (float) lx * x / rx;
        grid[1][index] = (float) ly * y / ry;
      }
    }

    // extract 3-D slice
    SetType type3 = (SetType) plane.getType();
    Gridded3DSet set3 = new Gridded3DSet(type3,
      plane.gridToValue(grid), resx, resy);
    FieldImpl slice3 = (FieldImpl)
      field.resample(set3, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);

    /*
    // CTR - TEMP
    visad.data.visad.VisADForm saver = new visad.data.visad.VisADForm(true);
    try { saver.save("ftemp1.vad", slice3, false); }
    catch (Exception exc) { exc.printStackTrace(); }
    */

    // compute normal to plane
    double[] vals1 = ((RealTuple) refs[2].getData()).getValues();
    double[] vals2 = ((RealTuple) refs[3].getData()).getValues();
    double[] vals3 = ((RealTuple) refs[4].getData()).getValues();
    double ux = vals2[0] - vals1[0];
    double uy = vals2[1] - vals1[1];
    double uz = vals2[2] - vals1[2];
    double vx = vals3[0] - vals1[0];
    double vy = vals3[1] - vals1[1];
    double vz = vals3[2] - vals1[2];
    double nx = ux * vy - uy * vx;
    double ny = uy * vz - uz * vy;
    double nz = uz * vx - ux * vz;

    // compute normal to plane defined by normal vector and (0, 0, -1)
    double rotx = 0;
    double roty = -ny;
    double rotz = nx;
    double rotlen = Math.sqrt(rotx * rotx + roty * roty + rotz * rotz);
    rotx /= rotlen;
    roty /= rotlen;
    rotz /= rotlen;

    // compute angle of rotation
    double a = nx * nx + ny * ny + nz * nz;
    double c = nx * nx + ny * ny + (nz + 1) * (nz + 1);
    double theta = Math.acos((a - c + 1) / (2 * Math.sqrt(a)));

    // rotate plane about (rotx, roty, rotz) at angle theta
    float[][] samp3 = set3.getSamples();
    rotate(samp3, theta, rotx, roty, rotz);

    /*
    // CTR - TEMP
    try {
      FlatField ff = new FlatField((FunctionType) slice3.getType(), new Gridded3DSet(type3, samp3, resx, resy));
      ff.setSamples(slice3.getValues());
      saver.save("ftemp2.vad", ff, false);
    }
    catch (Exception exc) { exc.printStackTrace(); }
    */

    // convert slice to 2-D
    float[][] samp2 = {samp3[0], samp3[1]};
    RealType[] rt = type3.getDomain().getRealComponents();
    FunctionType ftype3 = (FunctionType) slice3.getType();
    RealTupleType type2 = new RealTupleType(new RealType[] {rt[0], rt[1]});
    FunctionType ftype2 = new FunctionType(type2, ftype3.getRange());
    Gridded2DSet set2 = new Gridded2DSet(type2, samp2, resx, resy);
    FlatField slice2 = new FlatField(ftype2, set2);
    slice2.setSamples(slice3.getValues(), false);

    return slice2;
  }

  /** Adds a PlaneListener to be notified when plane changes. */
  public void addListener(PlaneListener l) { listeners.add(l); }

  /** Removes a PlaneListener. */
  public void removeListener(PlaneListener l) { listeners.remove(l); }


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

  /** Rotates the samples around vector (nx, ny, nz) by angle theta. */
  protected void rotate(float[][] samples, double theta,
    double nx, double ny, double nz)
  {
    double s = Math.cos(theta / 2);
    double sin = Math.sin(theta / 2);
    double vx = sin * nx;
    double vy = sin * ny;
    double vz = sin * nz;
    double v_v = vx * vx + vy * vy + vz * vz;
    vx /= v_v;
    vy /= v_v;
    vz /= v_v;
    int len = samples[0].length;
    for (int i=0; i<len; i++) {
      double px = samples[0][i];
      double py = samples[1][i];
      double pz = samples[2][i];
      // q1 * q2 = (s1*s2 - v1.v2, s1*v2 + s2*v1 + (v1 x v2))
      // q = (s, vx, vy, vz)
      // p = (0, px, py, pz)
      // q' = (s, -vx, -vy, -vz)
      double q_p = -(vx * px + vy * py + vz * pz);
      double q_px = s * px + vx * py - vy * px;
      double q_py = s * py + vy * pz - vz * py;
      double q_pz = s * pz + vz * px - vx * pz;
      samples[0][i] = (float) (-q_p * vx + s * q_px - q_px * vy + q_py * vx);
      samples[1][i] = (float) (-q_p * vy + s * q_py - q_py * vz + q_pz * vy);
      samples[2][i] = (float) (-q_p * vz + s * q_pz - q_pz * vx + q_px * vz);
    }
  }

}
