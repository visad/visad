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


  // -- FIELDS --

  /** VisBio frame. */
  protected VisBio bio;

  /** Number of timesteps. */
  protected int numIndices;

  /** Position of plane selector for each timestep. */
  protected double[][][] pos;

  /** Current timestep value. */
  protected int index;

  /** Whether to keep endpoint distances fixed. */
  protected boolean locked;

  /** Fixed distances between endpoints. */
  protected double dist12, dist13, dist23, dist14, dist34;


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public AlignmentPlane(VisBio biovis, DisplayImpl display) {
    super(display);
    bio = biovis;
    numIndices = bio.sm.getNumberOfIndices();
    pos = new double[numIndices][3][3];
    locked = false;
  }


  // -- API METHODS --

  /** Sets the current timestep. */
  public void setIndex(int index) {
    if (this.index == index || index < 0 || index >= numIndices) return;
    this.index = index;

    // set endpoint values to match those at current index
    for (int i=0; i<3; i++) {
      setData(i, pos[index][i][0], pos[index][i][1], pos[index][i][2]);
    }
  }

  /** Sets whether to lock endpoint distances to maintain size and shape. */
  public void setLocked(boolean locked) { this.locked = locked; }


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

  /** Refreshes the plane data from its endpoint locations. */
  protected boolean refresh() {
    if (bio.state.restoring) return true;
    for (int i=0; i<3; i++) {
      RealTuple tuple = (RealTuple) refs[i + 2].getData();
      if (tuple == null) continue;
      try {
        Real[] r = tuple.getRealComponents();
        for (int j=0; j<3; j++) pos[index][i][j] = r[j].getValue();
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }

    double[] p1 = pos[index][0], p2 = pos[index][1], p3 = pos[index][2];
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

    double[] u = new double[3];
    for (int i=0; i<3; i++) u[i] = p2[i] - p1[i];
    double[] v = new double[3];
    for (int i=0; i<3; i++) v[i] = p3[i] - p1[i];
    double d12 = BioUtil.getDistance(p1, p2, m);
    double d13 = BioUtil.getDistance(p1, p3, m);
    double d23 = BioUtil.getDistance(p2, p3, m);

    if (locked) {
      // snap 2nd endpoint to bounding sphere
      if (!Util.isApproximatelyEqual(dist12, d12)) {
        double lamda = dist12 / d12;
        double[] p = new double[3];
        for (int i=0; i<3; i++) p[i] = p1[i] + lamda * u[i];
        setData(1, p[0], p[1], p[2]);
        return false;
      }

      // snap 3rd endpoint to bounding circle
      if (!Util.isApproximatelyEqual(dist13, d13) ||
        !Util.isApproximatelyEqual(dist23, d23))
      {
        // compute normal to plane
        double[] n = new double[3];
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        for (int i=0; i<3; i++) n[i] = v[i] / l;
        double d = n[0] * p3[0] + n[1] * p3[1] + n[2] * p3[2];

        return false;
      }
    }
    else {
      dist12 = d12;
      dist13 = d13;
      dist23 = d23;
      double lamda = (u[0] * v[0] + u[1] * v[1] + u[2] * v[2]) /
        (u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
      double[] p4 = new double[3];
      for (int i=0; i<3; i++) p4[i] = p1[i] + lamda * u[i];
      dist14 = BioUtil.getDistance(p1, p4, m);
      dist34 = BioUtil.getDistance(p3, p4, m);

      // CTR - START HERE
      // p4 is a projection of p3 onto the p2-p1 line.
      // dist14 is the distance between p4 and p1.

      // In 3rd endpoint snapping, we want to project the new p3 onto
      // the plane perpendicular to the triangle's plane.  This plane
      // is defined by a normal vector N = p2 - p1 (normalized) and the
      // new p4 point, computed using dist14 from the new p1.

      // Q = p3 - (N.p3 - N.p4) * N

      // The new point Q lies on the desired plane, but its distance
      // from point p4 is probably not correct.  We can trim the point
      // to the correct distance using dist34, as done when snapping
      // the second endpoint.

      // The resulting point should be the correctly snapped p3.
    }

    if (!super.refresh()) return false;

    return true;
  }

  /** Moves the given reference point. */
  protected void setData(int i, double x, double y, double z) {
    super.setData(i, x, y, z);
    pos[index][i][0] = x;
    pos[index][i][1] = y;
    pos[index][i][2] = z;
  }

}
