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
import visad.VisADException;

/**
 * Provides support for creating adapters that bridge between DODS attributes
 * and the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class AttributeAdapterFactory
{
    private static final AttributeAdapterFactory	instance =
	new AttributeAdapterFactory();

    /**
     * Constructs from nothing.
     */
    protected AttributeAdapterFactory()
    {}

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static AttributeAdapterFactory attributeAdapterFactory()
    {
	return instance;
    }

    /**
     * Returns an adapter of a DODS attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.  The class of
     *				the object depends on the attribute.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public AttributeAdapter attributeAdapter(String name, Attribute attr)
	throws BadFormException, VisADException, RemoteException
    {
	AttributeAdapter	adapter;
	int			type = attr.getType();
	if (type == Attribute.STRING)
	    adapter = stringAdapter(name, attr);
	else if (type == Attribute.BYTE)
	    adapter = byteAdapter(name, attr);
	else if (type == Attribute.INT16)
	    adapter = int16Adapter(name, attr);
	else if (type == Attribute.UINT16)
	    adapter = uInt16Adapter(name, attr);
	else if (type == Attribute.INT32)
	    adapter = int32Adapter(name, attr);
	else if (type == Attribute.UINT32)
	    adapter = uInt32Adapter(name, attr);
	else if (type == Attribute.FLOAT32)
	    adapter = float32Adapter(name, attr);
	else if (type == Attribute.FLOAT64)
	    adapter = float64Adapter(name, attr);
	else if (type == Attribute.CONTAINER)
	    adapter = containerAdapter(name, attr);
	else if (type == Attribute.UNKNOWN)
	    adapter = unknownAdapter(name, attr);
	else
	    throw new BadFormException(
		getClass().getName() + ".attributeAdapter(): " +
		"Unknown DODS attribute type: " + attr.getTypeString());
	return adapter;
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#STRING} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public StringAttributeAdapter stringAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new StringAttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#BYTE} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ByteAttributeAdapter byteAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new ByteAttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#INT16} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int16AttributeAdapter int16Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Int16AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#UINT16} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt16AttributeAdapter uInt16Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new UInt16AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#INT32} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Int32AttributeAdapter int32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Int32AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#UINT32} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UInt32AttributeAdapter uInt32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new UInt32AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#FLOAT32} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float32AttributeAdapter float32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Float32AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#FLOAT64} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public Float64AttributeAdapter float64Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Float64AttributeAdapter(name, attr);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#CONTAINER} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public ContainerAttributeAdapter containerAdapter(
	String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new ContainerAttributeAdapter(name, attr, this);
    }

    /**
     * Returns an adapter of a DODS {@link Attribute#UNKNOWN} attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attr		The DODS attribute.
     * @return			An adapter of the DODS attribute.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public UnknownAttributeAdapter unknownAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return UnknownAttributeAdapter.unknownAttributeAdapter(name, attr);
    }
}
