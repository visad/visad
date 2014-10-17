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
import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/**
 * Provides support for adapting DODS {@link Attribute#STRING} attributes to
 * the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class StringAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;

    /**
     * Constructs from a name and an appropriate attribute.
     *
     * @param name		The name of the attribute.
     * @param attr		The attribute.  Must have the appropriate type.
     * @throws VisADException	VisAD failure.  Probably the attribute has an
     *				inappropriate type.
     * @throws RemoteException	Java RMI failure.
     */
    public StringAttributeAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	TextType	textType =
	    TextType.getTextType(scalarName(name));
	ArrayList	list = new ArrayList();
	for (Enumeration en = attr.getValues(); en.hasMoreElements(); )
	    list.add(new Text(textType, (String)en.nextElement()));
	data = 
	    list.size() == 1
		? (DataImpl)list.get(0)
		: new Tuple((Text[])list.toArray(new Text[0]), false);
    }

    /**
     * Returns the VisAD data object corresponding to this instance.
     *
     * @param copy		If true, then a copy is returned.
     * @return			The VisAD data object corresponding to this
     *				instance.
     */
    public DataImpl data(boolean copy)
    {
	return copy ? (DataImpl)data.dataClone() : data;
    }
}
