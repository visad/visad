/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.io.IOException;

/**
 * This MultiArray implementation wraps another MultiArray
 * and an IndexMap to provide a different view of the
 * wrapped MultiArray. Indices passed to access methods
 * are passed through a chain of mappings.
 *
 * @see MultiArray
 * @see IndexMap
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:06 $
 */
public class MultiArrayProxy implements MultiArray {

	/**
	 * Construct a new proxy.
	 * @param delegate MultiArray backing the proxy view provided.
	 * @param im IndexMap defining the proxy view.
	 */
	public
	MultiArrayProxy(MultiArray delegate, IndexMap im)
	{
		delegate_ = delegate;
		im_ = im;
		dlengths_ = delegate_.getLengths();
		im_.setLengths(dlengths_);
	}

 /* Begin MultiArrayInfo */

	/**
	 * Returns the Class object representing the component
	 * type of the array.
	 * @return Class The componentType
	 * @see java.lang.Class#getComponentType
	 */
	public Class
	getComponentType()
		{ return delegate_.getComponentType(); }

	/**
	 * Returns the number of dimensions of the backing MultiArray,
	 * as transformed by the <code>rankInverseMap()</code> method of
	 * the IndexMap.
	 * @return int number of dimensions
	 */
	public int
	getRank()
		{ return im_.getRank(); }

	/**
	 * Returns the shape of the backing MultiArray as transformed
	 * by the <code>dimensionsInverseMap()</code> method of
	 * the IndexMap.
	 * @return int array whose length is the rank of this
	 * MultiArray and whose elements represent the
	 * length of each of it's dimensions
	 */
	public int []
	getLengths()
	{
		if(isUnlimited())
		{
			// The delegate lengths might have changed.
			// This could better be handle by an event.
			System.arraycopy(delegate_.getLengths(), 0,
				dlengths_, 0, dlengths_.length);
		}
		final int [] lengths = new int[getRank()];
		return im_.getLengths(lengths);
	}

	/**
	 * Returns <code>true</code> if and only if the effective dimension
	 * lengths can change.
	 */
	public boolean
	isUnlimited()
		{ return delegate_.isUnlimited(); }

	/**
	 * Convenience interface; return <code>true</code>
	 * if and only if the rank is zero.
	 * @return boolean <code>true</code> iff rank == 0
	 */
	public boolean
	isScalar()
	{
		return 0 == this.getRank();
	}

 /* End MultiArrayInfo */
 /* Begin Accessor */

	/**
	 * @return Object value at <code>index</code>
	 * Length of index must equal rank() of this.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 */
	public Object
	get(int [] index)
		throws IOException
	{
		return delegate_.get(map(index));
	}

	public boolean
	getBoolean(int[] index)
		 throws IOException
	{
		return delegate_.getBoolean(map(index));
	}

	public char
	getChar(int[] index)
		 throws IOException
	{
		return delegate_.getChar(map(index));
	}

	public byte
	getByte(int[] index)
		 throws IOException
	{
		return delegate_.getByte(map(index));
	}

	public short
	getShort(int[] index)
		 throws IOException
	{
		return delegate_.getShort(map(index));
	}

	public int
	getInt(int[] index)
		 throws IOException
	{
		return delegate_.getInt(map(index));
	}

	public long
	getLong(int[] index)
		 throws IOException
	{
		return delegate_.getLong(map(index));
	}

	public float
	getFloat(int[] index)
		 throws IOException
	{
		return delegate_.getFloat(map(index));
	}

	public double
	getDouble(int[] index)
		 throws IOException
	{
		return delegate_.getDouble(map(index));
	}

	/**
	 * Length of index must equal rank() of this.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 */
	public void
	set(int [] index, Object value)
		throws IOException
	{
		delegate_.set(map(index), value);
	}

	public void
	setBoolean(int [] index, boolean value)
		 throws IOException
	{
		delegate_.setBoolean(map(index), value);
	}

	public void
	setChar(int [] index, char value)
		 throws IOException
	{
		delegate_.setChar(map(index), value);
	}

	public void
	setByte(int [] index, byte value)
		 throws IOException
	{
		delegate_.setByte(map(index), value);
	}

	public void
	setShort(int [] index, short value)
		 throws IOException
	{
		delegate_.setShort(map(index), value);
	}

	public void
	setInt(int [] index, int value)
		 throws IOException
	{
		delegate_.setInt(map(index), value);
	}

