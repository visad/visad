/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to reduce apparent rank by
 * merging adjacent dimensions. The total number of elements
 * remains constant.
 * <p>
 * This framework doesn't really support this operation
 * very well. See caveats in <code>get()</code>
 *
 * @see IntMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
FlattenMap
		implements IntMap
{
	/**
	 * Create an IntMap which merges two adjacent dimensions.
	 * Using this in an MultiArrayProxy will result in the
	 * a MultiArray of one less rank.
	 * @param position this dimension and
	 *	dimension <code>(position +1)</code>
	 *	will appear as a single dimension.
	 */
	public
	FlattenMap(int position)
	{
		next = new IntArrayAdapter();
		this.position = position;
	}

	/**
	 * Create an IntMap which merges two adjacent dimensions.
	 * Using this in an MultiArrayProxy will result in the
	 * a MultiArray of one less rank.
	 * @param next IntMap to be composed with this.
	 * @param position this dimension and
	 *	dimension <code>(position +1)</code>
	 */
	public
	FlattenMap(IntMap next, int position)
	{
		this.next = next;
		this.position = position;
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 * For this map, the value at position must be split for
	 * fetch by two separate calls. So, things only work if
	 * the calls to get are made in increasing order of key.
	 */
	public int
	get(int key)
	{
		if(key < position)
			return next.get(key);
		if(key == position)
		{
			length = prevLength(key + 1);
			final int got = next.get(key);
			value = got % length;
			return got / length;
		}
		if(key == position +1)
			return value;
		// else
		return next.get(key - 1);
		
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
		if(ii < position)
			return prevLength(ii);
		// else
		if(ii == position)
			return prevLength(ii) * prevLength(ii + 1);
		// else
			return prevLength(ii +1);
	}

 /**/
	private int
	prevLength(int ii)
	{
		if(prev instanceof IntMap)
			return ((IntMap)prev).getLength(ii);
		// else
		return Array.getInt(prev, ii);
	}

	/**
	 * Either an IntMap delegate for getLength(int)
	 * or an array of ints which can answer the
	 * question directly.
	 */
	private Object prev;
	private final IntMap next;
	private final int position;

	int length;
	int value;

 /* Begin Test */
	public static void
	main(String[] args)
	{
		final int [] shape = {32, 48, 64};
		MultiArrayImpl delegate =
			new MultiArrayImpl(Integer.TYPE, shape);
		{
			final int size = MultiArrayImpl.numberOfElements(shape);
			for(int ii = 0; ii < size; ii++)
				java.lang.reflect.Array.setInt(delegate.storage,
					ii, ii);

		}
		IntMap im = new FlattenMap(1);
		MultiArray ma = new MultiArrayProxy(delegate, im);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0]
					 + ", " + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {0, 0}));
			System.out.println(ma.getInt(new int[] {0, 1}));
			System.out.println(ma.getInt(new int[] {0, 63}));
			System.out.println(ma.getInt(new int[] {0, 64}));
			System.out.println(ma.getInt(new int[] {0, 3071}));
			System.out.println(ma.getInt(new int[] {1, 0}));
		}
		catch (java.io.IOException ee) {}
	}
 /* End Test */
}
