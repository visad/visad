//
// ScalarType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.util.*;

/**
   ScalarType is the superclass of the VisAD hierarchy of scalar data types.<P>
*/
public abstract class ScalarType extends MathType {

  // name of scalar type - enforce uniqueness locally
  // but do not rely on it - names may be duplicated on remote systems
  String Name;

  // Vector of scalar names used to make sure scalar names are unique
  // (within local VM)
  private static Vector ScalarVector = new Vector();

  /** all scalar types have a name */
  public ScalarType(String name) throws VisADException {
    super();
    if (name == null) {
      throw new TypeException("ScalarType: name cannot be null");
    }
    if (name.indexOf(".") > -1 ||
        name.indexOf(" ") > -1 ||
        name.indexOf("(") > -1 ||
        name.indexOf(")") > -1) {
      throw new TypeException("ScalarType: name cannot contain " +
                              "space . ( or ) " + name);
    }
    Enumeration scalars = ScalarVector.elements();
    while (scalars.hasMoreElements()) {
      ScalarType scalar = (ScalarType) scalars.nextElement();
      if (scalar.getName().equals(name)) {
        throw new TypeException("ScalarType: name already used");
      }
    }
    Name = name;
    ScalarVector.addElement(this);
  }

  /** trusted constructor for initializers */
  ScalarType(String name, boolean b) {
    super(b);
    Name = name;
    ScalarVector.addElement(this);
  }

  public static ScalarType getScalarTypeByName(String name) {
    Enumeration scalars = ScalarVector.elements();
    while (scalars.hasMoreElements()) {
      ScalarType scalar = (ScalarType) scalars.nextElement();
      if (name.equals(scalar.Name)) {
        return scalar;
      }
    }
    return null;
  }

  public String getName() {
    return Name;
  }

}

