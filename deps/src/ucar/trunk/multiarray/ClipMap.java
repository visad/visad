/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to limit the bounds of an
 * index to the delegate on a given dimension.
 * <p>
 * You could "clip" a 2d MultiArray to a window using
 * 2 of these.
 *
 * @see IntMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
ClipMap
		implements IntMap
{
	/**
	 * Create an IntMap which clips along a specific dimension.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * as <code>extent</code>.
	 *
	 * @param position the dimension number to clip along
	 * @param low the minimum value. 0 will map to this
	 * @param extent the new dimension length at position
	 */
	public
	ClipMap(int position, int low, int extent)
	{
		next = new IntArrayAdapter();
		this.position = position;
		this.low = low;
		this.extent = extent;
	}

	/**
	 * Create an IntMap which clips along a specific dimension
	 * and is functionally composed with another IntMap.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * as <code>extent</code>.
	 *
	 * @param next IntMap to be composed with this.
	 * @param position the dimension number to clip along
	 * @param low the minimum value. 0 will map to this
	 * @param extent the new dimension length at position
	 */
	public
	ClipMap(IntMap next, int position, int low, int extent)
	{
		this.next = next;
		this.position = position;
		this.low = low;
		this.extent = extent;
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 */
	public int
	get(int key)
	{
		if(key == position)
		{
			// TODO? could add runtime check here
			// assert(next.get(key) < extent);
			return next.get(key) + low;
		}
		// else
		return next.get(key);
		
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
		// TODO? could add consistancy checks here:
		// assert(low < getLength(position));
		// assert(getLength(position) <= extent);
		return next.tail(rank, this);
	}

	/**
	 * Traverse the inverse mapping chain to
	 * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii)
	{
		if(ii == position)
			return extent;
		// else
		if(prev instanceof IntMap)
			return ((IntMap)prev).getLength(ii);
		// else
		return Array.getInt(prev, ii);
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
	private final int low;
	private final int extent;

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
		IntMap im = new ClipMap(0, 8, 32);
		MultiArray ma = new MultiArrayProxy(delegate, im);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + " }");
			System.out.println(ma.getInt(new int[] {1, 0}));
		}
		catch (java.io.IOException ee) {}
	}
 /* End Test */
}