	public void
	setLong(int [] index, long value)
		 throws IOException
	{
		delegate_.setLong(map(index), value);
	}

	public void
	setFloat(int [] index, float value)
		 throws IOException
	{
		delegate_.setFloat(map(index), value);
	}

	public void
	setDouble(int[] index, double value)
		 throws IOException
	{
		delegate_.setDouble(map(index), value);
	}

	/**
	 * @see Accessor#copyout
	 */
	public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException
	{
		final int rank = getRank();
		if(origin.length != rank
				|| shape.length != rank)
			throw new IllegalArgumentException("Rank Mismatch");
		final MultiArrayImpl data = new MultiArrayImpl(
			getComponentType(),
			shape);
		AbstractAccessor.copyO(this, origin, data, shape);
		return data;
	}

	/**
	 * @see Accessor#copyin
	 */
	public void
	copyin(int [] origin, MultiArray data)
		throws IOException
	{
		final int rank = getRank();
		if(origin.length != rank
				|| data.getRank() != rank)
			throw new IllegalArgumentException("Rank Mismatch");
		// else
		if(data.getComponentType() != getComponentType())
			throw new ArrayStoreException();
		// else
		AbstractAccessor.copy(data, data.getLengths(), this, origin);
	}

	/**
	 * @see Accessor#toArray
	 * TODO: optimize?
	 */
	public Object
	toArray()
		throws IOException
	{
		return this.toArray(null, null, null);
	}

	/**
	 * @see Accessor#toArray
	 * TODO: optimize?
	 */
	public Object
	toArray(Object dst, int [] origin, int [] shape)
		throws IOException
	{
		final int rank = getRank();
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

		final int [] products = new int[rank];
		final int length = MultiArrayImpl.numberOfElements(shape,
			products);
		dst = MultiArrayImpl.fixDest(dst, length, getComponentType());
		final MultiArrayImpl data = new MultiArrayImpl(shape, products,
			dst);
		AbstractAccessor.copyO(this, origin, data, shape);
		return dst;
	}

 /* End Accessor */

	private synchronized int []
	map(int [] index)
	{
		final int [] converted = new int[dlengths_.length];
		return im_.transform(converted, index);
	}

	private final MultiArray delegate_;
	private final IndexMap im_;
	/**
	 * Storage of delegate dimension lengths
	 */
	private final int [] dlengths_;

 // TODO better test
 /* Begin Test */
	public static void
	main(String[] args)
	{
			System.out.println(">>  " + System.currentTimeMillis());
		final int [] shape = {48, 64};
		MultiArrayImpl delegate =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(delegate.storage,
					ii, ii);

		}
		IndexMap im = new ClipMap(0, 4, 40);
		MultiArray src = new MultiArrayProxy(delegate, im);

		int [] clip = new int[] {32, 64};
		int [] origin = new int[] {4, 0};
		MultiArray ma = (MultiArray) null;

		try {
			ma = src.copyout(origin, clip);
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {0, 0}));
			System.out.println(ma.getInt(new int[] {1, 0}));
			System.out.println(ma.getInt(new int[] {lengths[0] -1,								 lengths[1] -1}));
		}
		catch (java.io.IOException ee) {}

		MultiArrayImpl destD =
			new MultiArrayImpl(Integer.TYPE, shape);
		im = new ClipMap(0, 8, 36);
		MultiArray dest = new MultiArrayProxy(destD, im);
		try {
			origin = new int[] {0, 0};
			dest.copyin(origin, ma);
			System.out.println("***Rank  " + dest.getRank());
			int [] lengths = dest.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(destD.getInt(new int[] {0, 0}));
			System.out.println(destD.getInt(new int[] {7, 63}));
			System.out.println(destD.getInt(new int[] {8, 0}));
			System.out.println(destD.getInt(new int[] {8, 63}));
			System.out.println(destD.getInt(new int[] {9, 0}));
			System.out.println(destD.getInt(new int[] {39, 0}));
			System.out.println(destD.getInt(new int[] {40, 0}));
			System.out.println(destD.getInt(new int[] {47, 63}));
				
		}
		catch (java.io.IOException ee) {}

	}
 /* Test output java ucar.multiarray.MultiArrayProxy
Rank  2
Shape { 32, 64 }
512
576
2559
***Rank  2
Shape { 36, 64 }
0
0
512
575
576
2496
0
0
  */
  /* End Test */
}
