package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DBoolean variables to the
 * {@link visad.data.in} context.
 */
public class DBooleanAdapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DBooleanAdapter(DBoolean var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(realType(var, table), var.getValue() ? 1 : 0);
    }

    public static DBooleanAdapter instance(DBoolean var, AttributeTable table)
	throws VisADException
    {
	return new DBooleanAdapter(var, table);
    }

    public static MathType mathType(DBoolean var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
