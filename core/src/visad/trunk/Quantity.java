//
// Quantity.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Quantity.java,v 1.1 1998-04-27 17:13:31 steve Exp $
 */

package visad;

import java.io.Serializable;


/**
 * The following class represents a quantity -- usually a physical one
 * such as energy, viscosity, or velocity (although an artificial one
 * such as "money" should be possible).  A Quantity is immutable.
 *
 * @author Steven R. Emmerson
 */
public class Quantity
  implements	Serializable
{
  /**
   * The name of the quantity.
   */
  protected final String	name;

  /**
   * The mathematical type of the quantity.
   */
  protected final RealTupleType	type;

  /**
   * The names of the preferred display unit of the quantity.
   */
  protected final String[]	units;


  /**
   * Construct from a name and a mathematical type.
   *
   * @param name		The name of the quantity (e.g. "energy").
   * @param units		The preferred display units for the quantity.
   * @param type		The math type of the quantity.
   * @precondition		<code>units</code> and <code>type</code> are
   *				compatible.
   * @exception VisADException	Can't create using <code>type</code>.
   */
  public Quantity(String name, String[] units, MathType type)
    throws VisADException
  {
    throw new VisADException("can't create Quantity from (" + type + ")");
  }


  /**
   * Construct from a name and a real type.
   *
   * @param name		The name of the quantity (e.g. "energy").
   * @param unit		The preferred display unit for the quantity.
   * @param type		The math type of the quantity.
   * @precondition		<code>type</code> has a non-null, default Unit.
   * @precondition		<code>unit</code> and <code>type</code> are
   *				compatible.
   * @exception VisADException	Can't create using <code>type</code>.
   */
  public Quantity(String name, String unit, RealType type)
    throws VisADException
  {
    this(name, new String[] {unit}, new RealTupleType(new RealType[] {type}));
  }


  /**
   * Construct from a name and a set.
   *
   * @param name		The name of the quantity (e.g. "energy").
   * @param units		The preferred display units for the quantity.
   * @param type		The math type of the quantity.
   * @precondition		<code>type</code> has non-null, default units.
   * @precondition		<code>units</code> and <code>type</code> are
   *				compatible.
   * @exception VisADException	Can't create using <code>type</code>.
   */
  public Quantity(String name, String[] units, SetType type)
    throws VisADException
  {
    this(name, units, new RealTupleType(type.getDomain().getRealComponents()));
  }


  /**
   * Construct from a name and a tuple.
   *
   * @param name		The name of the quantity (e.g. "energy").
   * @param units		The preferred display units for the quantity.
   * @param type		The math type of the quantity.
   * @precondition		<code>type</code> has non-null, default units.
   * @precondition		<code>units</code> and <code>type</code> are
   *				compatible.
   * @exception VisADException	Can't create using <code>type</code>.
   */
  public Quantity(String name, String[] units, TupleType type)
    throws VisADException
  {
    this(name, units, new RealTupleType(type.getRealComponents()));
  }


  /**
   * Construct from a name and a tuple of real types.
   *
   * @param name		The name of the quantity (e.g. "energy").
   * @param units		The preferred display units for the quantity.
   * @param type		The components of the quantity.
   * @precondition		Each component has a non-null, default unit.
   * @precondition		<code>units</code> and <code>type</code> are
   *				compatible.
   * @exception VisADException	Can't create using <code>type</code>.
   */
  public Quantity(String name, String[] units, RealTupleType type)
    throws VisADException
  {
    int	rank = type.getDimension();

    for (int i = 0; i < rank; ++i)
    {
      if (((RealType)type.getComponent(i)).getDefaultUnit() == null)
	throw new VisADException("can't create Quantity from (" + type + ")");
    }

    this.name = name;
    this.units = units;
    this.type = type;
  }


  /**
   * Indicate whether or not this quantity is semantically identical to
   * another.
   *
   * @param obj		The other object.
   * @precondition	<code>obj</code> is a Quantity.
   * @return		<code>true</code> if and only if this quantity is
   *			semantically identical to <code>obj</code>.
   */
  public boolean equals(Object obj)
  {
    Quantity	other = (Quantity)obj;

    return name.equals(other.name) && type.equals(other.type);
  }


  /**
   * Return the name of this quantity.
   *
   * @return	The name of this quantity.
   */
  public String getName()
  {
    return name;
  }


  /**
   * Return the preferred units of this quantity.
   *
   * @return	The preferred units of this quantity.
   */
  public String[] getUnits()
  {
    return units;
  }


  /**
   * Return the mathematical type of this quantity.
   *
   * @return	The mathematical type of this quantity.
   */
  public MathType getMathType()
  {
    return type;
  }
}
