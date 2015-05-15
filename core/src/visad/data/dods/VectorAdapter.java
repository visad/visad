/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for adapting DODS primitive vectors to the VisAD
 * data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class VectorAdapter
    extends	Adapter
{
    private final VariableAdapter	varAdapter;

    /**
     * Constructs from a DODS vector and a factory for creating DODS variable
     * adapters.
     *
     * @param vector		A DODS vector to be adapted.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected VectorAdapter(
	    PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	varAdapter = factory.variableAdapter(vector.getTemplate(), das);
    }

    /**
     * Returns the adapter of the DODS variable that underlies this instance.
     *
     * @return			The adapter of the DODS variable that underlies
     *				this instance.
     */
    protected final VariableAdapter getVariableAdapter()
    {
	return varAdapter;
    }

    /**
     * Returns the VisAD math-type of this instance.
     *
     * @return			The math-type this instance.
     */
    public final MathType getMathType()
    {
	return varAdapter.getMathType();
    }

    /**
     * Indicates whether or not the VisAD {@link MathType} of this instance is
     * "flat".  A MathType is flat if it comprises a VisAD {@link RealType},
     * {@link RealTupleType}, or a {@link Tuple} of RealTypes and 
     * RealTupleTypes.
     *
     * @return			<code>true</code> if and only if the MathType of
     *				this instance is "flat".
     */
    public boolean isFlat()
    {
	return isFlat(getMathType());
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instance's data values in the range of a VisAD {@link FlatField}.
     *
     * @param copy		If true, then the array is cloned.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				the returned array only under extreme duress.
     */
    public final SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return varAdapter.getRepresentationalSets(copy);
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
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void setField(PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException, RemoteException
    {
	if (vector instanceof BooleanPrimitiveVector)
	    setField((BooleanPrimitiveVector)vector, field, copy);
	else if (vector instanceof BytePrimitiveVector)
	    setField((BytePrimitiveVector)vector, field, copy);
	else if (vector instanceof UInt16PrimitiveVector)
	    setField((UInt16PrimitiveVector)vector, field, copy);
	else if (vector instanceof Int16PrimitiveVector)
	    setField((Int16PrimitiveVector)vector, field, copy);
	else if (vector instanceof UInt32PrimitiveVector)
	    setField((UInt32PrimitiveVector)vector, field, copy);
	else if (vector instanceof Int32PrimitiveVector)
	    setField((Int32PrimitiveVector)vector, field, copy);
	else if (vector instanceof Float32PrimitiveVector)
	    setField((Float32PrimitiveVector)vector, field, copy);
	else if (vector instanceof Float64PrimitiveVector)
	    setField((Float64PrimitiveVector)vector, field, copy);
	else
	    setField((BaseTypePrimitiveVector)vector, field, copy);
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    BooleanPrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BooleanPrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @param field		A VisAD field to have its range values set.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    BytePrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BytePrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    UInt16PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(UInt16PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    Int16PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Int16PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    UInt32PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(UInt32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    Int32PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Int32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    Float32PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Float32PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     */
    public void setField(
	    Float64PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(Float64PrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @param field		A VisAD field to have its range values set.
     * @param copy		If true, then the range values are copied from
     *				the primitive vector.
     * @throws VisADException	The vector has the wrong DODS type.
     * @throws RemoteException	Java RMI failure.
     */
    public void setField(
	    BaseTypePrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".setField(BaseTypePrimitiveVector,...): " +
	    "Wrong type of vector");
    }

    /**
     * Throws a {@link VisADException}.  Override this method in subclasses
     * where appropriate.
     *
     * @param vector		A DODS primitive vector whose data values are
     *				to be used to set the range of the VisAD field.
     * @throws VisADException	The vector has the wrong DODS type.
     * @throws RemoteException	Java RMI failure.
     */
    
    public GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".griddedSet(PrimitiveVector): " +
	    "Wrong type of vector");
    }
}
