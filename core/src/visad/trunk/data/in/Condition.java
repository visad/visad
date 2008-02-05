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

$Id: Condition.java,v 1.7 2008-02-05 20:26:08 curtis Exp $
*/

package visad.data.in;

import visad.*;

/**
 * Provides support for applying arbitrary conditions to VisAD data objects.
 * This class supports data filters like {@link Selector}.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class Condition
{
    /**
     * The trivial condition.  The {@link #isSatisfied} method of this condition
     * always returns <code>true</code>.
     */
    public static Condition	TRIVIAL_CONDITION = 
	new Condition()
	{
	    public boolean isSatisfied(DataImpl data)
	    {
		return true;
	    }
	};

    /**
     * Indicates if a VisAD data object satisfies this condition.
     *
     * @param data		A VisAD data object.
     * @return			<code>true</code> if and only if the VisAD data
     *				object satisfies this instance's condition.
     */
    public abstract boolean isSatisfied(DataImpl data);
}
