
//
// RealTupleType.java
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

import java.rmi.*;

/**
   RealTupleType is the VisAD data type for tuples in R^n, for n>0.<P>
*/
public class RealTupleType extends TupleType {

  /** if not null, this coordinate system is used as a default for
      RealTuple-s of this type */
  private CoordinateSystem DefaultCoordinateSystem;

  /** default Unit-s derived from RealType components */
  private Unit[] DefaultUnits;

  /** if not null, this sampling is used as a default when this type
      is used as the domain or range of a field;
      null unless explicitly set */
  private Set DefaultSet;
  private boolean DefaultSetEverAccessed;

  /** system intrnsic RealTupleType-s;
      useful as Reference-s for CoordinateSystem-s */
  private static RealType[] components2c =
    {RealType.XAxis, RealType.YAxis};
  public static final RealTupleType SpatialCartesian2DTuple =
    new RealTupleType(components2c, true);
  private static RealType[] components3c =
    {RealType.XAxis, RealType.YAxis, RealType.ZAxis};
  public static final RealTupleType SpatialCartesian3DTuple =
    new RealTupleType(components3c, true);

  private static RealType[] components1t =
    {RealType.Time};
  public static final RealTupleType Time1DTuple =
    new RealTupleType(components1t, true);

  private static RealType[] components2g =
    {RealType.Generic, RealType.Generic};
  public final static RealTupleType Generic2D =
    new RealTupleType(components2g, true);

  private static RealType[] components3g = 
    {RealType.Generic, RealType.Generic, RealType.Generic};
  public final static RealTupleType Generic3D =
    new RealTupleType(components3g, true);

  public RealTupleType(RealType[] types) throws VisADException {
    this(types, null, null);
  }

  public RealTupleType(RealType a) throws VisADException {
    this(makeArray(a), null, null);
  }

  public RealTupleType(RealType a, RealType b) throws VisADException {
    this(makeArray(a, b), null, null);
  }

  public RealTupleType(RealType a, RealType b, RealType c)
         throws VisADException {
    this(makeArray(a, b, c), null, null);
  }

  public RealTupleType(RealType a, RealType b, RealType c, RealType d)
         throws VisADException {
    this(makeArray(a, b, c, d), null, null);
  }

