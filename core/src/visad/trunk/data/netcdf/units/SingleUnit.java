/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: SingleUnit.java,v 1.2 1998-02-23 15:58:43 steve Exp $
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
