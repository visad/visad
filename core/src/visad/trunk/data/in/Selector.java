/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

$Id: Selector.java,v 1.13 2008-02-05 20:26:08 curtis Exp $
*/

package visad.data.in;

import java.rmi.RemoteException;
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
    extends DataInputFilter
{
    private Condition	condition;

    /**
     * Constructs from an upstream data source.  The initial condition is set
     * to the trivial condition {@link Condition#TRIVIAL_CONDITION}.
     *
     * @param source		The upstream data source.  May not be
     *				<code>null</code>.
     * @throws VisADException	The upstream data source is <code>null</code>.
     */
    public Selector(DataInputStream source)
	throws VisADException
    {
	super(source);
	condition = Condition.TRIVIAL_CONDITION;
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
     * Returns the next VisAD data object in the input stream that satisfies
     * the selection condition. Returns <code>null</code> if there is no such
     * object.
     *
     * @return			A VisAD data object or <code>null</code> if 
     *				there are no more such objects.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized DataImpl readData()
	throws VisADException, RemoteException
    {
	DataInputStream	source = getSource();
	DataImpl	data;
	while ((data = source.readData()) != null)
	    if (condition.isSatisfied(data))
		break;
	return data;
    }
}
