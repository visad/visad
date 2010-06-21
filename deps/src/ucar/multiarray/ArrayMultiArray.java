// $Id: ArrayMultiArray.java,v 1.3 2003-02-03 20:09:03 donm Exp $
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
import java.lang.reflect.Array;
import java.io.IOException;

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
 * @author $Author: donm $
 * @version $Revision: 1.3 $ $Date: 2003-02-03 20:09:03 $
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
public class
ArrayMultiArray
	implements MultiArray
{

	/**
	 * Package private constructor which avoids some of the
	 * protections of the public constructor below.
	 */ 
	ArrayMultiArray(Object aro, int theRank, Class componentType)
	{
		jla = aro;
		rank = theRank;
		this.componentType = componentType;
	}

	/**
	 * Given a java Object, typically an array of primitive
	 * (or an array of array of primitive ...), Provide a MultiArray
	 * interface to the provided object.
	 * The wrapper provided does not copy the object, so it remains
	 * accessable via the language [] notation
	 * and the java.lang.reflect.Array methods.
	 * Synchronization not provided by this interface.
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
		jla = aro;
		rank = rank_;
		componentType = componentType_;
	}

	/**
	 * Create a new MultiArray of the given componentType and shape.
	 * Storage for the values is allocated and owned by this with
	 * default initialization.
	 * @param componentType Class of the primitives or objects to
	 * be contained.
	 * @param dimensions the shape of the MultiArray.
	 * dimensions.length determines the rank of the new MultiArray.
	 */
	public
	ArrayMultiArray(Class componentType, int [] dimensions)
	{
		rank = dimensions.length;
		if(rank == 0)
			 throw new IllegalArgumentException();
		this.componentType = componentType;
		jla = Array.newInstance(componentType, dimensions);
	}

	/**
	 * A copy constructor.
	 * <p>
	 * Create a new MultiArray with the same componentType and shape
	 * as the argument
	 * @param ma the MultiArray to copy.
	 */
	public
	ArrayMultiArray(MultiArray ma)
		throws IOException
	{
		rank = ma.getRank();
		if(rank == 0)
			 throw new IllegalArgumentException();
		componentType = ma.getComponentType();
		final int [] lengths = ma.getLengths();
		jla = Array.newInstance(componentType, lengths);

		IndexIterator odo = new IndexIterator(lengths);
		for(; odo.notDone(); odo.incr())
		{
			final int [] index = odo.value();	
			this.set(index, ma.get(index));
		}
	}

 /* Begin MultiArray Inquiry methods from MultiArrayInfo */

	/**
	 * Returns the Class object representing the component
	 * type of the wrapped array. If the rank is greater than
	 * 1, this will be the component type of the leaf (rightmost)
	 * nested array.
	 * @see MultiArrayInfo#getComponentType
	 * @return Class the component type
	 */
	public Class
	getComponentType() { return componentType; }

	/**
	 * @see MultiArrayInfo#getRank
	 * @return int number of dimensions of the array
	 */
	public int
	getRank() { return rank;}

	/**
	 * As if java.lang.reflect.Array.getLength() were called recursively
	 * on the wrapped object, return the dimension lengths.
	 * @see MultiArrayInfo#getLengths
	 *
	 * @return int array whose length is the rank of this
	 * MultiArray and whose elements represent the
	 * length of each of it's dimensions
	 */
	public int []
	getLengths() {
		int [] lengths = new int[rank];
		Object oo = jla;
		for(int ii = 0; ii < rank; ii++) {
			lengths[ii] = Array.getLength(oo);
			oo = Array.get(oo, 0);
		}
		return lengths;
	}

	/**
	 * Returns <code>true</code> if and only if the effective dimension
	 * lengths can change. Always returns <code>false</code> for this class.
	 * @see MultiArrayInfo#isUnlimited
	 * @return boolean <code>false</code>
	 */
	public boolean
	isUnlimited() { return false; }

	/**
	 * Always returns false for this class.
	 * @see MultiArrayInfo#isScalar
	 * @return false
	 */
	public boolean
	isScalar() { return rank == 0; }

 /* End MultiArrayInfo */
 /* Begin MultiArray Access methods from Accessor */

	/**
	 * @see Accessor#get
	 */
	public Object
	get(int [] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.get(oo, index[end]);
	}

	/**
	 * @see Accessor#getBoolean
	 */
	public boolean
	getBoolean(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getBoolean(oo, index[end]);
	}

	/**
	 * @see Accessor#getChar
	 */
	public char
	getChar(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getChar(oo, index[end]);
	}

	/**
	 * @see Accessor#getByte
	 */
	public byte
	getByte(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getByte(oo, index[end]);
	}

	/**
	 * @see Accessor#getShort
	 */
	public short
	getShort(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getShort(oo, index[end]);
	}

	/**
	 * @see Accessor#getInt
	 */
	public int
	getInt(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getInt(oo, index[end]);
	}

	/**
	 * @see Accessor#getLong
	 */
	public long
	getLong(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getLong(oo, index[end]);
	}

	/**
	 * @see Accessor#getFloat
	 */
	public float
	getFloat(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getFloat(oo, index[end]);
	}

	/**
	 * @see Accessor#getDouble
	 */
	public double
	getDouble(int[] index)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.getDouble(oo, index[end]);
	}

	/**
	 * @see Accessor#set
	 */
	public void
	set(int [] index, Object value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.set(oo, index[end], value);
		return;
	}

	/**
	 * @see Accessor#setBoolean
	 */
	public void
	setBoolean(int [] index, boolean value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setBoolean(oo, index[end], value);
	}

	/**
	 * @see Accessor#setChar
	 */
	public void
	setChar(int [] index, char value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setChar(oo, index[end], value);
	}

	/**
	 * @see Accessor#setByte
	 */
	public void
	setByte(int [] index, byte value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setByte(oo, index[end], value);
	}

	/**
	 * @see Accessor#setShort
	 */
	public void
	setShort(int [] index, short value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setShort(oo, index[end], value);
	}

	/**
	 * @see Accessor#setInt
	 */
	public void
	setInt(int [] index, int value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setInt(oo, index[end], value);
	}

	/**
	 * @see Accessor#setLong
	 */
	public void
	setLong(int [] index, long value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setLong(oo, index[end], value);
	}

	/**
	 * @see Accessor#setFloat
	 */
	public void
	setFloat(int [] index, float value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setFloat(oo, index[end], value);
	}

	/**
	 * @see Accessor#setDouble
	 */
	public void
	setDouble(int[] index, double value)
	{
		if(index.length < rank)
			 throw new IllegalArgumentException();
		final int end = rank -1;
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		Array.setDouble(oo, index[end], value);
	}

	/**
	 * @see Accessor#copyout
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
	{
		if(origin.length != rank
				|| shape.length != rank)
			throw new IllegalArgumentException("Rank Mismatch");
		final int [] shp = (int []) shape.clone();
		final int [] pducts = new int[shp.length];
		final int product = MultiArrayImpl.numberOfElements(shp,
				pducts);
		final Object dst = Array.newInstance(getComponentType(),
				product);
		int ji = rank -1;
		int src_pos = origin[ji];
		if(ji == 0)
		{
			// rank == 1
			// No loop required
			System.arraycopy(jla, src_pos,
				dst, 0, product);
		}
		else
		{
			ji--;
			final int contig = pducts[ji];
			final OffsetIndexIterator odo =
				new OffsetIndexIterator(truncCopy(origin),
					getTruncLengths());
			for(int dst_pos = 0; dst_pos < product;
				dst_pos += contig)
			{
				System.arraycopy(getLeaf(odo.value()), src_pos,
					dst, dst_pos, contig);
				odo.incr();
			}
		}

		return new MultiArrayImpl(shp, pducts,
			dst);
	}

	/* TODO: specialize & optimise? */
	/**
	 * @see Accessor#copyin
	 */
	public void
	copyin(int [] origin, MultiArray data)
		throws IOException
	{
		if(origin.length != rank
				|| data.getRank() != rank)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		if(data.getComponentType() != componentType)
			throw new ArrayStoreException();
		// else
		AbstractAccessor.copy(data, data.getLengths(), this, origin);
	}

	/**
	 * @see Accessor#toArray
	 */
	public Object
	toArray()
	{
		return this.toArray(null, null, null);
	}

	public Object getStorage () {
	    return jla;
	}

	/**
	 * @see Accessor#toArray
	 */
	public Object
	toArray(Object dst, int [] origin, int [] shape)
	{
		if(origin == null)
			origin = new int[rank];
		else if(origin.length != rank)
			throw new IllegalArgumentException("Rank Mismatch");

		int [] shp = null;
		if(shape == null)
			shp = getLengths();
		else if(shape.length == rank)
			shp = (int []) shape.clone();
		else
			throw new IllegalArgumentException("Rank Mismatch");

		final int [] pducts = new int[shp.length];
		final int product = MultiArrayImpl.numberOfElements(shp,
				pducts);
		dst = MultiArrayImpl.fixDest(dst, product, componentType);

		int ji = rank -1;
		int src_pos = origin[ji];
		if(ji == 0)
		{
			// rank == 1
			// No loop required
			System.arraycopy(jla, src_pos,
				dst, 0, product);
		}
		else
		{
			ji--;
			final int contig = pducts[ji];
			final OffsetIndexIterator odo =
				new OffsetIndexIterator(truncCopy(origin),
					getTruncLengths());
			for(int dst_pos = 0; dst_pos < product;
				dst_pos += contig)
			{
				System.arraycopy(getLeaf(odo.value()),
					src_pos,
					dst, dst_pos, contig);
				odo.incr();
			}
		}
		return dst;
	}

 /* End Accessor */

	static int []
	truncCopy(int [] src)
	{
		final int len = src.length -1;
		int [] dst = new int [len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	/**
	 * Peel the array by fixing the leftmost index value
	 * to the argument.  Reduces rank by 1.
	 * If the result would be of primitive type,
	 * it is appropriately wrapped.
	 * @return Object value at <code>index</code>
	 */
	public Object
	get(int index)
	{
		if(rank == 1)
			return Array.get(jla, index);
		// else
		return new ArrayMultiArray(Array.get(jla, index),
			rank -1,
			componentType);
	}

	/**
	 * Get the leaf array at Index.
	 */
	public Object
	getLeaf(int [] index)
	{
		final int end = rank -2;
		if(index.length <= end)
			 throw new IllegalArgumentException();
		Object oo = jla;
		for(int ii = 0 ; ii < end; ii++)
			oo = Array.get(oo, index[ii]);
		return Array.get(oo, index[end]);
	}

	/**
	 * @return int array whose length is the rank of this minus one
	 * MultiArray and whose elements represent the
	 * length of each of it's leading dimensions
	 */
	private int []
	getTruncLengths() {
		final int containRank = rank - 1;
		int [] lengths = new int[containRank];
		Object oo = jla;
		for(int ii = 0; ii < containRank; ii++) {
			lengths[ii] = Array.getLength(oo);
			oo = Array.get(oo, 0);
		}
		return lengths;
	}


	/**
	 * The java language array which this adapts.
	 */
	public final Object jla;
	private final int rank;
	private final Class componentType;

 /* Begin Test */
	public static void
	main(String[] args)
	{
			System.out.println(">>  " + System.currentTimeMillis());
		final int [] shape = {48, 64};
		MultiArrayImpl init =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(init.storage,
					ii, ii);

		}

		ArrayMultiArray src = (ArrayMultiArray) null;
		try {
			src = new ArrayMultiArray(init);
		}
		catch (java.io.IOException ee) {}

		int [] clip = new int[] {32, 64};
		int [] origin = new int[] {8, 0};
		MultiArray ma = src.copyout(origin, clip);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {0, 0}));
			System.out.println(ma.getInt(new int[] {1, 0}));
			System.out.println(ma.getInt(new int[] {lengths[0] -1,								 lengths[1] -1}));
		}
		catch (java.io.IOException ee) {}

		clip = new int[] {48, 48};
		origin = new int[] {0, 8};
		ma = src.copyout(origin, clip);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {0, 0}));
			System.out.println(ma.getInt(new int[] {1, 0}));
			System.out.println(ma.getInt(new int[] {lengths[0] -1,								 lengths[1] -1}));
		}
		catch (java.io.IOException ee) {}

		ArrayMultiArray dest =
			new ArrayMultiArray(Integer.TYPE, shape);
		try {
			dest.copyin(origin, ma);
			System.out.println("***Rank  " + dest.getRank());
			int [] lengths = dest.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(dest.getInt(new int[] {0, 0}));
			System.out.println(dest.getInt(new int[] {0, 7}));
			System.out.println(dest.getInt(new int[] {0, 8}));
			System.out.println(dest.getInt(new int[] {47, 55}));
			System.out.println(dest.getInt(new int[] {47, 56}));
			System.out.println(dest.getInt(new int[] {47, 63}));
		}
		catch (java.io.IOException ee) {}

	}
 /* End Test */
}
