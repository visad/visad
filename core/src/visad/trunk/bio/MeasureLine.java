//
// MeasureLine.java
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

import java.awt.Color;
import java.util.Vector;

/** A connecting line in the list of measurements. */
public class MeasureLine extends MeasureThing {

  // -- FIELDS --

  /** Endpoints of this line. */
  MeasurePoint ep1, ep2;

  /** Whether this line is selected. */
  boolean selected;


  // -- CONSTRUCTORS --

  /**
   * Constructs a line with the given endpoints,
   * color, group and selection status.
   */
  public MeasureLine(MeasurePoint ep1, MeasurePoint ep2,
    Color color, MeasureGroup group, boolean selected)
  {
    this.ep1 = ep1;
    this.ep2 = ep2;
    init(color, group, selected, stdType, stdId);
  }

  /** Constructs a line cloned from the given line. */
  public MeasureLine(MeasureLine line) {
    ep1 = new MeasurePoint(line.ep1);
    ep2 = new MeasurePoint(line.ep2);
    init(line.color, line.group, false, line.stdType, line.stdId);
  }

  /**
   * Constructs a line cloned from the given line,
   * but with a (possibly) different Z value.
   */
  public MeasureLine(MeasureLine line, double z) {
    ep1 = new MeasurePoint(line.ep1, z);
    ep2 = new MeasurePoint(line.ep2, z);
    init(line.color, line.group, false, line.stdType, line.stdId);
  }

  /**
   * Constructs a line cloned from the given line, but
   * with a (possibly) different set of endpoint values.
   */
  public MeasureLine(MeasureLine line, double[] v1, double[] v2) {
    ep1 = new MeasurePoint(line.ep1, v1);
    ep2 = new MeasurePoint(line.ep2, v2);
    init(line.color, line.group, false, line.stdType, line.stdId);
  }


  // -- API METHODS --

  /** Sets the line's color to match the given one. */
  public void setColor(Color color) {
    this.color = color;
    ep1.refreshColor();
    ep2.refreshColor();
  }

  /** Sets the line's standard id to match the given id. */
  public void setStandard(int stdType, int stdId) {
    super.setStandard(stdType, stdId);
    ep1.setStandard(stdType, stdId);
    ep2.setStandard(stdType, stdId);
  }


  // -- HELPER METHODS --

  /** Initializes this measurement line with the given information. */
  private void init(Color color, MeasureGroup group, boolean selected,
    int stdType, int stdId)
  {
    this.color = color;
    this.group = group;
    this.selected = selected;
    this.stdType = stdType;
    this.stdId = stdId;
    ep1.lines.add(this);
    ep2.lines.add(this);
  }

}
