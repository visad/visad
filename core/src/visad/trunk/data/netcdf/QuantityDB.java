//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.4 1998-11-16 18:23:40 steve Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import visad.Unit;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * Provides support for a database of quantities.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public abstract class
QuantityDB
    implements	Serializable
{
    /**
     * Return all quantities in the database whose name matches a 
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantities in the loal database with name
     *			<code>name</code> and whose unit is convertible with
     *			<code>unit</code>.
     */
    public abstract Quantity[] get(String name);


    /**
     * Return all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is 
     *			convertible with <code>unit</code>.
     */
    public abstract Quantity[] get(Unit unit);


    /**
     * Return the quantity in the database whose name matches a given name and
     * whose default unit is convertible with a given unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The unit of the quantity.
     * @return		The quantity in the database with name <code>name</code>
     *			and whose unit is convertible with <code>unit</code>,
     *			or <code>null</code> if no such quantity exists.
     */
    public abstract Quantity get(String name, Unit unit);


    /**
     * Return the quantity in the database whose name matches a given name and
     * whose default unit is convertible with a given unit specification.
     *
     * @param name	The name of the quantity.
     * @param unitSpec	The unit specification of the quantity.
     * @return		The quantity in the database with name <code>name</code>
     *			and whose unit is convertible with <code>unit</code>,
     *			or <code>null</code> if no such quantity exists.
     * @exception ParseException	Couldn't decode unit specification.
     */
    public Quantity get(String name, String unitSpec)
      throws ParseException
    {
	return get(name, Parser.parse(unitSpec));
    }


    /**
     * Returns the quantity corresponding to the best combination of
     * name and unit.
     *
     * @param name      The name of the quantity.
     * @param unit      The unit of the quantity.  May be <code>null</code>.
     * @return          The corresponding VisAD quantity or
     *                  <code>null</code> if no such quantity exists.
     */
    public synchronized Quantity
    getBest(String name, Unit unit)
    {
        return unit == null
                    ? getIfUnique(name)
                    : get(name, unit);
    }


    /**
     * Returns the unique quantity in the quantity database whose
     * name matches a given name.
     *
     * @param name	The name of the quantity.
     * @return		The unique quantity in the quantity database
     *			with name <code>name</code> or <code>null</code> if
     *			no such quantity exists or is not unique.
     */
    public synchronized Quantity
    getIfUnique(String name)
    {
	Quantity[]	quantities = get(name);

	return quantities.length == 1
		    ? quantities[0]
		    : null;
    }


    /**
     * Returns the first quantity corresponding to the given name.
     *
     * @param name      The name of the quantity.
     * @return          The first corresponding VisAD quantity or
     *                  <code>null</code> if no such quantity exists.
     */
    public synchronized Quantity
    getFirst(String name)
    {
	Quantity[]	quantities = get(name);

        return quantities.length == 0
                    ? null
                    : quantities[0];
    }
}
