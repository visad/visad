/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NetcdfQuantityDB.java,v 1.12 2001-11-27 22:29:35 dglo Exp $
 */

package visad.data.netcdf.in;

import java.util.Iterator;
import visad.Unit;
import visad.VisADException;
import visad.data.netcdf.Quantity;
import visad.data.netcdf.QuantityDB;


/**
 * Provides support for mapping netCDF elements to VisAD quantities by
 * decorating a existing quantity database with methods specifically
 * appropriate to netCDF variables.
 *
 * @author Steven R. Emmerson
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
     * Adds a given Quantity to the database under a given name.
     *
     * @param name		The name under which the quantity is to be
     *				added.  May be an alias.
     * @param quantity		The quantity to be added.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public void
    add(String name, Quantity quantity)
	throws VisADException
    {
	db.add(name, quantity);
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
