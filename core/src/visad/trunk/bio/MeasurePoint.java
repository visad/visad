//
// MeasurePoint.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** MeasurePoint maintains a DataReference for measuring points in a field. */
public class MeasurePoint extends MeasureThing {

  /** List of all measurement points. */
  private static final Vector points = new Vector();

  /** Id number for the point. */
  private int id;

  /** Associated line pool. */
  private LinePool pool;

  /** Constructs a measurement point. */
  public MeasurePoint(int dim, LinePool pool)
    throws VisADException, RemoteException
  {
    super(1, dim);
    this.pool = pool;
    id = pool.maxPtId++;
    points.add(this);
  }

  /** Adds the measuring data to the given display. */
  public void setDisplay(DisplayImpl d)
    throws VisADException, RemoteException
  {
    if (display != null) {
      // remove measuring data from old display
      display.removeReference(refs[0]);
    }
    display = d;
    if (d == null) return;

    // configure display appropriately
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(5.0f);

    // add endpoint
    renderers = new DataRenderer[1];
    renderers[0] = addDirectManipRef(d, refs[0]);
  }

  /** Sets the color (unimplemented). */
  public void setColor(Color color) { return; }

  /** Gets the id number of the point. */
  public int getId() { return id; }

}
