/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
 */

package ucar.multiarray;
import java.lang.reflect.Array;

/**
 * Use with MultiArrayProxy to transpose two dimensions.
 *
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:06 $
 */
public class
TransposeMap
		extends ConcreteIndexMap
{
	/**
	 * Create an IndexMap which swaps two dimensions.
	 *
	 * @param aa specifies one of the dimensions to swap
	 * @param bb specifies the other dimension to swap
	 */
	public
	TransposeMap(int aa, int bb)
	{
		init(new IMap(),
			new LengthsMap());
		aa_ = aa;
		bb_ = bb;
	}

	/**
	 * Create an IndexMap which swaps two dimensions.
	 *
	 * @param prev IndexMap to be composed with this.
	 * @param aa specifies one of the dimensions to swap
	 * @param bb specifies the other dimension to swap
	 */
	public
	TransposeMap(ConcreteIndexMap prev, int aa, int bb)
	{
		link(prev, new IMap(),
			new LengthsMap());
		aa_ = aa;
		bb_ = bb;
	}

	private class
	IMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key == aa_)
			{
				return super.get(bb_);
			}
			// else
			if(key == bb_)
			{
				return super.get(aa_);
			}
			// else
			return super.get(key);
			
		}
	}

	private class
	LengthsMap extends ZZMap
	{
		public int
		get(int key)
		{
			if(key == aa_)
				return super.get(bb_);
			// else
			if(key == bb_)
				return super.get(aa_);
			// else
			return super.get(key);
		}
	}

 /**/

	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int aa_;
	private /* final */ int bb_;

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
		IndexMap im = new TransposeMap(0, 2);
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
 /* Test output java ucar.multiarray.TransposeMap
Rank  3
Shape { 64, 48, 32 }
3072
64
1
 /* End Test */
}
