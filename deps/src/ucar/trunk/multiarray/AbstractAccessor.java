// $Id: AbstractAccessor.java,v 1.2 2002-05-29 20:32:38 steve Exp $
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
import java.io.IOException;

/**
 * This abstract class provides a skeletal implementation
 * of the Accessor interface.
 * <p>
 * A minimal concrete implementation
 * would provide concrete implementations
 * <code>Object get(int [] index)</code>,
 * <code>Object copyout(int [] origin, int [] shape)</code>,
 * <code>set(int [] index, Object value)</code>.
 * <code>copyin(int [] index, MultiArray value)</code>.
 * 
 * @see Accessor
 *
 * @author $Author: steve $
 * @version $Revision: 1.2 $ $Date: 2002-05-29 20:32:38 $
 */

public abstract class
AbstractAccessor
	implements Accessor
{
	/* NOTUSED, not correct?
	 * Analogous to System.arraycopy,
	 * copy elements from one Accessor to another.
	 * <p>
	 * The destination limits control the iteration.
	 * If the source limits are such that there is less
	 * data available in the source than requested in the
	 * destination, the source IndexIterator will silently
	 * "roll over", providing data from the beginning of
	 * the source.
	 *
	 * @param src the data source
	 * @param src_pos starting position in the data source
	 * @param src_limits limits on the source IndexIterator
	 *	typically < ((MultiArray)src).getLengths()
	 * @param dst the destination, values here are modified
	 * @param dst_pos starting position in the data source
	 * @param dst_limits limits on the destination IndexIterator,
	 *	typically < ((MultiArray)dst).getLengths()
	 * 
	static public void
	copy(Accessor src, int [] src_pos, int [] src_limits,
		Accessor dst, int [] dst_pos, int [] dst_limits)
			throws IOException
	{
		IndexIterator src_odo = new IndexIterator(src_pos, src_limits);
		IndexIterator dst_odo = new IndexIterator(dst_pos, dst_limits);

		while(dst_odo.notDone())
		{
			dst.set(dst_odo.value(), src.get(src_odo.value()));
			src_odo.incr();
			dst_odo.incr();
		}
	}
	 */

	/**
	 * Used to implement copyin.
	 *
	 * @param src the data source
	 * @param src_limits limits on the source IndexIterator
	 *	typically < ((MultiArray)src).getLengths()
	 * @param dst the destination, values here are modified
	 * @param dst_pos starting position in the data source
	 * 
	 */
	static public void
	copy(Accessor src, int [] src_limits,
		Accessor dst, int [] dst_pos )
			throws IOException
	{
		for(OffsetDualIndexIterator odo =
			new OffsetDualIndexIterator(dst_pos, src_limits);
				odo.notDone(); odo.incr())
		{
			dst.set(odo.offsetValue(), src.get(odo.value()));
		}
	}

	/**
	 * Used to implement copyout.
	 *
	 * @param src the data source
	 * @param src_pos starting position in the data source
	 * @param dst the destination, values here are modified
	 * @param dst_limits limits on the source IndexIterator
	 *	typically < ((MultiArray)dst).getLengths()
	 * 
	 */
	static public void
	copyO(Accessor src, int [] src_pos,
		Accessor dst, int [] dst_limits )
			throws IOException
	{
		for(OffsetDualIndexIterator odo =
			new OffsetDualIndexIterator(src_pos, dst_limits);
				odo.notDone(); odo.incr())
		{
			dst.set(odo.value(), src.get(odo.offsetValue()));
		}
	}

 /* Begin MultiArray read access methods */

	abstract public Object
	get(int [] index)
		throws IOException;

	public boolean
	getBoolean(int [] index)
		throws IOException
	{
		final Boolean nn = (Boolean) get(index);
		return nn.booleanValue();
	}

	public char
	getChar(int [] index)
		throws IOException
	{
		final Character nn = (Character) get(index);
		return nn.charValue();
	}

	public byte
	getByte(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.byteValue();
	}

	public short
	getShort(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.shortValue();
	}

	public int
	getInt(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.intValue();
	}

	public long
	getLong(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.longValue();
	}

	public float
	getFloat(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.floatValue();
	}

	public double
	getDouble(int [] index)
		throws IOException
	{
		final Number nn = (Number) get(index);
		return nn.doubleValue();
	}

 /* End MultiArray read access methods */
 /* Begin MultiArray write access methods */

    	abstract public void
	set(int [] index, Object value)
			throws IOException;

	public void
	setBoolean(int [] index, boolean value)
		throws IOException
	{
		set(index, new Boolean(value));
	}

	public void
	setChar(int [] index, char value)
		throws IOException
	{
		set(index, new Character(value));
	}

	public void
	setByte(int [] index, byte value)
		throws IOException
	{
		set(index, new Byte(value));
	}

	public void
	setShort(int [] index, short value)
		throws IOException
	{
		set(index, new Short(value));
	}

	public void
	setInt(int [] index, int value)
		throws IOException
	{
		set(index, new Integer(value));
	}

	public void
	setLong(int [] index, long value)
		throws IOException
	{
		set(index, new Long(value));
	}

	public void
	setFloat(int [] index, float value)
		throws IOException
	{
		set(index, new Float(value));
	}

	public void
	setDouble(int [] index, double value)
		throws IOException
	{
		set(index, new Double(value));
	}

 /* End MultiArray write access methods */

	abstract public MultiArray
	copyout(int [] origin, int [] shape)
			throws IOException;
	/*
	{
		final MultiArrayImpl data = new MultiArrayImpl(
			componentType,
			shape);
		for(OffsetDualIndexIterator odo =
			new OffsetDualIndexIterator(origin, data.getLengths());
				odo.notDone(); odo.incr())
		{
			data.set(odo.value(), get(odo.offsetValue()));
		}
		return data;
	}
	*/

	/**
	 * You almost always want to override this
	 */
	public void
	copyin(int [] origin, MultiArray data)
		throws IOException
	{
		for(OffsetDualIndexIterator odo =
			new OffsetDualIndexIterator(origin, data.getLengths());
				odo.notDone(); odo.incr())
		{
			set(odo.offsetValue(), data.get(odo.value()));
		}
	}

	abstract public Object
	toArray()
		throws IOException;

	abstract public Object
	toArray(Object dst, int [] origin, int [] shape)
		throws IOException;
}


final class
OffsetDualIndexIterator
	extends IndexIterator
{
	OffsetDualIndexIterator(int [] theOffset, int [] theLimits)
	{
		super(theLimits);
		offset = theOffset; // N.B. Not a copy
		offsetCounter = (int []) offset.clone();
	}

	/**
	 * Increment the odometer
	 */
	public void
	incr()
	{
		int digit = counter.length -1;
		if(digit < 0)
		{
			// counter is zero length array <==> scalar
			ncycles++;
			return;
		}

		while(digit >= 0)
		{
			offsetCounter[digit]++;
			counter[digit]++;
			if(counter[digit] < limits[digit])
			{
				break; // normal exit
			}
			// else, carry
			counter[digit] = 0;
			offsetCounter[digit] = offset[digit];
			if(digit == 0)
			{
				ncycles++; // rolled over
				break;
			}
			// else
			digit--;
		}
	}

	public int []
	offsetValue() { return offsetCounter; }

	private final int[] offset;
	private final int[] offsetCounter;
}
