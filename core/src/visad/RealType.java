//
// RealType.java
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

  /**
   * The attribute mask of this RealType.
   * @serial
   */
  private final int     attrMask;

  /**
   * The interval attribute.  This attribute should be used during construction
   * of a RealType if the RealType refers to an interval (e.g. length
   * difference, delta temperature).  In general, RealType-s that are temporal
   * in nature should have this attribute because the "time" variable in most
   * formulae actually refer to time differences (e.g. "time since the
   * beginning of the experiment").  One obvious exception to this lies with
   * cosmology, where "time" is absolute rather than an interval.
   */
  public static final int       INTERVAL = 1;

  /**
   * Monotonically-increasing counter for creating new, unique names.
   */
  private static int            count;

  /** Cartesian spatial coordinate - X axis */
  public final static RealType XAxis = new RealType("XAxis", null, true);
  /** Cartesian spatial coordinate - Y axis */
  public final static RealType YAxis = new RealType("YAxis", null, true);
  /** Cartesian spatial coordinate - Z axis */
  public final static RealType ZAxis = new RealType("ZAxis", null, true);

  /** Spherical spatial coordinate for Latitude*/
  public final static RealType Latitude =
    new RealType("Latitude", CommonUnit.degree, true);
  /** Spherical spatial coordinate for Longitude*/
  public final static RealType Longitude =
    new RealType("Longitude", CommonUnit.degree, true);
  public final static RealType Altitude =
    new RealType("Altitude", CommonUnit.meter, true);
  public final static RealType Radius =
    new RealType("Radius", null, true);

  /** Temporal-interval coordinate */
  public final static RealType TimeInterval =
    new RealType("TimeInterval", CommonUnit.second, INTERVAL, true);

  /** Timestamp coordinate */
  public final static RealType Time =
    new RealType("Time", CommonUnit.secondsSinceTheEpoch, true);

  /** astronomical coordinates */
  public final static RealType Declination =
    new RealType("Declination", CommonUnit.degree, true);
  public final static RealType RightAscension =
    new RealType("RightAscension", CommonUnit.degree, true);

  /** generic RealType */
  public final static RealType Generic =
    new RealType("GENERIC_REAL", CommonUnit.promiscuous, true);


  /**
   * Constructs from a name (two RealTypes are equal if their names are equal).
   * Assumes <code>null</code> for the default Unit and default Set and that
   * the RealType does <em>not</em> refer to an interval.
   * @param name                The name for the RealType.
   * @throws VisADException     Couldn't create necessary VisAD object.
   * @deprecated Use {@link #getRealType(String)}
   */
  public RealType(String name) throws VisADException {
    this(name, 0);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * and whether or not the RealType refers to an interval (e.g. length
   * difference, delta temperature).  Assumes <code>null</code> for the default
   * Unit and default Set.
   * @param name                The name for the RealType.
   * @param attrMask            The attribute mask. 0 or INTERVAL.
   * @throws VisADException     Couldn't create necessary VisAD object.
   * @deprecated Use {@link #getRealType(String, int)}
   */
  public RealType(String name, int attrMask) throws VisADException {
    this(name, null, null, attrMask);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * a default Unit, and a default Set.  Assumes that the RealType does
   * <em>not</em> refer to an interval.
   * @param name                The name for the RealType.
   * @param u                   The default unit for the RealType.  May be
   *                            <code>null</code>.
   * @param set                 The default sampling set for the RealType.
   *                            Used when this type is a FunctionType domain.
   *                            May be <code>null</code>.
   * @throws VisADException     Couldn't create necessary VisAD object.
   * @deprecated Use {@link #getRealType(String, Unit, Set)}
   */
  public RealType(String name, Unit u, Set set) throws VisADException {
    this(name, u, set, 0);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * and a default Unit.  Assumes the default Set, and that the
   * RealType does <em>not</em> refer to an interval.
   * @param name                The name for the RealType.
   * @param u                   The default unit for the RealType.  May be
   *                            <code>null</code>.  
   * @throws VisADException     Couldn't create necessary VisAD object.
   * @deprecated Use {@link #getRealType(String, Unit)}
   */
  public RealType(String name, Unit u) throws VisADException {
    this(name, u, null, 0);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * a default Unit, a default Set, and whether or not the RealType refers to
   * an interval (e.g. length difference, delta temperature).  This is the most
   * general, public constructor.
   * @param name                The name for the RealType.
   * @param u                   The default unit for the RealType.  May be
   *                            <code>null</code>.  If non-<code>null</code>
   *                            and the RealType refers to an interval,
   *                            then the default unit will actually be
   *                            <code>u.getAbsoluteUnit()</code>.
   * @param set                 The default sampling set for the RealType.
   *                            Used when this type is a FunctionType domain.
   *                            May be <code>null</code>.
   * @param attrMask            The attribute mask. 0 or INTERVAL.
   * @throws VisADException     Couldn't create necessary VisAD object.
   * @deprecated Use {@link #getRealType(String, Unit, Set, int)}
   */
  public RealType(String name, Unit u, Set set, int attrMask)
    throws VisADException
  {
    super(name);
    if (set != null && set.getDimension() != 1) {
      throw new SetException("RealType: default set dimension != 1");
    }
    DefaultUnit =
      u != null && isSet(attrMask, INTERVAL) ? u.getAbsoluteUnit() : u;
    DefaultSet = set;
    DefaultSetEverAccessed = false;
    if (DefaultUnit != null && DefaultSet != null) {
      Unit[] us = {DefaultUnit};
      if (!Unit.canConvertArray(us, DefaultSet.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
    this.attrMask = attrMask;
  }

  /** trusted constructor for initializers */
  protected RealType(String name, Unit u, boolean b) {
    this(name, u, 0, b);
  }

  /** trusted constructor for initializers */
  protected RealType(String name, Unit u, int attrMask, boolean b) {
    super(name, b);
    DefaultUnit =
      u != null && isSet(attrMask, INTERVAL) ? u.getAbsoluteUnit() : u;
    DefaultSet = null;
    DefaultSetEverAccessed = false;
    this.attrMask = attrMask;
  }

  /** trusted constructor for initializers */
  protected RealType(String name, Unit u, Set s, int attrMask, boolean b)
    throws SetException
  {
    super(name, b);
    if (s != null && s.getDimension() != 1) {
      throw new SetException("RealType: default set dimension != 1");
    }
    DefaultUnit =
      u != null && isSet(attrMask, INTERVAL) ? u.getAbsoluteUnit() : u;
    DefaultSet = s;
    DefaultSetEverAccessed = false;
    this.attrMask = attrMask;
  }

  /**
   * Gets the attribute mask of this RealType.
   * @return                    The attribute mask of this RealType.
   */
  public final int getAttributeMask() {
    return attrMask;
  }

  /**
   * Indicates whether or not this RealType refers to an interval (e.g.
   * length difference, delta temperature).
   * @return                    Whether or not this RealType refers to an
   *                            interval.
   */
  public final boolean isInterval() {
    return isSet(getAttributeMask(), INTERVAL);
  }

  /**
   * Indicates if the given bits are set in an integer.
   */
  private static boolean isSet(int value, int mask) {
    return (value & mask) == mask;
  }

  /** get default Unit */
  public Unit getDefaultUnit() {
    return DefaultUnit;
  }

  /** get default Set*/
  public synchronized Set getDefaultSet() {
    DefaultSetEverAccessed = true;
    return DefaultSet;
  }

  /** set the default Set;
      this is a violation of MathType immutability to allow a
      a RealType to be an argument (directly or through a
      SetType) to the constructor of its default Set;
      this method throws an Exception if getDefaultSet has
      previously been invoked */
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

  /** 
   * Check the equality of type with this RealType;
   * two RealType-s are equal if they have the same name, 
   * convertible DefaultUnit-s, same DefaultSet and attrMask;
   * a RealType copied from a remote Java virtual machine may have
   * the same name but different values for other fields
   * @param  type  object in question
   * @return true if type is a RealType and the conditions above are met
   */
  public boolean equals(Object type) {
    if (!(type instanceof RealType)) return false;

    // WLH 26 Aug 2001
    // return Name.equals(((RealType) type).Name);
    if (!Name.equals(((RealType) type).Name)) return false;
    if (DefaultUnit == null) {
      if (((RealType) type).DefaultUnit != null) return false;
    }
    else {
      // DRM 30 Nov 2001  - make less strict
      //if (!DefaultUnit.equals(((RealType) type).DefaultUnit)) return false;
      if (!Unit.canConvert(DefaultUnit, ((RealType) type).DefaultUnit)) return false;
    }
    if (DefaultSet == null) {
      if (((RealType) type).DefaultSet != null) return false;
    }
    else {
      if (!DefaultSet.equals(((RealType) type).DefaultSet)) return false;
    }
    return attrMask == ((RealType) type).attrMask;
  }

  /** any two RealType-s are equal except Name */
  public boolean equalsExceptName(MathType type) {
    if (type instanceof RealTupleType) {
      try {
        return (((RealTupleType) type).getDimension() == 1 &&
                ((RealTupleType) type).getComponent(0) instanceof RealType);
      }
      catch (VisADException e) {
        return false;
      }
    }
    return (type instanceof RealType);
  }

  /*- TDR May 1998  */
  /**
   * Check to see if type has convertible units with this RealType.
   * @param  type  MathType to check
   * @return true if type is a RealType or a RealTupleType of dimension
   *              1 AND the units are convertible with this RealType's
   *              default Unit.
   */
  public boolean equalsExceptNameButUnits(MathType type) {
    try {
      if (type instanceof RealTupleType &&
          ((RealTupleType) type).getDimension() == 1 &&
          ((RealTupleType) type).getComponent(0) instanceof RealType) {
          RealType rt = (RealType) ((RealTupleType) type).getComponent(0);
        return Unit.canConvert( this.getDefaultUnit(),
                                ((RealType)rt).getDefaultUnit() );
      }
    }
    catch (VisADException e) {
      return false;
    }
    if (!(type instanceof RealType)) {
      return false;
    }
    else {
      return Unit.canConvert( this.getDefaultUnit(),
                              ((RealType)type).getDefaultUnit() );
    }
  }

  /*- TDR June 1998  */
  public MathType cloneDerivative( RealType d_partial )
         throws VisADException
  {
    String newName = "d_"+this.getName()+"_"+
                      "d_"+d_partial.getName();

    RealType newType = null;
    Unit R_unit = this.DefaultUnit;
    Unit D_unit = d_partial.getDefaultUnit();
    Unit u = null;
    if ( R_unit != null && D_unit != null )
    {
      u = R_unit.divide( D_unit );
    }
    newType = getRealType(newName, u);

    return newType;
  }

  /*- TDR July 1998  */
  public MathType binary( MathType type, int op, Vector names )
         throws VisADException
  {
/* WLH 10 Sept 98 */
    if (type == null) {
      throw new TypeException("TupleType.binary: type may not be null" );
    }

    Unit newUnit = null;
    MathType newType = null;
    String newName;
    if (type instanceof RealType) {
      RealType that = (RealType)type;
      Unit unit = ((RealType)type).getDefaultUnit();
      Unit thisUnit = DefaultUnit;
      int newAttrMask = 0;

      /*
       * Determine the attributes of the RealType that will result from the
       * operation.
       */
      switch (op)
      {
        case Data.SUBTRACT:
        case Data.INV_SUBTRACT:
          if (isInterval() != that.isInterval())
              newAttrMask |= INTERVAL;
          break;
        case Data.ADD:
        case Data.MAX:
        case Data.MIN:
          if (isInterval() && that.isInterval())
            newAttrMask |= INTERVAL;
          break;
        case Data.MULTIPLY:
        case Data.DIVIDE:
        case Data.REMAINDER:
        case Data.INV_DIVIDE:
        case Data.INV_REMAINDER:
          if (isInterval() != that.isInterval())
            newAttrMask |= INTERVAL;
          break;
        case Data.POW:
          newAttrMask = getAttributeMask();
          break;
        case Data.INV_POW:
          newAttrMask = that.getAttributeMask();
          break;
        case Data.ATAN2:
        case Data.INV_ATAN2:
        case Data.ATAN2_DEGREES:
        case Data.INV_ATAN2_DEGREES:
        default:
      }

      /*
       * Determine the RealType that will result from the operation.  Use the
       * previously-determined attributes.
       */
      switch (op)
      {
        case Data.ADD:
        case Data.SUBTRACT:
        case Data.INV_SUBTRACT:
        case Data.MAX:
        case Data.MIN:
          if (CommonUnit.promiscuous.equals(unit)) {
            newType = this;
          }
          else if (CommonUnit.promiscuous.equals(thisUnit)) {
            newType = type;
          }
          else {
            if (thisUnit == null || unit == null) {
              newUnit = null;
              if (thisUnit == null && unit == null) {
                newName = Name;
              }
              else {
                newName = getUniqueGenericName(names, newUnit);
              }
            }
            else {
              if (!thisUnit.isConvertible(unit)) {
                throw new UnitException();
              }
              newUnit = thisUnit;
              newName = Name;
            }
            newType = getRealType(newName, newUnit, newAttrMask);
            if (newType == null) {
              /*
               * The new RealType can't be created -- possibly because the
               * attribute mask differs from an extant RealType.  Create a new
               * RealType from a new name.
               */
              newType = getRealType(newName(newUnit), newUnit, newAttrMask);
            }
          }
          break;

        case Data.MULTIPLY:
          if (CommonUnit.promiscuous.equals(unit) ||
              CommonUnit.dimensionless.isConvertible(unit)) {
            newType = this;
          }
          else if (CommonUnit.promiscuous.equals(thisUnit) ||
              CommonUnit.dimensionless.isConvertible(thisUnit)) {
            newType = type;
          }
          else {
            newUnit = (unit != null && thisUnit != null)
                ? thisUnit.multiply(unit)
                : null;
            newName = getUniqueGenericName(names, newUnit);
            newType = getRealType(newName, newUnit, newAttrMask);
          }
          break;

        case Data.DIVIDE:
          if (CommonUnit.promiscuous.equals(unit) ||
              CommonUnit.dimensionless.isConvertible(unit)) {
            newType = this;
          }
          else {
            newUnit = (unit != null && thisUnit != null)
                ? thisUnit.divide(unit)
                : null;
            newName = getUniqueGenericName(names, newUnit);
            newType = getRealType(newName, newUnit, newAttrMask);
          }
          break;

        case Data.INV_DIVIDE:
          if (CommonUnit.promiscuous.equals(thisUnit) ||
              CommonUnit.dimensionless.isConvertible(thisUnit)) {
            newType = type;
          }
          else {
            newUnit = (unit != null && thisUnit != null)
                ? unit.divide(thisUnit)
                : null;
            newName = getUniqueGenericName(names, newUnit);
            newType = getRealType(newName, newUnit, newAttrMask);
          }
          break;

        case Data.POW:
          if (thisUnit != null && CommonUnit.dimensionless.equals(thisUnit)) {
              newUnit = CommonUnit.dimensionless;
          }
          else {
                  newUnit = CommonUnit.promiscuous;
          }
          newName = getUniqueGenericName(names, newUnit);
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.INV_POW:
          if (unit != null && unit.equals(CommonUnit.dimensionless)) {
              newUnit = CommonUnit.dimensionless;
          }
          else {
                  newUnit = CommonUnit.promiscuous;
          }
          newName = getUniqueGenericName(names, newUnit);
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.ATAN2:
        case Data.INV_ATAN2:
          newUnit = CommonUnit.radian;
          newName = getUniqueGenericName(names, newUnit);
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.ATAN2_DEGREES:
        case Data.INV_ATAN2_DEGREES:
          newUnit = CommonUnit.degree;
          newName = getUniqueGenericName(names, newUnit);
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.REMAINDER:
          newType = this;
          break;

        case Data.INV_REMAINDER:
          newType = type;
          break;

        default:
          throw new ArithmeticException("RealType.binary: illegal operation");
      }
    }
    else if ( type instanceof TextType ) {
      throw new TypeException("RealType.binary: types don't match");
    }
    else if ( type instanceof TupleType ) {
      return type.binary( this, DataImpl.invertOp(op), names );
    }
    else if ( type instanceof FunctionType ) {
      return type.binary( this, DataImpl.invertOp(op), names );
    }
/* WLH 10 Sept 98 - not necessary
    else if (type instanceof RealTupleType) {
      int n_comps = ((TupleType) type).getDimension();
      RealType[] new_types = new RealType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        new_types[ii] = (RealType)
          (((TupleType) type).getComponent(ii)).binary(this,
                                       DataImpl.invertOp(op), names);
      }
      return new RealTupleType( new_types );
    }
    else if (type instanceof TupleType) {
      int n_comps = ((TupleType) type).getDimension();
      MathType[] new_types = new MathType[ n_comps ];
      for ( int ii = 0; ii < n_comps; ii++ ) {
        new_types[ii] =
          (((TupleType) type).getComponent(ii)).binary(this,
                                       DataImpl.invertOp(op), names);
      }
      return new TupleType( new_types );
    }
    else if (type instanceof FunctionType) {
      return new FunctionType(((FunctionType) type).getDomain(),
        ((FunctionType) type).getRange().binary(this,
        DataImpl.invertOp(op), names));
    }
    else {
      throw new TypeException("RealType.binary: types don't match" );
    }
*/

    return newType;
  }

  private static String newName(Unit unit) {
    return "RealType_" + Integer.toString(++count) + "_" + 
      (unit == null ? "nullUnit" : unit.toString());
  }

  /*- TDR July 1998 */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    MathType newType;
    Unit newUnit;
    String newName;

    /*
     * Determine the attributes of the RealType that will result from the
     * operation.
     */
    int newAttrMask;
    switch (op)
    {
      case Data.CEIL:
      case Data.FLOOR:
      case Data.RINT:
      case Data.ROUND:
      case Data.NOP:
      case Data.ABS:
      case Data.NEGATE:
        newAttrMask = getAttributeMask();
        break;
      case Data.ACOS:
      case Data.ASIN:
      case Data.ATAN:
      case Data.ACOS_DEGREES:
      case Data.ASIN_DEGREES:
      case Data.ATAN_DEGREES:
      case Data.COS:
      case Data.COS_DEGREES:
      case Data.SIN:
      case Data.SIN_DEGREES:
      case Data.TAN:
      case Data.TAN_DEGREES:
      case Data.SQRT:
      case Data.EXP:
      case Data.LOG:
      default:
        newAttrMask = 0;                // clear all attributes
    }

    /*
     * Determine the RealType that will result from the operation.  Use the
     * previously-determined attributes.
     */
    switch (op)
    {
      case Data.ABS:
      case Data.CEIL:
      case Data.FLOOR:
      case Data.NEGATE:
      case Data.NOP:
      case Data.RINT:
      case Data.ROUND:
        newType = this;
        break;

      case Data.ACOS:
      case Data.ASIN:
      case Data.ATAN:
        newUnit = CommonUnit.radian;
        newName = getUniqueGenericName( names, newUnit );
        newType = getRealType(newName, newUnit, newAttrMask);
        break;

      case Data.ACOS_DEGREES:
      case Data.ASIN_DEGREES:
      case Data.ATAN_DEGREES:
        newUnit = CommonUnit.degree;
        newName = getUniqueGenericName( names, newUnit );
        newType = getRealType(newName, newUnit, newAttrMask);
        break;

      case Data.COS:
      case Data.COS_DEGREES:
      case Data.SIN:
      case Data.SIN_DEGREES:
      case Data.TAN:
      case Data.TAN_DEGREES:
      case Data.EXP:
      case Data.LOG:
        newUnit = Unit.canConvert(CommonUnit.dimensionless, DefaultUnit)
          ? CommonUnit.dimensionless : null;
        newName = getUniqueGenericName( names, newUnit );
        newType = getRealType(newName, newUnit, newAttrMask);
        break;
      case Data.SQRT:
        newUnit = Unit.canConvert(CommonUnit.dimensionless, DefaultUnit)
          ? CommonUnit.dimensionless : 
              (DefaultUnit == null) 
                  ? null : DefaultUnit.sqrt();
        newName = getUniqueGenericName( names, newUnit );
        newType = getRealType(newName, newUnit, newAttrMask);
        break;

      default:
        throw new ArithmeticException("RealType.unary: illegal operation");
    }

    return newType;
  }

  private static String getUniqueGenericName( Vector names, String ext )
  {
    String name = null;

    /*
     * Ensure that the name is acceptable as a RealType name.
     */
    if (ext.indexOf(".") > -1)
      ext = ext.replace('.', '_');
    if (ext.indexOf(" ") > -1)
      ext = ext.replace(' ', '_');
    if (ext.indexOf("(") > -1)
      ext = ext.replace('(', '_');
    if (ext.indexOf(")") > -1)
      ext = ext.replace(')', '_');

    for ( int ii = 1; ; ii++ )
    {
      name = "Generic_"+ii +"_"+ext;
      if ( !names.contains(name) ) {
        names.addElement(name);
        break;
      }
    }
    return name;
  }

  private static String getUniqueGenericName(Vector names, Unit unit) {
    return getUniqueGenericName(
      names, unit == null ? "nullUnit" : unit.toString());
  }

  public Data missingData() throws VisADException {
    return new Real(this);
  }

  /**
   * Returns a RealType corresponding to a name.  If a RealType with the given
   * name doesn't exist, then it's created (with default unit, representational
   * set, and attribute mask) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * unit, representational set, and attribute mask are ignored in the
   * comparison); otherwise <code>null</code> is returned.
   *
   * @param name                    The name for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static final RealType getRealType(String name)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt == null)
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name and unit.  If a RealType
   * with the given name doesn't exist, then it's created (with default
   * representational set and attribute mask) and returned; otherwise, the
   * previously existing RealType is returned if it is compatible with the input
   * arguments (the representational set and attribute mask are ignored in the
   * comparison); otherwise <code>null</code> is returned.  Note that the unit
   * of the returned RealType will be convertible with the unit argument but
   * might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static final RealType getRealType(String name, Unit u)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt != null)
    {
      /*
       * Ensure that the previously-created instance conforms to the input
       * arguments.
       */
      if (!Unit.canConvert(u, rt.DefaultUnit)) {
// System.out.println("getRealType " + name + " unit mismatchA " + u + " " + rt.DefaultUnit);
        rt = null;
      }
    }
    else
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name, u);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
        if (rt != null) {
          if (!Unit.canConvert(u, rt.DefaultUnit)) {
// System.out.println("getRealType " + name + " unit mismatchB " + u + " " + rt.DefaultUnit);
            rt = null;
          }
        }
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name and attribute mask.  If a
   * RealType with the given name doesn't exist, then it's created (with
   * default unit and representational set) and returned; otherwise, the
   * previously existing RealType is returned if it is compatible with the
   * input arguments (the unit and representational set are ignored in the
   * comparison); otherwise <code>null</code> is returned.  Note that the unit
   * of the returned RealType will be convertible with the unit argument but
   * might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param attrMask                The attribute mask for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static RealType getRealType(String name, int attrMask)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt != null)
    {
      /*
       * Ensure that the previously-created instance conforms to the input
       * arguments.
       */
      if (attrMask != rt.attrMask)
        rt = null;
    }
    else
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name, attrMask);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
        if (rt != null) {
          if (attrMask != rt.attrMask) {
            rt = null;
          }
        }
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, and representational set.
   * If a RealType with the given name doesn't exist, then it's created (with a
   * default attribute mask) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * attribute mask is ignored in the comparison); otherwise <code>null</code>
   * is returned.  Note that the unit of the returned RealType will be
   * convertible with the unit argument but might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @param set                     The representational set for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static RealType getRealType(String name, Unit u, Set set)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt != null)
    {
      /*
       * Ensure that the previously-created instance conforms to the input
       * arguments.
       */
      if (!Unit.canConvert(u, rt.DefaultUnit) ||
          (set == null ? rt.DefaultSet != null : !set.equals(rt.DefaultSet)))
      {
        rt = null;
      }
    }
    else
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name, u, set);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
        if (rt != null) {
          if (!Unit.canConvert(u, rt.DefaultUnit) ||
              (set == null ? rt.DefaultSet != null :
                             !set.equals(rt.DefaultSet))) {
            rt = null;
          }
        }
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, and attribute mask.  If
   * a RealType with the given name doesn't exist, then it's created (with a
   * default representational set) and returned; otherwise, the previously
   * existing RealType is returned if it is compatible with the input arguments
   * (the representational set is ignored in the comparison); otherwise
   * <code>null</code> is returned.  Note that the unit of the returned RealType
   * will be convertible with the unit argument but might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @param attrMask                The attribute mask for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static final RealType getRealType(String name, Unit u, int attrMask)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt != null)
    {
      /*
       * Ensure that the previously-created instance conforms to the input
       * arguments.
       */
      if (!Unit.canConvert(u, rt.DefaultUnit) || rt.attrMask != attrMask)
        rt = null;
    }
    else
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name, u, null, attrMask);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
        if (rt != null) {
          if (!Unit.canConvert(u, rt.DefaultUnit) ||
              rt.attrMask != attrMask) {
            rt = null;
          }
        }
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, representational set,
   * and attribute mask.  If a RealType with the given name doesn't exist, then
   * it's created and returned; otherwise, the previously existing RealType
   * is returned if it is compatible with the input arguments; otherwise
   * <code>null</code> is returned.  Note that the unit of the returned RealType
   * will be convertible with the unit argument but might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @param set                     The representational set for the RealType.
   * @param attrMask                The attribute mask for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static final RealType getRealType(String name, Unit u, Set set,
                                           int attrMask)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt = getRealTypeByName(name);
    if (rt != null)
    {
      /*
       * Ensure that the previously-created instance conforms to the input
       * arguments.
       */
      if (!Unit.canConvert(u, rt.DefaultUnit) ||
          (set == null ? rt.DefaultSet != null : !set.equals(rt.DefaultSet)) ||
          rt.attrMask != attrMask)
      {
        rt = null;
      }
    }
    else
    {
      /*
       * An instance with the given name didn't exist but might have just been
       * created by another thread -- so we have to invoke the constructor
       * inside a try-block.
       */
      try
      {
        rt = new RealType(name, u, set, attrMask);
      }
      catch (VisADException e)
      {
        rt = getRealTypeByName(name);
        if (rt != null) {
          if (!Unit.canConvert(u, rt.DefaultUnit) ||
              (set == null ? rt.DefaultSet != null :
                             !set.equals(rt.DefaultSet)) ||
              rt.attrMask != attrMask) {
            rt = null;
          }
        }
      }
    }
    return rt;
  }

  /** return any RealType constructed in this JVM with name,
      or null */
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

