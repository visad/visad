//
// QuantityDBImpl.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBImpl.java,v 1.2 1999-01-07 17:01:29 steve Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Iterator;
import java.util.TreeMap;
import visad.DerivedUnit;
import visad.PromiscuousUnit;
import visad.QuantityDimension;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;

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
{
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
    private static final String	minName = "";

    /**
     * The maximum name value.
     */
    private static final String	maxName = "zzz";

    /**
     * The minimum unit value.
     */
    private static final Unit	minUnit = new DerivedUnit();

    /**
     * The maximum unit value.
     */
    private static final Unit	maxUnit = new DerivedUnit();


    /**
     * Add a quantity to the database under a given name.
     *
     * @param name		The name of the quantity (e.g. "length").
     *				May be an alias for the quantity.
     * @param quantity		The quantity.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    public synchronized void add(String name, Quantity quantity)
	throws VisADException
    {
	Unit	unit = quantity.getDefaultUnit();

	nameMap.put(new NameKey(name), quantity);
	unitMap.put(new UnitKey(unit, name), quantity);
    }


    /**
     * Add a quantity to the database given a name and a display unit
     * specification.
     *
     * @param name		The name of the quantity (e.g. "length").
     * @param unitSpec		The preferred display unit for the 
     *				quantity (e.g. "feet").
     * @exception VisADException	Couldn't create necessary VisAD object.
     * @exception ParseException	Couldn't decode unit specification.
     */
    public synchronized void add(String name, String unitSpec)
      throws VisADException, ParseException
    {
	add(name, new Quantity(name, unitSpec));
    }


    /**
     * Return an iterator of the quantities in the database.
     */
    public Iterator
    getIterator()
    {
	return nameMap.values().iterator();
    }


    /**
     * Return the quantity in the database whose name matches a 
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantity in the loal database with name
     *			<code>name</code>.
     */
    public synchronized Quantity get(String name)
    {
	return (Quantity)nameMap.get(new NameKey(name));
    }


    /**
     * Return all quantities in the database whose default unit is
     * convertible with a given unit.
     *
     * @param unit	The unit of the quantity.
     * @return		The quantities in the database whose unit is 
     *			convertible with <code>unit</code>.
     */
    public synchronized Quantity[] get(Unit unit)
    {
	return (Quantity[])unitMap.subMap
	    (new UnitKey(unit, minName), new UnitKey(unit, maxName))
		.values().toArray(new Quantity[0]);
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
