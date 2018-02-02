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
import java.util.*;
import visad.*;

/**
 * Provides support for adapting DODS numeric attributes to the VisAD
 * data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class NumericAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;
    private final RealType	realType;
    private final String	name;

    /**
     * Constructs from a name and an appropriate attribute.
     *
     * @param name		The name of the attribute.
     * @param attr		The attribute.  Must have the appropriate type.
     * @throws VisADException	VisAD failure.  Probably the attribute has an
     *				inappropriate type.
     */
    protected NumericAttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	this.name = name;
	realType = RealType.getRealType(scalarName(name));
	ArrayList	list = new ArrayList();
	for (Enumeration en = attr.getValues(); en.hasMoreElements(); )
	    list.add(number((String)en.nextElement()));
	data =
	    list.size() == 0
		? (DataImpl)null
		: list.size() == 1
		    ? (DataImpl)new Real(
			realType, ((Number)list.get(0)).doubleValue())
		    : visadSet(list);
    }

    /**
     * Returns the VisAD {@link RealType} of this instance.
     *
     * @return			The VisAD RealType of this instance.
     */
    public RealType getRealType()
    {
	return realType;
    }

    /**
     * Returns the name of the attribute used during construction of this
     * instance.
     *
     * @return			The name of the attribute used during
     *				construction of this instance.
     */
    public String getAttributeName()
    {
	return name;
    }

    /**
     * Returns the numeric value corresponding to an appropriate string
     * specification.
     *
     * @param spec		A string specification approrpriate to this
     *				instance.
     * @return			The numeric value corresponding to the
     *				string specification.
     */
    protected abstract Number number(String spec);

    /**
     * Returns the VisAD {@link Set} corresponding to the metadata of the
     * attribute used in constructing this instance and a list of numeric
     * values.
     * 
     * @param list		A list of numeric values.  Each element must 
     *				be of class {@link java.lang.Double}.
     * @return			A VisAD set corresponding to the input.
     * @throws VisADException	VisAD failure.
     */
    protected abstract visad.Set visadSet(List list)
	throws VisADException;

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
