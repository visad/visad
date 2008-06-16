//
// Quantity.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Quantity.java,v 1.13 2002-02-18 17:21:01 dglo Exp $
 */

package visad.data.netcdf;

import visad.RealType;
import visad.SimpleSet;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;
import visad.data.units.ParseException;
import visad.data.units.Parser;


/**
 * The following class represents a quantity -- usually a physical one
 * such as energy, viscosity, or velocity (although an artificial one
 * such as "money" should be possible).  It extends RealType by carrying
 * around a preferred unit as a string because a Unit is useless
 * for doing that.
 *
 * A Quantity is immutable.
 *
 * @author Steven R. Emmerson
 */
public class Quantity
  extends	RealType
{
  /**
   * The specification of the preferred unit of the quantity.
   */
  protected final String	unitSpec;


  /**
   * Constructs from a name, a unit specification, and a sample set.
   *
   * @param name		The name of the quantity (e.g. "length").
   * @param unitSpec		The preferred unit for the quantity
   *				(e.g. "feet").
   * @param set			The default sample set for the quantity.  May be
   *                            <code>null</code>.
   * @throws ParseException	Couldn't decode unit specification.
   * @throws VisADException	ScalarType of same name already exists.
   */
  public Quantity(String name, String unitSpec, SimpleSet set)
    throws VisADException, ParseException
  {
    super(name, Parser.parse(unitSpec), set, 0, false);

    this.unitSpec = unitSpec;
  }


  /**
   * Constructs from a name and a unit specification.
   *
   * @param name		The name of the quantity (e.g. "length").
   * @param unitSpec		The preferred unit for the quantity
   *				(e.g. "feet").
   * @exception VisADException	Can't create necessary VisAD object.
   * @exception ParseException	Couldn't decode unit specification.
   */
  public Quantity(String name, String unitSpec)
    throws VisADException, ParseException
  {
    this(name, unitSpec, (SimpleSet)null);
  }


  /**
   * Constructs from a VisAD RealType.
   *
   * @param realType		A VisAD realType.
   * @throws VisADException	Can't create necessary VisAD object.
   */
  Quantity(RealType realType)
    throws VisADException
  {
    /*
     * The following will create a duplicate RealType.
     */
    // TODO: eliminate use of trusted constructor (e.g. by merging
    // Quantity and RealType).
    super(realType.getName(), realType.getDefaultUnit(), true);

    Unit	unit = realType.getDefaultUnit();
    this.unitSpec = unit == null
	? null
	: unit.toString();
  }


  /**
   * Return the default unit of this quantity as a string.
   *
   * @return		The default unit of this quantity or
   *			<code>null</code> if no such unit.
   */
  public String getDefaultUnitString()
  {
    return unitSpec;
  }

  /** create a new Quantity, or return it if it already exists */
  public static Quantity getQuantity(String name, String unitSpec,
                                     SimpleSet set)
    throws ParseException
  {
    try {
      return new Quantity(name, unitSpec, set);
    }
    catch (TypeException e) {
      return getQuantityByName(name);
    }
    catch (VisADException e) {
      return null;
    }
  }

  /** create a new Quantity, or return it if it already exists */
  public static Quantity getQuantity(String name, String unitSpec)
    throws ParseException
  {
    return getQuantity(name, unitSpec, null);
  }

  /** return any Quantity constructed in this JVM with name,
      or null */
  public static Quantity getQuantityByName(String name) {
    RealType quant = RealType.getRealTypeByName(name);
    if (!(quant instanceof Quantity)) {
      try {
        return new Quantity(quant);
      } catch (VisADException ve) {
        return null;
      }
    }
    return (Quantity) quant;
  }
}
