/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
*/

package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS {@link DString} variables to the
 * VisAD data-import context.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class StringVariableAdapter
    extends	VariableAdapter
{
    private final TextType	textType;

    private StringVariableAdapter(DString var)
	throws VisADException
    {
	textType = TextType.getTextType(scalarName(var.getName()));
    }

    /**
     * Returns an instance of this class corresponding to a DODS {@link 
     * DString}.
     *
     * @param var		The DODS variable.  Only the DODS metadata is 
     *				used: the variable needn't have any actual data.
     * @param das		The DODS DAS in which the attribute
     *				table for the DODS variable is embedded.
     * @return			An instance of this class corresponding to the
     *				input arguments.
     * @throws VisADException	VisAD failure.
     */
    public static StringVariableAdapter stringVariableAdapter(
	    DString var, DAS das)
	throws VisADException
    {
	return new StringVariableAdapter(var);
    }

    /**
     * Returns the VisAD {@link MathType} of this instance.
     *
     * @return			The MathType of this instance.
     */
    public MathType getMathType()
    {
	return textType;
    }

    /**
     * Returns the VisAD {@link DataImpl} corresponding to a DODS {@link 
     * DString}.
     *
     * @param var		The DODS variable to have the corresponding
     *				VisAD data object returned.  The variable
     *				must be compatible with the variable used to
     *				construct this instance.
     * @param copy		If true, then data values are copied.
     * @return			The VisAD data object of this instance.  The
     *				class of the object will be {@link Text}.  The
     *				VisAD {@link MathType} of the data object will
     *				be based on the DODS variable used during
     *				construction of this instance.
     * @throws VisADException	VisAD failure.  Possibly the variable wasn't
     *				compatible with the variable used to construct
     *				this instance.
     */
    public DataImpl data(DString var, boolean copy)
	throws VisADException
    {
	return new Text(textType, var.getValue());
    }
}
