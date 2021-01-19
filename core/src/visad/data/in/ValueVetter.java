/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Arrays;

/**
 * Provides support for verifying data values (i.e. seeing that they aren't
 * equal to one or more special values).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class ValueVetter
    extends	ValueProcessor
{
    private static final ValueVetter	trivialVetter =
	new ValueVetter()
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
    protected ValueVetter()
    {}

    /**
     * Returns an instance of this class given an array of special values.
     * If the trivial vetter can be returned, then it is.  During processing,
     * if a value to be processed equals a special value, then it is replaced
     * with a NaN.
     *
     * @param values		The special values.  May be <code>null</code>.
     *				Elements may be NaN.  The array might not be
     *				cloned and might be reordered.
     */
    public static ValueVetter valueVetter(double[] values)
    {
	ValueVetter	vetter;
	if (values == null || values.length == 0)
	{
	    vetter = trivialVetter;
	}
	else
	{
	    int		count = 0;
	    double	prev = Double.NaN;
	    Arrays.sort(values);
	    for (int i = 0; i < values.length; ++i)
	    {
		double	value = values[i];
		if (value == value && !(value == prev))
		{
		    count++;
		    prev = value;
		}
	    }
	    if (count == 0)
	    {
		vetter = trivialVetter;
	    }
	    else
	    {
		if (count != values.length)
		{
		    double[]	vals = new double[count];
		    count = 0;
		    prev = Double.NaN;
		    for (int i = 0; i < values.length; ++i)
		    {
			double	value = values[i];
			if (value == value && !(value == prev))
			{
			    vals[count++] = value;
			    prev = value;
			}
		    }
		    values = vals;
		}
		if (values.length == 1)
		    vetter = SingleValueVetter.singleValueVetter(values[0]);
		else if (values.length == 2)
		    vetter = DoubleValueVetter.doubleValueVetter(
			values[0], values[1]);
		else
		    vetter = MultipleValueVetter.multipleValueVetter(values);
	    }
	}
	return vetter;
    }
}
