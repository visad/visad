//
// QuantityDBImpl.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDBImpl.java,v 1.1 1998-11-16 18:23:40 steve Exp $
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
     * The (Name, Unit) -> Quantity map (major key: name).
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

	nameMap.put(new NameKey(name, unit), quantity);
	unitMap.put(new UnitKey(name, unit), quantity);
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
     * Return all quantities in the database whose name matches a 
     * given name.
     *
     * @param name	The name of the quantity.
     * @return		The quantities in the loal database with name
     *			<code>name</code> and whose unit is convertible with
     *			<code>unit</code>.
     */
    public synchronized Quantity[] get(String name)
    {
	return (Quantity[])nameMap.subMap(
	    new NameKey(name, minUnit), new NameKey(name, maxUnit))
		.values().toArray(new Quantity[0]);
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
	    (new UnitKey(minName, unit), new UnitKey(maxName, unit))
		.values().toArray(new Quantity[0]);
    }


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
    public synchronized Quantity get(String name, Unit unit)
    {
	Quantity	quantity =
	    (Quantity)nameMap.get(new NameKey(name, unit));

	return quantity;
    }


    /**
     * The following class is the abstract superclass of the map keys.
     */
    private static abstract class
    Key
	implements	Serializable, Comparable
    {
	/**
	 * The comparison value for the name of the quantity.
	 */
	protected final CollationKey	name;

	/**
	 * The default unit of the quantity.
	 */
	protected final Unit		unit;


	/**
	 * Construct from the name of a quantity and its unit.
	 *
	 * @param name	The name of the quantity.
	 * @param unit	The default unit of the quantity.
	 */
	protected Key(CollationKey name, Unit unit)
	{
	    this.name = name;
	    this.unit = unit;
	}


	/**
	 * Compare this key to another (name first).  Overridden in UnitKey.
	 */
	public int compareTo(Object obj)
	  throws ClassCastException
	{
	    Key	that = (Key)obj;
	    int	i = this.name.compareTo(that.name);

	    return i != 0
		       ? i
		       : compare(this.unit, that.unit);
	}


	/**
	 * Compare one Unit to another.
	 */
	protected int compare(Unit a, Unit b)
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


    /**
     * The following class implements the key to the name map.
     *
     * Immutable.
     */
    protected static final class
    NameKey
	extends	Key
    {
	/**
	 * The Collator for the name component of this key.
	 */
	private static final Collator	collator;
	
	
	static
	{
	    collator = Collator.getInstance();
	    collator.setStrength(Collator.PRIMARY);
	}


	/**
	 * Construct from the name of a quantity and its unit.
	 *
	 * @param name	The name of the quantity.
	 * @param unit	The default unit of the quantity.
	 */
	protected NameKey(String name, Unit unit)
	{
	    super(collator.getCollationKey(name), unit);
	}
    }


    /**
     * The following class implements the key to the unit map.
     *
     * Immutable.
     */
    protected static final class
    UnitKey
	extends	Key
    {
	/**
	 * The Collator for the name component of this key.
	 */
	private static final Collator	collator = Collator.getInstance();


	/**
	 * Construct from the name of a quantity and its unit.
	 *
	 * @param name	The name of the quantity.
	 * @param unit	The default unit of the quantity.
	 */
	protected UnitKey(String name, Unit unit)
	{
	    super(collator.getCollationKey(name), unit);
	}


	/**
	 * Compare this key to another (unit first).
	 */
	public int compareTo(Object obj)
	  throws ClassCastException
	{
	    Key	that = (Key)obj;
	    int	i = compare(this.unit, that.unit);

	    return i != 0
		      ? i
		      : this.name.compareTo(that.name);
	}
    }
}
