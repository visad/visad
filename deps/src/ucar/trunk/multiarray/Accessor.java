// $Id: Accessor.java,v 1.3 2003-02-03 20:09:03 donm Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package ucar.multiarray;
import java.io.IOException;

/**
 *  Interface for multidimensional array data access.
 *  Given an index (array of integers), get or set the value
 *  at index.
 *  <p>
 *  Netcdf Variables implement this, but more general objects,
 *  such as java arrays, can be simply wrapped to provide
 *  this interface.
 *  <p>
 *  For lack of a better model, we use use naming conventions
 *  from java.lang.reflect.Array.
 *  In particular, we name the primitive specific "set" functions by type,
 *  rather than using overloading.
 *  This is symmetric with the "get" operations.
 *  <p>
 *  The primitive specific get and set methods are useful only if the
 *  the componentType is primitive (like java.lang.Double.TYPE).
 *  <p>
 *  Like java.lang.reflect.Array, classes that implement this
 *  interface should permit widening conversions to occur during a
 *  get or set operation, and throw IllegalArgumentException otherwise.
 *  Classes which implement this interface may be more lenient, however,
 *  only throwing the exception for narrowing conversions if
 *  the unconverted value is out of range for the target type.
 *  Implementations may throw UnsupportedOperationException,
 *	IllegalArgumentException, or ? for conversions to primitive
 *  which don't make sense.
 *  <p>
 *  The implementations may be file based or remote,
 *  so the methods throw java.io.IOException.
 *
 * @see AbstractAccessor
 * @see MultiArray
 * @see RemoteAccessor
 * @author $Author: donm $
 * @version $Revision: 1.3 $ $Date: 2003-02-03 20:09:03 $
 */
