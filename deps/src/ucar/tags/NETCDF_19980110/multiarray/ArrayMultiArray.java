/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * MultiArray implementation which is an adapter for java language arrays.
 * If you have a java array and want to wrap it
 * in a MultiArray interface, use this class.
 * Rank of these is always > 0, use ScalarMultiArray
 * for scalars.
 * <p>
 * The set, setXXX, get, getXXX methods use the
 * corresponding methods from java.lang.reflect.Array,
 * the conversion and exception characteristics of the methods
 * here are like the ones found there.
 *
 * @see java.lang.reflect.Array
 * @see MultiArray
 * @see ScalarMultiArray
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
/*
 * Implementation note:
 * It has been suggested that we "factor common code in the get/set
 * methods". 
 *
 *   public int getInt(int[] index)
 *   {
 *	return get(index).intValue();
 *   }
 *
 * This is probably desireable from a maintenance or clarity
 * point of view.
 * One would need to prove that this is not desireable from
 * from a performance point of view to justify leaving it
 * the way it is.
 * For now, it seems to work the way it is, so I'm leaving it.
 * Think of it as having been machine generated.
 */
public class ArrayMultiArray implements MultiArray {

    /**
     * Package private constructor which avoids some of the
     * protections of the public constructor below.
     */ 
    ArrayMultiArray(Object aro, int theRank, Class theComponentType)
    {
	obj = aro;
	rank = theRank;
	componentType = theComponentType;
    }

    /**
     * Given a java Object, typically an array of primitive
     * (or an array of array of primitive ...), Provide a MultiArray
     * interface to the provided object.
     * The wrapper provided does not copy the object, so it remained
     * accessable via the language [] notation and the java.lang.reflect.Array
     * methods. Synchronization not provided by this interface.
     * @param aro a (multi-dimensional) array of primitives.
     */
    public
    ArrayMultiArray(Object aro) {
	int rank_ = 0;
	Class componentType_ = aro.getClass();
	while(componentType_.isArray())
	{
		rank_++;
		componentType_ = componentType_.getComponentType();
	}
	if(rank_ == 0)
		 throw new IllegalArgumentException();
	obj = aro;
	rank = rank_;
	componentType = componentType_;
    }

    /**
     * Returns the Class object representing the component
     * type of the wrapped array. If the rank is greater than
     * 1, this will be the component type of the leaf (rightmost)
     * nested array.
     * @see MultiArray#getComponentType
     * @return Class the component type
     */
    public Class getComponentType() { return componentType; }

    /**
     * @see MultiArray#getRank
     * @return int number of dimensions of the array
     */
    public int getRank() { return rank;}

    /**
     * As if java.lang.reflect.Array.getLength() were called recursively
     * on the wrapped object, return the dimension lengths.
     * @see MultiArray#getLengths
     *
     * @return int array whose length is the rank of this
     * MultiArray and whose elements represent the
     * length of each of it's dimensions
     */
    public int [] getLengths() {
	int [] lengths = new int[rank];
	Object oo = obj;
	for(int ii = 0; ii < rank; ii++) {
		lengths[ii] = Array.getLength(oo);
		oo = Array.get(oo, 0);
	}
	return lengths;
    }

    /**
     * Returns <code>true</code> if and only if the effective dimension
     * lengths can change. Always returns <code>false</code> for this class.
     * @see MultiArray#isUnlimited
     * @return boolean <code>false</code>
     */
    public boolean isUnlimited() { return false; }

    /**
     * Always returns false for this class.
     * @see MultiArray#isScalar
     */
    public boolean isScalar() { return rank == 0; }

/****/

    /**
     * @see MultiArray#get
     */
    public Object get(int [] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.get(oo, index[end]);
    }

    /**
     * @see MultiArray#getBoolean
     */
    public boolean getBoolean(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getBoolean(oo, index[end]);
    }

    /**
     * @see MultiArray#getChar
     */
    public char getChar(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getChar(oo, index[end]);
    }

    /**
     * @see MultiArray#getByte
     */
    public byte getByte(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getByte(oo, index[end]);
    }

    /**
     * @see MultiArray#getShort
     */
    public short getShort(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getShort(oo, index[end]);
    }

    /**
     * @see MultiArray#getInt
     */
    public int getInt(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getInt(oo, index[end]);
    }

    /**
     * @see MultiArray#getLong
     */
    public long getLong(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getLong(oo, index[end]);
    }

    /**
     * @see MultiArray#getFloat
     */
    public float getFloat(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getFloat(oo, index[end]);
    }

    /**
     * @see MultiArray#getDouble
     */
    public double getDouble(int[] index)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	return Array.getDouble(oo, index[end]);
    }

    /**
     * @see MultiArray#set
     */
    public void set(int [] index, Object value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.set(oo, index[end], value);
    	return;
    }

    /**
     * @see MultiArray#setBoolean
     */
    public void setBoolean(int [] index, boolean value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setBoolean(oo, index[end], value);
    }

    /**
     * @see MultiArray#setChar
     */
    public void setChar(int [] index, char value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setChar(oo, index[end], value);
    }

    /**
     * @see MultiArray#setByte
     */
    public void setByte(int [] index, byte value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setByte(oo, index[end], value);
    }

    /**
     * @see MultiArray#setShort
     */
    public void setShort(int [] index, short value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setShort(oo, index[end], value);
    }

    /**
     * @see MultiArray#setInt
     */
    public void setInt(int [] index, int value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setInt(oo, index[end], value);
    }

    /**
     * @see MultiArray#setLong
     */
    public void setLong(int [] index, long value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setLong(oo, index[end], value);
    }

    /**
     * @see MultiArray#setFloat
     */
    public void setFloat(int [] index, float value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setFloat(oo, index[end], value);
    }

    /**
     * @see MultiArray#setDouble
     */
    public void setDouble(int[] index, double value)
    {
	if(index.length < rank)
		 throw new IllegalArgumentException();
	final int end = rank -1;
	Object oo = obj;
	for(int ii = 0 ; ii < end; ii++)
		oo = Array.get(oo, index[ii]);
	Array.setDouble(obj, index[end], value);
    }

    /**
     * Peel the array by fixing the leftmost index value
     * to the argument.  Reduces rank by 1.
     * If the result would be of primitive type, it is appropriately wrapped.
     * @return Object value at <code>index</code>
     */
    public Object get(int index)
    {
	if(rank == 1)
		return Array.get(obj, index);
	// else
	return new ArrayMultiArray(Array.get(obj, index),
		rank -1,
		componentType);
    }

	public MultiArray
	copyout(int [] origin, int [] shape)
	{
		throw new RuntimeException("Not Yet Implemented");
	}

	public void
	copyin(int [] origin, MultiArray data)
	{
		throw new RuntimeException("Not Yet Implemented");
	}

    private final Object obj;
    private final int rank;
    private final Class componentType;
}
