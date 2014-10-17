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
import java.util.*;
import visad.*;

/**
 * Provides support for adapting DODS floating-point attributes to the VisAD
 * data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class FloatAttributeAdapter
    extends	NumericAttributeAdapter
{
    /**
     * Constructs from a name and an appropriate attribute.
     *
     * @param name		The name of the attribute.
     * @param attr		The attribute.  Must have the appropriate type.
     * @throws VisADException	VisAD failure.  Probably the attribute has an
     *				inappropriate type.
     */
    protected FloatAttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    /**
     * Returns the numeric value corresponding to a floating-point 
     * string specification.
     *
     * @param spec		A floating-point string specification.
     * @return			The numeric value corresponding to the
     *				string specification.
     */
    protected Number number(String spec)
    {
	return new Float(floatValue(spec));
    }

    /**
     * Returns the numeric value corresponding to a floating-point 
     * string specification.
     *
     * @param spec		A floating-point string specification.
     * @return			The numeric value corresponding to the
     *				string specification.
     */
    protected abstract float floatValue(String spec);

    /**
     * Returns the VisAD {@link Set} corresponding to the metadata of the
     * attribute used in constructing this instance and a list of numeric
     * values.
     * 
     * @param list		A list of numeric values.  Each element must 
     *				be of class {@link java.lang.Float}.
     * @return			A VisAD set corresponding to the input.
     *				The class of the set is either {@link
     *				visad.Gridded1DSet} or {@link visad.List1DSet}
     *				-- depending on whether or not the list is
     *				sorted.
     * @throws VisADException	VisAD failure.
     */
    protected visad.Set visadSet(List list)
	throws VisADException
    {
	float[]	values = new float[list.size()];
	for (int i = 0; i < values.length; ++i)
	    values[i] = ((Float)list.get(i)).floatValue();
	boolean		isSorted = true;
	{
	    int		i = 1;
	    if (values[0] < values[1])
		while (i < values.length-1)
		    if (values[i] > values[++i])
			break;
	    else
		while (i < values.length-1)
		    if (values[i] < values[++i])
			break;
	    isSorted = i == values.length - 1;
	}
	return
	    isSorted
		? (visad.Set)new Gridded1DSet(
		    getRealType(), new float[][] {values}, values.length)
		: new List1DSet(values, getRealType(), null, null);
    }
}
