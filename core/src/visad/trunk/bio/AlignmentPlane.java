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
  protected double dist12, dist13, dist23;


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
    fout.println(numIndices);
    for (int ndx=0; ndx<numIndices; ndx++) {
      for (int i=0; i<3; i++) {
        for (int j=0; j<3; j++) fout.println(pos[ndx][i][j]);
      }
    }
  }

  /** Restores the plane selector state from the given input stream. */
  void restoreState(BufferedReader fin) throws IOException, VisADException {
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

    double[] m = {1, 1, 1};
    if (locked) {
      // maintain constant endpoint distances
      double d12 = BioUtil.getDistance(pos[index][0], pos[index][1], m);
      if (!Util.isApproximatelyEqual(dist12, d12)) {
        // CTR - TODO - adjust 2nd endpoint
      }
      double d13 = BioUtil.getDistance(pos[index][0], pos[index][2], m);
      double d23 = BioUtil.getDistance(pos[index][1], pos[index][2], m);
      if (!Util.isApproximatelyEqual(dist13, d13) ||
        !Util.isApproximatelyEqual(dist23, d23))
      {
        // CTR - TODO - adjust 3rd endpoint
      }
    }
    else {
      dist12 = BioUtil.getDistance(pos[index][0], pos[index][1], m);
      dist13 = BioUtil.getDistance(pos[index][0], pos[index][2], m);
      dist23 = BioUtil.getDistance(pos[index][1], pos[index][2], m);
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
