/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Apply the "Adapter" pattern to
 * convert the interface of Class (int []) to interface IntMap.
 * <p>
 * Instances of this class are constructed automatically
 * for you when no 'next' argument is provided to the other
 * IntMap constructors.
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
IntArrayAdapter
		implements IntMap
{
	public
	IntArrayAdapter()
		{ /*EMPTY*/ }

	/**
	 * Returns the value to which this Map maps the specified key.
	 * @return int adaptee[key];
	 */
	public int
	get(int key)
		{ return adaptee[key]; }

	/**
	 * Returns the number of key-value mappings in this Map.
	 * @return adaptee.length
	 */
	public int
	size()
		{ return adaptee.length; }

	/**
	 * Instances of this class are always
	 * the tail of an IntMap chain.
	 * Initialize the prev member.
	 * Call <code>this.rebind(new int[rank]);</code>
	 * @return this
	 */
	public IntArrayAdapter
	tail(int rank, Object prev)
	{
		this.prev = prev;
		rebind(new int[rank]);
		return this;
	}

	/**
         * Traverse the inverse mapping chain to
         * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii)
	{
		if(prev instanceof IntMap)
			return ((IntMap)prev).getLength(ii);
		return Array.getInt(prev, ii);
	}

	/**
	 * Reset the adaptee converted by this.
	 */
	public void
	rebind(int [] newAdaptee)
	{
		adaptee = newAdaptee;
	}

 /* */
	/**
	 * Either an IntMap delegate for getLength(int)
	 * or an array of ints which can answer the
	 * question directly.
	 */
	private Object prev;

	private int [] adaptee;
}
