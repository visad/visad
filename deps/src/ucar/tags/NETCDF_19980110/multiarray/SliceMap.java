/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to reduce the apparent rank of
 * the delegate by fixing an index at particular value.
 *
 * @see IntMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
SliceMap
		implements IntMap
{
	/**
	 * Create an IntMap which fixes the key for a particular
	 * dimension at a particular value.
	 *
	 * @param position the dimension number on which to fix the key.
	 * @param value the value at which to fix the key.
	 */
	public
	SliceMap(int position, int value)
	{
		next = new IntArrayAdapter();
		this.position = position;
		this.value = value;
	}

	/**
	 * Create an IntMap which fixes the key for a particular
	 * dimension at a particular value and is functionally composed
	 * with another IntMap.
	 *
	 * @param next IntMap to be composed with this.
	 * @param position the dimension number on which to fix the key.
	 * @param value the value at which to fix the key.
	 */
	public
	SliceMap(IntMap next, int position, int value)
	{
		this.next = next;
		this.position = position;
		this.value = value;
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 */
	public int
	get(int key)
	{
	/*
		return key < position
			? next.get(key) : key == position
				? value : next.get(key -1);
	*/
		if(key < position)
			return next.get(key);
		// else
		if(key == position)
			return value;
		// else
		return next.get(key -1);
		
	}

	/**
	 * Returns the number of key-value mappings in this Map.
	 */
	public int
	size()
	{
		return next.size();
	}

	/**
	 * Return the tail of a chain of IntMap.
	 * As side effects, connect the prev members and
	 * initialize the rank at the tail.
	 */
	public IntArrayAdapter
	tail(int rank, Object prev)
	{
		this.prev = prev;
		return next.tail(rank - 1, this);
	}

	/**
	 * Traverse the inverse mapping chain to
	 * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii)
	{
		final int adjust = ii < position ? ii : ii + 1;
		if(prev instanceof IntMap)
			return ((IntMap)prev).getLength(adjust);
		// else
		return Array.getInt(prev, adjust);
	}

 /**/
	/**
	 * Either an IntMap delegate for getLength(int)
	 * or an array of ints which can answer the
	 * question directly.
	 */
	private Object prev;

	private final IntMap next;
	private final int position;
	private final int value;

 /* Begin Test */
	public static void
	main(String[] args)
	{
		final int [] shape = {48, 64};
		MultiArrayImpl delegate =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(delegate.storage,
					ii, ii);

		}
		IntMap im = new SliceMap(1, 1);
		MultiArray ma = new MultiArrayProxy(delegate, im);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0]
					 + " }");
			System.out.println(ma.getInt(new int[] {1}));
		}
		catch (java.io.IOException ee) {}
	}
 /* End Test */
}
