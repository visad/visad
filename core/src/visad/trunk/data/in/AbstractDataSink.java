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

$Id: AbstractDataSink.java,v 1.3 2001-02-22 18:10:40 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Provides support for implementing a concrete {@link DataSink}.
 *
 * @author Steven R. Emmerson
 */
abstract public class AbstractDataSink
    implements DataSink
{
    /**
     * Receives an instance of a VisAD {@link Real}.
     *
     * @param real		The VisAD real to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(Real real)
	throws VisADException, RemoteException
    {
	receive((Scalar)real);
    }

    /**
     * Receives an instance of a VisAD {@link Text}.
     *
     * @param text		The VisAD text to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(Text text)
	throws VisADException, RemoteException
    {
	receive((Scalar)text);
    }

    /**
     * Receives an instance of a VisAD {@link Scalar}.
     *
     * @param scalar		The VisAD scalar to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(Scalar scalar)
	throws VisADException, RemoteException
    {
	receive((DataImpl)scalar);
    }

    /**
     * Receives an instance of a VisAD {@link Set}.
     *
     * @param set		The VisAD set to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(Set set)
	throws VisADException, RemoteException
    {
	receive((DataImpl)set);
    }

    /**
     * Receives an instance of a VisAD {@link Field}.
     *
     * @param field		The VisAD field to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(Field field)
	throws VisADException, RemoteException
    {
	receive((DataImpl)field);
    }

    /**
     * Receives an instance of a VisAD {@link Tuple}.
     *
     * @param tuple		The VisAD tuple to be received.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    public void receive(Tuple tuple)
	throws VisADException, RemoteException
    {
	receive((DataImpl)tuple);
    }
}
