//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.8 2000-06-05 22:36:35 steve Exp $
 */

package visad.data.netcdf;

import java.util.Iterator;
import visad.TypeException;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.NoSuchUnitException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


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
     * Returns the quantity that matches the given name and unit.  If
     * necessary, it creates the quantity and adds it to the database.
     *
     * @param name		The name of the quantity.
     * @param unit		The unit of the quantity.
     * @return			The quantity in the database that matches
     *				<code>name</code> and <code>unit</code>.  Note 
     *				that RETURN_VALUE<code>.getName().equals(name)
     *				</code> can be <code>false</code> due to
     *				aliasing and RETURN_VALUE<code>.
     *				getDefaultUnit().equals(unit)</code> can be 
     *				<code>false</code> due to allowable unit
     *				conversion.
     * @throws ParseException	Couldn't decode <code>unitSpec</code>.
     * @throws NoSuchUnitException
     *				<code>unitSpec</code> not in unit database.
     * @throws UnitException	The quantity already exists with an 
     *				incompatible unit.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public synchronized Quantity
    get(String name, String unitSpec)
	throws ParseException, NoSuchUnitException, UnitException,
	    VisADException
    {
	Quantity	quantity = get(name);
	if (quantity == null)
	{
	    quantity = new Quantity(name, unitSpec);
	    add(quantity);
	}
	else
	{
	    Unit	quantityUnit = quantity.getDefaultUnit();
	    if (!Unit.canConvert(Parser.parse(unitSpec), quantityUnit))
		throw new UnitException(
		    "Quantity " + name + " already exists; it's unit " +
		    quantity.getDefaultUnitString() +
		    " is inconvertible with " + unitSpec);
	}
	return quantity;
    }


    /**
     * Adds a given Quantity to the database under a given name.
     *
     * @param name		The name under which the quantity is to be
     *				added.  May be an alias.
     * @param quantity		The quantity to be added.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public abstract void
    add(String name, Quantity quantity)
	throws VisADException;


    /**
     * Adds a given Quantity to the database.
     *
     * @param quantity		The quantity to be added.  The quantity will
     *				be added under it own name.
     * @return			The database resulting from the addition.  May
     *				or may not be the original object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public void
    add(Quantity quantity)
	throws VisADException
    {
	add(quantity.getName(), quantity);
    }


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
