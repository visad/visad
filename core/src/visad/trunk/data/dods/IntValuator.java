/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
import visad.*;

/**
 * Provides support for processing integer numeric values in a DODS dataset.
 * Processing includes checking for validity and unpacking.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class IntValuator
    extends	Valuator
{
    private final double	lower;	// lower unpacked value limit
    private final double	upper;	// upper unpacked value limit

    /**
     * Constructs from the attributes of a DODS integer variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @param lower		Natural lower limit on packed values.
     * @param upper		Natural upper limit on packed values.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected IntValuator(AttributeTable table, long lower, long upper)
	throws BadFormException, VisADException, RemoteException
    {
	super(table);
	double	limitA = process(Math.max(lower, ranger.getMin()));
	double	limitB = process(Math.min(upper, ranger.getMax()));
	if (limitA < limitB)
	{
	    this.lower = limitA;
	    this.upper = limitB;
	}
	else
	{
	    this.lower = limitB;
	    this.upper = limitA;
	}
    }

    /**
     * Returns the set used to represent unpacked, numeric values associated
     * with this instance in the range of a VisAD {@link FlatField}.
     *
     * @return realType		The VisAD real-type for the set.
     * @return			The set used to represent numeric values
     *				associated with this instance.
     * @throws VisADException	VisAD failure.
     */
    public SimpleSet getRepresentationalSet(RealType realType)
	throws VisADException
    {
	SimpleSet	repSet;
	double		inc = unpacker.getIncrement();
	if (inc == inc && inc != 1)
	{
	    repSet =
		1+((upper-lower)/inc) < Integer.MAX_VALUE
		    ? (SimpleSet)new Linear1DSet(
			realType, lower, upper,
			1+(int)Math.round((upper-lower)/inc))
		    : new FloatSet(realType);
	}
	else
	{
	    /*
	     * The minimum, potential increment between values is one.
	     */
	    if (lower == 0)
	    {
		repSet =
		    1+upper <= Integer.MAX_VALUE
			? (SimpleSet)new Integer1DSet(
			    realType, 1+(int)Math.round(upper))
			: new FloatSet(realType);
	    }
	    else
	    {
		repSet =
		    1+upper-lower < Integer.MAX_VALUE
			? (SimpleSet)new Linear1DSet(realType, lower, upper,
			    1+(int)Math.round((upper-lower)))
			: new FloatSet(realType);
	    }
	}
	return repSet;
    }
}
