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

$Id: Selector.java,v 1.4 2001-02-22 18:10:41 steve Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
import visad.*;

/**
 * Provides support for selecting a VisAD data object of a particular type from
 * a stream of VisAD data objects.
 *
 * <P>Instances are modifiable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Selector extends DataFilter
    
{
    private MathType	targetType;

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
     * Sets the type of the VisAD object to be selected.  Only VisAD objects
     * whos {@link MathType} match the given one will be passed on the the 
     * downstream data sink.
     *
     * @param targetType	The {@link MathType} to pass.  May be
     *				<code>null</code> -- in which case everything is
     *				passed.
     */
    public void setMathType(MathType targetType)
    {
	this.targetType = targetType;
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
        if (data.getType().equals(targetType))
	    send(data);
    }
}
