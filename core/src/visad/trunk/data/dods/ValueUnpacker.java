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
 * Provides support for unpacking data values (i.e. converting them from less
 * spacious types to more spacious types).
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class ValueUnpacker
    extends	ValueProcessor
{
    private static final ValueUnpacker	trivialUnpacker =
	new ValueUnpacker()
	{
	    public double getIncrement()
	    {
		return Double.NaN;
	    }
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
     * Returns an instance of this class corresponding to the attributes of a
     * DODS variable.
     *
     * @param table		A DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				unpacker is returned.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI exception.
     */
    public static ValueUnpacker valueUnpacker(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	ValueUnpacker	unpacker;
	if (table == null)
	{
	    unpacker = trivialUnpacker;
	}
	else
	{
	    double	scale = decode("scale_factor", table, 0);
	    double	offset = decode("add_offset", table, 0);
	    if (scale == scale && scale != 1 &&
		offset == offset && offset != 0)
	    {
		unpacker =
		    ScaleAndOffsetUnpacker.scaleAndOffsetUnpacker(
			scale, offset);
	    }
	    else if (scale == scale && scale != 1)
	    {
		unpacker = ScaleUnpacker.scaleUnpacker(scale);
	    }
	    else if (offset == offset && offset != 0)
	    {
		unpacker = OffsetUnpacker.offsetUnpacker(offset);
	    }
	    else
	    {
		unpacker = trivialUnpacker;
	    }
	}
	return unpacker;
    }

    /**
     * Returns the minimum, potential increment between numeric values.
     * Typically, this is the absolute magnitude of the "scale_factor"
     * attribute.  If the increment is unset, then returns Double.NaN;
     *
     * @return			The increment between numeric values or 
     *				Double.NaN.
     */
    public abstract double getIncrement();
}
