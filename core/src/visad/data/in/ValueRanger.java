/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for ranging data values (i.e. checking to see that they
 * lie within a valid range).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class ValueRanger
    extends	ValueProcessor
{
    private float			floatLower = Float.NEGATIVE_INFINITY;
    private float			floatUpper = Float.POSITIVE_INFINITY;
    private double			doubleLower = Double.NEGATIVE_INFINITY;
    private double			doubleUpper = Double.POSITIVE_INFINITY;
    private static final WeakHashMap	map = new WeakHashMap();

    private static final ValueRanger	trivialRanger =
	new ValueRanger()
	{
	    public float process(float value)
	    {
		return value;
	    }
	    public double process(double value)
	    {
		return value;
	    }
	    public float[] process(float[] values)
	    {
		return values;
	    }
	    public double[] process(double[] values)
	    {
		return values;
	    }
	};

    /**
     * Constructs from nothing.
     */
    protected ValueRanger()
    {}

    /**
     * Constructs from valid-range limits.
     *
     * @param lower		The lower limit of the valid range.  May be
     *				NaN or Double.NEGATIVE_INFINITY to indicate
     *				no limit.
     * @param upper		The upper limit of the valid range.  May be
     *				NaN or Double.POSITIVE_INFINITY to indicate
     *				no limit.
     */
    private ValueRanger(double lower, double upper)
    {
	doubleLower = lower == lower ? lower : Double.NEGATIVE_INFINITY;
	doubleUpper = upper == upper ? upper : Double.POSITIVE_INFINITY;
	floatLower = (float)doubleLower;
	floatUpper = (float)doubleUpper;
    }

    /**
     * Returns an instance of this class corresponding to valid-range limits.
     *
     * @param lower		The lower limit of the valid range.  May be
     *				NaN or Double.NEGATIVE_INFINITY to indicate
     *				no limit.
     * @param upper		The upper limit of the valid range.  May be
     *				NaN or Double.POSITIVE_INFINITY to indicate
     *				no limit.
     */
    public static ValueRanger valueRanger(double lower, double upper)
    {
	ValueRanger	ranger;
	if ((lower != lower || lower == Double.NEGATIVE_INFINITY) &&
	    (upper != upper || upper == Double.POSITIVE_INFINITY))
	{
	    ranger = trivialRanger;
	}
	else
	{
	    ranger = new ValueRanger(lower, upper);
	    WeakReference	ref = (WeakReference)map.get(ranger);
	    if (ref == null)
	    {
		map.put(ranger, new WeakReference(ranger));
	    }
	    else
	    {
		ValueRanger	oldRanger = (ValueRanger)ref.get();
		if (oldRanger == null)
		    map.put(ranger, new WeakReference(ranger));
		else
		    ranger = oldRanger;
	    }
	}
	return ranger;
    }

    /**
     * Returns the minimum, valid value.
     *
     * @return			The minimum, valid value.
     */
    public double getMin()
    {
	return doubleLower;
    }

    /**
     * Returns the maximum, valid value.
     *
     * @return			The maximum, valid value.
     */
    public double getMax()
    {
	return doubleUpper;
    }

    /**
     * Ranges a value.
     *
     * @param value		The value to be processed.
     * @return			The original value if it lay within the valid
     *				range; otherwise Float.NaN.
     */
    public float process(float value)
    {
	return
	    value < floatLower || value > floatUpper
		? Float.NaN
		: value;
    }

    /**
     * Ranges a value.
     *
     * @param value		The values to be processed.
     * @return			The original value if it lay within the valid
     *				range; otherwise Double.NaN.
     */
    public double process(double value)
    {
	return
	    value < doubleLower || value > doubleUpper
		? Double.NaN
		: value;
    }

    /**
     * Ranges values.
     *
     * @param values	The value to be processed.
     * @return			The original array with values that fall outside
     *				the valid range replaced with Float.NaN.
     */
    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    if (value < floatLower || value > floatUpper)
		values[i] = Float.NaN;
	}
	return values;
    }

    /**
     * Ranges values.
     *
     * @param values		The values to be processed.
     * @return			The original array with values that fall outside
     *				the valid range replaced with Double.NaN.
     */
    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    if (value < doubleLower || value > doubleUpper)
		values[i] = Double.NaN;
	}
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
	    ValueRanger	that = (ValueRanger)obj;
	    equals = this == that || (
		doubleLower == that.doubleLower &&
		doubleUpper == that.doubleUpper);
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
	    new Double(doubleLower).hashCode() ^ 
	    new Double(doubleUpper).hashCode();
    }
}
