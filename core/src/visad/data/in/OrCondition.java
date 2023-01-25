/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

$Id: OrCondition.java,v 1.8 2009-03-02 23:35:49 curtis Exp $
*/

package visad.data.in;

import visad.*;

/**
 * Provides support for alternative conditions for a VisAD data object.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class OrCondition
    extends	Condition
{
    private final Condition	conditionA;
    private final Condition	conditionB;

    /**
     * Constructs from two, alternative conditions for a VisAD data object.
     * VisAD data objects that satisfy either condition will satisfy this
     * condition.
     *
     * @param conditionA	A condition for a VisAD data object.
     * @param conditionB	A condition for a VisAD data object.
     */
    protected OrCondition(Condition conditionA, Condition conditionB)
    {
	this.conditionA = conditionA;
	this.conditionB = conditionB;
    }

    /**
     * Returns an instance of this class.  Constructs from two, alternative
     * conditions for a VisAD data object.  VisAD data objects that satisfy
     * either condition will satisfy this condition.
     *
     * @param conditionA	A condition for a VisAD data object.
     * @param conditionB	A condition for a VisAD data object.
     * @return			An instance of this class.
     */
    public static OrCondition orCondition(
	Condition conditionA, Condition conditionB)
    {
	return new OrCondition(conditionA, conditionB);
    }

    /**
     * Indicates if a VisAD data object satisfies this condition.
     *
     * @param data		A VisAD data object.
     * @return			<code>true</code> if and only if the VisAD data
     *				object satisfies one of the conditions used
     *				during this instance's construction.
     */
    public boolean isSatisfied(DataImpl data)
    {
	return conditionA.isSatisfied(data) || conditionB.isSatisfied(data);
    }
}