public interface
Accessor
{
	/**
	 * Get (read) the array element at index.
	 * The returned value is wrapped in an object if it
	 * has a primitive type.
	 * Length of index must be greater than or equal to the rank of this.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 * @param index MultiArray index
	 * @return Object value at <code>index</code>
	 * @exception NullPointerException If the argument is null.
	 * @exception IllegalArgumentException If the array length of index is
	 *	 too small
	 * @exception ArrayIndexOutOfBoundsException If an index component
	 *  argument is negative, or if it is greater than or equal to the
	 *  corresponding dimension length.
	 */
	public Object
	get(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a boolean.
	 * @see Accessor#get
	 */
	public boolean
	getBoolean(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a char.
	 * @see Accessor#get
	 */
	public char
	getChar(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a byte.
	 * @see Accessor#get
	 */
	public byte
	getByte(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a short.
	 * @see Accessor#get
	 */
	public short
	getShort(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as an int.
	 * @see Accessor#get
	 */
	public int
	getInt(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a long.
	 * @see Accessor#get
	 */
	public long
	getLong(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a float.
	 * @see Accessor#get
	 */
	public float
	getFloat(int [] index)
		throws IOException;

	/**
	 * Get the array element at index, as a double.
	 * @see Accessor#get
	 */
	public double
	getDouble(int [] index)
		throws IOException;

	/**
	 * Set (modify, write) the array element at index
	 * to the specified value.
	 * If the array has a primitive component type, the value may
	 * be unwrapped.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 * @param index MultiArray index
	 * @param value the new value.
	 * @exception NullPointerException If the index argument is null, or
	 * if the array has a primitive component type and the value argument is
	 * null
	 * @exception IllegalArgumentException If the array length of index is
	 *	 too small
	 * @exception ArrayIndexOutOfBoundsException If an index component
	 *  argument is negative, or if it is greater than or equal to the
	 *  corresponding dimension length.
	 */
	public void
	set(int [] index, Object value)
		throws IOException;

	/**
	 * Set the array element at index to the specified boolean value.
	 * @see Accessor#set
	 */
	public void
	setBoolean(int [] index, boolean value)
		throws IOException;

	/**
	 * Set the array element at index to the specified char value.
	 * @see Accessor#set
	 */
	public void
	setChar(int [] index, char value)
		throws IOException;

	/**
	 * Set the array element at index to the specified byte value.
	 * @see Accessor#set
	 */
	public void
	setByte(int [] index, byte value)
		throws IOException;

	/**
	 * Set the array element at index to the specified short value.
	 * @see Accessor#set
	 */
	public void
	setShort(int [] index, short value)
		throws IOException;

	/**
	 * Set the array element at index to the specified int value.
	 * @see Accessor#set
	 */
	public void
	setInt(int [] index, int value)
		throws IOException;

	/**
	 * Set the array element at index to the specified long value.
	 * @see Accessor#set
	 */
	public void
	setLong(int [] index, long value)
		throws IOException;

	/**
	 * Set the array element at index to the specified float value.
	 * @see Accessor#set
	 */
	public void
	setFloat(int [] index, float value)
		throws IOException;

	/**
	 * Set the array element at index to the specified double value.
	 * @see Accessor#set
	 */
	public void
	setDouble(int [] index, double value)
		throws IOException;

	/**
	 * Aggregate read access.
	 * Return a new MultiArray of the
	 * same componentType as this, and with shape as specified,
	 * which is initialized to the values of this, as 
	 * clipped to (origin, origin + shape).
	 * <p>
	 * It is easier to implement than to specify :-).
	 * <p>
	 * The main reason to implement this instead of using
	 * the equivalent proxy is for remote or file access.
	 * <p>
	 * <code>assert(origin[ii] + shape[ii] <= lengths[ii]);</code>
	 *
	 * @param origin int array specifying the starting index.
	 * @param shape  int array specifying the extents in each
	 *	dimension. This becomes the shape of the return.
	 * @return the MultiArray with the specified shape
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException;

	/**
	 * Aggregate write access.
	 * Given a MultiArray, copy it into this at the specified starting index.
	 * TODO: clearer specification.
	 * <p>
	 * Hopefully this member can be optimized in various situations.
	 * <p>
	 * <code>assert(origin[ii] + (source.getLengths())[ii]
		<= (getLengths())[ii]);</code>
	 *
	 * @param origin int array specifying the starting index.
	 * @param source  MultiArray with the same componentType as
	 *      this and shape smaller than
	 *	<code>this.getLengths() - origin</code>
	 */
	public void
	copyin(int [] origin, MultiArray source)
		throws IOException;

	/**
	 * Returns a new array containing all of the elements in this
	 * MultiArray. The returned array is one dimensional.
	 * The order of the elements in the result is natural,
	 * as if we used an IndexIterator to step through the elements
	 * of this MultiArray. The component type of the result is
	 * the same as this.
	 * <p>
     	 * This method acts as bridge between array-based and MultiArray-based
	 * APIs.
	 * <p>
	 * This method is functionally equivalent to
	 * <pre>
		Object anArray = Array.newInstance(getComponentType(), 1);
		int [] origin = new int[getRank()]
		int [] shape = getDimensions();
		return toArray(anArray, origin, shape);
	 * </pre>
	 *
	 * @return a one dimensional Array containing all the elements
	 * in this MultiArray
	 */
	public Object
	toArray()
		throws IOException;
	




	

	/**
	 * Returns an array containing elements of this
	 * MultiArray specified by origin and shape,
	 * possibly converting the component type.
	 * The returned array is one dimensional.
	 * The order of the elements in the result is natural,
	 * as if we used an IndexIterator to step through the elements
	 * of this MultiArray.
	 * <p>
	 * The anArray argument should be an array.
	 * If it is large enough to contain the output,
	 * it is used and no new storage is allocated.
	 * Otherwise, new storage is allocated with the
	 * same component type as the argument, and the data
	 * is copied into it.
	 * <p>
     	 * This method acts as bridge between array-based and MultiArray-based
	 * APIs.
	 * <p>
	 * This method is similar to copyout(origin, shape).toArray(),
	 * but avoids a copy operation and (potentially) an allocation.
	 * <p>
	 * NOTE: Implementation of type conversion is deferred until
	 * JDK 1.2. Currently, the componentType of <code>anArray</code>
	 * must be the same as <code>this</code>
	 *
	 * @return a one dimensional Array containing the specified elements
	 */
	public Object
	toArray(Object anArray, int [] origin, int [] shape)
		throws IOException;

}
