// $Id: MultiArrayImpl.java,v 1.3 2003-02-03 20:09:04 donm Exp $
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
import java.io.Serializable;

/**
 * A concrete, space efficent implementation of the MultiArray interface.
 *
 * @see MultiArray
 *
 * @author $Author: donm $
 * @version $Revision: 1.3 $ $Date: 2003-02-03 20:09:04 $
 */
public class
MultiArrayImpl
	implements MultiArray, Cloneable, Serializable
{

	/**
	 * Used to figure out how storage is required for a
	 * given shape.
	 * Compute the right to left multiplicative product of the
	 * argument.
	 * @return int product of the dimensions.
	 * @param dimensions the shape.
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
	 * @return int product of the dimensions.
	 * @param dimensions the shape.
	 * @param products modified upon return to contain
	 *	the intermediate products
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
	 * @param componentType Class of the primitives or objects to
	 * be contained.
	 * @param dimensions the shape of the MultiArray.
	 * dimensions.length determines the rank of the new MultiArray.
	 */
	public
	MultiArrayImpl(Class componentType, int [] dimensions)
	{
		lengths = (int []) dimensions.clone();
		products = new int[dimensions.length];
		int product = numberOfElements(dimensions, products);
		/*
		 * Use array of length 1 for scalar storage
		 */
		if(product == 0)
			product = 1; 
		storage = Array.newInstance(componentType, product);
	}


	/**
	 * A copy constructor.
	 * <p>
	 * Create a new MultiArray with the same componentType and shape
	 * as the argument
	 * Storage for values is allocated and owned by this, and the values
	 * are initialized to the values of the argument.
	 * @param ma the MultiArray to copy.
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
	 * Create a new MultiArrayImpl of the given shape accessing
	 * externally created storage. It is up to the client to
	 * to mitigate conflicting access to the external storage.
	 * 
	 * @param lengths the shape of the MultiArray.
	 * @param storage array Object which is storage
	 */
	public
	MultiArrayImpl(int [] lengths, Object storage)
	{
		this.lengths = lengths;
		this.products = new int[lengths.length];
		final int length = numberOfElements(this.lengths,
				this.products);
		if(length > Array.getLength(storage))
			throw new IllegalArgumentException(
				"Inadequate storage");
		this.storage = storage;
	}

	/**
	 * Create a new MultiArrayImple of the given shape accessing
	 * externally created storage. It is up to the client to
	 * to mitigate conflicting access to the external storage.
	 * Should be a protected constructor?
	 * @param lengths describing the shape of the MultiArray.
	 * @param products right-to-left accumulated sizes.
	 * @param storage array Object which is storage
	 */
	public
	MultiArrayImpl(int [] lengths, int [] products, Object storage)
	{
		this.lengths = lengths;
		this.products = products;
		this.storage = storage;
	}


 /* MultiArray Inquiry methods from MultiArrayInfo */

	/**
	 * @see MultiArrayInfo#getComponentType
	 */
	public Class getComponentType()
	{
		 return storage.getClass().getComponentType();
	}

	/**
	 * @see  MultiArrayInfo#getRank
	 */
	public int getRank() { return lengths.length;}

	/**
	 * @see MultiArrayInfo#getLengths
	 */
	public int [] getLengths() {
		return (int []) lengths.clone();
	}

	/**
	 * @see MultiArrayInfo#isUnlimited
	 */
	public boolean isUnlimited() { return false; }

	/**
	 * @see MultiArrayInfo#isScalar
	 */
	public boolean isScalar() { return 0 == getRank(); }

 /* End MultiArrayInfo */
 /* MultiArray Access methods from Accessor */

	/**
	 * @see Accessor#get
	 */
	public Object get(int [] index)
	{
		return Array.get(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getBoolean
	 */
	public boolean getBoolean(int[] index)
	{
		return Array.getBoolean(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getChar
	 */
	public char getChar(int[] index)
	{
		return Array.getChar(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getByte
	 */
	public byte getByte(int[] index)
	{
		return Array.getByte(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getShort
	 */
	public short getShort(int[] index)
	{
		return Array.getShort(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getInt
	 */
	public int getInt(int[] index)
	{
		return Array.getInt(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getLong
	 */
	public long getLong(int[] index)
	{
		return Array.getLong(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getFloat
	 */
	public float getFloat(int[] index)
	{
		return Array.getFloat(storage, indexMap(index));
	}

	/**
	 * @see Accessor#getDouble
	 */
	public double getDouble(int[] index)
	{
		return Array.getDouble(storage, indexMap(index));
	}

	/**
	 * @see Accessor#set
	 */
	public void set(int [] index, Object value)
	{
		Array.set(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setBoolean
	 */
	public void setBoolean(int [] index, boolean value)
	{
		Array.setBoolean(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setChar
	 */
	public void setChar(int [] index, char value)
	{
		Array.setChar(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setByte
	 */
	public void setByte(int [] index, byte value)
	{
		Array.setByte(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setShort
	 */
	public void setShort(int [] index, short value)
	{
		Array.setShort(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setInt
	 */
	public void setInt(int [] index, int value)
	{
		Array.setInt(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setLong
	 */
	public void setLong(int [] index, long value)
	{
		Array.setLong(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setFloat
	 */
	public void setFloat(int [] index, float value)
	{
		Array.setFloat(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#setDouble
	 */
	public void setDouble(int[] index, double value)
	{
		Array.setDouble(storage, indexMap(index), value);
	}

	/**
	 * @see Accessor#copyout
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
	{
		if(origin.length != lengths.length
				|| shape.length != lengths.length)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		int ji = lengths.length -1 ;
		for(; ji >= 0; ji--)
		{
			if(origin[ji] != 0 || shape[ji] != lengths[ji])
				break;
		}
		if(ji < 0)
		{
			// origin is zero vector && target shape same as this
			return (MultiArrayImpl) this.clone();
		}
		// else
		// ji is the index where a maximum contiguous copy can occur
		final int [] shp = (int []) shape.clone();
		final int [] pducts = new int[shp.length];
		final int product = numberOfElements(shp, pducts);
		final Object dst = Array.newInstance(getComponentType(),
				product);
		int src_pos = indexMap(origin);
		if(ji == 0)
		{
			// No loop required
			System.arraycopy(storage, src_pos,
				dst, 0, product);
		}
		else
		{
			ji--;
			final int step = products[ji];
			final int contig = pducts[ji];
			for(int dst_pos = 0; dst_pos < product;
				dst_pos += contig)
			{
				System.arraycopy(storage, src_pos,
					dst, dst_pos, contig);
				src_pos += step;
			}
		}

		return new MultiArrayImpl(shp, pducts,
			dst);
	}

	/**
	 * Version <code>copyin</code> specialized and optimized for
	 * MultiArrayImpl.
	 * 
	 * @see Accessor#copyin
	 */
	public void
	copyin(int [] origin, MultiArrayImpl src)
	{
		if(origin.length != lengths.length
				|| src.getRank() != lengths.length)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		int ji = lengths.length -1 ;
		for(; ji >= 0; ji--)
		{
			if(origin[ji] != 0 || src.lengths[ji] != lengths[ji])
				break;
		}
		if(ji < 0)
		{
			// origin is zero vector && src shape same as this
			System.arraycopy(src.storage, 0,
				storage, 0,
				Array.getLength(storage));
			return;
		}
		// else
		// ji is the index where a maximum contiguous copy can occur
		int dst_pos = indexMap(origin);
		if(ji == 0)
		{
			// No loop required
			System.arraycopy(src.storage, 0,
				storage, dst_pos,
				Array.getLength(storage) - dst_pos);
			return;
		}
		// else
		{
			ji--;
			final int step = products[ji];
			final int contig = src.products[ji];
			final int src_length = Array.getLength(src.storage);
			for(int src_pos = 0; src_pos < src_length;
				src_pos += contig)
			{
				System.arraycopy(src.storage, src_pos,
					storage, dst_pos, contig);
				dst_pos += step;
			}
		}
	}

	/**
	 * @see Accessor#copyin
	 */
	public void
	copyin(int [] origin, MultiArray data)
		throws IOException
	{
		if(data instanceof MultiArrayImpl)
		{
			copyin(origin, (MultiArrayImpl)data);
			return;
		}
		// else
		if(origin.length != lengths.length
				|| data.getRank() != lengths.length)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		if(data.getComponentType() != getComponentType())
			throw new ArrayStoreException();
		// else
		AbstractAccessor.copy(data, data.getLengths(), this, origin);
	}

	static public Object
	fixDest(Object dst, int lengthNeeded, Class defaultComponentType)
	{
		if(dst == null || Array.getLength(dst) < lengthNeeded)
		{
			final Class ct = (dst == null ?
					defaultComponentType
					: dst.getClass().getComponentType());
			dst = Array.newInstance(
				ct, lengthNeeded);
		}
		return dst;
	}

	
	/**
	 * @see Accessor#getStorage
	 */
	public Object
	    getStorage() {
	    return storage;
	}






	/**
	 * @see Accessor#toArray
	 */
	public Object
	toArray()
	{
		// Clone storage. Would 
		// storage.getClass().getDeclaredMethod("clone", new Class [0])			//	.invoke(storage, new Object [0])
		// be better?
		final int length = Array.getLength(storage);
		final Object dst = Array.newInstance(getComponentType(),
			length);
		System.arraycopy(storage, 0, dst, 0, length);
		return dst;
	}

	/**
	 * @see Accessor#toArray
	 */
	public Object
	toArray(Object dst, int [] origin, int [] shape)
	{
		if(origin.length != lengths.length
				|| shape.length != lengths.length)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		int ji = lengths.length -1 ;
		for(; ji >= 0; ji--)
		{
			if(origin[ji] != 0 || shape[ji] != lengths[ji])
				break;
		}
		if(ji < 0)
		{
			final int length = Array.getLength(storage);
			dst = fixDest(dst, length, getComponentType());
			System.arraycopy(storage, 0,
				dst, 0, length);
			return dst;
		}

		// else
		// ji is the index where a maximum contiguous copy can occur
		final int [] shp = (int []) shape.clone();
		final int [] pducts = new int[shp.length];
		final int product = numberOfElements(shp, pducts);
		dst = fixDest(dst, product, getComponentType());

		int src_pos = indexMap(origin);
		if(ji == 0)
		{
			// No loop required
			System.arraycopy(storage, src_pos,
				dst, 0, product);
			return dst;
		}
		// else
		ji--;
		final int step = products[ji];
		final int contig = pducts[ji];
		for(int dst_pos = 0; dst_pos < product;
			dst_pos += contig)
		{
			System.arraycopy(storage, src_pos,
				dst, dst_pos, contig);
			src_pos += step;
		}
		return dst;
	}

 /* End Accessor */

	/**
	 * @see java.lang.Object#clone
	 */
	public Object
	clone()
	{
		return new MultiArrayImpl((int [])lengths.clone(),
			(int [])products.clone(), toArray());
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
	 * The actual storage. An array of componentType.
	 * This member is exposed so that System.arraycopy(), etc
	 * can be used directly on the storage.
	 * @serial
	 */
	public final Object storage;


	/**
	 * Right to left products used in indexMap() to compute
	 * offset into the array.
	 * When incrementing index[ii], one jumps through storage by
	 * products[ii].
	 * @serial
	 */
	private final int[] products;
	/**
	 * @serial
	 */
	private final int[] lengths;

 /* Begin Test */
	public static void
	main(String[] args)
	{
		final int [] shape = {48, 64};
		MultiArrayImpl src =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(src.storage,
					ii, ii);

		}
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

		MultiArrayImpl dest =
			new MultiArrayImpl(Integer.TYPE, shape);
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
