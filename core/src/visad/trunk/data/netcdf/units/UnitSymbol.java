/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitSymbol.java,v 1.1 1998-09-23 22:18:08 steve Exp $
 */

package visad.data.netcdf.units;


import visad.Unit;


/**
 * Unit symbol (e.g. "s").
 */
public class
UnitSymbol
    extends	SingleUnit
    implements	java.io.Serializable
{
    /**
     * Constructs from a string and a unit.
     *
     * @param symbol	The symbol for the unit.
     * @param unit	The unit.
     * @require		The arguments shall be non-null.
     */
    public
    UnitSymbol(String symbol, Unit unit)
    {
	super(symbol, unit);
    }


    /**
     * Indicates whether or not comparisons should be case-sensitive.
     *
     * @return			<code>true</code> (always).
     */
    public boolean
    isCaseSensitive()
    {
	return true;
    }
}
