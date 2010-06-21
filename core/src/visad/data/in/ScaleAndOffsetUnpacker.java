/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for unpacking data values by multiplying them by a constant
 * scale factor and then adding a constant offset value.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public final class ScaleAndOffsetUnpacker
    extends	ValueUnpacker
{
    private final float			floatScale;
    private final double		doubleScale;
    private final float			floatOffset;
    private final double		doubleOffset;
    private static final WeakHashMap	map = new WeakHashMap();

    private ScaleAndOffsetUnpacker(double scale, double offset)
    {
	floatScale = (float)scale;
	doubleScale = scale;
	floatOffset = (float)offset;
	doubleOffset = offset;
    }

    /**
     * Returns an instance of this class corresponding to an offset value and
     * scale factor.
     *
     * @param scale		The numeric amount to multiply each value by
     *				during processing.
     * @param offset		The numeric offset to be added to each value
     *				during processing.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     */
    public static synchronized ScaleAndOffsetUnpacker scaleAndOffsetUnpacker(
	double scale, double offset)
    {
	ScaleAndOffsetUnpacker	unpacker =
	    new ScaleAndOffsetUnpacker(scale, offset);
	WeakReference	ref = (WeakReference)map.get(unpacker);
	if (ref == null)
	{
	    map.put(unpacker, new WeakReference(unpacker));
	}
	else
	{
	    ScaleAndOffsetUnpacker	oldUnpacker =
		(ScaleAndOffsetUnpacker)ref.get();
	    if (oldUnpacker == null)
		map.put(unpacker, new WeakReference(unpacker));
	    else
		unpacker = oldUnpacker;
	}
	return unpacker;
    }

    /**
     * Returns the absolute value of the scale value.
     *
     * @return			The absolute value of the scale value.
     */
    public double getIncrement()
    {
	return Math.abs(doubleScale);
    }

    /**
     * Process a value.
     *
     * @param			The value to be processed.
     * @return			The value with the construction offset added to
     *				it.
     */
    public float process(float value)
    {
	return floatScale*value + floatOffset;
    }

    /**
     * Process a value.
     *
     * @param			The value to be processed.
     * @return			The value with the construction offset added to
     *				it.
     */
    public double process(double value)
    {
	return doubleScale*value + doubleOffset;
    }

    /**
     * Process values.
     *
     * @param			The values to be processed.
     * @return			The values with the construction offset added to
     *				them.  The same array is returned.
     */
    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] = values[i]*floatScale + floatOffset;
	return values;
    }

    /**
     * Process values.
     *
     * @param			The values to be processed.
     * @return			The values with the construction offset added to
     *				them.  The same array is returned.
     */
    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	    values[i] = values[i]*doubleScale + doubleOffset;
	return values;
    }

    /**
     * Indicates if this instance is semantically identical to another object.
     *
     * @param			The other object.
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
	    ScaleAndOffsetUnpacker	that = (ScaleAndOffsetUnpacker)obj;
	    equals = this == that || (
		doubleOffset == that.doubleOffset &&
		doubleScale == that.doubleScale);
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
	return
	    new Double(doubleOffset).hashCode() ^
	    new Double(doubleScale).hashCode();
    }
}
