//
// QuantityDBImpl.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBImpl.java,v 1.7 2006-02-13 22:30:07 curtis Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import visad.DerivedUnit;
import visad.PromiscuousUnit;
import visad.QuantityDimension;
import visad.RealType;
import visad.TypeException;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.units.ParseException;
import visad.data.units.Parser;

/**
 * Provides support for a database of quantities.
 *
 * Instances are modifiable.
 *
 * @author Steven R. Emmerson
 */
public class
QuantityDBImpl
    extends	QuantityDB
    implements	Serializable
{
    /**
     * The set of quantities.
     */
    private final SortedSet		quantitySet = new TreeSet();

    /**
     * The (Name) -> Quantity map.
     */
    private final TreeMap		nameMap = new TreeMap();

    /**
     * The (Unit, Name) -> Quantity map (major key: unit).
     */
    private final TreeMap		unitMap = new TreeMap();

    /**
     * The minimum name value.
     */
    private static final String		minName = "";

    /**
     * The maximum name value.
     */
    private static final String		maxName = "zzz";

    /**
     * The minimum unit value.
     */
    private static final Unit		minUnit = new DerivedUnit();

    /**
     * The maximum unit value.
     */
    private static final Unit		maxUnit = new DerivedUnit();

    /**
     * The quantity database to search after this one.
     */
    private /*final*/ QuantityDB	nextDB;


    /**
     * Constructs with another quantity database as the successor database.
     *
     * @param nextDB		The quantity database to search after this one.
     *				May be <code>null</code>.
     */
     public
     QuantityDBImpl(QuantityDB nextDB)
     {
	this.nextDB = nextDB;
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
	for (int i = 0; i < definitions.length; i += 2)
	    add(definitions[i], definitions[i+1]);

	for (int i = 0; i < aliases.length; i += 2)
	    add(aliases[i], get(aliases[i+1]));

	return this;
    }


    /**
     * Adds a quantity to the database under a given name.
     *
     * @param name		The name of the quantity (e.g. "length").
     *				May be an alias for the quantity.
     * @param quantity		The quantity.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public synchronized void
    add(String name, Quantity quantity)
	throws VisADException
    {
	if (name == null || quantity == null)
	    throw new VisADException("add(): null argument");

	Unit	unit = quantity.getDefaultUnit();

	quantitySet.add(quantity);
	nameMap.put(new NameKey(name), quantity);
	unitMap.put(new UnitKey(unit, quantity.getName()), quantity);
    }


    /**
     * Adds given Quantity-s to the database.
     *
     * @param quantities	The quantities to be added.  The quantity will
     *				be added under it own name.
     * @return			The database resulting from the addition.  May
     *				or may not be the original object.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public QuantityDB
    add(Quantity[] quantities)
	throws VisADException
    {
	for (int i = 0; i < quantities.length; i++)
	{
	    Quantity	quantity = quantities[i];
	    add(quantity);
	}
	return this;
    }


    /**
     * Adds a quantity to the database given a name and a display unit
     * specification.
     *
     * @param name		The name of the quantity (e.g. "length").
     * @param unitSpec		The preferred display unit for the
     *				quantity (e.g. "feet").
     * @throws ParseException	Couldn't decode unit specification.
     * @throws TypeException	Incompatible ScalarType of same name already
     *				exists.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected synchronized void
    add(String name, String unitSpec)
      throws ParseException, TypeException, VisADException
    {
	try
	{
	    add(name, new Quantity(name, unitSpec));
	}
	catch (VisADException e)
	{
	    if (!(e instanceof TypeException))
		throw e;

	    RealType	realType = RealType.getRealTypeByName(name);
	    if (realType == null ||
		!Unit.canConvert(
		    realType.getDefaultUnit(), Parser.parse(unitSpec)))
		throw (TypeException)e;
	}
    }


    /**
     * Provides support for iterating over the database.
     */
    protected abstract class
    Iterator
	implements	java.util.Iterator
    {
	/**
	 * The private iterator.
	 */
	protected java.util.Iterator	iterator;

	/**
	 * Whether or not we can switch to the other database.
	 */
	private boolean			canSwitch = nextDB != null;

	/*
	 * Returns <code>true</code> if <code>next</code> will return an object.
	 * @return		<code>true</code> if and only if <code>next()
	 *			</code> will return a Quantity.
	 */
	public boolean
	hasNext()
	{
	    boolean	have = iterator.hasNext();
	    if (!have && doSwitch())
		have = hasNext();
	    return have;
	}

	protected abstract Object
	nextObject();

	/*
	 * Returns the next thing in the database.
	 * @return		The next thing in the database if and only
	 *			if a prior <code>hasNext()</code> did or would
	 *			have returned <code>true</code>; otherwise
	 *			throws an exception.
	 * @throws NoSuchElementException	No more things in the database.
	 */
	public Object
	next()
	{
	    Object	object;
	    try
	    {
		object = nextObject();
	    }
	    catch (NoSuchElementException e)
	    {
		if (!doSwitch())
		    throw e;
		object = next();
	    }
	    return object;
	}

	/**
	 * Gets the iterator for the successor database.
	 */
	protected abstract java.util.Iterator
	nextIterator();

	/**
	 * Switchs to the other database.
	 * @return		<code>true</code> if an only if the other
	 *			database exists and this is the first switch
	 *			to it.
	 */
	protected boolean
	doSwitch()
	{
	    boolean	goodSwitch;
	    if (!canSwitch)
	    {
		goodSwitch = false;
	    }
	    else
	    {
		iterator = nextIterator();
		canSwitch = false;
		goodSwitch = true;
	    }
	    return goodSwitch;
	}

	/**
	 * Remove the element returned by the last <code>next()</code>.
	 * @throws UnsupportedOperationException
				Operation not supported.
	 */
	public void
	remove()
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException(
		"remove(): Can't remove elements from quantity database");
	}
    }


    /**
     * Provides support for iterating over the quantities in the database.
     */
    protected class
    QuantityIterator
	extends	Iterator
    {
	/**
	 * Constructs.
	 */
	protected
	QuantityIterator()
	{
	    iterator = quantitySet.iterator();
	}

	/**
	 * Returns the next quantity of the iterator.
	 */
	protected Object
	nextObject()
	{
	    return iterator.next();
	}

	/**
	 * Returns the iterator for the successor database.
	 */
	protected java.util.Iterator
	nextIterator()
	{
	    return nextDB.quantityIterator();
	}
    }


    /**
     * Provides support for iterating over the names in the database.
     * A name can only appear once in the database.
     */
    protected class
    NameIterator
	extends	Iterator
    {
	/**
	 * Constructs.
	 */
	protected
	NameIterator()
	{
	    iterator = nameMap.keySet().iterator();
	}

	/**
	 * Returns the next name in the iterator.
	 */
	protected Object
	nextObject()
	{
	    return ((NameKey)iterator.next()).getName();
	}

	/**
	 * Gets the iterator for the successor database.
	 */
	protected java.util.Iterator
	nextIterator()
	{
	    return nextDB.nameIterator();
	}
    }


    /**
     * Returns an iterator of the quantities in the database.
     * @return		An iterator of the quantities in the database.
     */
    public java.util.Iterator
    quantityIterator()
    {
	return new QuantityIterator();
    }


    /**
     * Returns an iterator of the names in the database.
     * @return		An iterator of the names in the database.
     */
    public java.util.Iterator
    nameIterator()
    {
	return new NameIterator();
    }


    /**
     * Returns the quantity in the database whose name matches a
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantity in the loal database with name
     *			<code>name</code>.
     */
    public synchronized Quantity
    get(String name)
    {
	Quantity	quantity = (Quantity)nameMap.get(new NameKey(name));
	return quantity != null
		? quantity
		: nextDB == null
		    ? null
		    : nextDB.get(name);
    }


    /**
     * Returns all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is
     *			convertible with <code>unit</code>.
     */
    public synchronized Quantity[]
    get(Unit unit)
    {
	Quantity[]	myQuantities = (Quantity[])unitMap.subMap
	    (new UnitKey(unit, minName), new UnitKey(unit, maxName))
		.values().toArray(new Quantity[0]);
	Quantity[]	nextQuantities = nextDB == null
					    ? new Quantity[] {}
					    : nextDB.get(unit);
	Quantity[]	quantities =
	    new Quantity[myQuantities.length + nextQuantities.length];
	System.arraycopy(myQuantities, 0, quantities, 0, myQuantities.length);
	System.arraycopy(nextQuantities, 0, quantities, myQuantities.length,
	    nextQuantities.length);
	myQuantities = null;
	nextQuantities = null;
	return quantities;
    }


    /**
     * Provides support for keys to the name map.
     *
     * Immutable.
     */
    protected static class
    NameKey
	implements	Serializable, Comparable
    {
	/**
	 * The name of the quantity.
	 */
	private final String		name;

	/**
	 * The Collator for the name.
	 */
	private static final Collator	collator;

	/**
	 * The comparison value for the name of the quantity.
	 */
	private final CollationKey	nameCookie;


	static
	{
	    collator = Collator.getInstance();
	    collator.setStrength(Collator.PRIMARY);
	}


	/**
	 * Constructs from the name of a quantity.
	 *
	 * @param name	The name of the quantity.
	 */
	protected NameKey(String name)
	{
	    this.name = name;
	    nameCookie = collator.getCollationKey(name);
	}


	/**
	 * Compare this key to another.
	 */
	public int compareTo(Object obj)
	  throws ClassCastException
	{
	    return nameCookie.compareTo(((NameKey)obj).nameCookie);
	}


	/**
	 * Returns the name of the quantity.
	 */
	public String
	getName()
	{
	    return name;
	}
    }


    /**
     * Provides support for keys to the unit map.
     *
     * Immutable.
     */
    protected static final class
    UnitKey
	extends	NameKey
    {
	/**
	 * The default unit of the quantity.
	 */
	protected final Unit		unit;


	/**
	 * Constructs from the unit of a quantity and its name.
	 *
	 * @param unit	The default unit of the quantity.
	 * @param name	The name of the quantity.
	 */
	protected UnitKey(Unit unit, String name)
	{
	    super(name);
	    this.unit = unit;
	}


	/**
	 * Compare this key to another (unit first).
	 */
	public int compareTo(Object obj)
	  throws ClassCastException
	{
	    UnitKey	that = (UnitKey)obj;
	    int		i = compare(this.unit, that.unit);

	    return i != 0
		      ? i
		      : super.compareTo(that);
	}


	/**
	 * Compare one Unit to another.
	 */
	private int compare(Unit a, Unit b)
	  throws ClassCastException
	{
	    int	comparison;

	    if (a instanceof PromiscuousUnit || b instanceof PromiscuousUnit)
	    {
		comparison = 0;
	    }
	    else
	    if (a == null || b == null)
	    {
		comparison = a == null && b == null
				? 0
				: a == null
				    ? -1
				    :  1;
	    }
	    else
	    {
		try
		{
		  comparison = (a == b)
		      ? 0
		      : (a == minUnit || b == maxUnit)
			  ? -1
			  : (a == maxUnit || b == minUnit)
			      ? 1
			      : new QuantityDimension(a).compareTo
				  (new QuantityDimension(b));
		}
		catch (UnitException e)
		{
		  throw new ClassCastException(e.getMessage());
		}
	    }
	    return comparison;
	}
    }
}
