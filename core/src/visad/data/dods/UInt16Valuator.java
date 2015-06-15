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
 * Provides support for processing unsigned, 16-bit integer values in a DODS
 * dataset.  Processing includes checking for validity and unpacking.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public final class UInt16Valuator
    extends	UIntValuator
{
    /**
     * Constructs from the attributes of a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    private UInt16Valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	super(table, 2*Short.MAX_VALUE+1);
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
	return new UInt16Valuator(table);
    }
}
