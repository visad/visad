/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

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
 *  As of this writing (jdk1.1),
 *  the rmi compiler <code>rmic</code> is braindead in the
 *  sense that it doesn't recognize that java.rmi.RemoteException isa
 *  java.io.IOException. Hence, we add the extraneous RemoteException
 *  to each <code>throws</code> clause to make it happy.
 *  Ann Wollrath @ JavaSoft says this will be fixed in jdk1.2.
 *
 * @see AbstractAccessor
 * @see MultiArray
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:05 $
 */
public interface
Accessor
	extends Remote
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
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a boolean.
	 * @see Accessor#get
	 */
	public boolean
	getBoolean(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a char.
	 * @see Accessor#get
	 */
	public char
	getChar(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a byte.
	 * @see Accessor#get
	 */
	public byte
	getByte(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a short.
	 * @see Accessor#get
	 */
	public short
	getShort(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as an int.
	 * @see Accessor#get
	 */
	public int
	getInt(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a long.
	 * @see Accessor#get
	 */
	public long
	getLong(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a float.
	 * @see Accessor#get
	 */
	public float
	getFloat(int [] index)
		throws IOException, RemoteException;

	/**
	 * Get the array element at index, as a double.
	 * @see Accessor#get
	 */
	public double
	getDouble(int [] index)
		throws IOException, RemoteException;

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
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified boolean value.
	 * @see Accessor#set
	 */
	public void
	setBoolean(int [] index, boolean value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified char value.
	 * @see Accessor#set
	 */
	public void
	setChar(int [] index, char value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified byte value.
	 * @see Accessor#set
	 */
	public void
	setByte(int [] index, byte value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified short value.
	 * @see Accessor#set
	 */
	public void
	setShort(int [] index, short value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified int value.
	 * @see Accessor#set
	 */
	public void
	setInt(int [] index, int value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified long value.
	 * @see Accessor#set
	 */
	public void
	setLong(int [] index, long value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified float value.
	 * @see Accessor#set
	 */
	public void
	setFloat(int [] index, float value)
		throws IOException, RemoteException;

	/**
	 * Set the array element at index to the specified double value.
	 * @see Accessor#set
	 */
	public void
	setDouble(int [] index, double value)
		throws IOException, RemoteException;

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
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException, RemoteException;

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
		throws IOException, RemoteException;

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
	 */
	public Object
	toArray()
		throws IOException, RemoteException;
	

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
	 */
	public Object
	toArray(Object anArray, int [] origin, int [] shape)
		throws IOException, RemoteException;

}
