/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.io.IOException;

/**
 * This MultiArray implementation wraps another MultiArray
 * and an IndexMapping to provide a different view of the
 * wrapped MultiArray. Indices passed to access methods
 * are passed through a chain of mappings.
 *
 * @see MultiArray
 * @see IntMap
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class MultiArrayProxy implements MultiArray {

	/**
	 * Construct a new proxy.
	 * @param delegate MultiArray backing the proxy view provided.
	 * @param head IndexMapping defining the proxy view.
	 */
	public
	MultiArrayProxy(MultiArray delegate, IntMap head)
	{
		this.delegate = delegate;
		this.head = head;
		dlengths = delegate.getLengths();
		this.tail = head.tail(dlengths.length, dlengths);
		this.converted = new int[dlengths.length];
		
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
		{ return delegate.getComponentType(); }

	/**
	 * Returns the number of dimensions of the backing MultiArray,
	 * as transformed by the <code>rankInverseMap()</code> method of
	 * the IndexMapping.
	 * @return int number of dimensions
	 */
	public int
	getRank()
		{ return head.size(); }

	/**
	 * Returns the shape of the backing MultiArray as transformed
	 * by the <code>dimensionsInverseMap()</code> method of
	 * the IndexMapping.
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
			System.arraycopy(delegate.getLengths(), 0,
				dlengths, 0, dlengths.length);
		}
		final int [] lengths = new int[getRank()];
		for(int ii = 0; ii < lengths.length; ii++)
			lengths[ii] = tail.getLength(ii);
		return lengths;
	}

	/**
	 * Returns <code>true</code> if and only if the effective dimension
	 * lengths can change.
	 */
	public boolean
	isUnlimited()
		{ return delegate.isUnlimited(); }

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
		return delegate.get(map(index));
	}

	public boolean
	getBoolean(int[] index)
		 throws IOException
	{
		return delegate.getBoolean(map(index));
	}

	public char
	getChar(int[] index)
		 throws IOException
	{
		return delegate.getChar(map(index));
	}

	public byte
	getByte(int[] index)
		 throws IOException
	{
		return delegate.getByte(map(index));
	}

	public short
	getShort(int[] index)
		 throws IOException
	{
		return delegate.getShort(map(index));
	}

	public int
	getInt(int[] index)
		 throws IOException
	{
		return delegate.getInt(map(index));
	}

	public long
	getLong(int[] index)
		 throws IOException
	{
		return delegate.getLong(map(index));
	}

	public float
	getFloat(int[] index)
		 throws IOException
	{
		return delegate.getFloat(map(index));
	}

	public double
	getDouble(int[] index)
		 throws IOException
	{
		return delegate.getDouble(map(index));
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
		delegate.set(map(index), value);
	}

	public void
	setBoolean(int [] index, boolean value)
		 throws IOException
	{
		delegate.setBoolean(map(index), value);
	}

	public void
	setChar(int [] index, char value)
		 throws IOException
	{
		delegate.setChar(map(index), value);
	}

	public void
	setByte(int [] index, byte value)
		 throws IOException
	{
		delegate.setByte(map(index), value);
	}

	public void
	setShort(int [] index, short value)
		 throws IOException
	{
		delegate.setShort(map(index), value);
	}

	public void
	setInt(int [] index, int value)
		 throws IOException
	{
		delegate.setInt(map(index), value);
	}

	public void
	setLong(int [] index, long value)
		 throws IOException
	{
		delegate.setLong(map(index), value);
	}

	public void
	setFloat(int [] index, float value)
		 throws IOException
	{
		delegate.setFloat(map(index), value);
	}

	public void
	setDouble(int[] index, double value)
		 throws IOException
	{
		delegate.setDouble(map(index), value);
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

 /* End Accessor */

	private int []
	map(int [] index)
	{
		tail.rebind(index);
		for(int ii = 0; ii < converted.length; ii++)
			converted[ii] = head.get(ii);
		return converted;
	}

	private final MultiArray delegate;
	private final IntMap head;
	/**
	 * Storage of delegate dimension lengths
	 */
	private final int [] dlengths;
	private final IntArrayAdapter tail;
	/**
	 * Scratch space used as storage for converted args.
	 * Reference must be synchronized.
	 */
	private final int [] converted;
}
