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
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:06 $
 */
public class
SliceMap
		extends ConcreteIndexMap
{
	/**
	 * Create an ConcreteIndexMap which fixes the key for a particular
	 * dimension at a particular value.
	 *
	 * @param position the dimension number on which to fix the key.
	 * @param value the value at which to fix the key.
	 */
	public
	SliceMap(int position, int value)
	{
		init(new IMap(),
			new LengthsMap());
		position_ = position;
		value_ = value;
	}

	/**
	 * Create an ConcreteIndexMap which fixes the key for a particular
	 * dimension at a particular value and is functionally composed
	 * with another ConcreteIndexMap.
	 *
	 * @param prev ConcreteIndexMap to be composed with this.
	 * @param position the dimension number on which to fix the key.
	 * @param value the value at which to fix the key.
	 */
	public
	SliceMap(ConcreteIndexMap prev, int position, int value)
	{
		link(prev, new IMap(),
			new LengthsMap());
		position_ = position;
		value_ = value;
	}

	private class
	IMap extends ZZMap
	{

		public synchronized int
		get(int key)
		{
		/*
			return key < position_
				? super.get(key) : key == position_
					? value_ : super.get(key -1);
		*/
			if(key < position_)
				return super.get(key);
			// else
			if(key == position_)
				return value_;
			// else
			return super.get(key -1);
			
		}
	
		public synchronized int
		size()
		{
			return super.size() +1;
		}
	}

	private class
	LengthsMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			final int adjust = key < position_ ? key : key + 1;
			return super.get(adjust);
		}
	
		public synchronized int
		size()
		{
			return super.size() -1;
		}
	}

	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int position_;
	private /* final */ int value_;

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
		IndexMap im = new SliceMap(1, 1);
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
 /* Test output java ucar.multiarray.SliceMap
Rank  1
Shape { 48 }
65
  */
 /* End Test */
}
