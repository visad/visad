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

  /** Flag for free mode versus lockable mode. */
  protected boolean lockMode;

  /** Flags for whether each endpoint is locked at each timestep. */
  protected boolean[][] locked;

  /** Position of plane selector for each timestep. */
  protected double[][][] pos;

  /** Number of timesteps. */
  protected int numIndices;

  /** Current timestep value. */
  protected int index;


  // -- CONSTRUCTOR --

  /** Constructs a plane selector. */
  public AlignmentPlane(DisplayImpl display) { super(display); }


  // -- API METHODS --

  /** Toggles the plane's mode between manipulable endpoints and rotatable. */
  public void setMode(boolean lockable) {
    if (lockMode == lockable) return;
    lockMode = lockable;
    if (lockMode) display.removeDisplayListener(this);
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
    else if (id == DisplayEvent.MOUSE_RELEASED) {
      // CTR - TODO - lock or unlock picked point
    }
  }

}
