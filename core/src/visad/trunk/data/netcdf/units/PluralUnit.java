/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: PluralUnit.java,v 1.2 1998-02-23 15:58:43 steve Exp $
 */

package visad.data.netcdf.units;


import visad.Unit;


/**
 * Unit with a name that has a plural form (e.g. "second").
 */
public class
PluralUnit
    extends	NamedUnit
    implements	java.io.Serializable
{
    /**
     * Construct.
     *
     * @param name	The name for the named unit.
     * @param unit	The unit for the named unit.
     * @require		The arguments shall be non-null.
     */
    public
    PluralUnit(String name, Unit unit)
    {
	super(name, unit);
    }


    /**
     * Indicate whether or not the unit name has a plural form.
     *
     * @return	true (always).
     */
    public boolean
    hasPlural()
    {
	return true;
    }
}
