/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SingleUnit.java,v 1.1 1997-11-17 19:35:06 steve Exp $
 */

package visad.data.netcdf.units;


import visad.Unit;


/**
 * Unit with a name that doesn't have a plural form (e.g. "feet").
 */
public class
SingleUnit
    extends	NamedUnit
    implements	java.io.Serializable
{
    /**
     * Construct.
     *
     * @param name	The name for the single unit.
     * @param unit	The unit for the single unit.
     * @require		The arguments shall be non-null.
     */
    public
    SingleUnit(String name, Unit unit)
    {
	super(name, unit);
    }


    /**
     * Indicate whether or not the unit name has a plural form.
     *
     * @return	False (always).
     */
    public boolean
    hasPlural()
    {
	return false;
    }
}
