/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;

/**
 * An immutable Map of int by int key.
 * An Map maps keys to values.
 * A Map cannot contain duplicate keys;
 * each key can map to at most one value.
 * <p>
 * An IntMap is like a readonly 1-d array of int.
 * The <code>size()</code> method returns the array length.
 * The <code>get(int ii)</code> method returns the int stored at
 * position <code>ii</code>;
 * <p>
 * MultiArray uses array of int for as index (key) values.
 * This interface is an abstraction of those, so that we can
 * implement transformations on them. Beyond the <code>get()</code> and
 * <code>size()</code> methods of the map abstraction, methods used in the
 * context of MultiArrayProxy are present to support connecting the
 * reverse chain of a linked list of IntMap and to traverse the inverse
 * map to discover the shape.
 * 
 * @see ClipMap
 * @see DecimateMap
 * @see FlattenMap
 * @see SliceMap
 * @see TransposeMap
 * @see MultiArrayProxy
 * @see IntArrayAdapter
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public interface
IntMap
{
	/**
	 * Returns the value to which this Map maps the specified key.
	 * If you think of this as a 1-d  array of int, then 
	 * ia.get(ii) is like ia[ii].
	 * @param key int
	 * @return int value
	 */
	public int
	get(int key);

	/**
	 * Returns the number of key-value mappings in this Map.
	 * If you think of this as a 1-d  array of int, then 
	 * ia.size() is like ia.length.
	 * @return int size
	 */
	public int
	size();

	/**
	 * Return the tail of a chain of IntMap.
	 * As side effects, connect the prev members and
	 * initialize the rank at the tail.
	 */
	public IntArrayAdapter
	tail(int rank, Object prev);

	/**
	 * Traverse the inverse mapping chain to
	 * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii);
}
