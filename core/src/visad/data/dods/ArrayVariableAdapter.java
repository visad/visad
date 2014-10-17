/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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
import dods.dap.Server.InvalidParameterException;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;

/**
 * Provides support for adapting DODS {@link DArray} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class ArrayVariableAdapter
    extends	VariableAdapter
{
    private final FunctionType		funcType;
    private final VectorAdapter		vectorAdapter;

    private ArrayVariableAdapter(
	    DArray array,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	vectorAdapter = factory.vectorAdapter(array.getPrimitiveVector(), das);
	int		rank = array.numDimensions();
	RealType[]	realTypes = new RealType[rank];
	for (int i = 0; i < rank; ++i)
	{
	    try
	    {
		/*
		 * The following reverses the dimension order to conform to
		 * the VisAD convention of innermost first.
		 */
		realTypes[rank-1-i] =
		    RealType.getRealType(
			array.getDimension(i).getName() + "_ndx");
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".<init>: " +
		    "Couldn't get DArray dimension: " + e);
	    }
	}
	funcType =
	    new FunctionType(mathType(realTypes), vectorAdapter.getMathType());
    }

    /**
     * Returns an instance corresponding to a DODS {@link DArray}.
     *
     * @param array		The DODS DArray.  Only the DODS metadata is 
     *				used: the array needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param factory		A factory for creating variable adapters.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static ArrayVariableAdapter arrayVariableAdapter(
	    DArray array, DAS das, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ArrayVariableAdapter(array, das, factory);
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
     * Returns the VisAD {@link FunctionType} of this instance.
     *
     * @return			The FunctionType of this instance.
     */
    public FunctionType getFunctionType()
    {
	return funcType;
    }

    /**
     * Returns the VisAD {@link Set}s that will be used to represent the data
     * values in the range of a VisAD {@link FlatField}.
     *
     * @param copy		If true, then the array is copied.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return vectorAdapter.getRepresentationalSets(copy);
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DArray}.
     *
     * @param array		The DODS {@link DArray} to have the 
     *				corresponding VisAD data object returned.
     *				The array must be compatible with the array
     *				used to construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			The VisAD data object of this instance.  The
     *				object will be a {@link FieldImpl} and may be
     *				a {@link FlatField} or {@link 
     *				visad.data.FileFlatField}.
     * @throws VisADException	VisAD failure.  Possible the array wasn't
     *				compatible with the array used to construct this
     *				instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(DArray array, boolean copy)
	throws VisADException, RemoteException
    {
	RealTupleType	domainType = funcType.getDomain();
	int		rank = domainType.getDimension();
	int[]		firsts = new int[rank];
	int[]		lasts = new int[rank];
	int[]		lengths = new int[rank];
	boolean		allIntegerSets = true;
	for (int i = 0; i < rank; ++i)
	{
	    int			j = rank - 1 - i;	// reverse dimensions
	    DArrayDimension	dim;
	    try
	    {
		dim = array.getDimension(i);
	    }
	    catch (InvalidParameterException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".data(DArray,...): " +
		    "Couldn't get DArray dimension: " + e);
	    }
	    int			first = dim.getStart();
	    int			last = dim.getStop();
	    int			stride = dim.getStride();
	    firsts[j] = first;
	    lasts[j] = last;
	    lengths[j] = 1 + (last - first) / stride;
	    allIntegerSets &=
		((stride == 1 && first == 0) || (stride == -1 && last == 0));
	}
	SampledSet	domain =
	    allIntegerSets
		? (SampledSet)IntegerNDSet.create(domainType, lengths)
		: (SampledSet)LinearNDSet.create(
		    domainType, doubleArray(firsts), doubleArray(lasts),
		    lengths);
	PrimitiveVector	vector = array.getPrimitiveVector();
	FieldImpl	field;
	if (vectorAdapter.isFlat())
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

    /**
     * Sets a compatible VisAD {@link Field}.  This method is used by {@link
     * GridVariableAdapter} for the DArray portion of a DODS DGrid.
     *
     * @param array		The DODS {@link DArray} to be used to set a
     *				compatible VisAD Field.  The array must be
     *				compatible with the array used to construct this
     *				instance.
     * @param copy		If true, then the data values are copied.
     * @param field		The VisAD Field to be set.  The field must be
     *				compatible with the array.
     */
    public void setField(DArray array, FieldImpl field, boolean copy)
	throws VisADException, RemoteException
    {
	vectorAdapter.setField(array.getPrimitiveVector(), field, copy);
    }

    /**
     * Returns an array of doubles corresponding to an array of ints.
     *
     * @param ints		The array of doubles.
     * @return			The corresponding array of doubles.
     */
    private double[] doubleArray(int[] ints)
    {
	double[]	doubles = new double[ints.length];
	for (int i = 0; i < ints.length; ++i)
	    doubles[i] = ints[i];
	return doubles;
    }
}
