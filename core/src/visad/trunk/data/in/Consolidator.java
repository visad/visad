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

$Id: Consolidator.java,v 1.5 2001-03-01 21:14:27 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/**
 * Consolidates VisAD data objects together.  In general, an instance of this
 * class will be the final module in a data-import pipe.
 *
 * @author Steven R. Emmerson
 */
public class Consolidator
    extends DataFilter
{
    private boolean	allReals;
    private DataImpl	data;

    /**
     * @supplierCardinality 1 
     */
    private List	components;

    /**
     * Constructs from nothing.  The downstream data sink will be 
     * <code>null</code>.
     */
    public Consolidator()
    {
        this(null);
    }

    /**
     * Constructs with a particular downstream data sink.
     *
     * @param downstream	The downstream data sink.  May be 
     *				<code>null</code>.
     */
    public Consolidator(DataSink downstream)
    {
        super(downstream);
	clear();
    }

    /**
     * Handles an incoming data object.  Consolidates the incoming data
     * object with any previously-existing data object -- replacing the
     * previously-existing object.
     *
     * @param data		Incoming data object.
     * @throws VisADException	VisAD failure.
     */
    public synchronized void receive(DataImpl incomingData)
	throws VisADException
    {
	components.add(incomingData);
	allReals &= incomingData instanceof Real;
	data = null;
    }

    /**
     * Flushes the data consolidated so far to the downstream {@link DataSink}
     * and then clears the consolidated data.
     *
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized void flush()
	throws VisADException, RemoteException
    {
	if (getDownstream() != null)
	    send(getData());
	clear();
    }

    /**
     * Clears the data consolidated so far.
     */
    public synchronized void clear()
    {
        components = new ArrayList();
	allReals = true;
	data = null;
    }

    /**
     * Returns the same object until a {@link #receive(DataImpl)} is invoked.
     * Does not copy the VisAD data objects.  May return <code>null</code>.
     *
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized final DataImpl getData()
	throws VisADException, RemoteException
    {
	if (data == null)
	{
	    int		count = components.size();
	    if (count == 0)
	    {
		data = null;
	    }
	    else if (count == 1)
	    {
		data = (DataImpl)components.get(0);
	    }
	    else
	    {
		data =
		    allReals
			? (DataImpl)
			    new RealTuple(
				(Real[])components.toArray(new Real[0]))
			: new Tuple(
			    (DataImpl[])components.toArray(new DataImpl[0]),
			    /*copy=*/false);
	    }
	}
	return data;
    }
}
