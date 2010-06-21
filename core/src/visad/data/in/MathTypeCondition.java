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

$Id: MathTypeCondition.java,v 1.8 2009-03-02 23:35:48 curtis Exp $
*/

package visad.data.in;

import visad.*;

/**
 * Provides support for matching the MathType of a VisAD data object.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class MathTypeCondition
    extends	Condition
{
    private final MathType	mathType;

    /**
     * Constructs from a VisAD math-type.  VisAD data objects whose math-type
     * equals the given one will satisfy this condition.
     *
     * @param mathType		The VisAD math-type to match against VisAD data
     *				objects.
     */
    protected MathTypeCondition(MathType mathType)
    {
	this.mathType = mathType;
    }

    /**
     * Returns an instance of this class.  Constructs from a VisAD math-type.
     * VisAD data objects whose math-type equals the given one will satisfy this
     * condition.
     *
     * @param mathType		The VisAD math-type to match against VisAD data
     *				objects.
     * @return			An instance of this class.
     */
    public static MathTypeCondition mathTypeCondition(MathType mathType)
    {
	return new MathTypeCondition(mathType);
    }

    /**
     * Indicates if a VisAD data object satisfies this condition.
     *
     * @param data		A VisAD data object.
     * @return			<code>true</code> if and only if the math-type
     *				of the VisAD data object equals the math-type
     *				used during this instance's construction.
     */
    public boolean isSatisfied(DataImpl data)
    {
	return data.getType().equals(mathType);
    }
}
