/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

$Id: NotCondition.java,v 1.8 2009-03-02 23:35:49 curtis Exp $
*/

package visad.data.in;

import visad.*;

/**
 * Provides support for negating the condition for a VisAD data object.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class NotCondition
    extends	Condition
{
    private final Condition	condition;

    /**
     * Constructs from a condition for a VisAD data object.  VisAD data objects
     * that do not satisfy the given condition will satisfy this condition.
     *
     * @param condition		A condition for a VisAD data object.
     */
    protected NotCondition(Condition condition)
    {
	this.condition = condition;
    }

    /**
     * Returns an instance of this class.  Constructs from a condition for a
     * VisAD data object.  VisAD data objects that do not satisfy the given
     * condition will satisfy this condition.
     *
     * @param condition		A condition for a VisAD data object.
     * @return			An instance of this class.
     */
    public static NotCondition notCondition(Condition condition)
    {
	return new NotCondition(condition);
    }

    /**
     * Indicates if a VisAD data object satisfies this condition.
     *
     * @param data		A VisAD data object.
     * @return			<code>true</code> if and only if the VisAD data
     *				object doesn't satisfy the condition used during
     *				this instance's construction.
     */
    public boolean isSatisfied(DataImpl data)
    {
	return !condition.isSatisfied(data);
    }
}
