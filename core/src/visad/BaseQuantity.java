//
// BaseQuantity.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


/**
 * This class represents a base quantity (e.g. "length").
 *
 * This class is mutable but monotonic: new base quantities can be added but
 * not removed.
 */
public final class BaseQuantity
  implements	Serializable
{
  /**
   * The name/quantity map-view of the database.
   */
  private static final Map		nameDB = new TreeMap();

  /**
   * The alias/quantity map-view of the database.
   */
  private static final Map		aliasDB = new TreeMap();

  /**
   * The index/quantity map-view of the database.
   */
  private static final AbstractList	indexDB = new Vector(8);

  /**
   * The name of this base quantity.
   */
  private final String			name;

  /**
   * The index of this base quantity.
   */
  private final int			index;


  /*
   * Initialize the database with standard base quantities.
   */
  static
  {
    try
    {
      add("electric current",
	new String[] {SI.ampere.quantityName(), "current"});
      add("luminous intensity", SI.candela.quantityName());
      add("thermodynamic temperature",
	new String[] {SI.kelvin.quantityName(), "temperature"});
      add("mass", SI.kilogram.quantityName());
      add("length", SI.meter.quantityName());
      add("time", SI.second.quantityName());
      add("amount of substance", SI.mole.quantityName());
      add("plane angle", new String[] {SI.radian.quantityName(), "angle"});
      add("solid angle", SI.steradian.quantityName());
    }
    catch (VisADException e)
    {
      /*
       * With our godlike powers of observation, we know that this
       * VisADException can't occur -- so we ignore it.
       */
    }
  }


  /**
   * Construct a base quantity.  Private to ensure use of the get...()
   * methods.
   *
   * @param name	The name of the base qantity.
   */
  private BaseQuantity(String name, int index)
  {
    this.name = name;
    this.index = index;
  }


  /**
   * Add a base quantity to the database.
   *
   * @param name		The name of the base quantity.
   * @precondition		<code>name</code> isn't already in the database.
   * @exception VisADException	Attempt to redefine an existing base quantity.
   */
  public static synchronized BaseQuantity add(String name)
    throws VisADException
  {
    String	key = key(name);

    if (nameDB.containsKey(key))
      throw new VisADException("Attempt to redefine existing base quantity \"" +
	name + "\"");

    BaseQuantity	q = new BaseQuantity(name, indexDB.size());

    nameDB.put(key, q);
    indexDB.add(q);

    return q;
  }


  /**
   * Add a base quantity with an alias to the database.
   *
   * @param name		The name of the base quantity being added to
   *				the database (e.g. "plane angle", "foobility").
   * @param alias		An alias for the base quantity (e.g. "angle").
   * @precondition              Neither <code>name</code> nor <code>alias</code>
   *				is in the database.
   * @return			A reference to the new base quantity in the
   *				database.
   * @exception VisADException  Attempt to redefine an existing base quantity
   *				or alias.  If thrown, then the database is
   *				unmodified.
   */
  public static synchronized BaseQuantity add(String name, String alias)
    throws VisADException
  {
    return add(name, new String[] {alias});
  }


  /**
   * Add a base quantity with aliases to the database.
   *
   * @param name		The name of the base quantity being added to
   *				the database (e.g. "plane angle", "foobility").
   * @param aliases		Aliases for the base quantity (e.g. "angle").
   * @precondition		Neither <code>name</code> nor any name in
   *				<code>aliases</code> is in the database.
   * @postcondition		<code>size()</code> will return one greater
   *				than on entry.
   * @return			A reference to the new base quantity in the
   *				database.
   * @exception VisADException	Attempt to redefine an existing base quantity
   *				or alias.  If thrown, then the database is
   *				unmodified.
   */
  public static synchronized BaseQuantity add(String name, String[] aliases)
    throws VisADException
  {
    for (int i = 0; i < aliases.length; ++i)
    {
      if (aliasDB.containsKey(key(aliases[i])))
	throw new VisADException(
	  "Attempt to redefine existing base quantity alias \"" +
	  aliases[i] + "\"");
    }

    BaseQuantity	q = add(name);

    for (int i = 0; i < aliases.length; ++i)
      aliasDB.put(key(aliases[i]), q);

    return q;
  }


  /**
   * Convert the given name into a database key.
   *
   * @param name		The name or alias of the base quantity.
   * @return			The database key.
   */
  private static String key(String name)
  {
    return name.toLowerCase();
  }


  /**
   * Return the number of base qantities in the database.
   *
   * @return			The current number of base quantities in the
   *				database.  This number is strictly monotonic:
   *				it will increase by one each time a new base
   *				quantity is added to the database.
   */
  public static synchronized int size()
  {
    return indexDB.size();
  }


  /**
   * Return the name of this base quantity.
   *
   * @return			The name of this base quantity.
   */
  public String getName()
  {
    return name;
  }


  /**
   * Return the index of this base quantity.
   *
   * @return			The index of this base quantity.
   */
  public int getIndex()
  {
    return index;
  }


  /**
   * Retrieve a base quantity from the database based on a
   * match of the name.
   *
   * @param name		The name of the base quantity to be retrieved.
   * @return			The base quantity in the database corresponding
   *				to <code>name</code> or <code>null</code> if
   *				no such quantity exists.
   */
  public static synchronized BaseQuantity getByName(String name)
  {
    return (BaseQuantity)nameDB.get(key(name));
  }


  /**
   * Retrieve a base quantity from the database based on a
   * match of an alias.
   *
   * @param name		An alias of the base quantity to be retrieved.
   * @return			The base quantity in the database corresponding
   *				to <code>name</code> or <code>null</code> if
   *				no such quantity exists.
   */
  public static synchronized BaseQuantity getByAlias(String name)
  {
    return (BaseQuantity)aliasDB.get(key(name));
  }


  /**
   * Retrieve a base quantity from the database based on a
   * match of either the name or an alias.  Try the name first.
   *
   * @param name		The name or an alias of the base quantity to
   *				be retrieved.
   * @return			The base quantity in the database corresponding
   *				to <code>name</code> or <code>null</code> if
   *				no such quantity exists.
   */
  public static synchronized BaseQuantity get(String name)
  {
    BaseQuantity	q = getByName(name);

    return q != null ? q : getByAlias(name);
  }


  /**
   * Retrieve the base quantity associated with a given index.
   *
   * @param i			The origin-0 index of the base quantity.
   * @precondition		<code>i >= 0 && i < size()</code>.
   * @return			The base quantity at index <code>i</code>.
   */
  public static BaseQuantity get(int i)
  {
    return (BaseQuantity)indexDB.get(i);
  }
}
