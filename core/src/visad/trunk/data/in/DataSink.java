/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

$Id: DataSink.java,v 1.3 2001-02-22 18:10:41 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Interface for a filter-module in a data-import pipe.  In general,
 * Such filter-modules receive VisAD data objects, act on them, and emit 
 * (possibly transformed) VisAD data objects.
 *
 * @see AbstractDataSink
 * @author Steven R. Emmerson
 */
public interface DataSink 
{
    /**
     * Receives a VisAD {@link Real}.
     *
     * @param real		The VisAD real to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Real real)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link Text}.
     *
     * @param text		The VisAD text to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Text text)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link Scalar}.
     *
     * @param scalar		The VisAD scalar to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Scalar scalar)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link Set}.
     *
     * @param set		The VisAD set to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Set set)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link Field}.
     *
     * @param field		The VisAD field to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Field field)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link Tuple}.
     *
     * @param tuple		The VisAD tuple to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(Tuple tuple)
	throws VisADException, RemoteException;

    /**
     * Receives a VisAD {@link DataImpl}.
     *
     * @param data		The VisAD data to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void receive(DataImpl data)
	throws VisADException, RemoteException;

    /**
     * Flushes this module.  Any VisAD data objects that have not been sent to
     * any[ downstream {@link DataSink}s will be sent and those data sinks will
     * have their {@link #flush()} methods invoked.
     *
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    void flush()
	throws VisADException, RemoteException;
}
