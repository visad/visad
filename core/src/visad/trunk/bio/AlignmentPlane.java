//
// AlignmentPlane.java
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
import visad.util.Util;

/**
 * AlignmentPlane maintains an arbitrary plane
 * specifying a spatial alignment in 3-D.
 */
public class AlignmentPlane extends PlaneSelector {

  // -- CONSTANTS --

  /** Header for alignment plane data in state file. */
  private static final String ALIGN_HEADER = "# Alignment";

  /** Mode where drift correction is off. */
  public static final int OFF_MODE = 0;

  /** Mode where user has free control over alignment plane endpoints. */
  public static final int SET_MODE = 1;

  /** Mode where alignment plane can be moved but not resized. */
  public static final int ADJUST_MODE = 2;

  /** Mode where alignment plane settings are applied to the display. */
  public static final int APPLY_MODE = 3;


  // -- FIELDS --

  /** VisBio frame. */
  protected VisBio bio;

  /** Number of timesteps. */
  protected int numIndices;

  /** Position of plane selector for each timestep. */
  protected double[][][] pos;

  /** Current timestep value. */
  protected int index;

  /** Alignment plane mode. */
  protected int mode;

  /** Fixed distances between endpoints. */
  protected double dist12, dist13, dist23, dist34, d_dist;


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public AlignmentPlane(VisBio biovis, DisplayImpl display) {
    super(display);
    bio = biovis;
    numIndices = bio.sm.getNumberOfIndices();
    pos = new double[numIndices][3][3];
    mode = OFF_MODE;
  }


  // -- API METHODS --

  /** Sets the current timestep. */
  public void setIndex(int index) {
    if (this.index == index || index < 0 || index >= numIndices) return;
    int old_index = index;
    this.index = index;

    // set endpoint values to match those at current index
    for (int i=0; i<3; i++) setData(i, descale(pos[index][i]));

    if (mode == APPLY_MODE) alignDisplay(old_index);
  }

  /** Sets whether to lock endpoint distances to maintain size and shape. */
  public void setMode(int mode) {
    if (mode != OFF_MODE && mode != SET_MODE &&
      mode != ADJUST_MODE && mode != APPLY_MODE)
    {
      return;
    }
    this.mode = mode;
    toggle(mode == SET_MODE || mode == ADJUST_MODE);
  }


  // -- INTERNAL API METHODS --

  /** Writes the alignment plane state to the given output stream. */
  void saveState(PrintWriter fout) throws IOException, VisADException {
    fout.println(ALIGN_HEADER);
    fout.println(numIndices);
    for (int ndx=0; ndx<numIndices; ndx++) {
      for (int i=0; i<3; i++) {
        for (int j=0; j<3; j++) fout.println(pos[ndx][i][j]);
      }
    }
  }

