//
// LineChangedEvent.java
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

/** Event class for changes to a MeasureLine's endpoints. */
public class LineChangedEvent {

  /** The line that changed. */
  private MeasureLine line;

  /** The values of the line's endpoints. */
  private float[][] endpoints;

  /** Constructs a LineChangedEvent. */
  public LineChangedEvent(MeasureLine line, float[][] endpoints) {
    this.line = line;
    this.endpoints = new float[endpoints.length][];
    for (int i=0; i<endpoints.length; i++) {
      int len = endpoints[i].length;
      this.endpoints[i] = new float[len];
      System.arraycopy(endpoints[i], 0, this.endpoints[i], 0, len);
    }
  }

  /** Gets the line that changed. */
  public MeasureLine getLine() { return line; }

  /** Gets the new values of the line's endpoints. */
  public float[][] getEndpoints() { return endpoints; }

}
