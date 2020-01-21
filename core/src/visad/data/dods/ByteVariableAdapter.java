/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for adapting DODS DByte variables to the
 * {@link visad.data.in} context.
 */
public class ByteVariableAdapter
    extends	VariableAdapter
{
    private final RealType	realType;
    private final Valuator	valuator;
    private final SimpleSet[]	repSets;

    private ByteVariableAdapter(DByte var, DAS das)
	throws VisADException, RemoteException
    {
	AttributeTable	table = attributeTable(das, var);
	realType = realType(var, table);
	valuator = Valuator.valuator(table, Attribute.BYTE);
	repSets = new SimpleSet[] {valuator.getRepresentationalSet(realType)};
    }

    public static ByteVariableAdapter byteVariableAdapter(DByte var, DAS das)
	throws VisADException, RemoteException
    {
	return new ByteVariableAdapter(var, das);
    }

    public MathType getMathType()
    {
	return realType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instances data values in the range of a VisAD {@link FlatField}.
     *
     * @param copy		If true, then the data values are copied.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return copy ? (SimpleSet[])repSets.clone() : repSets;
    }

    /**
     * Returns a VisAD data object corresponding to a DODS {@link DByte}.
     * The DByte must be compatible with the the DByte used to construct
     * this instance.  In particular, the name of the DByte used to construct
     * this instance will be used in naming the returned VisAD {@link Real}.
     *
     * @param var		The DODS variable.  The variable must be
     *				compatible with the the variable used to 
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A corresponding VisAD data object.  The class of
     *				the object will be {@link Real}.
     */
    public DataImpl data(DByte var, boolean copy)
    {
	return new Real(realType, valuator.process(var.getValue()));
    }
}
