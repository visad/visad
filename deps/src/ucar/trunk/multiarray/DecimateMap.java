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
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.2 $ $Date: 2000-08-28 21:43:05 $
 */
public class
DecimateMap
		extends ConcreteIndexMap
{
	/**
	 * Create an ConcreteIndexMap which decimates along
	 * a specific dimension.
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
		init(new IMap(),
			new LengthsMap());
		position_ = position;
		pattern_ = (boolean []) pattern.clone();
		nset_ = nbset(pattern_, pattern_.length);
	}

	/**
	 * Create an ConcreteIndexMap which decimates along
	 * a specific dimension.
	 * Using this in an MultiArrayProxy will result in the
	 * the length of dimension <code>position</code> appearing
	 * smaller. The values which will show through are selected
	 * by the pattern argument.
	 *
	 * @param prev ConcreteIndexMap to be composed with this.
	 * @param position the dimension number to clip along
	 * @param pattern index values along the dimension will
	 *	show through where pattern is set to <code>true</code>.
	 *	If pattern.length is less than the source dimension length,
	 *	the pattern is repeated.
	 */
	public
	DecimateMap(ConcreteIndexMap prev, int position, boolean [] pattern)
	{
		link(prev, new IMap(),
			new LengthsMap());
		position_ = position;
		pattern_ = (boolean []) pattern.clone();
		nset_ = nbset(pattern_, pattern_.length);
	}

	private class
	IMap extends ZZMap
	{
		public synchronized int
		get(int key)
		{
			if(key == position_)
			{
				// TODO: better algorithm ?
				final int input = super.get(key);
				final int nstrides = input / nset_;
				final int which = nset_ > 1 ? input % nset_ : 0;
				int offset = 0;
				for(int nhits = 0; offset < pattern_.length;
						offset++)
				{
					if(pattern_[offset])
					{
						nhits++;
						if(nhits > which)
							break; // normal exit
					}
				}
				return nstrides * pattern_.length + offset;
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
			final int plen = super.get(key);
			if(key != position_)
				return plen;
			// else
			final int len = (plen / pattern_.length) * nset_;
			final int rem = plen % pattern_.length;
			if(rem == 0)
				return len;
			// else
			return len + nbset(pattern_, rem);
		}
	}

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

	/* WORKAROUND: Inner class & blank final initialize compiler bug */
	private /* final */ int position_;
	private /* final */ boolean [] pattern_;
	private /* final */ int nset_;

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
		IndexMap im = new DecimateMap(0, pattern);
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
 /* Test output java ucar.multiarray.DecimateMap
Rank  2
Shape { 32, 64 }
128
  */
 /* End Test */
}
