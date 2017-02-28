/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for creating adapters that bridge between DODS primitive
 * vectors and the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class VectorAdapterFactory
{
    private static final VectorAdapterFactory	instance =
	new VectorAdapterFactory();

    /**
     * Constructs from nothing.
     */
    protected VectorAdapterFactory()
    {}

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static VectorAdapterFactory vectorAdapterFactory()
    {
	return instance;
    }

    /**
     * Returns the adapter corresponding to a DODS primitive vector.
     *
     * @param vector		A DODS primitive vector.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public VectorAdapter vectorAdapter(
	    PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	VectorAdapter	adapter;
	if (vector instanceof BooleanPrimitiveVector)
	    adapter =
		booleanVectorAdapter(
		    (BooleanPrimitiveVector)vector, das, factory);
	else if (vector instanceof BytePrimitiveVector)
	    adapter =
		byteVectorAdapter(
		    (BytePrimitiveVector)vector, das, factory);
	else if (vector instanceof UInt16PrimitiveVector)
	    adapter =
		uInt16VectorAdapter(
		    (UInt16PrimitiveVector)vector, das, factory);
	else if (vector instanceof Int16PrimitiveVector)
	    adapter =
		int16VectorAdapter(
		    (Int16PrimitiveVector)vector, das, factory);
	else if (vector instanceof UInt32PrimitiveVector)
	    adapter =
		uInt32VectorAdapter(
		    (UInt32PrimitiveVector)vector, das, factory);
	else if (vector instanceof Int32PrimitiveVector)
	    adapter =
		int32VectorAdapter(
		    (Int32PrimitiveVector)vector, das, factory);
	else if (vector instanceof Float32PrimitiveVector)
	    adapter =
		float32VectorAdapter(
		    (Float32PrimitiveVector)vector, das, factory);
	else if (vector instanceof Float64PrimitiveVector)
	    adapter =
		float64VectorAdapter(
		    (Float64PrimitiveVector)vector, das, factory);
	else
	    adapter =
		baseTypeVectorAdapter(
		    (BaseTypePrimitiveVector)vector, das, factory);
	return adapter;
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * BooleanPrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public BooleanVectorAdapter booleanVectorAdapter(
	    BooleanPrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new BooleanVectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * BytePrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ByteVectorAdapter byteVectorAdapter(
	    BytePrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new ByteVectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * UInt16PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt16VectorAdapter uInt16VectorAdapter(
	    UInt16PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new UInt16VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * Int16PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int16VectorAdapter int16VectorAdapter(
	    Int16PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Int16VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * UInt32PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt32VectorAdapter uInt32VectorAdapter(
	    UInt32PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new UInt32VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * Int32PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int32VectorAdapter int32VectorAdapter(
	    Int32PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Int32VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * Float32PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float32VectorAdapter float32VectorAdapter(
	    Float32PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Float32VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * Float64PrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float64VectorAdapter float64VectorAdapter(
	    Float64PrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return new Float64VectorAdapter(vector, das, factory);
    }

    /**
     * Returns the adapter corresponding to a DODS {@link
     * BaseTypePrimitiveVector}.
     *
     * @param vector		A DODS primitive vector of the appropriate type.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS vector is embedded.
     * @param factory		A factory for creating adapters of DODS
     *				variables.
     * @return			The adapter corresponding to the DODS
     *				primitive vector.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public BaseTypeVectorAdapter baseTypeVectorAdapter(
	    BaseTypePrimitiveVector vector,
	    DAS das,
	    VariableAdapterFactory factory)
	throws VisADException, RemoteException
    {
	return
	    BaseTypeVectorAdapter.baseTypeVectorAdapter(vector, das, factory);
    }
}
