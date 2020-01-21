/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
import visad.data.BadFormException;

/**
 * Provides support for adapting DODS objects to the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class VariableAdapter
    extends	Adapter
{
    private static final SimpleSet[]		defaultRepSets =
	new SimpleSet[] {null};

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public abstract MathType getMathType();

    /**
     * Returns the VisAD {@link DataImpl} corresponding to the data of a DODS
     * variable and the metaData of the DODS variable used during construction
     * of this instance.
     *
     * @param baseType		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.  The
     *				class of the object will depend upon the DODS
     *				variable used during construction.
     * @throws BadFormException The DODS variable is corrupt.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(BaseType baseType, boolean copy)
	throws BadFormException, VisADException, RemoteException
    {
	DataImpl	data;
	if (baseType instanceof DString)
	    data = data((DString)baseType, copy);
	else if (baseType instanceof DBoolean)
	    data = data((DBoolean)baseType, copy);
	else if (baseType instanceof DByte)
	    data = data((DByte)baseType, copy);
	else if (baseType instanceof DUInt16)
	    data = data((DUInt16)baseType, copy);
	else if (baseType instanceof DInt16)
	    data = data((DInt16)baseType, copy);
	else if (baseType instanceof DUInt32)
	    data = data((DUInt32)baseType, copy);
	else if (baseType instanceof DInt32)
	    data = data((DInt32)baseType, copy);
	else if (baseType instanceof DFloat32)
	    data = data((DFloat32)baseType, copy);
	else if (baseType instanceof DFloat64)
	    data = data((DFloat64)baseType, copy);
	else if (baseType instanceof DStructure)
	    data = data((DStructure)baseType, copy);
	else if (baseType instanceof DList)
	    data = data((DList)baseType, copy);
	else if (baseType instanceof DSequence)
	    data = data((DSequence)baseType, copy);
	else if (baseType instanceof DArray)
	    data = data((DArray)baseType, copy);
	else if (baseType instanceof DGrid)
	    data = data((DGrid)baseType, copy);
	else
	    throw new BadFormException(
		getClass().getName() + ".data(BaseType,boolean): " +
		"Unknown DODS type: " + baseType.getTypeName());
	return data;
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DString var, boolean copy)
	throws VisADException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DString,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DBoolean var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DBoolean,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DByte var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DByte,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DUInt16 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DUInt16,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DInt16 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt16,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DUInt32 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DUInt32,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DInt32 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DInt32,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DFloat32 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat32,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DFloat64 var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DFloat64,boolean): " +
	    "Can't make VisAD data object");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DStructure var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DStructure,boolean): " +
	    "Can't make VisAD data object from DODS DStructure");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DList var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DList,boolean): " +
	    "Can't make VisAD data object from DODS DList");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DArray var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DArray,boolean): " +
	    "Can't make VisAD data object from DODS DArray");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DGrid var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DGrid,boolean): " +
	    "Can't make VisAD data object from DODS DGrid");
    }

    /**
     * Throws a {@link VisADException}.  Override in subclasses where
     * appropriate.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			A VisAD data object corresponding to the data of
     *				the DODS variable and the metadata of the DODS
     *				variable used during construction.
     * @throws VisADException	Don't know how to create a VisAD data object
     *				from the given DODS variable.
     */
    public DataImpl data(DSequence var, boolean copy)
	throws VisADException, RemoteException
    {
	throw new VisADException(
	    getClass().getName() + ".data(DSequence,boolean): " +
	    "Can't make VisAD data object from DODS DSequence");
    }

    /**
     * Returns the default VisAD {@link Set}s that will be used to represent
     * this instances data values in the range of a VisAD {@link FlatField}.
     *
     * Override this method in subclasses where appropriate.
     *
     * @param copy		If true, then the array is cloned.
     * @return			The default VisAD Sets used to represent the
     *				data values in the range of a FlatField.
     *				Will never be <code>null</code> -- though an
     *				individual elements might be (e.g. for {@link
     *				TextType} objects).
     */
    public SimpleSet[] getRepresentationalSets(boolean copy)
    {
	return copy ? (SimpleSet[])defaultRepSets.clone() : defaultRepSets;
    }

    /**
     * Returns the VisAD {@link MathType} corresponding to an array of adapters
     * of DODS variables.  If the array has zero length, then the returned
     * MathType will be <code>null</code>; otherwise, if the array has a single
     * element, then a MathType corresponding to the element will be returned;
     * otherwise, the returned MathType will be a {@link RealTupleType} or a
     * {@link TupleType} as appropriate.
     *
     * @param adapters		An array of adapters of DODS variables.  May
     *				not be <code>null</code>, nor may any element
     *				be <code>null</code>.  May have zero length.
     * @return			A VisAD MathType corresponding to the array 
     *				of adapters.
     */
    protected static MathType mathType(VariableAdapter[] adapters)
	throws VisADException, RemoteException
    {
	MathType[]	mathTypes = new MathType[adapters.length];
	for (int i = 0; i < mathTypes.length; ++i)
	    mathTypes[i] = adapters[i].getMathType();
	return mathType(mathTypes);
    }
}
