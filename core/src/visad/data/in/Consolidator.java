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

$Id: Consolidator.java,v 1.12 2009-03-02 23:35:48 curtis Exp $
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
public final class Consolidator
    extends	DataInputFilter
{
    /**
     * Constructs with a particular upstream data source.
     *
     * @param source		The upstream data source.  May not be
     *				<code>null</code>.
     * @throws VisADException	The upstream data source is <code>null</code>.
     */
    public Consolidator(DataInputStream source)
	throws VisADException
    {
        super(source);
    }

    /**
     * Returns the next VisAD data object in the input stream. Returns 
     * <code>null</code> if there is no next object.
     *
     * @return			A VisAD data object or <code>null</code> if 
     *				there are no more such objects.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public synchronized DataImpl readData()
	throws VisADException, RemoteException
    {
	System.gc();
	DataInputStream	source = getSource();
	boolean		allReals = true;
	DataImpl	data;
	List		datums = new ArrayList();
	while ((data = source.readData()) != null)
	{
	    allReals &= data instanceof Real;
	    datums.add(data);
	}
	int	count = datums.size();
	if (count == 0)
	{
	    data = null;
	}
	else if (count == 1)
	{
	    data = (DataImpl)datums.get(0);
	}
	else
	{
	    data =
		allReals
		    ? (DataImpl)
			new RealTuple((Real[])datums.toArray(new Real[0]))
		    : new Tuple(
			(DataImpl[])datums.toArray(new DataImpl[0]),
			/*copy=*/false);
	}
	return data;
    }
}
