/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

$Id: DataInputSource.java,v 1.7 2009-03-02 23:35:48 curtis Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.VisADException;

/**
 * Interface for sources of VisAD data objects.
 *
 * @author Steven R. Emmerson
 */
public interface DataInputSource
    extends	DataInputStream
{
    /**
     * Opens an existing dataset.
     *
     * @param spec		The specification of the existing dataset.
     * @throws BadFormException	The DODS dataset is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
	
    void open(String spec)
	throws BadFormException, RemoteException, VisADException;
}
