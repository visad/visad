/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to transpose two dimensions.
 *
 * @see IntMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */
public class
TransposeMap
		implements IntMap
{
	/**
	 * Create an IntMap which swaps two dimensions.
	 *
	 * @param aa int specifying one of the dimensions to swap
	 * @param bb int specifying the other dimension to swap
	 */
	public
	TransposeMap(int aa, int bb)
	{
		next = new IntArrayAdapter();
		this.aa = aa;
		this.bb = bb;
	}

	/**
	 * Create an IntMap which swaps two dimensions.
	 *
	 * @param next IntMap to be composed with this.
	 * @param aa int specifying one of the dimensions to swap
	 * @param bb int specifying the other dimension to swap
	 */
	public
	TransposeMap(IntMap next, int aa, int bb)
	{
		this.next = next;
		this.aa = aa;
		this.bb = bb;
	}

	/**
	 * Returns the value to which this Map maps the specified key.
	 */
	public int
	get(int key)
	{
		if(key == aa)
		{
			return next.get(bb);
		}
		// else
		if(key == bb)
		{
			return next.get(aa);
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

	/**
	 * Traverse the inverse mapping chain to
	 * retrieve the dimension length at ii.
	 */
	public int
	getLength(int ii)
	{
		if(ii == aa)
			return prevLength(bb);
		// else
		if(ii == bb)
			return prevLength(aa);
		// else
		return prevLength(ii);
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
	private final int aa;
	private final int bb;

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
		IntMap im = new TransposeMap(0, 2);
		MultiArray ma = new MultiArrayProxy(delegate, im);

		try {
			System.out.println("Rank  " + ma.getRank());
			int [] lengths = ma.getLengths();
			System.out.println("Shape { " + lengths[0] + ", "
					 + lengths[1] + ", "
					 + lengths[2] + " }");
			System.out.println(ma.getInt(new int[] {0, 0, 1}));
			System.out.println(ma.getInt(new int[] {0, 1, 0}));
			System.out.println(ma.getInt(new int[] {1, 0, 0}));
		}
		catch (java.io.IOException ee) {}
	}
 /* End Test */
}
