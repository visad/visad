/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;
import java.io.IOException;

/**
 * A concrete, space efficent implementation of the MultiArray interface.
 *
 * @see MultiArray
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class MultiArrayImpl implements MultiArray, Cloneable {

    /**
     * Used to figure out how storage is required for a
     * given shape.
     * Compute the right to left multiplicative product of the
     * argument.
     * @returns int product of the dimensions.
     * @param dimensions int [] describing the shape.
     */
    static public int
    numberOfElements(int [] dimensions)
    {
	int product = 1;
	for(int ii = dimensions.length -1; ii >= 0; ii--)
	{
		product *= dimensions[ii];
	}
	return product;
    }

    /**
     * Used to figure out how storage is required for a
     * given shape, retaining intermediate products.
     * Compute the right to left multiplicative product of the first
     * argument, modifying the second argument so that
     * it contains the intermediate products.
     * @returns int product of the dimensions.
     * @param dimensions int [] describing the shape.
     * @param products int [] modified upon return to contain the intermediate
     *		products
     */
    static public int
    numberOfElements(int [] dimensions, int [] products)
    {
	int product = 1;
	for(int ii = dimensions.length -1; ii >= 0; ii--)
	{
		final int thisDim = dimensions[ii];
		if(thisDim < 0)
			throw new NegativeArraySizeException();
		products[ii] = product;
		product *= thisDim;
	}
	return product;
    }

    /**
     * Create a new MultiArray of the given componentType and shape.
     * Storage for the values is allocated and owned by this with
     * default initialization.
     * @param theComponentType Class of the primitives or objects to
     * be contained.
     * @param dimensions int [] describing the shape of the MultiArray.
     * dimensions.length determines the rank of the new MultiArray.
     */
    public
    MultiArrayImpl(Class theComponentType, int [] dimensions)
    {
	lengths = (int []) dimensions.clone();
	products = new int[dimensions.length];
	int product = numberOfElements(dimensions, products);
	/*
	 * Use array of length 1 for scalar storage
	 */
	if(product == 0)
		product = 1; 
	storage = Array.newInstance(theComponentType, product);
    }


    /**
     * A copy constructor.
     * <p>
     * Create a new MultiArray with the same componentType and shape
     * as the argument
     * Storage for values is allocated and owned by this, and the values
     * are initialized to the values of the argument.
     * @param ma The MultiArray to copy.
     */
    public
    MultiArrayImpl(MultiArray ma)
	throws IOException
    {
	lengths = (int []) ma.getLengths().clone();
	products = new int[lengths.length];
	int product = numberOfElements(lengths, products);
	/*
	 * Use array of length 1 for scalar storage
	 */
	if(product == 0)
		product = 1; 
	storage = Array.newInstance(ma.getComponentType(), product);

		IndexIterator odo = new IndexIterator(lengths);
		for(; odo.notDone(); odo.incr())
		{
			final int [] index = odo.value();	
			this.set(index, ma.get(index));
		}
    }

    /**
     * Create a new MultiArrayImple of the given shape accessing
     * externally created storage. It is up to the client to
     * to mitigate conflicting access to the external storage.
     * Should be a protected constructor.
     * @param lengths int [] describing the shape of the MultiArray.
     * @param products int [] right-to-left accumulated sizes.
     * @param storage array Object which is storage
     */
    public
    MultiArrayImpl(int [] lengths, int [] products, Object storage)
    {
	this.lengths = lengths;
	this.products = products;
	this.storage = storage;
    }


 /* Inquiry methods from MultiArray */

    /**
     * @see MultiArray#getComponentType
     */
    public Class getComponentType()
    {
	 return storage.getClass().getComponentType();
    }

    /**
     * @see MultiArray#getRank
     */
    public int getRank() { return lengths.length;}

    /**
     * @see MultiArray#getLengths
     */
    public int [] getLengths() {
	return (int []) lengths.clone();
    }

    /**
     * @see MultiArray#isUnlimited
     */
    public boolean isUnlimited() { return false; }

    /**
     * @see MultiArray#isScalar
     */
    public boolean isScalar() { return 0 == getRank(); }

 /* Access methods from MultiArray */

    /**
     * @see MultiArray#get
     */
    public Object get(int [] index)
    {
	return Array.get(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getBoolean
     */
    public boolean getBoolean(int[] index)
    {
	return Array.getBoolean(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getChar
     */
    public char getChar(int[] index)
    {
	return Array.getChar(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getByte
     */
    public byte getByte(int[] index)
    {
	return Array.getByte(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getShort
     */
    public short getShort(int[] index)
    {
	return Array.getShort(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getInt
     */
    public int getInt(int[] index)
    {
	return Array.getInt(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getLong
     */
    public long getLong(int[] index)
    {
	return Array.getLong(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getFloat
     */
    public float getFloat(int[] index)
    {
	return Array.getFloat(storage, indexMap(index));
    }

    /**
     * @see MultiArray#getDouble
     */
    public double getDouble(int[] index)
    {
	return Array.getDouble(storage, indexMap(index));
    }

    /**
     * @see MultiArray#set
     */
    public void set(int [] index, Object value)
    {
	Array.set(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setBoolean
     */
    public void setBoolean(int [] index, boolean value)
    {
	Array.setBoolean(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setChar
     */
    public void setChar(int [] index, char value)
    {
	Array.setChar(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setByte
     */
    public void setByte(int [] index, byte value)
    {
	Array.setByte(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setShort
     */
    public void setShort(int [] index, short value)
    {
	Array.setShort(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setInt
     */
    public void setInt(int [] index, int value)
    {
	Array.setInt(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setLong
     */
    public void setLong(int [] index, long value)
    {
	Array.setLong(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setFloat
     */
    public void setFloat(int [] index, float value)
    {
	Array.setFloat(storage, indexMap(index), value);
    }

    /**
     * @see MultiArray#setDouble
     */
    public void setDouble(int[] index, double value)
    {
	Array.setDouble(storage, indexMap(index), value);
    }

	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException
	{
		throw new RuntimeException("Not Yet Implemented");
	}

	public void
	copyin(int [] origin, MultiArray data)
		throws IOException
	{
		throw new RuntimeException("Not Yet Implemented");
	}

/****/

    /**
     * Convert index vector into integer index into storage.
     */
    public int
    indexMap(int [] index)
    {
	int value = 0;
	for(int ii = 0; ii < lengths.length; ii++)
	{
		final int thisIndex = index[ii];
		if( thisIndex < 0 || thisIndex >= lengths[ii])
			 throw new ArrayIndexOutOfBoundsException();
		value += thisIndex * products[ii];
	}
	return value;
    }

    /**
     * How much to move in storage
     * when incrementing index at whichDim.
    public int
    stride(int whichDim)
    {
	return products[whichDim];
    }
     */

    /**
     * The actual storage. An array of componentType.
     * This member is exposed so that System.arraycopy(), etc
     * can be used for directly on the storage.
     */
    public final Object storage;

    /**
     * Right to left products used in indexMap() to compute
     * offset into the array.
     * When incrementing index[ii], one jumps through storage by
     * products[ii].
     */
    private final int[] products;
    private final int[] lengths;
}
