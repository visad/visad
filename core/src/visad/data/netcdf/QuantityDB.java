//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.12 2002-09-20 18:15:29 steve Exp $
 */

package visad.data.netcdf;

import java.util.Iterator;
import java.util.NoSuchElementException;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.units.NoSuchUnitException;
import visad.data.units.ParseException;
import visad.data.units.Parser;

/**
 * Provides support for a database of quantities.
 *
 * @author Steven R. Emmerson
 */
public abstract class
QuantityDB
{
    /**
     * The empty quantity database.  This is useful if, for example, you do
     * not want the netCDF import package to map incoming netCDF variables to
     * canonical ones by, effectively, altering their names and units.  This
     * database cannot be altered.
     */
    public static final QuantityDB emptyDB;

    static {
	try {
	    emptyDB = 
		new QuantityDB() {
		    public Quantity get(String name) {
			return null;
		    }
		    public Quantity[] get(Unit unit) {
			return null;
		    }
		    public void add(String name, Quantity quantity) {
			throw new UnsupportedOperationException();
		    }
		    public Iterator quantityIterator() {
			return NilIterator.INSTANCE;
		    }
		    public Iterator nameIterator() {
			return NilIterator.INSTANCE;
		    }
		};
	}
	catch (Exception ex) {
	    throw new ExceptionInInitializerError();
	}
    }

    /**
     * Returns the quantity in the database whose name matches a
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantity in the loal database that matches
     *			<code>name</code>.  Note that
     *			RETURN_VALUE<code>.getName().equals(name)</code> can
     *			be <code>false</code> due to aliasing.
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
     * @param unitSpec	The unit of the quantity.
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
		    "Quantity " + name + " already exists; its unit " +
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

    /**
     * A nil {@link Iterator}.  Such an {@link Iterator} iterates over nothing.
     */
    static class NilIterator implements Iterator {

	static final NilIterator INSTANCE;

	static {
	    INSTANCE = new NilIterator();
	}

	private NilIterator() {
	}

	/**
	 * Indicates if another element exists.  This implementation always 
	 * returns <code>false</code>.
	 *
	 * @return                     <code>false</code>.
	 */
	public boolean hasNext() {
	    return false;
	}

	/**
	 * Returns the next element.  This implementation always throws a
	 * {@link NoSuchElementException}.
	 *
	 * @throws NoSuchElementException if this method is invoked.
	 */
	public Object next() {
	    throw new NoSuchElementException();
	}

	/**
	 * Removes the current element.  This implementation always throws an
	 * {@link UnsupportedOperationException}.
	 *
	 * @throws UnsupportedOperationException if this method is invoked.
	 */
	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
}
