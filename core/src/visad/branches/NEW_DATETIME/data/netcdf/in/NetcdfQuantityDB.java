/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfQuantityDB.java,v 1.8 1999-01-21 17:44:45 steve Exp $
 */

package visad.data.netcdf.in;

import java.util.Iterator;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.TypeException;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;
import visad.data.netcdf.units.ParseException;


/**
 * Provides support for mapping netCDF elements to VisAD quantities by
 * decorating a existing quantity database with methods specifically
 * appropriate to netCDF variables.
 */
public class
NetcdfQuantityDB
    extends	QuantityDB
{
    /**
     * The quantity database to decorate.
     */
    private QuantityDB	db;


    /**
     * Constructs from another quantity database.
     */
    public
    NetcdfQuantityDB(QuantityDB db)
    {
	this.db = db;
    }

	
    /**
     * Return the VisAD quantity corresponding to the best combination of
     * long name and name.
     *
     * @param longName	The long name of the quantity.  May be 
     *			<code>null</code>.
     * @param name	The name of the quantity.
     * @return		The corresponding, unique, VisAD quantity or 
     *			<code>null</code> if no such quantity exists.
     */
    public Quantity
    getBest(String longName, String name)
    {
	Quantity	quantity = longName == null
					? null
					: get(longName);

	return quantity != null
		? quantity
		: get(name);
    }


    /**
     * Returns an iterator of the names in the database.
     */
    public Iterator
    nameIterator()
    {
	return db.nameIterator();
    }


    /**
     * Returns an iterator of the quantities in the database.
     */
    public Iterator
    quantityIterator()
    {
	return db.quantityIterator();
    }


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
    public QuantityDB
    add(String[] definitions, String[] aliases)
	throws ParseException, TypeException, VisADException
    {
	db = db.add(definitions, aliases);
	return this;
    }


    /**
     * Returns all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is 
     *			convertible with <code>unit</code>.
     */
    public Quantity[]
    get(Unit unit)
    {
	return db.get(unit);
    }


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
    public Quantity
    get(String name)
    {
	return db.get(name);
    }
}
