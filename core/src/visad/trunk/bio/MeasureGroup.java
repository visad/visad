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

/** MeasureGroup represents a possible grouping for measurements. */
public class MeasureGroup {

  // -- FIELDS --

  /** Name of the group. */
  private String name;

  /** Description of the group. */
  private String description;

  /** Id number for the group. */
  private int id;


  // -- CONSTRUCTOR --

  /** Constructs a measurement group. */
  public MeasureGroup(VisBio biovis, String name) {
    this.name = name;
    description = "";
    id = biovis.mm.maxGID++;
    biovis.mm.groups.add(this);
  }


  // -- API METHODS --

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


  // -- INTERNAL API METHODS --

  /** Sets the group's id. */
  void setId(int id) { this.id = id; }

}
