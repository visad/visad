/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * Provides support for unpacking data values (i.e. converting them from smaller
 * types to bigger types).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class ValueUnpacker
    extends	ValueProcessor
{
    private static final ValueUnpacker	TRIVIAL_UNPACKER =
	new ValueUnpacker()
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
    protected ValueUnpacker()
    {}

    /**
     * Returns the trivial value unpacker.  The trivial value unpacker does
     * nothing to the value during processing and its {@link #getIncrement()}
     * method returns NaN.
     *
     * @return			The trivial value unpacker.
     */
    public static ValueUnpacker valueUnpacker()
    {
	return TRIVIAL_UNPACKER;
    }

    /**
     * Returns the minimum, potential increment between numeric values.
     * Typically, this is the absolute magnitude of a "scale factor" attribute.
     * If the increment is undefined or not applicable, then Double.NaN is
     * returned.  This method should be overridden in appropriate subclasses.
     *
     * @return			Double.NaN.
     */
    public double getIncrement()
    {
	return Double.NaN;
    }
}
