/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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
 * Provides support for processing byte values in a DODS dataset.  Processing
 * includes checking for validity and unpacking.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public final class ByteValuator
    extends	IntValuator
{
    /**
     * Constructs from the attributes of a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    private ByteValuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	super(table, -128, 127);
    }

    /**
     * Returns an instance of this class corresponding to the attributes for a
     * DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static Valuator valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return new ByteValuator(table);
    }
}
