/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to reduce the length along a particular
 * dimension by sampling the domain according to a (repeated) pattern.
 *
 * @see IntMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
DecimateMap
		implements IntMap
{
	/**
	 * Create an IntMap which decimates along a specific dimension.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * smaller. The values which will show through are selected
	 * by the pattern argument.
	 *
	 * @param position the dimension number to clip along
	 * @param pattern index values along the dimension will
	 *	show through where pattern is set to <code>true</code>.
	 *	If pattern.length is less than the source dimension length,
	 *	the pattern is repeated.
	 */
	public
	DecimateMap(int position, boolean [] pattern)
	{
		next = new IntArrayAdapter();
		this.position = position;
		this.pattern = (boolean []) pattern.clone();
		nset = nbset(this.pattern, this.pattern.length);
	}

	/**
	 * Create an IntMap which decimates along a specific dimension.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * smaller. The values which will show through are selected
	 * by the pattern argument.
	 *
	 * @param next IntMap to be composed with this.
	 * @param position the dimension number to clip along
	 * @param pattern index values along the dimension will
	 *	show through where pattern is set to <code>true</code>.
	 *	If pattern.length is less than the source dimension length,
	 *	the pattern is repeated.
	 */
	public
	DecimateMap(IntMap next, int position, boolean [] pattern)
	{
		this.next = next;
		this.position = position;
		this.pattern = (boolean []) pattern.clone();
		nset = nbset(this.pattern, this.pattern.length);
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 */
	public int
	get(int key)
	{
		if(key == position)
		{
			// TODO: better algorithm ?
			final int input = next.get(key);
			final int nstrides = input / nset;
			final int which = nset > 1 ? input % nset : 0;
			int offset = 0;
			for(int nhits = 0; offset < pattern.length; offset++)
			{
				if(pattern[offset])
				{
					nhits++;
					if(nhits > which)
						break; // normal loop exit
				}
			}
			return nstrides * pattern.length + offset;
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
		return next.tail(rank, this);
	}

	private int
	prevLength(int ii)
	{
		if(prev instanceof IntMap)
			return ((IntMap)prev).getLength(ii);
		// else
		return Array.getInt(prev, ii);
	}

	/**
	 * Traverse the inverse mapping chain to
	 * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii)
	{
		final int plen = prevLength(ii);
		if(ii != position)
			return plen;
		// else
		final int len = (plen / pattern.length) * nset;
		final int rem = plen % pattern.length;
		if(rem == 0)
			return len;
		// else
		return len + nbset(pattern, rem);
	}

 /**/

	/**
	 * Compute the number of <code>true</code>
	 * elements in <code>apattern</code>.
	 */
	static private int
	nbset(boolean [] apattern, int len)
	{
		int nhits = 0;
		for(int ii = 0; ii < len; ii++)
			if(apattern[ii])
				nhits++;
		return nhits;
	}

	/**
	 * Either an IntMap delegate for getLength(int)
	 * or an array of ints which can answer the
	 * question directly.
	 */
	private Object prev;

	private final IntMap next;
	private final int position;
	private final boolean [] pattern;
	private final int nset;

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
		boolean [] pattern = new boolean [] { true, false, true };
		IntMap im = new DecimateMap(0, pattern);
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
