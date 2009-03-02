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
import java.util.*;

/**
 * Provides support for verifying data values (i.e. seeing that they aren't
 * equal to any special values).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
final class MultipleValueVetter
    extends	ValueVetter
{
    private double[]			doubleValues;
    private float[]			floatValues;
    private static final WeakHashMap	map = new WeakHashMap();

    /**
     * Constructs from an array of special values.
     *
     * @param specialValues	The special values.  Elements may be NaN.
     *				WARNING: The array isn't cloned, so don't
     *				modify it.
     */
    private MultipleValueVetter(double[] values)
    {
	this.doubleValues = values;
	floatValues = new float[values.length];
	for (int i = 0; i < values.length; ++i)
	    floatValues[i] = (float)values[i];
    }

    /**
     * Returns an instance of this class corresponding to special values.
     *
     * @param specialValues	The special values.  Elements may be NaN.
     *				WARNING: The array isn't cloned, so don't
     *				modify it.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     */
    static synchronized MultipleValueVetter multipleValueVetter(
	double[] values)
    {
	MultipleValueVetter	vetter = new MultipleValueVetter(values);
	WeakReference	ref = (WeakReference)map.get(vetter);
	if (ref == null)
	{
	    map.put(vetter, new WeakReference(vetter));
	}
	else
	{
	    MultipleValueVetter	oldVetter = (MultipleValueVetter)ref.get();
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
     * @return			If the value equals one of the special values,
     *				then Float.NaN; otherwise, the original value.
     */
    public float process(float value)
    {
	for (int i = 0; i < floatValues.length; ++i)
	    if (value == floatValues[i])
		return Float.NaN;
	return value;
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     * @return			If the value equals one of the special values,
     *				then Double.NaN; otherwise, the original value.
     */
    public double process(double value)
    {
	for (int i = 0; i < doubleValues.length; ++i)
	    if (value == doubleValues[i])
		return Double.NaN;
	return value;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     *				If an element equals one of the special values,
     *				then that element is set to Float.NaN.
     */
    public float[] process(float[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    for (int j = 0; j < floatValues.length; ++j)
		if (values[i] == floatValues[j])
		{
		    values[i] = Float.NaN;
		    break;
		}
	}
	return values;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     *				If an element equals one of the special values,
     *				then that element is set to Float.NaN.
     */
    public double[] process(double[] values)
    {
	for (int i = 0; i < values.length; ++i)
	{
	    for (int j = 0; j < doubleValues.length; ++j)
		if (values[i] == doubleValues[j])
		{
		    values[i] = Double.NaN;
		    break;
		}
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
	    MultipleValueVetter	that = (MultipleValueVetter)obj;
	    equals = this == that ||
		Arrays.equals(doubleValues, that.doubleValues);
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
	int	code = 0;
	for (int i = 0; i < doubleValues.length; ++i)
	    code ^= new Double(doubleValues[i]).hashCode();
	return code;
    }
}
