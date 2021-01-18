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
import visad.*;

/**
 * Provides support for processing 64-bit floating-point values in a DODS
 * dataset.  Processing includes checking for validity and unpacking.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public final class Float64Valuator
    extends	Valuator
{
    /**
     * Constructs from the attributes of a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    private Float64Valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	super(table);
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
	return new Float64Valuator(table);
    }

    /**
     * Returns the set used to represent unpacked, numeric values associated
     * with this instance in the range of a VisAD {@link FlatField}.
     *
     * @return realType		The VisAD real-type for the set.
     * @return			The set used to represent numeric values
     *				associated with this instance.
     * @throws VisADException	VisAD failure.
     */
    public SimpleSet getRepresentationalSet(RealType realType)
	throws VisADException
    {
	return new DoubleSet(realType);
    }
}
