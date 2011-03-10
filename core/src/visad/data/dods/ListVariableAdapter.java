/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.*;

/**
 * Provides support for adapting a DODS {@link DList} variable to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class ListVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType	funcType;
    private final VectorAdapter	vectorAdapter;

    private ListVariableAdapter(
	    DList list, DAS das, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter =
	    factory.vectorAdapter(list.getPrimitiveVector(), das);
	funcType =
	    new FunctionType(RealType.Generic, vectorAdapter.getMathType());
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link DList}.
     *
     * @param list		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param factory		A factory for creating variable adapters.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static ListVariableAdapter listVariableAdapter(
	    DList list, DAS das, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ListVariableAdapter(list, das, factory);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return funcType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent this
     * instances data values in the range of a VisAD {@link FlatField}.
     *
     * @param copy		If true, then the array is cloned.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				only under duress.
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return vectorAdapter.getRepresentationalSets(copy);
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DList}.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then the data values are copied.
     * @return			The VisAD data object of this instance.
     *				The class of the object will be {@link
     *				visad.data.FileFlatField} {@link FlatField}, or
     *				{@link FieldImpl}.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(DList list, boolean copy)
	throws VisADException, RemoteException
    {
	SampledSet	domain = new Integer1DSet(list.getLength());
	PrimitiveVector	vector = list.getPrimitiveVector();
	FieldImpl	field;
	if (funcType.getFlat())
	{
	    /*
	     * TODO: Either modify FileFlatField or subclass it to support
	     * a domainFactor(...) method that uses FileFlatField-s.
	     */
	    field =
		new FileFlatField(
		    new VectorAccessor(funcType, vectorAdapter, domain, vector),
		    getCacheStrategy());
	}
	else
	{
	    field = new FieldImpl(funcType, domain);
	    vectorAdapter.setField(vector, field, copy);
	}
	return field;
    }
}
