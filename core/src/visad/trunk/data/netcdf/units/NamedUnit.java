/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NamedUnit.java,v 1.1 1997-11-17 19:35:01 steve Exp $
 */

package visad.data.netcdf.units;


import visad.Unit;


/**
 * Unit with a name.
 *
 * This class is basically a helper class for the units database.
 */
public abstract class
NamedUnit
    implements	java.io.Serializable
{
    /**
     * The name of the unit.
     * Effectively "final".
     */
    protected String	name;

    /**
     * The unit.
     * Effectively "final".
     */
    protected Unit	unit;


    /**
     * Construct an instance.
     *
     * @param name	The name of the unit.
     * @param unit	The unit.
     * @require		The arguments shall be non-null.
     */
    public
    NamedUnit(String name, Unit unit)
    {
	this.name = name;
	this.unit = unit;
    }


    /**
     * Get the name of the unit.
     *
     * @return	The name of the named unit.
     */
    public String
    getName()
    {
	return name;
    }


    /**
     * Get the unit.
     *
     * @return	The unit of the named unit.
     */
    public Unit
    getUnit()
    {
	return unit;
    }


    /**
     * Indicate whether or not the unit name has a plural form.
     *
     * @return	true if and only if the named unit has a plural form that
     *		ends in an `s'.
     */
    public abstract boolean
    hasPlural();


    /**
     * Return a string representation of this named unit.
     */
    public String
    toString()
    {
	return name + ": " + unit;
    }
}