  public RealTupleType(RealType[] types, CoordinateSystem coord_sys, Set set)
         throws VisADException {
    super(types);
    if (coord_sys != null && types.length != coord_sys.getDimension()) {
      throw new CoordinateSystemException(
        "RealTupleType: bad CoordinateSystem dimension");
    }
    DefaultCoordinateSystem = coord_sys;
    DefaultSet = set;
    DefaultSetEverAccessed = false;
    setDefaultUnits(types);
    if (DefaultCoordinateSystem != null &&
        !Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                              DefaultUnits)) {
      throw new UnitException("RealTupleType: CoordinateSystem Units must be " +
                              "convertable with default Units");
    }
    if (DefaultSet != null &&
        !Unit.canConvertArray(DefaultSet.getSetUnits(), DefaultUnits)) {
      throw new UnitException("RealTupleType: default Set Units must be " +
                              "convertable with default Units");
    }
    if (DefaultCoordinateSystem != null && DefaultSet != null) {
      CoordinateSystem cs = DefaultSet.getCoordinateSystem();
      if (cs != null &&
          !cs.getReference().equals(DefaultCoordinateSystem.getReference())) {
        throw new CoordinateSystemException("RealTupleType: DefaultSet " +
                        "CoordinateSystem must match DefaultCoordinateSystem");
      }
      if (!Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                                DefaultSet.getSetUnits())) {
        throw new UnitException("RealTupleType: CoordinateSystem Units must be " +
                                "convertable with default Set Units");
      }
    }
  }

  public RealTupleType(RealType a, CoordinateSystem coord_sys,
         Set set) throws VisADException {
    this(makeArray(a), coord_sys, set);
  }

  public RealTupleType(RealType a, RealType b,
         CoordinateSystem coord_sys, Set set) throws VisADException {
    this(makeArray(a, b), coord_sys, set);
  }

  public RealTupleType(RealType a, RealType b, RealType c,
         CoordinateSystem coord_sys, Set set) throws VisADException {
    this(makeArray(a, b, c), coord_sys, set);
  }

  public RealTupleType(RealType a, RealType b, RealType c, RealType d,
         CoordinateSystem coord_sys, Set set) throws VisADException {
    this(makeArray(a, b, c, d), coord_sys, set);
  }

  /** trusted constructor for initializers */
  RealTupleType(RealType[] types, boolean b) {
    this(types, null, b);
  }

  /** trusted constructor for initializers */
  RealTupleType(RealType[] types, CoordinateSystem coord_sys, boolean b) {
    super(types, b);
    DefaultCoordinateSystem = coord_sys;
    DefaultSet = null;
    DefaultSetEverAccessed = false;
    setDefaultUnits(types);
    if (DefaultCoordinateSystem != null &&
        !Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                              DefaultUnits)) {
      throw new VisADError("RealTupleType (trusted): CoordinateSystem Units " +
                           "must be convertable with default Units");
    }
  }

  private static RealType[] makeArray(RealType a) {
    RealType[] types = {a};
    return types;
  }

  private static RealType[] makeArray(RealType a, RealType b) {
    RealType[] types = {a, b};
    return types;
  }

  private static RealType[] makeArray(RealType a, RealType b, RealType c) {
    RealType[] types = {a, b, c};
    return types;
  }

  private static RealType[] makeArray(RealType a, RealType b, RealType c,
                                      RealType d) {
    RealType[] types = {a, b, c, d};
    return types;
  }

  private void setDefaultUnits(RealType[] types) {
    int n = types.length;
    DefaultUnits = new Unit[n];
    for (int i=0; i<n; i++) {
      DefaultUnits[i] = types[i].getDefaultUnit();
    }
  }

  /** copy DefaultUnits array to ensure that it cannot be altered */
  public Unit[] getDefaultUnits() {
    return Unit.copyUnitsArray(DefaultUnits);
  }

  public CoordinateSystem getCoordinateSystem() {
    return DefaultCoordinateSystem;
  }

  /** set the default sampling; cannot be called after getDefaultSet */
  public synchronized void setDefaultSet(Set sampling) throws VisADException {
    if (sampling.getDimension() != getDimension()) {
      throw new SetException(
           "RealTupleType.setDefaultSet: dimensions don't match");
    }
    if (DefaultSetEverAccessed) {
      throw new TypeException("RealTupleType: DefaultSet already accessed" +
                               " so cannot change");
    }
    DefaultSet = sampling;

    /* WLH 4 Feb 98 copied from constructor */
    if (DefaultSet != null &&
        !Unit.canConvertArray(DefaultSet.getSetUnits(), DefaultUnits)) {
      throw new UnitException("RealTupleType: default Set Units must be " +
                              "convertable with default Units");
    }
    if (DefaultCoordinateSystem != null && DefaultSet != null) {
      CoordinateSystem cs = DefaultSet.getCoordinateSystem();
      if (cs != null &&
          !cs.getReference().equals(DefaultCoordinateSystem.getReference())) {
        throw new CoordinateSystemException("RealTupleType: DefaultSet " +
                        "CoordinateSystem must match DefaultCoordinateSystem");
      }
      if (!Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                                DefaultSet.getSetUnits())) {
        throw new UnitException("RealTupleType: CoordinateSystem Units must be " +
                                "convertable with default Set Units");
      }
    }
  }

  public synchronized Set getDefaultSet() {
    DefaultSetEverAccessed = true;
    return DefaultSet;
  }

  public Data missingData() {
    return new RealTuple(this);
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowRealTupleType(this, link, parent);
  }

}

