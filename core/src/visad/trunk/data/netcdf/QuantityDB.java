//
// QuantityDB.java
//

/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: QuantityDB.java,v 1.1 1998-06-22 18:32:02 visad Exp $
 */

package visad.data.netcdf;

import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import visad.DerivedUnit;
import visad.QuantityDimension;
import visad.Unit;
import visad.UnitException;
import visad.VisADException;
import visad.data.netcdf.units.NoSuchUnitException;
import visad.data.netcdf.units.ParseException;
import visad.data.netcdf.units.Parser;


/**
 * The following class implements a database of quantities.
 *
 * @author Steven R. Emmerson
 */
public class QuantityDB
  implements	Serializable
{
  /**
   * The quantity database to search after this one.
   */
  private /*final*/ QuantityDB	otherDB;

  /**
   * The (Name, Unit) -> Quantity map.
   */
  private final TreeMap		nameMap = new TreeMap();

  /**
   * The (Unit, Name) -> Quantity map.
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
   * Construct.
   *
   * @param otherDB		The quantity database for the get...()
   *				methods to search after this one if no
   *				entry found.  May be <code>null</code>.
   */
  public QuantityDB(QuantityDB otherDB)
  {
    this.otherDB = otherDB;
  }


  /**
   * Add a quantity to the database under a given name.
   *
   * @param name		The name of the quantity (e.g. "length").
   *				May be an alias for the quantity.
   * @param quantity		The quantity.
   * @exception VisADException	Couldn't create necessary VisAD object.
   */
  protected synchronized void add(String name, Quantity quantity)
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
   */
  public synchronized void add(String name, String unitSpec)
    throws VisADException, ParseException, NoSuchUnitException
  {
    add(name, new Quantity(name, unitSpec));
  }


  /**
   * Return all quantities in the local database whose name matches a 
   * given name.
   *
   * @param name	The name of the quantity.
   * @return		The quantities in the loal database with name
   *			<code>name</code> and whose unit is convertible with
   *			<code>unit</code>.
   */
  public synchronized Quantity[] getLocal(String name)
  {
    return (Quantity[])nameMap.subMap(
      new NameKey(name, minUnit), new NameKey(name, maxUnit))
      .values().toArray(new Quantity[0]);
  }


  /**
   * Return all quantities in the global database whose name matches a 
   * given name.
   *
   * @param name	The name of the quantity.
   * @return		All quantities in the global database with name
   *			<code>name</code>.
   */
  public synchronized Quantity[] get(String name)
  {
    Quantity[]	values;
    Quantity[]	myValues = getLocal(name);

    if (otherDB == null)
    {
      values = myValues;
    }
    else
    {
      Quantity[]	otherValues = otherDB.get(name);

      values = new Quantity[myValues.length + otherValues.length];

      System.arraycopy(myValues, 0, values, 0, myValues.length);
      System.arraycopy(otherValues, 0, values, myValues.length, 
	otherValues.length);
    }

    return values;
  }


  /**
   * Return the unique quantity in the global database whose name matches a 
   * given name.
   *
   * @param name	The name of the quantity.
   * @return		The unique quantity in the global database with name
   *			<code>name</code> or <code>null</code> if no such
   *			quantity exists or is not unique.
   */
  public synchronized Quantity getIfUnique(String name)
  {
    Quantity[]	values = get(name);

    return values.length == 1
		? values[0]
		: null;
  }


  /**
   * Return all quantities in the local database whose default unit is
   * convertible with a given unit.
   *
   * @param unit	The unit of the quantity.
   * @return		The quantities in the local database whose unit is 
   *			convertible with <code>unit</code>.
   */
  public synchronized Quantity[] getLocal(Unit unit)
  {
    return (Quantity[])unitMap.subMap
      (new UnitKey(minName, unit), new UnitKey(maxName, unit))
      .values().toArray(new Quantity[0]);
  }


  /**
   * Return all quantities in the global database whose default unit is
   * convertible with a given unit.
   *
   * @param unit	The unit of the quantity.
   * @return		All quantities in the global database whose unit is 
   *			convertible with <code>unit</code>.
   */
  public synchronized Quantity[] get(Unit unit)
  {
    Quantity[]  values;
    Quantity[]	myValues = getLocal(unit);

    if (otherDB == null)
    {
      values = myValues;
    }
    else
    {
      Quantity[]	otherValues = otherDB.get(unit);

      values = new Quantity[myValues.length + otherValues.length];

      System.arraycopy(myValues, 0, values, 0, myValues.length);
      System.arraycopy(otherValues, 0, values, myValues.length, 
	otherValues.length);
    }

    return values;
  }


  /**
   * Return the unique quantity in the global database whose unit is
   * convertible with a given unit.
   *
   * @param unit	The unit of the quantity.
   * @return		The unique quantity in the global database whose
   *			unit is convertible with <code>unit</code>, or
   *			<code>null</code> if no such quantity exists or is
   *			not unique.
   */
  public synchronized Quantity getIfUnique(Unit unit)
  {
    Quantity[]	values = get(unit);

    return values.length == 1
		? values[0]
		: null;
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
    Quantity	quantity = (Quantity)nameMap.get(new NameKey(name, unit));

    if (quantity == null && otherDB != null)
      quantity = otherDB.get(name, unit);

    return quantity;
  }


  /**
   * Return the quantity in the database whose name matches a given name and
   * whose default unit is convertible with a given unit specification.
   *
   * @param name	The name of the quantity.
   * @param unitSpec	The unit specification of the quantity.
   * @return		The quantity in the database with name <code>name</code>
   *			and whose unit is convertible with <code>unit</code>,
   *			or <code>null</code> if no such quantity exists.
   */
  public synchronized Quantity get(String name, String unitSpec)
    throws ParseException, NoSuchUnitException
  {
    return get(name, Parser.parse(unitSpec));
  }


  /**
   * The following class is the abstract superclass of the map keys.
   */
  private abstract class Key
    implements	Serializable, Comparable
  {
    /**
     * The name of the quantity.
     */
    protected final String	name;

    /**
     * The default unit of the quantity.
     */
    protected final Unit	unit;


    /**
     * Construct from the name of a quantity and its unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The default unit of the quantity.
     */
    protected Key(String name, Unit unit)
    {
      this.name = name.toLowerCase();
      this.unit = unit;
    }


    /**
     * Compare this key to another.
     */
    public abstract int compareTo(Object obj)
      throws ClassCastException;


    /**
     * Compare one Unit to another.
     */
    protected int compare(Unit a, Unit b)
      throws ClassCastException
    {
      try
      {
	return (a == b)
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
  }


  /**
   * The following class implements the key to the name map.
   *
   * Immutable.
   */
  protected final class NameKey
    extends	Key
  {
    /**
     * Construct from the name of a quantity and its unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The default unit of the quantity.
     */
    protected NameKey(String name, Unit unit)
    {
      super(name, unit);
    }


    /**
     * Compare this key to another.
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
  }


  /**
   * The following class implements the key to the unit map.
   *
   * Immutable.
   */
  protected final class UnitKey
    extends	Key
  {
    /**
     * Construct from the name of a quantity and its unit.
     *
     * @param name	The name of the quantity.
     * @param unit	The default unit of the quantity.
     */
    protected UnitKey(String name, Unit unit)
    {
      super(name, unit);
    }


    /**
     * Compare this key to another.
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
