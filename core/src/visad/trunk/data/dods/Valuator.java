/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.data.in.ValueProcessor;
import visad.VisADException;

/**
 * Provides support for processing numeric values in a DODS dataset.  Processing
 * includes unpacking and checking for validity.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public class Valuator
    extends	ValueProcessor
{
    private final ValueVetter	vetter;
    private final ValueUnpacker	unpacker;

    /**
     * Constructs from the attributes for a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected Valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	vetter = ValueVetter.valueVetter(table);
	unpacker = ValueUnpacker.valueUnpacker(table);
    }

    /**
     * Returns an instance of this class corresponding to the attributes for a
     * DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static Valuator valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return new Valuator(table);
    }

    /**
     * Returns the minimum, valid value.
     *
     * @return			The minimum, valid value.
     */
    public double getMin()
    {
	return vetter.getMin();
    }

    /**
     * Returns the maximum, valid value.
     *
     * @return			The maximum, valid value.
     */
    public double getMax()
    {
	return vetter.getMax();
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public float process(float value)
    {
	return unpacker.process(vetter.process(value));
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public float[] process(float[] values)
    {
	return unpacker.process(vetter.process(values));
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     */
    public double process(double value)
    {
	return unpacker.process(vetter.process(value));
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     */
    public double[] process(double[] values)
    {
	return unpacker.process(vetter.process(values));
    }
}
