//
// QuantityDBMap.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityMap.java,v 1.2 1998-11-16 18:23:40 steve Exp $
 */

package visad.data.netcdf;

import java.util.Stack;
import java.util.Vector;
import java.io.Serializable;
import visad.SI;
import visad.Unit;
import visad.VisADException;


/**
 * Provides for a static stack of quantity databases, which map between
 * physical quantities and VisAD Quantity-s.
 *
 * @author Steven R. Emmerson
 */
public class
QuantityMap
    implements	Serializable
{
    /**
     * The stack of quantity databases.
     */
    private static /*final*/ Stack	stack;


    static
    {
        stack = new Stack();

	try
	{
	    push(StandardQuantityDB.instance());
	}
	catch (Exception e)
	{
	}
    }


    /**
     * Constructs from nothing.  Protected to prevent instantiation.
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected
    QuantityMap()
    {
    }


    /**
     * Pushes a quantity database onto the stack of databases.
     *
     * @param db	The quantity database to be pushed onto the stack of
     *			quantity databases.
     * @return		The database argument.
     */
    public static synchronized QuantityDB
    push(QuantityDB db)
    {
	return (QuantityDB)stack.push(db);
    }


    /**
     * Pops the quantity database most recently pushed onto the stack of
     * databases.
     *
     * @return          <code>null</code> if the stack contains only
     *                  the standard quantity database; otherwise, the
     *                  quantity database most recently pushed onto the
     *                  stack.  It is not possible to pop the standard
     *                  quantity database off the stack.
     */
    public static synchronized QuantityDB
    pop()
    {
	return stack.size() == 1
		    ? null
		    : (QuantityDB)stack.pop();
    }


    /**
     * Returns all quantities in the stack of quantity databases whose name
     * matches a given name.
     *
     * @param name	The name of the quantity.
     * @return		All quantities in the stack of quantity databases
     *			with name <code>name</code>.
     */
    public static synchronized Quantity[]
    get(String name)
    {
	int		numDBs = stack.size();
	Vector		quantities = new Vector(numDBs);// assume one per DB

	for (int i = numDBs-1; i >= 0; --i)
	{
	    Quantity[]	quants = ((QuantityDB)stack.elementAt(i)).get(name);

	    for (int j = 0; j < quants.length; ++j)
		quantities.add(quants[j]);
	}

	return (Quantity[])quantities.toArray(new Quantity[quantities.size()]);
    }


    /**
     * Returns the unique quantity in the stack of quantity databases whose
     * name matches a given name.
     *
     * @param name	The name of the quantity.
     * @return		The unique quantity in the stack of quantity databases
     *			with name <code>name</code> or <code>null</code> if
     *			no such quantity exists or is not unique.
     */
    public static Quantity
    getIfUnique(String name)
    {
	Quantity[]	quantities = get(name);

	return quantities.length == 1
		    ? quantities[0]
		    : null;
    }


    /**
     * Returns all quantities in the stack of quantity databases whose
     * default unit is convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		All quantities in the stack of quantity database
     *			whose unit is convertible with <code>unit</code>.
     */
    public static synchronized Quantity[]
    get(Unit unit)
    {
	int		numDBs = stack.size();
	Vector		quantities = new Vector(numDBs);// assume one per DB

	for (int i = numDBs-1; i >= 0; --i)
	{
	    Quantity[]	quants = ((QuantityDB)stack.elementAt(i)).get(unit);

	    for (int j = 0; j < quants.length; ++j)
		quantities.add(quants[j]);
	}

	return (Quantity[])quantities.toArray(new Quantity[quantities.size()]);
    }


    /**
     * Returns the unique quantity in the stack of quantity databases whose
     * unit is convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The unique quantity in the stack of quantity databases
     *			whose unit is convertible with <code>unit</code>, or
     *			<code>null</code> if no such quantity exists or is
     *			not unique.
     */
    public static Quantity
    getIfUnique(Unit unit)
    {
	Quantity[]	quantities = get(unit);

	return quantities.length == 1
		    ? quantities[0]
		    : null;
    }


    /**
     * Returns the first quantity in the stack of quantity databases whose
     * name matches a given name and whose default unit is convertible with
     * a given unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The unit of the quantity.
     * @return		The quantity in the stack of quantity databases with
     *			name <code>name</code> and whose unit is convertible
     *			with <code>unit</code>, or <code>null</code> if no such
     *			quantity exists.
     */
    public static synchronized Quantity
    get(String name, Unit unit)
    {
	Quantity	quantity = null;

	for (int i = stack.size()-1; i >= 0 && quantity == null; --i)
	    quantity = ((QuantityDB)stack.elementAt(i)).get(name, unit);

	return quantity;
    }


    /**
     * Returns the quantity corresponding to the best combination of
     * name and unit.
     *
     * @param name      The name of the quantity.
     * @param unit      The unit of the quantity.  May be <code>null</code>.
     * @return          The corresponding, unique, VisAD quantity or
     *                  <code>null</code> if no such quantity exists.
     */
    public static synchronized Quantity
    getBest(String name, Unit unit)
    {
        return unit == null
                    ? getIfUnique(name)
                    : get(name, unit);
    }


    /**
     * Returns the first quantity corresponding to the given name.
     *
     * @param name      The name of the quantity.
     * @return          The first corresponding VisAD quantity or
     *                  <code>null</code> if no such quantity exists.
     */
    public static synchronized Quantity
    getFirst(String name)
    {
	Quantity[]	quantities = get(name);

        return quantities.length == 0
                    ? null
                    : quantities[0];
    }
}
