//
// MeasureGroup.java
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

import java.util.Vector;

/** MeasureGroup represents a possible grouping for measurements. */
public class MeasureGroup {

  /** First free id number for groups. */
  static int maxId = 0;

  /** List of all groups. */
  static Vector groups = new Vector();

  /** Name of the group. */
  String name;

  /** Description of the group. */
  String description;

  /** Id number for the group. */
  int id;

  /** Constructs a measurement group. */
  public MeasureGroup(String name) {
    this.name = name;
    description = "";
    id = maxId++;
    groups.add(this);
  }

  /** Sets the group's description. */
  public void setDescription(String desc) { description = desc; }

  /** Gets the group's string representation (name). */
  public String toString() { return name; }

  /** Gets the group's name. */
  public String getName() { return name; }

  /** Gets the group's description. */
  public String getDescription() { return description; }

  /** Gets the id number of the group. */
  public int getId() { return id; }

}
