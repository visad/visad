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
 * Provides support for adapting DODS {DUInt32} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class UInt32VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private UInt32VariableAdapter(DUInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	realType = realType(var, table);
	valuator = Valuator.valuator(table);
	long		min = (long)Math.max(valuator.getMin(),          0);
	long		max = (long)Math.min(valuator.getMax(), 4294967295L);
	SimpleSet	repSet;
	if (min == 0 && max <= Integer.MAX_VALUE)
	{
	    repSet = new Integer1DSet(realType, (int)max);
	}
	else
	{
	    long	count = (max-min) + 1;
	    repSet =
		count > Integer.MAX_VALUE
		    ? (SimpleSet)new DoubleSet(realType)
		    : new Linear1DSet(realType, min, max, (int)count);
	}
	repSets = new SimpleSet[] {repSet};
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link
     * DUInt32}.
     *
     * @param var		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param table		The DODS attribute table associated with the
     *				variable.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static UInt32VariableAdapter uInt32VariableAdapter(
	    DUInt32 var, AttributeTable table)
	throws VisADException, RemoteException
    {
	return new UInt32VariableAdapter(var, table);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return realType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instances data values in the range of a VisAD {@link FlatField}.  The
     * same array is returned each time, so modifications to the array will
     * affect all subsequent invocations of this method.
     *
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				only under duress.
     */
    public SimpleSet[] getRepresentationalSets()
	throws VisADException
    {
	return repSets;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to the value of a DODS
     * {@link DUInt16} and the DODS variable used during construction of this
     * instance.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link Real}.  The
     *				VisAD {@link MathType} of the data object will
     *				be based on the DODS variable used during
     *				construction of this instance.
     */
    public DataImpl data(DUInt32 var)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
