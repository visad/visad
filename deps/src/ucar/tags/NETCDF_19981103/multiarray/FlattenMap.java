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
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:05 $
 */
public class
FlattenMap
		extends ConcreteIndexMap
{
	/**
	 * Create an ConcreteIndexMap which merges two adjacent dimensions.
	 * Using this in an MultiArrayProxy will result in the
	 * a MultiArray of one less rank.
	 * @param position this dimension and
	 *	dimension <code>(position +1)</code>
	 *	will appear as a single dimension.
	 */
	public
	FlattenMap(int position)
	{
		init(new IMap(),
			new LengthsMap());
		position_ = position;
	}

	/**
	 * Create an ConcreteIndexMap which merges two adjacent dimensions.
	 * Using this in an MultiArrayProxy will result in the
	 * a MultiArray of one less rank.
	 * @param prev ConcreteIndexMap to be composed with this.
	 * @param position this dimension and
	 *	dimension <code>(position +1)</code>
	 */
	public
	FlattenMap(ConcreteIndexMap prev, int position)
	{
		link(prev, new IMap(),
			new LengthsMap());
		position_ = position;
	}

	private class
	IMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key < position_)
				return super.get(key);
			if(key == position_)
			{
				length_ = ((LengthsMap)lengthsMap_)
						.superGet(key + 1);
				final int got = super.get(key);
				value_ = got % length_;
				return got / length_;
			}
			if(key == position_ +1)
				return value_;
			// else
			return super.get(key - 1);
			
		}
	
		public synchronized int
		size()
		{
			return super.size() +1;
		}

		private int length_;
		private int value_;
	}

	private class
	LengthsMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key < position_)
				return super.get(key);
			// else
			if(key == position_)
				return super.get(key) *
					super.get(key + 1);
			// else
				return super.get(key +1);
		}

		public synchronized int
		size()
		{
			return super.size() -1;
		}

		int
		superGet(int key)
		{
			return super.get(key);
		}

	}

 /**/
	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int position_;


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
		ConcreteIndexMap im = new FlattenMap(1);
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
 /* Test output java ucar.multiarray.FlattenMap
Rank  2
Shape { 32, 3072 }
0
1
63
64
3071
3072
  */
 /* End Test */
}
