//
// RealTupleType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import java.util.Vector;

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

  private static RealType[] components2c =
    {RealType.XAxis, RealType.YAxis};
  /**
   * System intrinsic
   * RealTupleType for (RealType.XAxis, RealType.YAxis)
   */
  public static final RealTupleType SpatialCartesian2DTuple =
    new RealTupleType(components2c, true);


  private static RealType[] components3c =
    {RealType.XAxis, RealType.YAxis, RealType.ZAxis};
  /**
   * System intrinsic
   * for (RealType.XAxis, RealType.YAxis, RealType.ZAxis)
   */
  public static final RealTupleType SpatialCartesian3DTuple =
    new RealTupleType(components3c, true);


  private static RealType[] components2e =
    {RealType.Longitude, RealType.Latitude};
  /**
   * System intrinsic
   * for (RealType.Longitude, RealType.Latitude)
   */
  public final static RealTupleType SpatialEarth2DTuple =
    new RealTupleType(components2e, true);


  private static RealType[] componentsll =
    {RealType.Latitude, RealType.Longitude};
  /**
   * System intrinsic
   * for (RealType.Latitude, RealType.Longitude)
   */
  public final static RealTupleType LatitudeLongitudeTuple =
    new RealTupleType(componentsll, true);


  private static RealType[] components3e =
    {RealType.Longitude, RealType.Latitude, RealType.Altitude};
  /**
   * System intrinsic
   * for (RealType.Longitude, RealType.Latitude, RealType.Altitude)
   */
  public final static RealTupleType SpatialEarth3DTuple =
    new RealTupleType(components3e, true);


  private static RealType[] componentslla =
    {RealType.Latitude, RealType.Longitude, RealType.Altitude};
  /**
   * System intrinsic
   * for (RealType.Latitude, RealType.Longitude, RealType.Altitude)
   */
  public final static RealTupleType LatitudeLongitudeAltitude =
    new RealTupleType(componentslla, true);


  private static RealType[] components1t =
    {RealType.Time};
  /**
   * System intrinsic for (RealType.Time)
   */
  public static final RealTupleType Time1DTuple =
    new RealTupleType(components1t, true);


  private static RealType[] components2g =
    {RealType.Generic, RealType.Generic};
  /**
   * System intrinsic for (RealType.Generic, RealType.Generic)
   */
  public final static RealTupleType Generic2D =
    new RealTupleType(components2g, true);

  private static RealType[] components3g =
    {RealType.Generic, RealType.Generic, RealType.Generic};
  /**
   * System intrinsic
   * for (RealType.Generic, RealType.Generic, RealType.Generic)
   */
  public final static RealTupleType Generic3D =
    new RealTupleType(components3g, true);


  /** array of component types;
      default CoordinateSystem and Set are null */
  public RealTupleType(RealType[] types) throws VisADException {
    this(types, null, null);
  }

  /** construct a RealTupleType with one component */
  public RealTupleType(RealType a) throws VisADException {
    this(makeArray(a), null, null);
  }

  /** construct a RealTupleType with two components */
  public RealTupleType(RealType a, RealType b) throws VisADException {
    this(makeArray(a, b), null, null);
  }

  /** construct a RealTupleType with three components */
  public RealTupleType(RealType a, RealType b, RealType c)
         throws VisADException {
    this(makeArray(a, b, c), null, null);
  }

  /** construct a RealTupleType with four components */
  public RealTupleType(RealType a, RealType b, RealType c, RealType d)
         throws VisADException {
    this(makeArray(a, b, c, d), null, null);
  }

  /** array of component types;
      default CoordinateSystem for values of this type (including
      Function domains) and may be null; default Set used when this
      type is a FunctionType domain and may be null */
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
        throw new CoordinateSystemException("RealTupleType: Default coordinate system " +
                                            (coord_sys == null ? null :
                                             coord_sys.getReference()) +
                                            " must match" +
                                            " default set CoordinateSystem " +
                                            (cs == null ? null :
                                             cs.getReference()));
      }
      if (!Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                                DefaultSet.getSetUnits())) {
        throw new UnitException("RealTupleType: CoordinateSystem Units must be " +
                                "convertable with default Set Units");
      }
    }
  }

  /** construct a RealTupleType with one component */
  public RealTupleType(RealType a, CoordinateSystem coord_sys,
         Set set) throws VisADException {
    this(makeArray(a), coord_sys, set);
  }

  /** construct a RealTupleType with two components */
  public RealTupleType(RealType a, RealType b,
         CoordinateSystem coord_sys, Set set) throws VisADException {
    this(makeArray(a, b), coord_sys, set);
  }

  /** construct a RealTupleType with three components */
  public RealTupleType(RealType a, RealType b, RealType c,
         CoordinateSystem coord_sys, Set set) throws VisADException {
    this(makeArray(a, b, c), coord_sys, set);
  }

  /** construct a RealTupleType with four components */
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

  /**
   * Performs an arithmetic operation with another MathType.  CoordinateSystem
   * information, if it exists, will be lost.
   *
   * @param type		The other  MathType.
   * @param op			The arithmetic operation (see
   *				<code>Data</code>).
   * @param names		Database of names.
   * @return                    The MathType corresponding to the specified
   *                            arithmetic operation between this and the
   *                            other MathType.  If the returned type is
   *                            a RealTupleType, then it will not have a
   *                            CoordinateSystem.
   * @throws TypeException	<code>type</code> is </code>null</code> or
   *				can't be arithmetically combined with a
   *				RealTupleType.
   * @throws VisADException	Couldn't create necessary VisAD object.
   * @see visad.Data
   */
  public MathType binary( MathType type, int op, Vector names )
                  throws TypeException, VisADException
  {
    if (type == null) {
      throw new TypeException("RealTupleType.binary: type may not be null" );
    }
    if (type instanceof RealTupleType) {
      int n_comps = getDimension();
      RealType[] new_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        RealType type_component =
          (RealType) ((RealTupleType) type).getComponent(ii);
        new_types[ii] =
          (RealType) (this.getComponent(ii)).binary( type_component, op, names );
      }
      return new RealTupleType( new_types );
    }
    else if (type instanceof RealType) {
      int n_comps = getDimension();
      RealType[] new_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        new_types[ii] =
          (RealType) (this.getComponent(ii)).binary( type, op, names );
      }
      return new RealTupleType( new_types );
    }
    else if (type instanceof FunctionType &&
             ((FunctionType) type).getRange().equalsExceptName(this)) {
      return new FunctionType(((FunctionType) type).getDomain(),
        ((FunctionType) type).getRange().binary(this, DataImpl.invertOp(op), names));
    }
    else {
      throw new TypeException("RealTupleType.binary: types don't match" );
    }
