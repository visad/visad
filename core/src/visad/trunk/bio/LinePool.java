//
// LinePool.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

/** LinePool maintains a collection of MeasureLine objects. */
public class LinePool {

  /** Internal list of MeasureLine objects. */
  private Vector lines;

  /** Associated VisAD display. */
  private DisplayImpl display;

  /** Total number of lines. */
  private int size;

  /** Number of lines allocated. */
  private int used;

  /** Constructs a pool of lines. */
  public LinePool(DisplayImpl display) {
    lines = new Vector();
    this.display = display;
    size = 0;
    used = 0;
  }

  /**
   * Sets the endpoint values for all lines in the
   * line pool to match the given measurements.
   */
  public void setLines(Measurement[] m) {
    int numLines = m.length;

    // set each reference accordingly
    used = 0;
    for (int i=0; i<numLines; i++) addLine(m[i]);

    // hide extra references
    for (int i=numLines; i<size; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.hide();
    }
  }

  /** Sets a line in the line pool to match the given measurement. */
  public void addLine(Measurement m) {
    if (used == size) {
      try {
        final MeasureLine line = new MeasureLine();
        lines.add(line);
        line.setMeasurement(m);
        Thread t = new Thread(new Runnable() {
          public void run() {
            try {
              // add line to display in new thread, for safety
              line.addToDisplay(display);
            }
            catch (VisADException exc) { exc.printStackTrace(); }
            catch (RemoteException exc) { exc.printStackTrace(); }
          }
        });
        t.start();
        size++;
      }
      catch (VisADException exc) { exc.printStackTrace(); }
      catch (RemoteException exc) { exc.printStackTrace(); }
    }
    else {
      MeasureLine line = (MeasureLine) lines.elementAt(used);
      line.setMeasurement(m);
    }
    used++;
  }

}
