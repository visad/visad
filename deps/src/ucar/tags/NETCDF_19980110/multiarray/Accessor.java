/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
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
 *  Since the implementations may be file based or remote,
 *  the operations may throw java.io.IOException.
 *
 * @see AbstractAccessor
 * @see MultiArray
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
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
	 * @param index int [] MultiArray index
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
	 * @param index int [] MultiArray index
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

	/*
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
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException;

	/*
	 * Aggregate write access.
	 * Given a MultiArray, copy it into this.
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

}
