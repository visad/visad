//
// Quantity.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Quantity.java,v 1.2 1998-08-12 18:38:42 visad Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.FloatSet;
import visad.RealType;
import visad.SimpleSet;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * The following class represents a quantity -- usually a physical one
 * such as energy, viscosity, or velocity (although an artificial one
 * such as "money" should be possible).  It extends RealType by carrying
 * around a preferred display unit as a string because a Unit is useless
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
   * The specification of the preferred display unit of the quantity.
   */
  protected final String	unitSpec;


  /**
   * Construct from a name, a unit specification, and a sample set.
   *
   * @param name		The name of the quantity (e.g. "length").
   * @param unitSpec		The preferred display unit for the quantity
   *				(e.g. "feet").
   * @param set			The default sample set of the quantity.
   * @exception VisADException	Can't create necessary VisAD object.
   * @exception ParseException	Couldn't decode unit specification.
   */
  public Quantity(String name, String unitSpec, SimpleSet set)
    throws VisADException, ParseException
  {
    // TODO: eliminate use of trusted constructor (e.g. by merging
    // Quantity and RealType).
    super(name, Parser.parse(unitSpec), true);

    setDefaultSet(set);

    this.unitSpec = unitSpec;
  }


  /**
   * Construct from a name and a unit specification.
   *
   * @param name		The name of the quantity (e.g. "length").
   * @param unitSpec		The preferred display unit for the quantity
   *				(e.g. "feet").
   * @exception VisADException	Can't create necessary VisAD object.
   * @exception ParseException	Couldn't decode unit specification.
   */
  public Quantity(String name, String unitSpec)
    throws VisADException, ParseException
  {
    // TODO: eliminate use of trusted constructor (e.g. by merging
    // Quantity and RealType).
    super(name, Parser.parse(unitSpec), true);

    setDefaultSet(new FloatSet(this, /*CoordinateSystem=*/null,
	  new Unit[] {Parser.parse(unitSpec)}));

    this.unitSpec = unitSpec;
  }


  /**
   * Return the preferred display unit of this quantity as a string.
   *
   * @return	The preferred display unit of this quantity.
   */
  public String getDefaultUnitString() {
    return unitSpec;
  }
}
