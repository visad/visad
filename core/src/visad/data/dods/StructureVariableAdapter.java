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
import java.util.ArrayList;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;

/**
 * Provides support for adapting DODS {@link DStructure} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class StructureVariableAdapter
    extends	VariableAdapter
{
    private final MathType		mathType;
    private final VariableAdapter[]	adapters;
    private final boolean		isFlat;
    private final SimpleSet[]		repSets;

    private StructureVariableAdapter(
	    DStructure structure, DAS das, VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	ArrayList	setList = new ArrayList();
	adapters = new VariableAdapter[structure.elementCount()];
	for (int i = 0; i < adapters.length; ++i)
	{
	    BaseType	var;
	    try
	    {
		var = structure.getVar(i);
	    }
	    catch (NoSuchVariableException e)
	    {
		throw new BadFormException(
		    getClass().getName() + ".data(...): " +
		    "DStructure is missing variable " + i + ": " + e);
	    }
	    adapters[i] = factory.variableAdapter(var, das);
	    SimpleSet[]	setArray = adapters[i].getRepresentationalSets(false);
	    for (int j = 0; j < setArray.length; ++j)
		setList.add(setArray[j]);
	}
	mathType = mathType(adapters);
	isFlat = isFlat(mathType);
	repSets = (SimpleSet[])setList.toArray(new SimpleSet[0]);
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link 
     * DStructure}.
     *
     * @param structure		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param factory		A factory for creating variable adapters.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static StructureVariableAdapter structureVariableAdapter(
	    DStructure structure, DAS das, VariableAdapterFactory factory)
	throws BadFormException, VisADException, RemoteException
    {
	return new StructureVariableAdapter(structure, das, factory);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return mathType;
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
	return copy ? (SimpleSet[])repSets.clone() : repSets;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to the values of a DODS
     * {@link DStructure} and the DODS variable used during construction of this
     * instance.
     *
     * @param structure		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be determined by the
     *				components of the structure used during
     *				construction of this instance.  Will be
     *				<code>null</code> if the construction
     *				structure had no components.
     * @throws BadFormException	The DODS variable is corrupt.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(DStructure structure, boolean copy)
	throws BadFormException, VisADException, RemoteException
    {
	DataImpl	data;
	try
	{
	    if (adapters.length == 0)
	    {
		data = null;
	    }
	    else if (adapters.length == 1)
	    {
		data = adapters[0].data(structure.getVar(0), copy);
	    }
	    else
	    {
		if (isFlat)
		{
		    Real[]	components = new Real[adapters.length];
		    for (int i = 0; i < adapters.length; ++i)
			components[i] = 
			    (Real)adapters[i].data(structure.getVar(i), copy);
		    data = new RealTuple(components);
		}
		else
		{
		    DataImpl[]	components = new DataImpl[adapters.length];
		    for (int i = 0; i < adapters.length; ++i)
			components[i] =
			    adapters[i].data(structure.getVar(i), copy);
		    data = new Tuple(components);
		}
	    }
	}
	catch (NoSuchVariableException e)
	{
	    throw new BadFormException(
		getClass().getName() + ".data(...): " +
		"DStructure is missing variable: " + e);
	}
	return data;
    }
}
