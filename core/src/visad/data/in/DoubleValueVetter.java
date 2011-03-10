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
 * Provides support for verifying data values (i.e. seeing that they aren't
 * equal to either of two special values).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
final class DoubleValueVetter
    extends	ValueVetter
{
    private double			doubleValue1;
    private double			doubleValue2;
    private float			floatValue1;
    private float			floatValue2;
    private static final WeakHashMap	map = new WeakHashMap();

    /**
     * Constructs from two special values.
     *
     * @param value1		A special value.
     * @param value2		The other special value.
     */
    private DoubleValueVetter(double value1, double value2)
    {
	doubleValue1 = value1;
	doubleValue2 = value2;
	floatValue1 = (float)value1;
	floatValue2 = (float)value2;
    }

    /**
     * Returns an instance of this class corresponding to two special values.
     *
     * @param value1		A special value.
     * @param value2		The other special value.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     */
    static synchronized DoubleValueVetter doubleValueVetter(
	double value1, double value2)
    {
	DoubleValueVetter	vetter = new DoubleValueVetter(value1, value2);
	WeakReference	ref = (WeakReference)map.get(vetter);
	if (ref == null)
	{
	    map.put(vetter, new WeakReference(vetter));
	}
	else
	{
	    DoubleValueVetter	oldVetter = (DoubleValueVetter)ref.get();
	    if (oldVetter == null)
		map.put(vetter, new WeakReference(vetter));
	    else
		vetter = oldVetter;
	}
	return vetter;
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     * @return			If the value equals the special value,
     *				then Float.NaN; otherwise, the original value.
     */
    public float process(float value)
    {
	return
	    value == floatValue1 || value == floatValue2
		? Float.NaN
		: value;
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     * @return			If the value equals the special value,
     *				then Double.NaN; otherwise, the original value.
     */
    public double process(double value)
    {
	return
	    value == doubleValue1 || value == doubleValue2
		? Double.NaN
		: value;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     *				If an element equals the special value,
     *				then that element is set to Float.NaN.
     */
    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    float	value = values[i];
	    if (value == floatValue1 || value == floatValue2)
		values[i] = Float.NaN;
	}
	return values;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     *				If an element equals the special value,
     *				then that element is set to Double.NaN.
     */
    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    if (value == doubleValue1 || value == doubleValue2)
		values[i] = Double.NaN;
	}
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
	    DoubleValueVetter	that = (DoubleValueVetter)obj;
	    equals = this == that || (
		doubleValue1 == that.doubleValue1 &&
		doubleValue2 == that.doubleValue2);
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
	    new Double(doubleValue1).hashCode() ^
	    new Double(doubleValue2).hashCode();
    }
}
