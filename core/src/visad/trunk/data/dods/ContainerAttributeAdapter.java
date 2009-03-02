/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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
import java.util.*;
import visad.*;

/**
 * Provides support for adapting a DODS {@link Attribute#CONTAINER} attribute
 * to the VisAD data-import context.  A container attribute is, basically, an
 * inner-level attribute table.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class ContainerAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;

    /**
     * Constructs from a name, an appropriate attribute, and a factory for
     * creating adapters for DODS variables.
     *
     * @param name		The name of the attribute.
     * @param attr		The attribute.  Must have the appropriate type.
     * @param factory		A factor for creating adapters for DODS 
     *				variables.
     * @throws VisADException	VisAD failure.  Probably the attribute has an
     *				inappropriate type.
     * @throws RemoteException	Java RMI failure.
     */
    public ContainerAttributeAdapter(
	    String name, Attribute attr, AttributeAdapterFactory factory)
	throws VisADException, RemoteException
    {
	ArrayList	list = new ArrayList();
	AttributeTable	table = attr.getContainer();
	boolean		allReals = true;
	for (Enumeration en = table.getNames(); en.hasMoreElements(); )
	{
	    name = (String)en.nextElement();
	    DataImpl	data =
		factory.attributeAdapter(name, table.getAttribute(name))
		.data(false);
	    list.add(data);
	    allReals &= data instanceof Real;
	}
	if (list.size() == 1)
	{
	    data = (DataImpl)list.get(0);
	}
	else
	{
	    data =
		allReals
		    ? (DataImpl)new RealTuple((Real[])list.toArray(new Real[0]))
		    : new Tuple((Data[])list.toArray(new Data[0]), false);
	}
    }

    /**
     * Returns the VisAD data object corresponding to this instance.
     *
     * @param copy		If true, then a copy of the data object is
     *				returned.
     * @return			The VisAD data object corresponding to this
     *				instance.
     */
    public DataImpl data(boolean copy)
    {
	return copy ? (DataImpl)data.dataClone() : data;
    }
}
