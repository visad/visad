
//
// RealType.java
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
import java.rmi.*;

/**
   RealType is the VisAD scalar data type for real number variables.<P>
*/
public class RealType extends ScalarType {

  private Unit DefaultUnit; // default Unit of RealType

  /** if not null, this sampling is used as a default when this type
      is used as the domain or range of a field;
      null unless explicitly set */
  private Set DefaultSet;
  private boolean DefaultSetEverAccessed;


  /** Cartesian spatial coordinates */
  public final static RealType XAxis = new RealType("XAxis", null, true);
  public final static RealType YAxis = new RealType("YAxis", null, true);
  public final static RealType ZAxis = new RealType("ZAxis", null, true);

  /** Spherical spatial coordinates */
  public final static RealType Latitude =
    new RealType("Latitude", CommonUnit.degree, true);
  public final static RealType Longitude =
    new RealType("Longitude", CommonUnit.degree, true);
  public final static RealType Radius =
    new RealType("Radius", null, true);

  /** Temporal coordinate */
  public final static RealType Time =
    new RealType("Time", SI.second, true);

  /** generic RealType */
  public final static RealType Generic =
    new RealType("GENERIC_REAL", CommonUnit.promiscuous, true);

 
  /** construct a RealType with null Unit and default set */
  public RealType(String name) throws VisADException {
    this(name, null, null);
  }

  /** construct a RealType with given Unit and default set */
  public RealType(String name, Unit u, Set set) throws VisADException {
    super(name);
    if (set != null && set.getDimension() != 1) {
      throw new SetException("RealType: default set dimension != 1");
    }
    DefaultUnit = u;
    DefaultSet = set;
    DefaultSetEverAccessed = false;
    if (DefaultUnit != null && DefaultSet != null) {
      Unit[] us = {DefaultUnit};
      if (!Unit.canConvertArray(us, DefaultSet.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
  }

  /** trusted constructor for initializers */
  RealType(String name, Unit u, boolean b) {
    super(name, b);
    DefaultUnit = u;
    DefaultSet = null;
    DefaultSetEverAccessed = false;
  }

  public Unit getDefaultUnit() {
    return DefaultUnit;
  }

  public synchronized Set getDefaultSet() {
    DefaultSetEverAccessed = true;
    return DefaultSet;
  }

  /** set the default sampling; cannot be called after getDefaultSet */
  public synchronized void setDefaultSet(Set sampling) throws VisADException {
    if (sampling.getDimension() != 1) {
      throw new SetException(
           "RealType.setDefaultSet: default set dimension != 1");
    }
    if (DefaultSetEverAccessed) {
      throw new TypeException("RealType: DefaultSet already accessed" +
                               " so cannot change");
    }
    DefaultSet = sampling;
 
    if (DefaultUnit != null && DefaultSet != null) {
      Unit[] us = {DefaultUnit};
      if (!Unit.canConvertArray(us, DefaultSet.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
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

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowRealType(this, link, parent);
  }

  public String toString() {
    return getName();
  }

}

