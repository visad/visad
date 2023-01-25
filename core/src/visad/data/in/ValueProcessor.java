/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

$Id: ValueProcessor.java,v 1.9 2009-03-02 23:35:49 curtis Exp $
*/

package visad.data.in;

/**
 * Provides support for processing primitive data values (i.e. checking their
 * values, converting them, etc.).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class ValueProcessor
{
    /**
     * The trivial processor (does nothing).
     */
    protected static final ValueProcessor	trivialProcessor =
	new ValueProcessor()
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
    protected ValueProcessor()
    {}

    /**
     * Processes a float value.
     *
     * @param value		The value to be processed.
     * @return			The processed value.
     */
    public abstract float process(float value);

    /**
     * Processes a double value.
     *
     * @param value		The value to be processed.
     * @return			The processed value.
     */
    public abstract double process(double value);

    /**
     * Processes float values.
     *
     * @param values		The values to be processed.
     * @return			The processed values (same array as input).
     */
    public abstract float[] process(float[] values);

    /**
     * Processes double values.
     *
     * @param values		The values to be processed.
     * @return			The processed values (same array as input).
     */
    public abstract double[] process(double[] values);
}
