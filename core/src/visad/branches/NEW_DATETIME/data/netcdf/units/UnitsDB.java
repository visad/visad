/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: UnitsDB.java,v 1.4 1998-02-23 15:59:05 steve Exp $
 */

package visad.data.netcdf.units;


import visad.Unit;
import visad.UnitException;


/**
 * The units database abstract class.
 *
 * This class exists to allow the user to construct their own units database.
 */
public abstract class
UnitsDB
    implements	java.io.Serializable
{
    /**
     * Singleton instance of this class:
     * Effectively "final".
     */
    protected static UnitsDB	db;


    /**
     * Put a named unit.
     *
     * @param unit	The named unit to be added to the database.
     * @return		The previous entry with the same name or null.
     * @require		<code>unit</code> shall be non-null.
     */
    public abstract NamedUnit
    put(NamedUnit unit);


    /**
     * Put a unit.
     *
     * @param name	The name of the unit to be added to the database.
     * @param unit	The unit to be added to the database.
     * @param hasPlural	Whether or not the name of the unit has a plural form
     *			that ends with an `s'.
     * @require		The arguments shall be non-null.  The name shall be 
     *			non-empty.
     */
    public abstract Unit
    put(String name, Unit unit, boolean hasPlural);


    /**
     * Get a unit.
     *
     * @param name	The name of the unit to be retrieved from the database.
     * @return		The matching unit entry in the database.
     * @require		<code>name</code> shall be non-null.
     */
    public abstract Unit
    get(String name);


    /**
     * Inner class for enumerating the units in the database.
     */
    public interface
    Enumeration
    {
	public abstract boolean
	hasMoreElements();

	public abstract NamedUnit
	nextElement();
    }


    /**
     * Get an enumeration of the units in the database.
     */
    public abstract Enumeration
    getEnumeration();


    /**
     * List the units in the database.
     */
    public void
    list()
    {
	Enumeration	enum = getEnumeration();
	
	while (enum.hasMoreElements())
	    System.out.println(enum.nextElement().toString());
    }
}
