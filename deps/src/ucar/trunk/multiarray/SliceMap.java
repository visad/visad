// $Id: SliceMap.java,v 1.2 2002-05-29 20:32:40 steve Exp $
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
 * Use with MultiArrayProxy to reduce the apparent rank of
 * the delegate by fixing an index at particular value.
 *
 * @see IndexMap
 * @see MultiArrayProxy
 *
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:40 $
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