/* WLH 10 Sept 98
    int n_comps = getDimension();
    MathType new_type = null;
    if (type instanceof RealTupleType)
    {
      RealType[] R_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        R_types[ii] = (RealType) getComponent(ii).binary(
                                 ((RealTupleType)type).getComponent(ii), op, names );
      }
      new_type = new RealTupleType( R_types, DefaultCoordinateSystem, null );
    }
    else if (type instanceof RealType)
    {
      RealType[] R_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        R_types[ii] = (RealType) getComponent(ii).binary( type, op, names );
      }
      new_type = new RealTupleType( R_types, DefaultCoordinateSystem, null );
    }
    else if (type instanceof TupleType)
    {
      throw new TypeException();
    }
    else if (type instanceof FunctionType )
    {
      new_type = type.binary( this, DataImpl.invertOp(op), names );
    }
    return new_type;
*/
  }

  public MathType unary( int op, Vector names )
                  throws VisADException
  {
    int n_comps = getDimension();
    RealType[] R_types = new RealType[ n_comps ];

    for ( int ii = 0; ii < n_comps; ii++ ) {
      R_types[ii] = (RealType) getComponent(ii).unary(op, names);
    }

    return new RealTupleType( R_types, DefaultCoordinateSystem, null );
  }

  public static RealType[] makeArray(RealType a) {
    RealType[] types = {a};
    return types;
  }

  public static RealType[] makeArray(RealType a, RealType b) {
    RealType[] types = {a, b};
    return types;
  }

  public static RealType[] makeArray(RealType a, RealType b, RealType c) {
    RealType[] types = {a, b, c};
    return types;
  }

  public static RealType[] makeArray(RealType a, RealType b, RealType c,
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

  /** get default Units of RealType components; copy DefaultUnits
      array to ensure that it cannot be altered */
  public Unit[] getDefaultUnits() {
    return Unit.copyUnitsArray(DefaultUnits);
  }

  /** get default CoordinateSystem */
  public CoordinateSystem getCoordinateSystem() {
    return DefaultCoordinateSystem;
  }

  /** set the default sampling;
      this is an unavoidable violation of MathType immutability -
      a RealTupleType must be an argument (directly or through a
      SetType) to the constructor of its default Set;
      this method throws an Exception if getDefaultSet has
      previously been invoked */
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
        throw new CoordinateSystemException("RealTupleType: Default coordinate system " +
                                            DefaultCoordinateSystem.getReference() +
                                            " must match" +
                                            " default set CoordinateSystem " +
                                            (cs == null ? null :
                                             cs.getReference()));
      }
      if (!Unit.canConvertArray(DefaultCoordinateSystem.getCoordinateSystemUnits(),
                                DefaultSet.getSetUnits())) {
        throw new UnitException("RealTupleType: CoordinateSystem Units must be " +
                                "convertable with default Set Units");
      }
    }
  }

  /** get default Set*/
  public synchronized Set getDefaultSet() {
    DefaultSetEverAccessed = true;
    return DefaultSet;
  }

  public boolean equalsExceptName(MathType type) {
    int n = getDimension();
    try {
      if (type instanceof RealType) {
        return (n == 1 && getComponent(0) instanceof RealType);
      }
      if (!(type instanceof RealTupleType)) return false;
      if (n != ((TupleType) type).getDimension()) return false;
      boolean flag = true;
      for (int i=0; i<n; i++) {
        flag = flag && getComponent(i).equalsExceptName(
                              ((TupleType) type).getComponent(i) );
      }
      return flag;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public boolean equalsExceptNameButUnits(MathType type) {
    int n = getDimension();
    try {
      if (type instanceof RealType) {
        return type.equalsExceptNameButUnits(this);
      }
      if (!(type instanceof RealTupleType)) return false;
      if (n != ((RealTupleType) type).getDimension()) return false;
      boolean flag = true;
      for (int i=0; i<n; i++) {
        flag = flag && getComponent(i).equalsExceptNameButUnits(
                              ((RealTupleType) type).getComponent(i) );
      }
      return flag;
    }
    catch (VisADException e) {
      return false;
    }
  }

  public String prettyString(int indent) {
    try {
      if (getDimension() == 1) {
        return ((RealType) getComponent(0)).toString();
      }
    }
    catch (VisADException e) {
      // return toString();  WLH 5 Jan 2000
      return super.prettyString(0);
    }
    // return toString();  WLH 5 Jan 2000
    return super.prettyString(0);
  }

  public Data missingData() {
    return new RealTuple(this);
  }

  public ShadowType buildShadowType(DataDisplayLink link, ShadowType parent)
             throws VisADException, RemoteException {
    return link.getRenderer().makeShadowRealTupleType(this, link, parent);
  }

}

