//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.6 1999-01-20 18:05:38 steve Exp $
 */

package visad.data.netcdf;

import java.util.Iterator;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;


/**
 * Provides support for a database of quantities.
 *
 * @author Steven R. Emmerson
 */
public abstract class
QuantityDB
{
    /**
     * Returns the quantity in the database whose name matches a 
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantity in the loal database that matches
     *			<code>name</code>.  Note that 
     *			RETURN_VALUE<code>.getName().equals(name)</code> can
     *			be <code>false due to aliasing.
     */
    public abstract Quantity
    get(String name);


    /**
     * Returns all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is 
     *			convertible with <code>unit</code>.
     */
    public abstract Quantity[]
    get(Unit unit);


    /**
     * Adds the given quantities and aliases to the database.
     *
     * @param definitions	New quantities and their definitions.
     *				<code>definitions[2*i]</code> contains the
     *				name (e.g. "speed") of the quantity whose
     *				preferred unit specification (e.g. "m/s") is
     *				in <code>definitions[2*i+1]</code>.
     * @param aliases		Aliases for quantities.  <code>aliases[2*i]
     *				</code> contains the alias for the quantity
     *				named in <code>aliases[2*i+1]</code>.
     * @return			The database resulting from the addition.  May
     *				or may not be the original object.
     * @throws ParseException	A unit specification couldn't be parsed.
     * @throws TypeException	An incompatible version of the quantity already
     *				exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public abstract QuantityDB
    add(String[] definitions, String[] aliases)
	throws ParseException, TypeException, VisADException;


    /**
     * Returns an iterator of the quantities in the database.
     */
    public abstract Iterator
    quantityIterator();


    /**
     * Returns an iterator of the names in the database.
     */
    public abstract Iterator
    nameIterator();
}