  /** Restores the plane selector state from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
    if (!fin.readLine().trim().equals(ALIGN_HEADER)) {
      throw new VisADException("AlignmentPlane: incorrect state format");
    }
    numIndices = Integer.parseInt(fin.readLine());
    pos = new double[numIndices][3][3];
    for (int ndx=0; ndx<numIndices; ndx++) {
      for (int i=0; i<3; i++) {
        for (int j=0; j<3; j++) {
          pos[ndx][i][j] = Double.parseDouble(fin.readLine());
        }
      }
    }
  }


  // -- HELPER METHODS --

  /** Aligns the display to match the alignment plane. */
  protected void alignDisplay(int old_index) {
    // make_matrix(rx, ry, rz, 1.0, tx, ty, tz);
    double[] old_v = pos[old_index][0];
    double[] v = pos[index][0];
    double[] t = new double[3];
    for (int i=0; i<3; i++) t[i] = old_v[i] - v[i];
    // CTR - ?
    t[0] /= bio.sm.res_x;
    t[1] /= bio.sm.res_y;
    t[2] /= bio.sm.getNumberOfSlices();

    ProjectionControl control = display.getProjectionControl();
    double[] matrix = control.getMatrix();
    double[] mult = display.make_matrix(0.0, 0.0, 0.0, 1.0, t[0], t[1], t[2]);
    try {
      control.setMatrix(display.multiply_matrix(mult, matrix));
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /** Refreshes the plane data from its endpoint locations. */
  protected boolean refresh() {
    if (bio.state == null || bio.state.restoring) return true;
    for (int i=0; i<3; i++) {
      RealTuple tuple = (RealTuple) refs[i + 2].getData();
      if (tuple == null) continue;
      try {
        Real[] r = tuple.getRealComponents();
        double[] vals = new double[3];
        for (int j=0; j<3; j++) vals[j] = r[j].getValue();
        setPos(i, vals);
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }

    double[] p1 = pos[index][0], p2 = pos[index][1], p3 = pos[index][2];
    double[] m = {1, 1, 1};

    double[] u = new double[3];
    for (int i=0; i<3; i++) u[i] = p2[i] - p1[i];
    double[] v = new double[3];
    for (int i=0; i<3; i++) v[i] = p3[i] - p1[i];
    double d12 = BioUtil.getDistance(p1, p2, m);
    double d13 = BioUtil.getDistance(p1, p3, m);
    double d23 = BioUtil.getDistance(p2, p3, m);

    if (mode == ADJUST_MODE) {
      // snap 2nd endpoint to bounding sphere
      if (!Util.isApproximatelyEqual(dist12, d12)) {
        double lamda = dist12 / d12;
        double[] p = new double[3];
        for (int i=0; i<3; i++) p[i] = p1[i] + lamda * u[i];
        setData(1, descale(p));
        return false;
      }

      // snap 3rd endpoint to bounding circle
      if (!Util.isApproximatelyEqual(dist13, d13) ||
        !Util.isApproximatelyEqual(dist23, d23))
      {
        // p = 3rd endpoint after tweaking (what we are ultimately computing)
        // p4 = p's projection onto p2-p1 line
        // n = normal to p4-p plane
        // d = distance between p4-p plane and origin
        // q = p3's projection onto p4-p plane

        // compute n
        double[] n = new double[3];
        double l = Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
        for (int i=0; i<3; i++) n[i] = u[i] / l;

        // compute p4 and d
        double[] p4 = new double[3];
        for (int i=0; i<3; i++) p4[i] = p1[i] + d_dist * u[i];
        double d = n[0] * p4[0] + n[1] * p4[1] + n[2] * p4[2];

        // compute q
        double c = (d - (n[0] * p3[0] + n[1] * p3[1] + n[2] * p3[2])) /
          (n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
        double[] q = new double[3];
        for (int i=0; i<3; i++) q[i] = c * n[i] + p3[i];

        // compute p
        double d34 = BioUtil.getDistance(q, p4, m);
        double lamda = dist34 / d34;
        double[] p = new double[3];
        for (int i=0; i<3; i++) p[i] = p4[i] + lamda * (q[i] - p4[i]);

        setData(2, descale(p));
        return false;
      }
    }
    else {
      dist12 = d12;
      dist13 = d13;
      dist23 = d23;
      d_dist = (u[0] * v[0] + u[1] * v[1] + u[2] * v[2]) /
        (u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
      double[] p4 = new double[3];
      for (int i=0; i<3; i++) p4[i] = p1[i] + d_dist * u[i];
      dist34 = BioUtil.getDistance(p3, p4, m);
    }

    if (!super.refresh()) return false;

    return true;
  }

  /** Moves the given reference point. */
  protected void setData(int i, double[] vals) {
    super.setData(i, vals);
    setPos(i, vals);
  }

  /** Updates internal position values. */
  protected void setPos(int i, double[] vals) {
    double[] m = getScale();
    double[] v = new double[3];
    boolean equal = true;
    for (int j=0; j<3; j++) {
      v[j] = m[j] * vals[j];
      if (!Util.isApproximatelyEqual(pos[index][i][j], v[j], 0.0001)) {
        equal = false;
      }
    }
    if (equal) return;
    int startIndex = mode == ADJUST_MODE ? index : 0;
    for (int ndx=startIndex; ndx<numIndices; ndx++) {
      for (int j=0; j<3; j++) pos[ndx][i][j] = v[j];
    }
  }

  /** Convert point from scaled to non-scaled. */
  protected double[] descale(double[] vals) {
    double[] m = getScale();
    double[] v = new double[3];
    for (int i=0; i<3; i++) v[i] = vals[i] / m[i];
    return v;
  }

  /** Gets coordinate system scale. */
  protected double[] getScale() {
    double[] m = bio.mm.getMicronDistances();
    if (m[0] == m[0] && m[1] == m[1] && m[2] == m[2]) {
      m[0] /= bio.sm.res_x;
      m[1] /= bio.sm.res_y;
    }
    else {
      m[0] = 1.0 / bio.sm.res_x;
      m[1] = 1.0 / bio.sm.res_y;
      m[2] = 1.0 / bio.sm.getNumberOfSlices();
    }
    return m;
  }

}
