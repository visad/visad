package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.CacheStrategy;
import visad.data.units.Parser;

/**
 * Instances are immutable;
 */
public abstract class Adapter
{
    private static final CacheStrategy	cacheStrategy = new CacheStrategy();

    protected static String scalarName(String name)
    {
	return name
	    .replace('.', '-')
	    .replace(' ', '_')
	    .replace('(', '<')
	    .replace(')', '>');
    }

    protected static boolean isFlat(MathType mathType)
    {
	return
	    mathType instanceof RealType ||
	    mathType instanceof RealTupleType ||
	    (mathType instanceof TupleType && ((TupleType)mathType).getFlat());
    }

    protected static RealType realType(BaseType variable, AttributeTable table)
    {
	return realType(variable.getName(), table);
    }

    protected static RealType realType(String name, AttributeTable table)
    {
	Unit	unit;
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
		    String	unitSpec = attr.getValueAt(0);
		    try
		    {
			unit = Parser.instance().parse(unitSpec);
		    }
		    catch (Exception e)
		    {
			System.err.println(
			    "visad.data.dods.Adapter.realType(String,...): " +
			    "Ignoring non-decodable unit-specification \"" +
			    unitSpec + "\" of variable \"" + name + "\"");
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
    protected static AttributeTable attributeTable(
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
    protected static AttributeTable attributeTable(
	AttributeTable table, BaseType baseType)
    {
	return attributeTable(table, baseType.getName());
    }

    /**
     * @return			The MathType of the input aggregate.  Will be
     *				<code>null</code> if zero-length input array.
     */
    protected static MathType mathType(MathType[] mathTypes)
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
		RealType[]	realTypes = new RealType[mathTypes.length];
		for (int i = 0; i < realTypes.length; ++i)
		    realTypes[i] = (RealType)mathTypes[i];
		mathType = new RealTupleType(realTypes);
	    }
	    else
	    {
		mathType = new TupleType(mathTypes);
	    }
	}
	return mathType;
    }

    protected CacheStrategy getCacheStrategy()
    {
	return cacheStrategy;
    }
}
