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

/**
 * AlignmentPlane maintains an arbitrary plane
 * specifying a spatial alignment in 3-D.
 */
public class AlignmentPlane extends PlaneSelector {

  // -- FIELDS --

  /** VisBio frame. */
  protected VisBio bio;

  /** Flags for whether each endpoint is locked at each timestep. */
  protected boolean[][] locked;

  /** Position of plane selector for each timestep. */
  protected double[][][] pos;

  /** Number of timesteps. */
  protected int numIndices;

  /** Current timestep value. */
  protected int index;

  /** Maximum timestep value. */
  protected int maxIndex;


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public AlignmentPlane(VisBio biovis, DisplayImpl display) {
    super(display);
    bio = biovis;
    maxIndex = 10;
    locked = new boolean[maxIndex][3];
    pos = new double[maxIndex][3][3];
  }


  // -- API METHODS --

  /** Sets the current timestep. */
  public void setIndex(int index) {
    if (this.index == index) return;
    this.index = index;
    if (index >= maxIndex) {
      int ndx = 2 * index + 1;
      boolean[][] nlock = new boolean[ndx][3];
      double[][][] npos = new double[ndx][3][3];
      System.arraycopy(locked, 0, nlock, 0, maxIndex);
      System.arraycopy(pos, 0, npos, 0, maxIndex);
      locked = nlock;
      pos = npos;
      maxIndex = ndx;
    }
    // set endpoint values to match those at current index
    for (int i=0; i<3; i++) {
      setData(i, pos[index][i][0], pos[index][i][1], pos[index][i][2]);
    }
  }


  // -- HELPER METHODS --

  /** Refreshes the plane data from its endpoint locations. */
  protected boolean refresh() {
    if (!super.refresh()) return false;
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
