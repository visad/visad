// $Id: ClipMap.java,v 1.2 2002-05-29 20:32:38 steve Exp $
/*
 * Copyright 1997-2000 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:38 $
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
