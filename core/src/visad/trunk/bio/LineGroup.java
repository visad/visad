//
// LineGroup.java
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

import java.awt.Color;

/** LineGroup represents a possible grouping for measurement lines. */
public class LineGroup {

  /** Name of the group. */
  private String name;

  /** Description of the group. */
  private String description;

  /** Default color of lines in the group. */
  private Color color;

  /** Constructs a pool of lines. */
  public LineGroup(String name, Color color) {
    this.name = name;
    this.color = color;
    description = "";
  }

  /** Sets the group's description. */
  public void setDescription(String desc) { description = desc; }

  /** Gets the group's name. */
  public String getName() { return name; }

  /** Gets the group's description. */
  public String getDescription() { return description; }

  /** Gets the group's default line color. */
  public Color getDefaultColor() { return color; }

}
