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

  /** Number of lines in a block. */
  private int blockSize;

  /** Total number of lines. */
  private int size;

  /** Number of lines allocated. */
  private int used;

  /** Constructs a pool of lines. */
  public LinePool(DisplayImpl display, int blockSize) {
    lines = new Vector();
    this.display = display;
    this.blockSize = blockSize;
    size = 0;
    used = 0;
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int numLines) {
    if (size == 0) {
      System.out.println("LinePool.expand: warning: " +
        "Cannot expand from zero without domain type");
      return;
    }
    MeasureLine line = (MeasureLine) lines.elementAt(0);
    RealTupleType domain = line.getDomain();
    expand(numLines, domain);
  }

  /** Ensures the line pool is at least the given size. */
  public void expand(int numLines, RealTupleType domain) {
    if (numLines <= size) return;
    int n = numLines - size;
    if (n % blockSize > 0) n += blockSize - n % blockSize;
    MeasureLine[] l = new MeasureLine[n];
    try {
      for (int i=0; i<n; i++) {
        l[i] = new MeasureLine();
        l[i].setType(domain);
        l[i].hide();
        lines.add(l[i]);
      }
      synchronized (this) {
        display.disableAction();
        for (int i=0; i<l.length; i++) {
          try {
            l[i].addToDisplay(display);
          }
          catch (VisADException exc) { exc.printStackTrace(); }
          catch (RemoteException exc) { exc.printStackTrace(); }
        }
        display.enableAction();
      }
      size += l.length;
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    catch (RemoteException exc) { exc.printStackTrace(); }
  }

  /**
   * Sets the endpoint values for all lines in the
   * line pool to match the given measurements.
   */
  public void setLines(Measurement[] m) {
    int numLines = m.length;

    // set each reference accordingly
    expand(numLines);
    for (int i=0; i<numLines; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.setMeasurement(m[i]);
    }

    // hide extra references
    for (int i=numLines; i<size; i++) {
      MeasureLine line = (MeasureLine) lines.elementAt(i);
      line.hide();
    }

    used = numLines;
  }

  /** Sets a line in the line pool to match the given measurement. */
  public void addLine(Measurement m) {
    expand(used + 1);
    MeasureLine line = (MeasureLine) lines.elementAt(used);
    line.setMeasurement(m);
    used++;
  }

}
