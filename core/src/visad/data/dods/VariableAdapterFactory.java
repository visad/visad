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
import visad.data.BadFormException;
import visad.VisADException;

/**
 * Provides support for creating adapters that bridge between DODS variables
 * and the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class VariableAdapterFactory
{
    private static final VariableAdapterFactory	instance =
	new VariableAdapterFactory();
    private static final VectorAdapterFactory	vectorAdapterFactory =
	VectorAdapterFactory.vectorAdapterFactory();

    /**
     * Constructs from nothing.
     */
    protected VariableAdapterFactory()
    {}

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static VariableAdapterFactory variableAdapterFactory()
    {
	return instance;
    }

    /**
     * Returns the adapter corresponding to a DODS variable.
     *
     * @param var		A DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public VariableAdapter variableAdapter(BaseType var, DAS das)
	throws BadFormException, VisADException, RemoteException
    {
	VariableAdapter	adapter;
	if (var instanceof DString)
	    adapter = stringVariableAdapter((DString)var, das);
	else if (var instanceof DBoolean)
	    adapter = booleanVariableAdapter((DBoolean)var, das);
	else if (var instanceof DByte)
	    adapter = byteVariableAdapter((DByte)var, das);
	else if (var instanceof DUInt16)
	    adapter = uInt16VariableAdapter((DUInt16)var, das);
	else if (var instanceof DInt16)
	    adapter = int16VariableAdapter((DInt16)var, das);
	else if (var instanceof DUInt32)
	    adapter = uInt32VariableAdapter((DUInt32)var, das);
	else if (var instanceof DInt32)
	    adapter = int32VariableAdapter((DInt32)var, das);
	else if (var instanceof DFloat32)
	    adapter = float32VariableAdapter((DFloat32)var, das);
	else if (var instanceof DFloat64)
	    adapter = float64VariableAdapter((DFloat64)var, das);
	else if (var instanceof DStructure)
	    adapter = structureVariableAdapter((DStructure)var, das);
	else if (var instanceof DList)
	    adapter = listVariableAdapter((DList)var, das);
	else if (var instanceof DSequence)
	    adapter = sequenceVariableAdapter((DSequence)var, das);
	else if (var instanceof DArray)
	    adapter = arrayVariableAdapter((DArray)var, das);
	else if (var instanceof DGrid)
	    adapter = gridVariableAdapter((DGrid)var, das);
	else
	    throw new BadFormException(
		getClass().getName() + ".variableAdapter(...): " +
		"Unknown DODS type: " + var.getTypeName());
	return adapter;
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DString}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public StringVariableAdapter stringVariableAdapter(DString var, DAS das)
	throws VisADException, RemoteException
    {
	return StringVariableAdapter.stringVariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DBoolean}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public BooleanVariableAdapter booleanVariableAdapter(DBoolean var, DAS das)
	throws VisADException, RemoteException
    {
	return BooleanVariableAdapter.booleanVariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DByte}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ByteVariableAdapter byteVariableAdapter(DByte var, DAS das)
	throws VisADException, RemoteException
    {
	return ByteVariableAdapter.byteVariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DUInt16}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt16VariableAdapter uInt16VariableAdapter(DUInt16 var, DAS das)
	throws VisADException, RemoteException
    {
	return UInt16VariableAdapter.uInt16VariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DInt16}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int16VariableAdapter int16VariableAdapter(DInt16 var, DAS das)
	throws VisADException, RemoteException
    {
	return Int16VariableAdapter.int16VariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DUInt32}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt32VariableAdapter uInt32VariableAdapter(DUInt32 var, DAS das)
	throws VisADException, RemoteException
    {
	return UInt32VariableAdapter.uInt32VariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DInt32}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int32VariableAdapter int32VariableAdapter(DInt32 var, DAS das)
	throws VisADException, RemoteException
    {
	return Int32VariableAdapter.int32VariableAdapter(var, das);
    }
    /**
     * Returns the adapter corresponding to a DODS {@link DFloat32}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float32VariableAdapter float32VariableAdapter(DFloat32 var, DAS das)
	throws VisADException, RemoteException
    {
	return Float32VariableAdapter.float32VariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DFloat64}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float64VariableAdapter float64VariableAdapter(DFloat64 var, DAS das)
	throws VisADException, RemoteException
    {
	return Float64VariableAdapter.float64VariableAdapter(var, das);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DStructure}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public StructureVariableAdapter structureVariableAdapter(
	    DStructure var, DAS das)
	throws VisADException, RemoteException
    {
	return
	    StructureVariableAdapter.structureVariableAdapter(var, das, this);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DList}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ListVariableAdapter listVariableAdapter(
	    DList var, DAS das)
	throws VisADException, RemoteException
    {
	return ListVariableAdapter.listVariableAdapter(var, das, this);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DSequence}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public SequenceVariableAdapter sequenceVariableAdapter(
	    DSequence var, DAS das)
	throws VisADException, RemoteException
    {
	return
	    SequenceVariableAdapter.sequenceVariableAdapter(var, das, this);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DArray}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ArrayVariableAdapter arrayVariableAdapter(DArray var, DAS das)
	throws VisADException, RemoteException
    {
	return ArrayVariableAdapter.arrayVariableAdapter(var, das, this);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link DGrid}.
     *
     * @param var		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				variable.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public GridVariableAdapter gridVariableAdapter(DGrid var, DAS das)
	throws VisADException, RemoteException
    {
	return GridVariableAdapter.gridVariableAdapter(var, das, this);
    }

    /**
     * Returns the adapter corresponding to the coordinate mapping-
     * vectors of a DODS {@link DGrid}.
     *
     * @param array		The coordinate mapping vectors of a DODS {@link
     *				DGrid}.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the coordinate
     *				mapping-vectors of the DODS grid.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public GridVariableMapAdapter gridVariableMapAdapter(
	    DArray array, DAS das)
	throws VisADException, RemoteException
    {
	return 
	    GridVariableMapAdapter.gridVariableMapAdapter(array, das, this);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link PrimitiveVector}.
     *
     * @param vector		An appropriate DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public VectorAdapter vectorAdapter(PrimitiveVector vector, DAS das)
	throws VisADException, RemoteException
    {
	return vectorAdapterFactory.vectorAdapter(vector, das, this);
    }
}
