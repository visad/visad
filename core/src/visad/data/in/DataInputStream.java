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

$Id: DataInputStream.java,v 1.7 2009-03-02 23:35:48 curtis Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Interface for a filter-module in a data-import pipe.  In general, such
 * a filter-module obtains VisAD data objects its upstream data source and
 * transforms them in some way before passing them on.
 *
 * @author Steven R. Emmerson
 */
public interface DataInputStream 
{
    /**
     * Returns the next VisAD data object in the input stream. Returns 
     * <code>null</code> if there is no next object.
     *
     * @return			A VisAD data object or <code>null</code> if 
     *				there are no more such objects.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    DataImpl readData()
	throws VisADException, RemoteException;
}
