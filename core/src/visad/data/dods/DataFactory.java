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
import visad.data.BadFormException;
import visad.*;

/**
 * Provides support for creating adapters that bridge between DODS data objects
 * and the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class DataFactory
{
    private static final DataFactory		instance = new DataFactory();
    private final AttributeAdapterFactory	attributeFactory;
    private final VariableAdapterFactory	variableFactory;

    /**
     * Constructs from nothing.
     */
    protected DataFactory()
    {
	this(
	    AttributeAdapterFactory.attributeAdapterFactory(),
	    VariableAdapterFactory.variableAdapterFactory());
    }

    /**
     * Constructs from adapter factories for DODS attributes and DODS variables.
     *
     * @param attributeFactory	An adapter factory for DODS attributes.
     * @param variableFactory	An adapter factory for DODS variables.
     */
    protected DataFactory(
	AttributeAdapterFactory attributeFactory,
	VariableAdapterFactory variableFactory)
    {
	this.attributeFactory = attributeFactory;
	this.variableFactory = variableFactory;
    }

    /**
     * Returns an instance of this class.
     *
     * @return			An instance of this class.
     */
    public static DataFactory dataFactory()
    {
	return instance;
    }

    /**
     * Returns an instance of this class corresponding to adapter factories for
     * DODS attributes and DODS variables.
     *
     * @param attributeFactory	An adapter factory for DODS attributes.
     * @param variableFactory	An adapter factory for DODS variables.
     * @return			An instance of this class corresponding to the
     *				input.
     */
    public static DataFactory dataFactory(
	AttributeAdapterFactory attributeFactory,
	VariableAdapterFactory variableFactory)
    {
	return new DataFactory(attributeFactory, variableFactory);
    }

    /**
     * Returns the VisAD data object corresponding to a DODS attribute.
     *
     * @param name		The name of the DODS attribute.
     * @param attribute		A DODS attribute.
     * @param copy		If true, then a copy of the data object is
     *				returned.
     * @return			The VisAD data object corresponding to the DODS
     *				attribute.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(String name, Attribute attribute, boolean copy)
	throws BadFormException, VisADException, RemoteException
    {
	return attributeFactory.attributeAdapter(name, attribute).data(copy);
    }

    /**
     * Returns the VisAD data object corresponding to a DODS variable.
     *
     * @param var		A DODS variable.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @param copy		If true, then a copy of the data object is
     *				returned.
     * @return			The VisAD data object corresponding to the DODS
     *				variable.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public DataImpl data(BaseType var, DAS das, boolean copy)
	throws BadFormException, VisADException, RemoteException
    {
	return variableFactory.variableAdapter(var, das).data(var, copy);
    }
}
