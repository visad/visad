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
import visad.data.in.ArithProg;

/**
 * Provides support for adapting a DODS {@link Float64PrimitiveVector} to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Float64VectorAdapter
    extends NumericVectorAdapter
{
    private final Valuator	valuator;

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
    public Float64VectorAdapter(
	    Float64PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	super(vector, das, factory);
	valuator =
	    Valuator.valuator(
		attributeTable(das, vector.getTemplate()), Attribute.FLOAT64);
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
    public final void setField(
	    PrimitiveVector vector, FieldImpl field, boolean copy)
	throws VisADException, RemoteException
    {
	if (field.isFlatField())
	    ((FlatField)field).setSamples(
		new double[][] {getDoubles(vector, copy)}, /*copy=*/false);
	else
	    field.setSamples(new double[][] {getDoubles(vector, copy)});
    }

    /**
     * Returns the VisAD {@link GriddedSet} corresponding to the metadata of
     * the DODS primitive vector used during construction of this instance and
     * the data values of a compatible DODS primitive vector.
     *
     * @param vector		A DODS primitive vector whose metadata is
     *				compatible with the metadata of the primitive
     *				vector used in construting this instance.
     * @return			A VisAD GriddedSet corresponding to the input.
     *				The (super)class of the object is {@link
     *				visad.Gridded1DSet}.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public GriddedSet griddedSet(PrimitiveVector vector)
	throws VisADException, RemoteException
    {
	double[]	vals = getDoubles(vector, true);
	ArithProg	ap = new ArithProg();
	ap.accumulate(vals);
	return
	    ap.isConsistent()
		/*
		 * NOTE: A Linear1DSet is quite capable of representing double
		 * values.
		 */
		? (Gridded1DSet)new Linear1DSet(
		    getMathType(),
		    ap.getFirst(),
		    ap.getLast(),
		    (int)ap.getNumber())
		: Gridded1DDoubleSet.create(
		    getMathType(), vals, null, null, null);
    }

    /**
     * Returns the numeric values of a compatible DODS primitive vector.
     *
     * @param vec		A DODS primitive vector that is compatible with
     *				the primitive vector used to construct this
     *				instance.
     * @param copy		If true, then a copy is returned.
     * @return			The numeric values of the primitive vector.
     */
    public double[] getDoubles(PrimitiveVector vec, boolean copy)
    {
	Float64PrimitiveVector	vector = (Float64PrimitiveVector)vec;
	double[]		values = new double[vector.getLength()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = vector.getValue(i);
	return valuator.process(values);
    }
}
