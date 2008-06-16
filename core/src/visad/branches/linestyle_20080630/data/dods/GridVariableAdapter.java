/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.ArrayList;
import visad.*;
import visad.data.*;

/**
 * Provides support for adapting DODS {@link DGrid} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class GridVariableAdapter
    extends	VariableAdapter
{
    private ArrayVariableAdapter	arrayAdapter;
    private FunctionType		funcType;
    private boolean			isFlat;
    private GridVariableMapAdapter[]	domainAdapters;	// VisAD order
    private static RealTupleType	latLonType;
    private static RealTupleType	lonLatType;

    static
    {
	try
	{
	    latLonType =
		new RealTupleType(RealType.Latitude, RealType.Longitude);
	    lonLatType =
		new RealTupleType(RealType.Longitude, RealType.Latitude);
	}
	catch (Exception e)
	{
	    throw new VisADError(
		"visad.data.dods.GridVariableAdapter.<clinit>: " +
		"Couldn't initialize class: " + e);
	}
    }

    private GridVariableAdapter(
	    GridVariableMapAdapter[] domainAdapters,
	    ArrayVariableAdapter arrayAdapter)
	throws BadFormException, VisADException, RemoteException
    {
	MathType	rangeType = arrayAdapter.getFunctionType().getRange();
	funcType = new FunctionType(mathType(domainAdapters), rangeType);
	this.arrayAdapter = arrayAdapter;
	isFlat = isFlat(rangeType);
	this.domainAdapters = domainAdapters;
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link DGrid}.
     *
     * @param grid		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param factory		A factory for creating variable adapters.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static GridVariableAdapter gridVariableAdapter(
	    DGrid grid, DAS das, VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	ArrayVariableAdapter		arrayAdapter;
	MathType			domainType;
	GridVariableMapAdapter[]	domainAdapters;
	try
	{
	    DArray	array = (DArray)grid.getVar(0);
	    int		rank = array.numDimensions();
	    arrayAdapter = factory.arrayVariableAdapter(array, das);
	    domainAdapters = new GridVariableMapAdapter[rank];
	    for (int i = 1; i <= rank; ++i)
	    {
		array = (DArray)grid.getVar(i);
		BaseType	template =
		    array.getPrimitiveVector().getTemplate();
		domainAdapters[rank-i] =	// reverse dimensions
		    factory.gridVariableMapAdapter(array, das);
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		"visad.data.dods.GridVariableAdapter.gridVariableAdapter(...): "
		+ "No such variable: " + e);
	}
	return new GridVariableAdapter(domainAdapters, arrayAdapter);
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
     * Returns the VisAD {@link Set}s that will be used to represent
     * this instances data values in the range of a VisAD {@link
     * visad.data.FileFlatField}.
     *
     * @param copy		If true, then the array is cloned.
     * @return			The VisAD Sets used to represent the data values
     *				in the range of a FlatField.  WARNING: Modify
     *				only under duress.
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return arrayAdapter.getRepresentationalSets(copy);
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DGrid}.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link 
     *				visad.data.FileFlatField}, {@link FlatField}, 
     *				or {@link FieldImpl}.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(DGrid grid, boolean copy)
	throws VisADException, RemoteException
    {
	FieldImpl	field;
	try
	{
	    int		rank = domainAdapters.length;
	    SampledSet	domain;
	    if (rank == 1)
	    {
		domain =
		    (SampledSet)domainAdapters[0].data(
			(DArray)grid.getVar(1), copy);
	    }
	    else
	    {
		Gridded1DSet[]	domainSets = new Gridded1DSet[rank];
		/*
		 * NOTE: "domainAdapters" is in VisAD order (innermost first).
		 */
		for (int i = 0; i < rank; ++i)
		    domainSets[i] = (Gridded1DSet)
			domainAdapters[i].data(
			    (DArray)grid.getVar(rank-i), copy);
		/*
		 * Consolidate contiguous sequences of LinearSet-s into 
		 * Gridded*DSet-s.
		 */
		ArrayList	griddedSets = new ArrayList(rank);
		for (int start = 0; start < rank; )
		{
		    if (!(domainSets[start] instanceof LinearSet))
		    {
			griddedSets.add(domainSets[start++]);
		    }
		    else
		    {
			int	stop;
			for (stop = start + 1;
			    stop < rank &&
				domainSets[stop] instanceof LinearSet;
			    ++stop);
			int	n = stop - start;
			if (n == 1)
			{
			    griddedSets.add(domainSets[start]);
			}
			else
			{
			    RealType[]	rTypes = new RealType[n];
			    double[]	firsts = new double[n];
			    double[]	lasts = new double[n];
			    int[]	lengths = new int[n];
			    for (int i = 0; i < n; ++i)
			    {
				Linear1DSet	lin1D = (Linear1DSet)
				    domainSets[start+i];
				rTypes[i] = (RealType)
				    ((SetType)lin1D.getType())
				    .getDomain().getComponent(0);
				firsts[i] = lin1D.getFirst();
				lasts[i] = lin1D.getLast();
				lengths[i] = lin1D.getLength();
			    }
			    griddedSets.add(
				LinearNDSet.create(
				    new RealTupleType(rTypes),
				    firsts, lasts, lengths));
			}
			start = stop;
		    }
		}
		if (griddedSets.size() == 1)
		{
		    domain = (GriddedSet)griddedSets.get(0);
		}
		else
		{
		    domain =
			new ProductSet(
			    funcType.getDomain(),
			    (GriddedSet[])griddedSets.toArray(
				new GriddedSet[0]));
		}
	    }
	    DArray	array = (DArray)grid.getVar(0);
	    if (isFlat)
	    {
		/*
		 * TODO: Either modify FileFlatField or subclass it to support
		 * a domainFactor(...) method that uses FileFlatField-s.
		 */
		field =
		    new FileFlatField(
			new GridAccessor(domain, array), getCacheStrategy());
	    }
	    else
	    {
		field = new FieldImpl(funcType, domain);
		arrayAdapter.setField(array, field, copy);
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(...): " +
		"No such variable: " + e);
	}
	return field;
    }

    /**
     * Provides support for accessing a DODS DGrid as a VisAD {@link 
     * visad.data.FileFlatField}.
     *
     * <P>Instances are immutable.</P>
     *
     * @author Steven R. Emmerson
     */
    protected class GridAccessor
	extends	FileAccessor
    {
	private final SampledSet	domain;
	private final DArray		array;

	/**
	 * Constructs from a domain and a DODS {@link DArray}.
	 *
	 * @param domain		The domain for the FileFlatField.
	 * @param array			The DODS variable.
	 */
	public GridAccessor(SampledSet domain, DArray array)
	{
	    this.domain = domain;
	    this.array = array;
	}

	/*
	 * Returns the VisAD {@link FunctionType} of this instance.
	 *
	 * @return			The FunctionType of this instance.
	 */
	public FunctionType getFunctionType()
	{
	    return funcType;
	}

	/**
	 * Returns a VisAD {@link FlatField} corresponding to this instance.
	 *
	 * @return			A FlatField corresponding to the
	 *				construction arguments.
	 * @throws VisADException	VisAD failure.
	 * @throws RemoteException	Java RMI failure.
	 */
	public FlatField getFlatField()
	    throws VisADException, RemoteException
	{
	    FlatField	field =
		new FlatField(
		    funcType,
		    domain,
		    (CoordinateSystem[])null,
		    arrayAdapter.getRepresentationalSets(false),
		    (Unit[])null);
	    arrayAdapter.setField(array, field, false);
	    return field;
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param values		Some values.
	 * @param template		A template FlatField.
	 * @param fileLocation		An array of positional parameters.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public void writeFlatField(
	    double[][] values, FlatField template, int[] fileLocation)
	{
	    throw new VisADError(
		getClass().getName() + ".writeFlatField(...): " +
		"Unimplemented method");
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param template		A template FlatField.
	 * @param fileLocation		An array of positional parameters.
	 * @return			<code>null</code>.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public double[][] readFlatField(FlatField template, int[] fileLocation)
	{
	    throw new VisADError(
		getClass().getName() + ".readFlatField(...): " +
		"Unimplemented method");
	}

	/**
	 * Throws a VisADError.
	 *
	 * @param fileLocation		An array of positional parameters.
	 * @param range			The range of a FlatField.
	 * @throws VisADError		This method does nothing and should not
	 *				have been invoked.  Always thrown.
	 */
	public void writeFile(int[] fileLocation, Data range)
	{
	    throw new VisADError(
		getClass().getName() + ".writeFile(...): " +
		"Unimplemented method");
	}
    }
}
