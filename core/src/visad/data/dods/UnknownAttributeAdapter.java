/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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
import visad.*;

/**
 * Provides support for adapting a DODS {@link Attribute#UNKNOWN} attribute to
 * the VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class UnknownAttributeAdapter
    extends	AttributeAdapter
{
    private static final UnknownAttributeAdapter	instance =
	new UnknownAttributeAdapter();

    private UnknownAttributeAdapter()
    {}

    /**
     * Returns an instance of this class corresponding to a name and
     * appropriate attribute.
     *
     * @param name		The name of the attribute.
     * @param attr		The attribute.  Must have the appropriate type.
     */
    public static UnknownAttributeAdapter unknownAttributeAdapter(
	String name, Attribute attr)
    {
	return instance;
    }

    /**
     * Returns <code>null</code>.
     *
     * @param copy		If true, then the data values are copied.
     * @return			<code>null</code>.
     */
    public DataImpl data(boolean copy)
    {
	return null;
    }
}
