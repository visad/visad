//
// QuantityDBList.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBList.java,v 1.1 1998-11-16 18:23:40 steve Exp $
 */

package visad.data.netcdf;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.io.Serializable;
import visad.SI;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for a list of quantity databases.
 *
 * @author Steven R. Emmerson
 */
public class
QuantityDBList
    extends	QuantityDB
{
    /**
     * The collection of quantity databases.
     */
    private final LinkedList	list = new LinkedList();


    /**
     * Constructs from nothing.
     */
    public
    QuantityDBList()
    {
    }


    /**
     * Constructs from an initial quantity database.
     * @param QuantityDB	The initial quantity database or 
     *				<code>null</code>.
     */
    public
    QuantityDBList(QuantityDB db)
    {
	list.add(db);
    }


    /**
     * Appends a quantity database to the list of databases.
     *
     * @param db	The quantity database to be appended to the list of
     *			quantity databases.
     * @return		The database argument.
     */
    public synchronized void
    append(QuantityDB db)
    {
	list.add(db);
    }


    /**
     * Returns all quantities in the list of quantity databases whose name
     * matches a given name.
     *
     * @param name	The name of the quantity.
     * @return		All quantities in the list of quantity databases
     *			with name <code>name</code>.
     */
    public synchronized Quantity[]
    get(String name)
    {
	int		numDBs = list.size();
	Vector		quantities = new Vector(numDBs);// assume one per DB

	for (Iterator iter = list.iterator(); iter.hasNext(); )
	{
	    Quantity[]	quants = ((QuantityDB)iter.next()).get(name);

	    for (int j = 0; j < quants.length; ++j)
		quantities.add(quants[j]);
	}

	return (Quantity[])quantities.toArray(new Quantity[quantities.size()]);
    }


    /**
     * Returns all quantities in the list of quantity databases whose
     * default unit is convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		All quantities in the list of quantity database
     *			whose unit is convertible with <code>unit</code>.
     */
    public synchronized Quantity[]
    get(Unit unit)
    {
	int		numDBs = list.size();
	Vector		quantities = new Vector(numDBs);// assume one per DB

	for (Iterator iter = list.iterator(); iter.hasNext(); )
	{
	    Quantity[]	quants = ((QuantityDB)iter.next()).get(unit);

	    for (int j = 0; j < quants.length; ++j)
		quantities.add(quants[j]);
	}

	return (Quantity[])quantities.toArray(new Quantity[quantities.size()]);
    }


    /**
     * Returns the first quantity in the list of quantity databases whose
     * name matches a given name and whose default unit is convertible with
     * a given unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The unit of the quantity.
     * @return		The quantity in the list of quantity databases with
     *			name <code>name</code> and whose unit is convertible
     *			with <code>unit</code>, or <code>null</code> if no such
     *			quantity exists.
     */
    public synchronized Quantity
    get(String name, Unit unit)
    {
	Quantity	quantity = null;

	for (
	    Iterator iter = list.iterator();
	    quantity == null && iter.hasNext(); 
	    quantity = ((QuantityDB)iter.next()).get(name, unit))
	    ;	// EMPTY

	return quantity;
    }
}
