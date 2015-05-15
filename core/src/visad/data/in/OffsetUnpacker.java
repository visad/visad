/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.in;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Provides support for unpacking data values by adding a constant offset to 
 * them.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public final class OffsetUnpacker
    extends	ValueUnpacker
{
    private final float			floatOffset;
    private final double		doubleOffset;
    private static final WeakHashMap	map = new WeakHashMap();

    private OffsetUnpacker(double offset)
    {
	floatOffset = (float)offset;
	doubleOffset = offset;
    }

    /**
     * Returns an instance of this class corresponding to an offset value.
     *
     * @param offset		The numeric offset to be added to each value
     *				during processing.
     * @return			An instance of this class corresponding to the
     *				input argument.
     */
    public static synchronized OffsetUnpacker offsetUnpacker(double offset)
    {
	OffsetUnpacker	unpacker = new OffsetUnpacker(offset);
	WeakReference	ref = (WeakReference)map.get(unpacker);
	if (ref == null)
	{
	    map.put(unpacker, new WeakReference(unpacker));
	}
	else
	{
	    OffsetUnpacker	oldUnpacker = (OffsetUnpacker)ref.get();
	    if (oldUnpacker == null)
		map.put(unpacker, new WeakReference(unpacker));
	    else
		unpacker = oldUnpacker;
	}
	return unpacker;
    }

    /**
     * Process a value.
     *
     * @param value		The value to be processed.
     * @return			The value with the construction offset added to
     *				it.
     */
    public float process(float value)
    {
	return floatOffset + value;
    }

    /**
     * Process a value.
     *
     * @param value		The value to be processed.
     * @return			The value with the construction offset added to
     *				it.
     */
    public double process(double value)
    {
	return doubleOffset + value;
    }

    /**
     * Process values.
     *
     * @param values	The values to be processed.
     * @return			The values with the construction offset added to
     *				them.  The same array is returned.
     */
    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] += floatOffset;
	return values;
    }

    /**
     * Process values.
     *
     * @param values	The values to be processed.
     * @return			The values with the construction offset added to
     *				them.  The same array is returned.
     */
    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] += doubleOffset;
	return values;
    }

    /**
     * Indicates if this instance is semantically identical to another object.
     *
     * @param obj		The other object.
     * @return			<code>true</code> if and only if this instance
     *				is semantically identical to the other object.
     */
    public boolean equals(Object obj)
    {
	boolean	equals;
	if (!getClass().isInstance(obj))
	{
	    equals = false;
	}
	else
	{
	    OffsetUnpacker	that = (OffsetUnpacker)obj;
	    equals = this == that || doubleOffset == that.doubleOffset;
	}
	return equals;
    }

    /**
     * Returns the hash code of this instance.
     *
     * @return			The hash code of this instance.
     */
    public int hashCode()
    {
	return new Double(doubleOffset).hashCode();
    }
}
