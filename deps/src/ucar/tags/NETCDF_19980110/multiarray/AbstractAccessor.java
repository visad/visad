/*
 * Copyright 1997, University Corporation for Atmospheric Research
 * See COPYRIGHT file for copying and redistribution conditions.
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
 * @author $Author: dglo $
 * @version $Revision: 1.1.1.1 $ $Date: 2000-08-28 21:42:24 $
 */

public abstract class
AbstractAccessor
	implements Accessor
{
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
		set(index, new Float(value));
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
