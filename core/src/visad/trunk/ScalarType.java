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

import java.util.Hashtable;

/**
 * ScalarType is the superclass of the VisAD hierarchy of scalar data types.
 */
public abstract class ScalarType extends MathType {

  // name of scalar type - enforce uniqueness locally
  // but do not rely on it - names may be duplicated on remote systems
  String Name;

  // Hashtable of scalar names used to make sure scalar names are unique
  // (within local VM)
  private static Hashtable ScalarHash = new Hashtable();

  /**
   * Create a <CODE>ScalarType</CODE> with the specified name.
   *
   * @param name The name of this <CODE>ScalarType</CODE>
   *
   * @exception VisADException If the name is not valid.
   */
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
    if (ScalarHash.containsKey(name)) {
      throw new TypeException("ScalarType: name already used");
    }
    Name = name;
    ScalarHash.put(name, this);
  }

  /**
   * Trusted constructor used to create standard VisAD <CODE>RealType</CODE>s
   * without all the name-checking overhead.
   *
   * @param name Trusted name.
   * @param b Dummy value used to indicate that this is a trusted constructor.
   */
  ScalarType(String name, boolean b) {
    super(b);
    Name = name;
    ScalarHash.put(name, this);
  }

  /**
   * Returns this <CODE>ScalarType</CODE>'s name.
   *
   * @return The name of this <CODE>ScalarType</CODE>.
   */
  public String getName() {
    return Name;
  }

  /**
   * Get the <CODE>ScalarType</CODE> which has the specified name.
   *
   * @param name Name of <CODE>ScalarType</CODE>.
   * @return Either the <CODE>ScalarType</CODE> if found,
   *          or <CODE>null</CODE>.
   */
  public static ScalarType getScalarTypeByName(String name) {
    return (ScalarType )ScalarHash.get(name);
  }
}
