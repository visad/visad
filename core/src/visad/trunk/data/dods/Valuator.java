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
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.*;

/**
 * Provides support for processing numeric values in a DODS dataset.  Processing
 * includes checking for non-equality with "missing" or "fill" values, 
 * unpacking into more capacious data types, and checking that the values lie
 * within a valid range.
 *
 * <P>Instances are immutable.</P>
 *
 * @author Steven R. Emmerson
 */
public abstract class Valuator
{
    protected final ValueVetter		vetter;
    protected final ValueUnpacker	unpacker;
    protected final ValueRanger		ranger;

    /**
     * Constructs from the attributes of a DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected Valuator(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	vetter = valueVetter(table);
	unpacker = valueUnpacker(table);
	ranger = valueRanger(table);
    }

    /**
     * Returns an instance of this class corresponding to the attributes for a
     * DODS variable.
     *
     * @param table		The attribute table for a DODS variable.
     * @param type		The type of packed variable: {@link
     *				Attribute#BYTE}, {@link Attribute#INT16}, etc.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static Valuator valuator(AttributeTable table, int type)
	throws BadFormException, VisADException, RemoteException
    {
	Valuator	valuator;
	switch (type)
	{
	    case Attribute.BYTE:
		valuator = 
		    valueRanger(table).getMin() >= 0
			? UByteValuator.valuator(table)
			: ByteValuator.valuator(table);
		break;
	    case Attribute.FLOAT32:
		valuator = Float32Valuator.valuator(table);
		break;
	    case Attribute.FLOAT64:
		valuator = Float64Valuator.valuator(table);
		break;
	    case Attribute.INT16:
		valuator = Int16Valuator.valuator(table);
		break;
	    case Attribute.INT32:
		valuator = Int32Valuator.valuator(table);
		break;
	    case Attribute.UINT16:
		valuator = UInt16Valuator.valuator(table);
		break;
	    case Attribute.UINT32:
		valuator = UInt32Valuator.valuator(table);
		break;
	    default:
		throw new BadFormException(
		    "Valuator.valuator(AttributeTable,int): " +
		    "Unknown variable type: " + type);
	}
	return valuator;
    }

    /**
     * Returns the set used to represent unpacked, numeric values associated
     * with this instance in the range of a VisAD {@link FlatField}.
     *
     * @return realType		The VisAD real-type for the set.
     * @return			The set used to represent numeric values
     *				associated with this instance.
     * @throws VisADException	VisAD failure.
     */
    public abstract SimpleSet getRepresentationalSet(RealType realType)
	throws VisADException;

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public float process(float value)
    {
	return ranger.process(unpacker.process(vetter.process(value)));
    }

    /**
     * Processes a value.
     *
     * @param value		The value to be processed.
     */
    public float[] process(float[] values)
    {
	return ranger.process(unpacker.process(vetter.process(values)));
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     */
    public double process(double value)
    {
	return ranger.process(unpacker.process(vetter.process(value)));
    }

    /**
     * Processes values.
     *
     * @param values		The values to be processed.
     */
    public double[] process(double[] values)
    {
	return ranger.process(unpacker.process(vetter.process(values)));
    }

    /**
     * Decodes an attribute for a DODS variable.
     *
     * @param name		The name of the attribute.
     * @param table		The attribute table of the DODS variable.
     * @param index		The index of the attribute element to be 
     *				decoded.
     * @throws BadFormException	The DODS information is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    protected static double decode(String name, AttributeTable table, int index)
	throws BadFormException, VisADException, RemoteException
    {
	double		value = Double.NaN;	// default value
	Attribute	attr = table.getAttribute(name);
	if (attr != null)
	{
	    DataImpl	data =
		AttributeAdapterFactory.attributeAdapterFactory()
		    .attributeAdapter(name, attr).data(false);
	    if (data instanceof Real && index == 0)
		value = ((Real)data).getValue();
	    else if (data instanceof Gridded1DDoubleSet)
		value =
		    ((Gridded1DSet)data).indexToDouble(new int[] {index})[0][0];
	    else if (data instanceof Gridded1DSet)
		value =
		    ((Gridded1DSet)data).indexToValue(new int[] {index})[0][0];
	    else
		System.err.println(
		    "ValueProcessor.decode(String,AttributeTable,int): " +
		    "Attribute \"" + name + "\" has non-numeric type: " +
		    attr.getTypeString());
	}
	return value;
    }

    /**
     * Returns an instance of a value vetter corresponding to the attributes 
     * of a DODS variable.
     *
     * @param table		The DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				vetter is returned.
     * @return			A value vetter.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI failure.
     */
    public static ValueVetter valueVetter(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	double	fill = Double.NaN;	// default
	double	missing = Double.NaN;	// default
	if (table != null)
	{
	    fill = decode("_FillValue", table, 0);
	    missing = decode("missing_value", table, 0);
	}
	return ValueVetter.valueVetter(new double[] {fill, missing});
    }

    /**
     * Returns an instance of a value unpacker corresponding to the attributes 
     * of a DODS variable.
     *
     * @param table		A DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				unpacker is returned.
     * @return			A value unpacker.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI exception.
     */
    public static ValueUnpacker valueUnpacker(
	    AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	ValueUnpacker	unpacker;
	if (table == null)
	{
	    unpacker = ValueUnpacker.valueUnpacker();
	}
	else
	{
	    double	scale = decode("scale_factor", table, 0);
	    double	offset = decode("add_offset", table, 0);
	    if (scale == scale && scale != 1 &&
		offset == offset && offset != 0)
	    {
		unpacker = ScaleAndOffsetUnpacker.scaleAndOffsetUnpacker(
		    scale, offset);
	    }
	    else if (scale == scale && scale != 1)
	    {
		unpacker = ScaleUnpacker.scaleUnpacker(scale);
	    }
	    else if (offset == offset && offset != 0)
	    {
		unpacker = OffsetUnpacker.offsetUnpacker(offset);
	    }
	    else
	    {
		unpacker = ValueUnpacker.valueUnpacker();
	    }
	}
	return unpacker;
    }

    /**
     * Returns an instance of a value ranger corresponding to the attributes of
     * a DODS variable.
     *
     * @param table		A DODS attribute table.  May be 
     *				<code>null</code>, in which case a trivial
     *				ranger is returned.
     * @return			A value ranger.
     * @throws BadFormException	The attribute table is corrupt.
     * @throws VisADException	VisAD failure.
     * @throws RemoteException	Java RMI exception.
     */
    public static ValueRanger valueRanger(AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	double	lower = Double.NEGATIVE_INFINITY;
	double	upper = Double.POSITIVE_INFINITY;
	if (table != null)
	{
	    if (table.getAttribute("valid_range") == null)
	    {
		lower = decode("valid_min", table, 0);
		upper = decode("valid_max", table, 0);
	    }
	    else
	    {
		lower = decode("valid_range", table, 0);
		upper = decode("valid_range", table, 1);
	    }
	}
	return ValueRanger.valueRanger(lower, upper);
    }
}
