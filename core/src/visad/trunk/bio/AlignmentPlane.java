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
public class AlignmentPlane extends PlaneSelector implements DisplayListener {

  // -- FIELDS --

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
  public AlignmentPlane(DisplayImpl display) {
    super(display);
    maxIndex = 10;
    locked = new boolean[maxIndex][3];
    pos = new double[maxIndex][3][3];
    display.addDisplayListener(this);
  }


  // -- API METHODS --

  /** Sets the current timestep. */
  public void setIndex(int index) {
    if (this.index == index) return;
    this.index = index;
    if (index >= maxIndex) {
      int ndx = index + 1;
      boolean[][] nlock = new boolean[ndx][3];
      double[][][] npos = new double[ndx][3][3];
      System.arraycopy(locked, 0, nlock, 0, maxIndex);
      System.arraycopy(pos, 0, npos, 0, maxIndex);
      maxIndex = ndx;
    }
    // set endpoint values to match those at current index
    for (int i=0; i<3; i++) {
      setData(i, pos[index][i][0], pos[index][i][1], pos[index][i][2]);
    }
  }


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
    boolean left = (mods & InputEvent.BUTTON1_MASK) != 0;
    boolean ctrl = (mods & InputEvent.CTRL_MASK) != 0;

    // ignore non-left button events
    if (!left) return;

    if (id == DisplayEvent.MOUSE_PRESSED) {
      mx = x;
      my = y;
      m_ctrl = ctrl;
    }
    else if (id == DisplayEvent.MOUSE_RELEASED && x == mx && y == my) {
      // compute picked point
      DisplayImpl d = (DisplayImpl) e.getDisplay();
      MouseBehavior mb = d.getDisplayRenderer().getMouseBehavior();
      VisADRay ray = mb.findRay(x, y);
      double[] a = ray.position;
      int len = a.length;
      double[] b = new double[len];
      for (int i=0; i<len; i++) b[i] = a[i] + ray.vector[i];
      double ndx = -1;
      double mindist = Double.POSITIVE_INFINITY;
      //System.out.println("Mouse=(" + a[0] + ", " + a[1] + ", " + a[2] + ") - (" + b[0] + ", " + b[1] + ", " + b[2] + ")"); /* TEMP */
      for (int j=0; j<3; j++) {
        double[] v = pos[index][j];
        double dist = BioUtil.getDistance(a, b, v, false);
        //System.out.println("Checking #" + j + ": (" + v[0] + ", " + v[1] + ", " + v[2] + ") - dist=" + dist); /* TEMP */
        if (dist < mindist) {
          ndx = j;
          mindist = dist;
        }
      }

      // compute maximum distance threshold
      double[] e1 = BioUtil.pixelToDomain(display, 0, 0);
      double[] e2 = BioUtil.pixelToDomain(display,
        VisBio.PICKING_THRESHOLD, 0);
      double threshold = e2[0] - e1[0];

      if (mindist <= threshold) {
        // lock or unlock picked point
        //System.out.println("Picked: " + ndx); /* TEMP */
      }
    }
  }


  // -- HELPER METHODS --

  /** Refreshes the plane data from its endpoint locations. */
  protected boolean refresh() {
    if (!super.refresh()) return false;
    for (int i=0; i<3; i++) {
      RealTuple tuple = (RealTuple) refs[i + 2].getData();
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
