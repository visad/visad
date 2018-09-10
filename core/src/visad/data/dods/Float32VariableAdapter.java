/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * Provides support for adapting DODS DFloat32 variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Float32VariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private Float32VariableAdapter(DFloat32 var, DAS das)
	throws VisADException, RemoteException
    {
	AttributeTable	table = attributeTable(das, var);
	realType = realType(var, table);
	valuator = Valuator.valuator(table, Attribute.FLOAT32);
	repSets = new SimpleSet[] {new FloatSet(realType)};
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link 
     * DFloat32}.
     *
     * @param var		The DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			A instance of this class corresponding to the
     *				input.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static Float32VariableAdapter float32VariableAdapter(
	    DFloat32 var, DAS das)
	throws VisADException, RemoteException
    {
	return new Float32VariableAdapter(var, das);
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
     * instances data values in the range of a VisAD {@link FlatField}.
     *
     * @param copy		If true, then the array is cloned.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return copy ? (SimpleSet[])repSets.clone() : repSets;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DFloat32}.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link Real}.
     */
    public DataImpl data(DFloat32 var, boolean copy)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
