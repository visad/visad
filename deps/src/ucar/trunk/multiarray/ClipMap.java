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
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:05 $
 */
public class
ClipMap
		extends ConcreteIndexMap
{
	/**
	 * Create an IndexMap which clips along a specific dimension.
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
		init(new IMap(),
			new LengthsMap());
		position_ = position;
		low_ = low;
		extent_ = extent;
	}

	/**
	 * Create an IndexMap which clips along a specific dimension
	 * and is functionally composed with another IndexMap.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * as <code>extent</code>.
	 *
	 * @param prev IndexMap to be composed with this.
	 * @param position the dimension number to clip along
	 * @param low the minimum value. 0 will map to this
	 * @param extent the new dimension length at position
	 */
	public
	ClipMap(ConcreteIndexMap prev, int position, int low, int extent)
	{
		link(prev, new IMap(),
			new LengthsMap());
		position_ = position;
		low_ = low;
		extent_ = extent;
	}

	private class
	IMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key == position_)
			{
				return super.get(key) + low_;
			}
			// else
			return super.get(key);
		}

	}
	
	private class
	LengthsMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key == position_)
			{
				return extent_;
			}
			// else
			return super.get(key);
		}
	}

	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int position_;
	private /* final */ int low_;
	private /* final */ int extent_;

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
		IndexMap im = new ClipMap(0, 8, 32);
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
 /* Test output java ucar.multiarray.ClipMap
Rank  2
Shape { 32, 64 }
576
  */
 /* End Test */
}
