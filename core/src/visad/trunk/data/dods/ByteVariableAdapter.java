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
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DByte variables to the
 * {@link visad.data.in} context.
 */
public class ByteVariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private ByteVariableAdapter(DByte var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table, Attribute.BYTE);
	int	min = (int)Math.round(valuator.getMin());
	int	max = (int)Math.round(valuator.getMax());
	repSets =
	    new SimpleSet[] {
		min == 0
		    ? (SimpleSet)new Integer1DSet(realType, max)
		    : new Linear1DSet(realType, min, max, (max-min)+1)};
    }

    public static ByteVariableAdapter byteVariableAdapter(
	    DByte var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new ByteVariableAdapter(var, table);
    }

    public MathType getMathType()
    {
	return realType;
    }

    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return repSets;
    }

    public DataImpl data(DByte var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
