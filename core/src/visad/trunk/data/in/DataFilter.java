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

$Id: DataFilter.java,v 1.3 2001-02-22 18:10:41 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Provides support for a filter-module in a data-import pipe.  In general,
 * Such filter-modules receive VisAD data objects, act on them, and emit 
 * (possibly transformed) VisAD data objects.
 *
 * @author Steven R. Emmerson
 */
abstract public class DataFilter
    extends AbstractDataSink
{
    /**
     * @supplierCardinality 1 
     */
    private final DataSink	downstream;

    /**
     * Constructs from a downstream data sink.
     *
     * @param downstream	The downstream data sink.
     */
    protected DataFilter(DataSink downstream)
    {
        this.downstream = downstream;
    }

    /**
     * Returns the downstream data sink..
     *
     * @return			The downstream data sink.
     */
    protected DataSink getDownstream()
    {
	return downstream;
    }

    /**
     * Receives an instance of a VisAD {@link DataImpl}.  Sends the object to
     * the downstream data sink.
     *
     * @param data		The VisAD DataImpl to be received.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    public void receive(DataImpl data)
	throws VisADException, RemoteException
    {
	send(data);
    }

    /**
     * Flushes this instance.  All VisAD data object contained by this module
     * will be sent to the downstream data sink, which will also have its
     * {@link #flush()} method invoked.
     *
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    public void flush()
	throws VisADException, RemoteException
    {
	downstream.flush();
    }

    /**
     * Sends an instance of a VisAD {@link Real} to the downstream data sink.
     *
     * @param real		The VisAD real to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(Real real)
	throws VisADException, RemoteException
    {
	downstream.receive(real);
    }

    /**
     * Sends an instance of a VisAD {@link Text} to the downstream data sink.
     *
     * @param text		The VisAD text to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(Text text)
	throws VisADException, RemoteException
    {
	downstream.receive(text);
    }

    /**
     * Sends an instance of a VisAD {@link Set} to the downstream data sink.
     *
     * @param set		The VisAD set to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(Set set)
	throws VisADException, RemoteException
    {
	downstream.receive(set);
    }

    /**
     * Sends an instance of a VisAD {@link Field} to the downstream data sink.
     *
     * @param field		The VisAD field to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(Field field)
	throws VisADException, RemoteException
    {
	downstream.receive(field);
    }

    /**
     * Sends an instance of a VisAD {@link Tuple} to the downstream data sink.
     *
     * @param tuple		The VisAD tuple to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(Tuple tuple)
	throws VisADException, RemoteException
    {
	downstream.receive(tuple);
    }

    /**
     * Sends an instance of a VisAD {@link DataImpl} to the downstream data 
     * sink.
     *
     * @param data		The VisAD data to be sent.
     * @throws VisADException	VisAD failure (unlikely).
     * @throws RemoteException	Java RMI failure (unlikely).
     */
    protected void send(DataImpl data)
	throws VisADException, RemoteException
    {
	if (data instanceof Real)
	    send((Real)data);
	else if (data instanceof Text)
	    send((Text)data);
	else if (data instanceof Set)
	    send((Set)data);
	else if (data instanceof Field)
	    send((Field)data);
	else if (data instanceof Tuple)
	    send((Tuple)data);
    }
}
