
//
// ScalarType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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
                              ". ( or ) " + name);
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

