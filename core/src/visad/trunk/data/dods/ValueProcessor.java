/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for processing numeric values in a DODS dataset.  Processing
 * includes unpacking and checking for validity.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class ValueProcessor
    extends	visad.data.in.ValueProcessor
{
    /**
     * Decodes an attribute for a DODS variable.
     *
     * @param name		The name of the attribute.
     * @param table		The attribute table of the DODS variable.
     * @param index		The index of the attribute element to be 
     *				decoded.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected static double decode(String name, AttributeTable table, int index)
	throws BadFormException, VisADException, RemoteException
    {
	double		value = Double.NaN;	// default value
	Attribute	attr = table.getAttribute(name);
	if (attr != null)
	{
	    DataImpl	data =
		AttributeAdapterFactory.attributeAdapterFactory()
		    .attributeAdapter(name, attr).data();
	    if (data instanceof Real && index == 0)
		value = ((Real)data).getValue();
	    else if (data instanceof Gridded1DDoubleSet)
		value =
		    ((Gridded1DSet)data).indexToDouble(new int[] {index})[0][0];
	    else if (data instanceof Gridded1DSet)
		value =
		    ((Gridded1DSet)data).indexToValue(new int[] {index})[0][0];
	}
	return value;
    }
}
