
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

  /*
   * Whether or not this RealType refers to an interval (e.g. length difference,
   * delta temperature).
   * @serial
   */
  private final boolean	isInterval;

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
  public final static RealType Radius =
    new RealType("Radius", null, true);

  /** Temporal-interval coordinate */
  public final static RealType Time =
    new RealType("Time", CommonUnit.second, true);

  /** Timestamp coordinate */
  public final static RealType DateTime =
    new RealType("DateTime", CommonUnit.secondsSinceTheEpoch, true);

  /** generic RealType */
  public final static RealType Generic =
    new RealType("GENERIC_REAL", CommonUnit.promiscuous, true);

 
  /**
   * Constructs from a name (two RealTypes are equal if their names are equal).
   * Assumes <code>null</code> for the default Unit and default Set and that the
   * RealType does <em>not</em> refer to an interval.
   * @param name		The name for the RealType.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public RealType(String name) throws VisADException {
    this(name, false);
  }
 
  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * and whether or not the RealType refers to an interval (e.g. length
   * difference, delta temperature).  Assumes <code>null</code> for the default
   * Unit and default Set.
   * @param name		The name for the RealType.
   * @param isInterval		Whether or not the RealType refers to an
   *				interval.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public RealType(String name, boolean isInterval) throws VisADException {
    this(name, null, null, isInterval);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal)
   * a default Unit, and a default Set.  Assumes that the RealType does
   * <em>not</em> refer to an interval.
   * @param name		The name for the RealType.
   * @param u                   The default unit for the RealType.  May be
   *                            <code>null</code>.  If non-<code>null</code>
   *                            and <code>isInterval</code> is
   *                            <code>true</code>, then the default unit will 
   *				actually be <code>u.getAbsoluteUnit()</code>.
   * @param set			The default sampling set for the RealType.
   *				Used when this type is a FunctionType domain.
   *				May be <code>null</code>.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public RealType(String name, Unit u, Set set) throws VisADException {
    this(name, u, set, false);
  }

  /**
   * Constructs from a name (two RealTypes are equal if their names are equal) a
   * default Unit, a default Set, and whether or not the RealType refers to an
   * interval (e.g. length difference, delta temperature).  This is the most
   * general, public constructor.
   * @param name		The name for the RealType.
   * @param u                   The default unit for the RealType.  May be
   *                            <code>null</code>.  If non-<code>null</code>
   *                            and <code>isInterval</code> is
   *                            <code>true</code>, then the default unit will
   *				actually be <code>u.getAbsoluteUnit()</code>.
   * @param set			The default sampling set for the RealType.
   *				Used when this type is a FunctionType domain.
   *				May be <code>null</code>.
   * @param isInterval		Whether or not the RealType refers to an
   *				interval.
   * @throws VisADException	Couldn't create necessary VisAD object.
   */
  public RealType(String name, Unit u, Set set, boolean isInterval) throws VisADException {
    super(name);
    if (set != null && set.getDimension() != 1) {
      throw new SetException("RealType: default set dimension != 1");
    }
    DefaultUnit = u != null && isInterval ? u.getAbsoluteUnit() : u;
    DefaultSet = set;
    DefaultSetEverAccessed = false;
    if (DefaultUnit != null && DefaultSet != null) {
      Unit[] us = {DefaultUnit};
      if (!Unit.canConvertArray(us, DefaultSet.getSetUnits())) {
        throw new UnitException("RealType: default Unit must be convertable " +
                                "with Set default Unit");
      }
    }
    this.isInterval = isInterval;
  }

  /** trusted constructor for initializers */
  protected RealType(String name, Unit u, boolean b) {
    this(name, u, false, b);
  }

  /** trusted constructor for initializers */
  protected RealType(String name, Unit u, boolean isInterval, boolean b) {
    super(name, b);
    DefaultUnit = u != null && isInterval ? u.getAbsoluteUnit() : u;
    DefaultSet = null;
    DefaultSetEverAccessed = false;
    this.isInterval = isInterval;
  }

  /**
   * Indicates whether or not this RealType refers to an interval (e.g.
   * length difference, delta temperature).
   * @return			Whether or not this RealType refers to an 
   *				interval.
   */
  public final boolean isInterval() {
    return isInterval;
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

  /*- TDR May 1998  */
  public boolean equalsExceptNameButUnits(MathType type) {
    if (!(type instanceof RealType)) {
      return false;
    }
    else if (!Unit.canConvert( this.getDefaultUnit(), ((RealType)type).getDefaultUnit()) ) {
      return false;
    }
    else {
      return true;
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

    try {
      newType = new RealType( newName, u, null );
    }
    catch ( TypeException e ) {
      newType = RealType.getRealTypeByName( newName );
    }

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
    String newName = null;
    if (type instanceof RealType) {
      Unit unit = ((RealType)type).getDefaultUnit();
      Unit thisUnit = DefaultUnit;

      switch (op)
      {
        case Data.ADD:
        case Data.SUBTRACT:
        case Data.INV_SUBTRACT:
        case Data.MAX:
        case Data.MIN:
          if ( unit == null || thisUnit == null ) {
            if ( thisUnit == null ) {
              newType = this;
              break;
            }
            if ( unit == null ) { 
              newName = getUniqueGenericName( names, "nullUnit" );
              newUnit = null;
              try {
                newType = new RealType( newName, newUnit, null );
              }
              catch ( TypeException e ) {
                newType = RealType.getRealTypeByName( newName );
              }
              break;
            }
          }
          else if ( thisUnit.equals( CommonUnit.promiscuous ) ) {
            newType = type;
          }
          else if ( unit.equals( CommonUnit.promiscuous ) ) {
            newType = this;
          }
          else {
            if ( thisUnit.equals( unit ) ) {
              newType = this;
            }
            else if ( Unit.canConvert( thisUnit, unit ) ) {
              newType = this;
            }
            else {
              throw new UnitException();
            }
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
              newName = getUniqueGenericName( names, "nullUnit" );
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
            newName = getUniqueGenericName( names, newUnit.toString());
          }

          try {
            newType = new RealType( newName, newUnit, null );
          }
          catch ( TypeException e ) {
            newType = RealType.getRealTypeByName( newName );
          }
          break;

        case Data.POW:
        case Data.INV_POW:
          if ( thisUnit == null ) {
            newType = this;
            break;
          }
          newUnit = null;
          newName = getUniqueGenericName( names, "nullUnit" );
          try {
            newType = new RealType( newName, newUnit, null );
          }
          catch ( TypeException e ) {
            newType = RealType.getRealTypeByName( newName );
          }
          break;

        case Data.ATAN2:
          newUnit = CommonUnit.radian;
        case Data.INV_ATAN2:
          newUnit = CommonUnit.radian;
          newName = getUniqueGenericName( names, newUnit.toString() );
          try {
            newType = new RealType( newName, newUnit, null );
          }
          catch ( TypeException e ) {
            newType = RealType.getRealTypeByName( newName );
          }
          break;

        case Data.ATAN2_DEGREES:
          newUnit = CommonUnit.degree;
        case Data.INV_ATAN2_DEGREES: 
          newUnit = CommonUnit.degree;
          newName = getUniqueGenericName( names, "deg" );
          try {
            newType = new RealType( newName, newUnit, null );
          }
          catch ( TypeException e ) {
            newType = RealType.getRealTypeByName( newName );
          }
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
        ((FunctionType) type).getRange().binary(this, DataImpl.invertOp(op), names));
    }
    else {
      throw new TypeException("RealType.binary: types don't match" );
    }
*/

    return newType;
  }

  /*- TDR July 1998 */
  public MathType unary( int op, Vector names )
         throws VisADException
  {
    MathType newType;
    Unit newUnit;
    String newName;
    switch (op)
    {
      case Data.ABS:
      case Data.CEIL:
      case Data.FLOOR:
      case Data.RINT:
      case Data.ROUND:
      case Data.NEGATE:
      case Data.NOP:
        newType = this;
        break;

      case Data.ACOS:
      case Data.ASIN:
      case Data.ATAN:
        newUnit = CommonUnit.radian;
        newName = getUniqueGenericName( names, newUnit.toString() );
        try {
          newType = new RealType( newName, newUnit, null );
        }
        catch ( TypeException e ) {
          newType = RealType.getRealTypeByName( newName );
        }
        break;

      case Data.ACOS_DEGREES:
      case Data.ASIN_DEGREES:
      case Data.ATAN_DEGREES:
        newUnit = CommonUnit.degree;
        newName = getUniqueGenericName( names, "deg" );
        try {
          newType = new RealType( newName, newUnit, null );
        }
        catch ( TypeException e ) {
          newType = RealType.getRealTypeByName( newName );
        }
        break;

      case Data.COS:
      case Data.COS_DEGREES:
      case Data.SIN:
      case Data.SIN_DEGREES:
      case Data.TAN:
      case Data.TAN_DEGREES:
      case Data.SQRT:
      case Data.EXP:
      case Data.LOG:
        if ( DefaultUnit == null ) {
          newType = this;
          break;
        }
        else {
          newUnit = CommonUnit.dimensionless.equals( DefaultUnit ) ? DefaultUnit : null;
          String ext = (newUnit == null) ? "nullUnit" : newUnit.toString();
          newName = getUniqueGenericName( names, ext );
          try {
            newType = new RealType( newName, newUnit, null );
          }
          catch ( TypeException e ) {
            newType = RealType.getRealTypeByName( newName );
          }
        }
        break;

      default:
        throw new ArithmeticException("RealType.binary: illegal operation");
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

  public String toString() {
    return getName();
  }

  public String prettyString(int indent) {
    return toString();
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

