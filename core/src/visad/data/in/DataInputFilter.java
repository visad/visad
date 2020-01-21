/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

$Id: DataInputFilter.java,v 1.7 2009-03-02 23:35:48 curtis Exp $
*/

package visad.data.in;

import visad.VisADException;

/**
 * Provides support for a filter-module in a data-import pipe.	In general,
 * such a filter-module obtains VisAD data objects its upstream data source and
 * transforms them in some way before passing them on.
 *
 * @author Steven R. Emmerson
 */
abstract public class DataInputFilter
    implements	DataInputStream
{
    private final DataInputStream	source;

    /**
     * Constructs from an upstream data source.
     *
     * @param source		The upstream data source.  May not be
     *				<code>null</code>.
     * @throws VisADException	The upstream data source is <code>null</code>.
     */
    protected DataInputFilter(DataInputStream source)
	throws VisADException
    {
	if (source == null)
	    throw new VisADException(
		getClass().getName() + ".<init>(DataInputStream): " +
		"Null data source");
        this.source = source;
    }

    /**
     * Returns the upstream data source.
     *
     * @return			The upstream data source.
     */
    public final DataInputStream getSource()
    {
	return source;
    }
}
