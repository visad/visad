
//
// RealType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
import java.rmi.*;

/**
   RealType is the VisAD scalar data type for real number variables.<P>
*/
public class RealType extends ScalarType {

  private Unit DefaultUnit; // default Unit of RealType
  private Set DefaultSet;

  /** Cartesian spatial coordinates */
  public final static RealType XAxis = new RealType("XAxis", null, true);
  public final static RealType YAxis = new RealType("YAxis", null, true);
  public final static RealType ZAxis = new RealType("ZAxis", null, true);

  /** Spherical spatial coordinates */
  public final static RealType Latitude =
    new RealType("Latitude", Unit.degree, true);
  public final static RealType Longitude =
    new RealType("Longitude", Unit.degree, true);
  public final static RealType Radius =
    new RealType("Radius", null, true);

  /** Temporal coordinate */
  public final static RealType Time =
    new RealType("Time", SI.second, true);

  /** generic RealType */
  public final static RealType Generic =
    new RealType("GENERIC_REAL", Unit.promiscuous, true);

  /** construct a RealType with given Unit and default set */
  public RealType(String name, Unit u, Set set) throws VisADException {
    super(name);
    if (set != null && set.getDimension() != 1) {
      throw new SetException("RealType: default set dimension != 1");
    }
    DefaultSet = set;
    if (u != null && set != null) {
      Unit[] us = {u};
      if (!Unit.canConvertArray(us, set.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
    DefaultUnit = u;
  }

  /** trusted constructor for initializers */
  RealType(String name, Unit u, boolean b) {
    super(name, b);
    DefaultUnit = u;
    DefaultSet = null;
  }

  public Unit getDefaultUnit() {
    return DefaultUnit;
  }

  public Set getDefaultSet() {
    return DefaultSet;
  }

  /** two RealType-s are equal if they have the same name; this allows
      equality with a RealType copied from a remote Java virtual machine;
      it doesn't matter whether DefaultUnit and DefaultSet are equal */
  public boolean equals(Object type) {
    if (!(type instanceof RealType)) return false;
    return Name.equals(((RealType) type).Name);
  }

  /** any two RealType-s are equal except Name */
  public boolean equalsExceptName(MathType type) {
    return (type instanceof RealType);
  }

  public Data missingData() throws VisADException {
    return new Real(this);
  }

  public static RealType getRealTypeByName(String name) {
    ScalarType real = ScalarType.getScalarTypeByName(name);
    if (!(real instanceof RealType)) {
      return null;
    }
    return (RealType) real;
  }

  ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return new ShadowRealType(this, link, parent);
  }

  public String toString() {
    return getName();
  }

}

