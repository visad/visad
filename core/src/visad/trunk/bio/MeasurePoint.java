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

import java.rmi.RemoteException;
import java.util.Vector;
import visad.*;

/** MeasurePoint maintains a DataReference for measuring points in a field. */
public class MeasurePoint extends MeasureThing {

  /** List of all measurement points. */
  private static final Vector points = new Vector();

  /** First free id number for points. */
  private static int maxId = 0;
  
  /** Id number for the point. */
  int id;

  /** Constructs a measurement object to match the given field. */
  public MeasurePoint() throws VisADException, RemoteException {
    super(1, 2);
    id = maxId++;
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
    d.disableAction();

    // configure display appropriately
    d.getGraphicsModeControl().setPointSize(5.0f);
    d.getDisplayRenderer().setPickThreshhold(5.0f);

    // add endpoint
    addDirectManipRef(d, refs[0]);

    d.enableAction();
  }

}
