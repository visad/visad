package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.data.units.Parser;

public final class DODSUtil
{
    public static MathType mathType(BaseType var, AttributeTable table)
    {
	return null;	// TODO
    }

    public static RealType getRealType(
	BaseType baseType, AttributeTable table)
    {
	return realType(baseType.getName(), table);
    }

    public static RealType realType(BaseType variable, AttributeTable table)
    {
	return realType(variable.getName(), table);
    }

    public static RealType realType(String name, AttributeTable table)
    {
	Unit		unit;
	if (table == null)
	{
	    unit = null;
	}
	else
	{
	    Attribute	attr = table.getAttribute("units");
	    if (attr == null)
		attr = table.getAttribute("unit");
	    if (attr == null)
		attr = table.getAttribute("UNITS");
	    if (attr == null)
		attr = table.getAttribute("UNIT");
	    if (attr == null)
	    {
		unit = null;
	    }
	    else
	    {
		if (attr.getType() != Attribute.STRING)
		{
		    unit = null;
		}
		else
		{
		    try
		    {
			unit = Parser.instance().parse(attr.getValueAt(0));
		    }
		    catch (Exception e)
		    {
			System.err.println(
			    "VariableAdapter.getRealType(String,...): " +
			    "Ignoring variable \"" + name + 
			    "\" non-decodable unit-specification: " +
			    attr.getValueAt(0));
			unit = null;
		    }
		}
	    }
	}
	return RealType.getRealType(name, unit);
    }

    /**
     * @param table		The higher-level attribute table. May be
     *				<code>null</code>.
     * @param name		The type of the sub-component.  May be
     *				<code>null</code>.
     */
    public static AttributeTable getAttributeTable(
	AttributeTable table, String name)
    {
	AttributeTable	newTable;
	if (table == null)
	{
	    newTable = null;
	}
	else
	{
	    Attribute	attr = table.getAttribute(name);
	    newTable =
		(attr != null && attr.getType() == Attribute.CONTAINER)
		    ? attr.getContainer()
		    : null;
	}
	return newTable;
    }

    /**
     * @param table		The higher-level attribute table. May be
     *				<code>null</code>.
     * @param baseType		The type of the sub-component.  May not be
     *				<code>null</code>.
     */
    public static AttributeTable getAttributeTable(
	AttributeTable table, BaseType baseType)
    {
	return getAttributeTable(table, baseType.getName());
    }

    /**
     * @return			The MathType of the input aggregate.  Will be
     *				<code>null</code> if zero-length input array.
     */
    public static MathType mathType(MathType[] mathTypes)
	throws VisADException
    {
	MathType	mathType;
	if (mathTypes.length == 0)
	{
	    mathType = null;
	}
	else if (mathTypes.length == 1)
	{
	    mathType = mathTypes[0];
	}
	else
	{
	    boolean	allReals = true;
	    for (int i = 0; i < mathTypes.length && allReals; ++i)
		allReals &= mathTypes[i] instanceof RealType;
	    if (allReals)
	    {
		mathType = new RealTupleType((RealType[])mathTypes);
	    }
	    else
	    {
		mathType = new TupleType(mathTypes);
	    }
	}
	return mathType;
    }

    protected static double decode(
	    String name, AttributeTable table, int index)
	throws BadFormException
    {
	double		value;
	Attribute	attr = table.getAttribute(name);
	if (attr == null)
	{
	    value = Double.NaN;
	}
	else
	{
	    int	type = attr.getType();
	    try
	    {
		if (type == Attribute.BYTE)
		    value = Byte.decode(attr.getValueAt(index)).doubleValue();
		else if (type == Attribute.INT16)
		    value = Short.decode(attr.getValueAt(index)).doubleValue();
		else if (type == Attribute.INT32)
		    value =
			Integer.decode(attr.getValueAt(index)).doubleValue();
		else if (type == Attribute.FLOAT32)
		    value = Float.parseFloat(attr.getValueAt(index));
		else if (type == Attribute.FLOAT64)
		    value = Double.parseDouble(attr.getValueAt(index));
		else
		    throw new BadFormException(
			"DODSUtil.decode(...): " +
			"Couldn't decode \"" + name + "\" attribute: " +
			"Non-numeric type: " + attr.getTypeString());
	    }
	    catch (NumberFormatException e)
	    {
		throw new BadFormException(
		    "DODSUtil.decode(...): " +
		    "Couldn't decode \"" + name + "\" attribute: " +
		    "Format exception: \"" + attr.getValueAt(index) + "\"");
	    }
	}
	return value;
    }
}