/* WLH 5 Jan 2000
  public String toString() {
    return getName();
  }
*/

  public String prettyString(int indent) {
    // return toString();  WLH 5 Jan 2000
    return getName();
  }

  public static void main( String[] args )
         throws VisADException
  {

  //- Tests for unary   --*
    MathType m_type;
    RealType real_R = new RealType( "Red_Brightness", null, null );
    RealType real_G = new RealType( "Green_Brightness", null, null );
    RealType real_B = new RealType( "Blue_Brightness", null, null );
    RealType[] reals = { real_R, real_G, real_B };

    RealTupleType RGBtuple = new RealTupleType( reals );

    m_type = RGBtuple.unary( Data.COS, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.unary( Data.COS_DEGREES, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.unary( Data.ABS, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.unary( Data.ACOS, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.unary( Data.ACOS_DEGREES, new Vector() );
      System.out.println( m_type.toString() );

    RealType real_A = new RealType( "distance", SI.meter, null );

    m_type = real_A.unary( Data.EXP, new Vector() );
      System.out.println( m_type.toString() );


  //- Tests for binary   --*

    real_A = RealType.Generic;
 // real_A = new RealType( "temperature", SI.kelvin, null );
    m_type = RGBtuple.binary( real_A, Data.ADD, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.binary( real_A, Data.MULTIPLY, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.binary( real_A, Data.POW, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.binary( real_A, Data.ATAN2, new Vector() );
      System.out.println( m_type.toString() );

    m_type = RGBtuple.binary( real_A, Data.ATAN2_DEGREES, new Vector() );
      System.out.println( m_type.toString() );

    // and finally, force an Exception
    System.out.println("force a TypeException:");
    RealType r = new RealType("a.b");

  }

}

