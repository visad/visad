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

$Id: Selector.java,v 1.5 2001-02-23 17:04:50 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import java.util.ArrayList;
import visad.*;

/**
 * Provides support for removing unwanted VisAD data objects from
 * a stream of VisAD data objects.
 *
 * <P>Instances are modifiable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Selector
    extends DataFilter
{
    private Condition	condition;

    /**
     * Constructs from a downstream data sink.
     *
     * @param downstream	The downstream data sink.
     */
    public Selector(DataSink downstream)
    {
	super(downstream);
    }

    /**
     * Sets the condition for passing VisAD data objects.  A VisAD object that
     * satisfies the condition will be passed on to the the downstream data
     * sink.  All others will be rejected.
     *
     * @param condition		The pass/reject condition.
     */
    public void setCondition(Condition condition)
    {
	this.condition = condition;
    }

    /**
     * Receives a VisAD {@link DataImpl}.
     *
     * @param data		The VisAD data object to be received.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public void receive(DataImpl data)
	throws VisADException, RemoteException
    {
        if (condition.isSatisfied(data))
	    send(data);
    }
}
