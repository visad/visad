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
import visad.*;

/**
 * Provides support for adapting DODS numeric primitive vectors to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class NumericVectorAdapter
    extends VectorAdapter
{
    private final RealType	realType;

    /**
     * Constructs from a DODS vector and a factory for creating DODS variable
     * adapters.
     *
     * @param vector		A DODS vector to be adapted.
     * @param table		The DODS attribute table associated with the
     *				DODS vector.  May be <code>null</code>.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected NumericVectorAdapter(
	    PrimitiveVector vector,
	    AttributeTable table,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	super(vector, table, factory);
	realType = realType(vector.getTemplate(), table);
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
     * Sets the range of a compatible VisAD {@link Field}.  The range values are
     * taken from a DODS primitive vector whose metadata must be compatible with
     * the metadata of the primitive vector used during construction of this
     * instance.  The range values are not copied from the primitive vector,
     * so subsequently modifying them in the field might cause subsequent
     * identical invocations of this method to return different values.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     *				WARNING: Subsequently modify the range values
     *				of the field only under extreme duress.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public abstract void setField(PrimitiveVector vector, FieldImpl field)
	throws VisADException, RemoteException;

    /**
     * Returns the VisAD {@link GriddedSet} corresponding to the metadata of
     * the DODS primitive vector used during construction of this instance and
     * the data values of a compatible DODS primitive vector.
     *
     * @param vector		A DODS primitive vector whose metadata is
     *				compatible with the metadata of the primitive
     *				vector used in construting this instance.
     * @return			A VisAD GriddedSet corresponding to the input.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public abstract GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException;
}
