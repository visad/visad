//
// RealType.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
 * <p>RealType is the VisAD scalar data type for real number variables.</p>
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
  private int               attrMask;

  private static final long serialVersionUID = 0; // TODO

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
   */
  public RealType(String name, Unit u) throws VisADException {
    this(name, u, null, 0);
  }

  /*
   * Constructs from a name, a default Unit, a default sampling set, and
   * an immediate supertype.  If the default unit or default sampling set
   * are <code>null</code>, then the supertype's values are used if it is
   * non-<code>null</code>; otherwise, they are set to <code>null</code>.
   * The attribute mask is set to the supertype's value if it is
   * non-<code>null</code> and 0 otherwise.
   *
   * @param name                  The name for the instance.
   * @param unit                  The default unit for the instance or
   *                              <code>null</code>.  If non-<code>null</code>
   *                              and the instance refers to an interval,
   *                              then the default unit will actually be
   *                              <code>u.getAbsoluteUnit()</code>.  Must agree
   *                              with the default unit of the supertype if that
   *                              exists.
   * @param set                   The default sampling set for the instance or
   *                              <code>null</code>.  Used when this type is a
   *                              FunctionType domain.
   * @param supertype             The supertype instance or <code>null</code>.
   * @throws TypeException        if the name is invalid for a {@link 
   *                              ScalarType}.
   * @throws UnitException        if the set is non-<code>null</code> and the
   *                              default unit isn't compatible with the
   *                              set's default unit or if the supertype is
   *                              non-<code>null</code> and the default unit
   *                              isn't compatible with the supertype's default
   *                              unit.
   * @see ScalarType(String)
   */
  private RealType(String name, Unit unit, Set set, RealType supertype) throws
    VisADException {

    this(
      name, unit, set, supertype == null ? 0 : supertype.attrMask, supertype);
  }

  /**
   * Constructs from a name, a default Unit, a default Set, and whether or
   * not the RealType refers to an interval (e.g. length difference, delta
   * temperature).  This is the most general, public constructor.
   *
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
   */
  public RealType(String name, Unit u, Set set, int attrMask)
    throws VisADException
  {
    this(name, u, set, attrMask, null);
  }

  /*
   * Constructs from a name, a default Unit, a default sampling set, an
   * attribute mask, and an immediate supertype.  If the default unit or default
   * sampling set are <code>null</code>, then the supertype's values are used if
   * it is non-<code>null</code>; otherwise, they are set to <code>null</code>.
   * The attribute mask must agree with the supertype's value if the supertype
   * is non-<code>null</code>.
   *
   * @param name                  The name for the instance.
   * @param unit                  The default unit for the instance or
   *                              <code>null</code>.  If non-<code>null</code>
   *                              and the instance refers to an interval,
   *                              then the default unit will actually be
   *                              <code>u.getAbsoluteUnit()</code>.  Must agree
   *                              with the default unit of the supertype if that
   *                              exists.
   * @param set                   The default sampling set for the instance or
   *                              <code>null</code>.  Used when this type is a
   *                              FunctionType domain.
   * @param attrMask              The attribute mask. 0 or INTERVAL.
   * @param supertype             The supertype instance or <code>null</code>.
   * @throws SetException         if the default sampling set is non-<code>
   *                              null</code> and its dimension != 1.
   * @throws TypeException        if the name is invalid for a {@link 
   *                              ScalarType} or the supertype is non-<code>
   *                              null</code> and the attribute mask doesn't
   *                              match the supertype's.
   * @throws UnitException        if the set is non-<code>null</code> and the
   *                              default unit isn't compatible with the
   *                              set's default unit or if the supertype is
   *                              non-<code>null</code> and the default unit
   *                              isn't compatible with the supertype's default
   *                              unit.
   */
  private RealType(
      String name, Unit unit, Set set, int attrMask, RealType supertype) throws
    SetException, TypeException, UnitException {

    super(name, supertype);

    if (supertype != null) {
      if (unit == null) {
	unit = supertype.DefaultUnit;
      }
      else {
	if (!Unit.canConvert(unit, supertype.DefaultUnit))
	  throw new UnitException(
	    "name=\"" + name + "\", unit=" + unit + ", supertype.DefaultUnit=" +
	    supertype.DefaultUnit);
      }

      if (set == null)
	set = supertype.DefaultSet;

      if (attrMask != supertype.attrMask)
	throw new TypeException("attrMask=" + attrMask + 
	  ", supertype.attrMask=" + supertype.attrMask);
    }

    if (set != null && set.getDimension() != 1)
      throw new SetException("RealType: default set dimension != 1");

    DefaultUnit =
      unit != null && isSet(attrMask, INTERVAL) ? unit.getAbsoluteUnit() : unit;
    DefaultSet = set;
    DefaultSetEverAccessed = false;
    this.attrMask = attrMask;

    if (DefaultUnit != null && DefaultSet != null) {
      Unit[] us = {DefaultUnit};
      if (!Unit.canConvertArray(us, DefaultSet.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
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

  /**
   * Returns the default sampling set.  Once this method is invoked,
   * the method {@link #setDefaultSet(Set)} will always throw an exception.
   *
   * @return                    The default sampling set.
   * @see #setDefaultSet(Set)
   */
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
   * Indicates if this instance is equal to an object.  Two instances are equal
   * if they have the same name, default sampling set, and attribute mask, and
   * if their default units are compatible.
   *
   * @param  obj  the other object.
   * @return true if and only if this instance equals the other object.
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;  // short circuit

    if (!(obj instanceof RealType))
      return false;

    RealType that = (RealType)obj;

    // WLH 26 Aug 2001
    // return Name.equals(that.Name);
    if (!Name.equals(that.Name))
      return false;

    if (DefaultUnit == null) {
      if (that.DefaultUnit != null)
	return false;
    }
    else {
      // DRM 30 Nov 2001  - make less strict
      //if (!DefaultUnit.equals(that.DefaultUnit)) return false;
      if (!Unit.canConvert(DefaultUnit, that.DefaultUnit))
	return false;
    }

    if (DefaultSet == null) {
      if (that.DefaultSet != null)
	return false;
    }
    else {
      if (!DefaultSet.equals(that.DefaultSet))
	return false;
    }

    return attrMask == that.attrMask;
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
          if ( thisUnit == null ) {
            newUnit = null;
	    newName = Name;
          }
          else if ( unit == null ) {
            newUnit = null;
	    newName = that.Name;
          }
          else if ( thisUnit.equals( CommonUnit.promiscuous ) ) {
            newUnit = that.DefaultUnit.getAbsoluteUnit();
	    newName = that.Name;
          }
          else if ( unit.equals( CommonUnit.promiscuous ) ||
                    Unit.canConvert( thisUnit, unit ) ) {
            newUnit = thisUnit.getAbsoluteUnit();
	    newName = Name;
          }
          else {
            throw new UnitException();
          }
          newType = getRealType(newName, newUnit, newAttrMask);
	  if (newType == null) {
	    /*
	     * The new RealType can't be create -- possibly because the
	     * attribute mask differs from an extant RealType.  Create a new
	     * RealType from a new name.
	     */
	    newType = getRealType(newName(), newUnit, newAttrMask);
	  }
          break;

        case Data.MULTIPLY:
        case Data.DIVIDE:
        case Data.INV_DIVIDE:
          if ( unit == null || thisUnit == null ) {
            if ( thisUnit == null ) {
              newType = this;
              break;
            }
            if ( unit == null ) {
              newUnit = null;
            }
          }
          else {
            switch (op) {
              case Data.MULTIPLY:
                newUnit = thisUnit.multiply( unit );
                break; // WLH 26 Jan 99
              case Data.DIVIDE:
                newUnit = thisUnit.divide( unit );
                break; // WLH 26 Jan 99
              case Data.INV_DIVIDE:
                newUnit = unit.divide( thisUnit );
                break; // WLH 26 Jan 99
            }
          }
	  newName = newName();

          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.POW:
          if ( thisUnit == null ) {
            newType = this;
          }
          else
          {
            if ( Unit.canConvert(CommonUnit.dimensionless, thisUnit) ) {
              newUnit = CommonUnit.dimensionless;
            }
            else
            {
              newUnit = null;
            }
	    newName = newName();
            newType = getRealType(newName, newUnit, newAttrMask);
          }
          break;

        case Data.INV_POW:
          if ( thisUnit == null ) {
            newType = this;
            break;
          }
          newUnit = null;
	  newName = newName();
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.ATAN2:
          newUnit = CommonUnit.radian;
        case Data.INV_ATAN2:
          newUnit = CommonUnit.radian;
	  newName = newName();
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.ATAN2_DEGREES:
          newUnit = CommonUnit.degree;
        case Data.INV_ATAN2_DEGREES:
          newUnit = CommonUnit.degree;
	  newName = newName();
          newType = getRealType(newName, newUnit, newAttrMask);
          break;

        case Data.REMAINDER:
          newType = this;
          break;
        case Data.INV_REMAINDER:
          if ( thisUnit == null ) {
            newType = this;
            break;
          }
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

  private static String newName() {
    return "RealType_" + Integer.toString(++count);
  }

  private static String newName( String oldName, int attrMask ) {
    String      intervalSuffix = "Interval";
    return
       !isSet( attrMask, INTERVAL ) || oldName.endsWith( intervalSuffix )
          ? oldName
          : oldName + intervalSuffix;
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

    newName = newName(getName(), newAttrMask);

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
        newName = getUniqueGenericName( names, newUnit.toString() );
        newType = getRealType(newName, newUnit, newAttrMask);
        break;

      case Data.ACOS_DEGREES:
      case Data.ASIN_DEGREES:
      case Data.ATAN_DEGREES:
        newUnit = CommonUnit.degree;
        newName = getUniqueGenericName( names, "deg" );
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
        if ( DefaultUnit == null ) {
          newType = this;
          break;
        }
        else {
          newUnit = Unit.canConvert(CommonUnit.dimensionless, DefaultUnit)
            ? CommonUnit.dimensionless : null;
          String ext = (newUnit == null) ? "nullUnit" : newUnit.toString();
          newName = getUniqueGenericName( names, ext );
          newType = getRealType(newName, newUnit, newAttrMask);
        }
        break;
      case Data.SQRT:
        if ( DefaultUnit == null ) {
          newType = this;
          break;
        }
        else {
          newUnit = Unit.canConvert(CommonUnit.dimensionless, DefaultUnit)
            ? CommonUnit.dimensionless : DefaultUnit.sqrt();
          String ext = (newUnit == null) ? "nullUnit" : newUnit.toString();
          newName = getUniqueGenericName( names, ext );
          newType = getRealType(newName, newUnit, newAttrMask);
        }
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

  public Data missingData() throws VisADException {
    return new Real(this);
  }

  /**
   * Returns a RealType corresponding to a name.  If a RealType with the given
   * name doesn't exist, then it's created (with default unit, sampling set,
   * and attribute mask) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * unit, sampling set, and attribute mask are ignored in the comparison);
   * otherwise <code>null</code> is returned.
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
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
      if (rt == null)
      {
	try
	{
	  rt = new RealType(name);
	}
	catch (VisADException e)
	{}
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name and unit.  If a RealType with
   * the given name doesn't exist, then it's created (with default sampling
   * set and attribute mask) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * sampling set and attribute mask are ignored in the comparison); otherwise
   * <code>null</code> is returned.  Note that the unit of the returned RealType
   * will be compatible with the unit argument but might not equal it.
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
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
      if (rt != null)
      {
	/*
	 * Ensure that the previously-created instance conforms to the input
	 * arguments.
	 */
	if (!Unit.canConvert(u, rt.DefaultUnit))
	  rt = null;
      }
      else
      {
	try
	{
	  rt = new RealType(name, u);
	}
	catch (VisADException e)
	{}
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name and attribute mask.  If a
   * RealType with the given name doesn't exist, then it's created (with
   * default unit and sampling set) and returned; otherwise, the previously
   * existing RealType is returned if it is compatible with the input arguments
   * (the unit and sampling set are ignored in the comparison); otherwise
   * <code>null</code> is returned.  Note that the unit of the returned RealType
   * will be compatible with the unit argument but might not equal it.
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
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
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
	try
	{
	  rt = new RealType(name, attrMask);
	}
	catch (VisADException e)
	{}
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, and sampling set.  If
   * a RealType with the given name doesn't exist, then it's created (with a
   * default attribute mask) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * attribute mask is ignored in the comparison); otherwise <code>null</code>
   * is returned.  Note that the unit of the returned RealType will be
   * compatible with the unit argument but might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @param set                     The sampling set for the RealType.
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
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
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
	{}
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, and attribute mask.  If
   * a RealType with the given name doesn't exist, then it's created (with a
   * default sampling set) and returned; otherwise, the previously existing
   * RealType is returned if it is compatible with the input arguments (the
   * sampling set is ignored in the comparison); otherwise <code>null</code> is
   * returned.  Note that the unit of the returned RealType will be compatible
   * with the unit argument but might not equal it.
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
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
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
	{}
      }
    }
    return rt;
  }

  /**
   * Returns a RealType corresponding to a name, unit, sampling set, and
   * attribute mask.  If a RealType with the given name doesn't exist, then
   * it's created and returned; otherwise, the previously existing RealType
   * is returned if it is compatible with the input arguments; otherwise
   * <code>null</code> is returned.  Note that the unit of the returned RealType
   * will be compatible with the unit argument but might not equal it.
   *
   * @param name                    The name for the RealType.
   * @param unit                    The unit for the RealType.
   * @param set                     The sampling set for the RealType.
   * @param attrMask                The attribute mask for the RealType.
   * @return                        A RealType corresponding to the input
   *                                arguments or <code>null</code>.
   * @throws NullPointerException   if the name is <code>null</code>.
   */
  public static final RealType getRealType(
    String name, Unit u, Set set, int attrMask)
  {
    if (name == null)
      throw new NullPointerException();
    /*
     * The following should catch most of the times that an instance with the
     * given name was previously-created -- without the performance-hit of 
     * catching an exception.
     */
    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);
      if (rt != null)
      {
	/*
	 * Ensure that the previously-created instance conforms to the input
	 * arguments.
	 */
	if (!Unit.canConvert(u, rt.DefaultUnit) ||
	    (set == null 
	      ? rt.DefaultSet != null : !set.equals(rt.DefaultSet)) ||
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
	{}
      }
    }
    return rt;
  }

  /*
   * Returns an instance corresponding to a name, a default Unit, a default
   * sampling set, and a supertype instance.  If an equivalent instance was
   * previously created, then it is returned.  An equivalent instance has the
   * same name, default sampling set, and supertype instance, and a compatible
   * default unit.  If a previously-create equivalent instance doesn't exist,
   * then a new instance is created and returned.  In this case, if the default
   * unit and default sampling set are <code>null</code> and the supertype
   * argument is non-<code>null</code>, then the supertype's values are used and
   * the attribute mask is set to the supertype's value; otherwise, the default
   * unit, default sampling set and attribute mask are set to <code>null</code>,
   * <code>null</code>, and 0, respectively.
   *
   * @param name                  The name for the instance.
   * @param unit                  The default unit for the instance or
   *                              <code>null</code>.  If non-<code>null</code>
   *                              and the instance refers to an interval,
   *                              then the default unit will actually be
   *                              <code>u.getAbsoluteUnit()</code>.  Must
   *                              agree with the default unit of the supertype
   *                              instance if available.
   * @param set                   The default sampling set for the instance or
   *                              <code>null</code>.  Used when this instance is
   *                              the domain of a {@link FunctionType}.
   * @param supertype             The supertype instance.
   * @return                      A corresponding instance.
   * @throws IllegalArgumentException
   *                              if the name is invalid for a {@link
   *                              ScalarType} or if the set is
   *                              non-<code>null</code> and the default unit
   *                              isn't compatible with the set's default unit
   *                              or if the supertype is non-<code>null</code>
   *                              and the default unit isn't compatible with
   *                              the supertype's default unit.
   * @see ScalarType(String)
   */
  public static RealType getRealType(
      String name, Unit unit, Set set, RealType supertype) {

    RealType rt;
    synchronized(ScalarType.class) {
      rt = getRealTypeByName(name);

      if (rt == null) {
	try {
	  rt = new RealType(name, unit, set, supertype);
	}
	catch (VisADException e) {
	  throw new IllegalArgumentException(e.getMessage());
	}
      }
      else if (!(Unit.canConvert(rt.DefaultUnit, unit) &&
  	        (set == null
	          ? rt.DefaultSet == null : set.equals(rt.DefaultSet)) &&
	        (supertype != null && supertype.equals(rt.getParent())))) {

	throw new IllegalArgumentException(
          "name=\"" + name + "\", rt.DefaultUnit=" + rt.DefaultUnit +
          ", set=" + set + ", rt.DefaultSet=" + rt.DefaultSet + 
          ", rt.supertype=" + rt.getParent() + ", supertype=" + supertype);
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

