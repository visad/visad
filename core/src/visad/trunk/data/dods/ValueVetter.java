/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.*;

/**
 * Provides support for verifying data values (i.e. seeing that they aren't
 * equal to a "missing" or "fill" value).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class ValueVetter
    extends	ValueProcessor
{
    private double			fill = Double.NaN;
    private double			missing = Double.NaN;
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
     * Constructs from a fill-value and a missing value.
     *
     * @param fill		The fill-value.  May be NaN.
     * @param missing		The missing-value value.  May be NaN.
     */
    protected ValueVetter(double fill, double missing)
    {
	this.fill = fill;
	this.missing = missing;
    }

    /**
     * Returns an instance of this class corresponding to the attributes of a
     * DODS variable.
     *
     * @param table		The DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				vetter is returned.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static ValueVetter valueVetter(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	ValueVetter	vetter;
	if (table == null)
	{
	    vetter = trivialVetter;
	}
	else
	{
	    double	fill = decode("_FillValue", table, 0);
	    double	missing = decode("missing_value", table, 0);
	    vetter =
		fill == fill || missing == missing
		    ? new ValueVetter(fill, missing)
		    : trivialVetter;
	}
	return vetter;
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public float process(float value)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	return
	    value == missing || value == fill
		? Float.NaN
		: value;
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public double process(double value)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	return
	    value == missing || value == fill
		? Double.NaN
		: value;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     */
    public float[] process(float[] values)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    values[i] =
		value == missing || value == fill
		    ? Float.NaN
		    : (float)value;
	}
	return values;
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     * @return			Vetted values (same array as input).
     */
    public double[] process(double[] values)
    {
	/*
	 * NB: An unset test value will be NaN -- so mind the sense of the 
	 * comparisons.
	 */
	for (int i = 0; i < values.length; ++i)
	{
	    double	value = values[i];
	    values[i] =
		value == missing || value == fill
		    ? Double.NaN
		    : value;
	}
	return values;
    }
}
