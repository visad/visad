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
*/

package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;

/**
 * Provides support for adapting a DODS {@link BaseTypePrimitiveVector} to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class BaseTypeVectorAdapter
    extends VectorAdapter
{
    /**
     * Constructs from a DODS vector and a factory for creating DODS variable
     * adapters.
     *
     * @param vector		A DODS vector to be adapted.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected BaseTypeVectorAdapter(
	    BaseTypePrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, das, factory);
    }

    /**
     * Returns an instance of this class corresponding to a DODS vector and a
     * factory for creating DODS variable adapters.
     *
     * @param vector		A DODS vector to be adapted.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static BaseTypeVectorAdapter baseTypeVectorAdapter(
	    BaseTypePrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new BaseTypeVectorAdapter(vector, das, factory);
    }

    /**
     * Sets the range of a compatible VisAD {@link Field}.  The range values are
     * taken from a DODS primitive vector whose metadata must be compatible with
     * the metadata of the primitive vector used during construction of this
     * instance.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then range values are copied from the
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void setField(
	    BaseTypePrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException, RemoteException
    {
	int		length = vector.getLength();
	for (int i = 0; i < length; ++i)
	    field.setSample(
		i,
		getVariableAdapter().data(vector.getValue(i), copy),
		false);
    }
}
