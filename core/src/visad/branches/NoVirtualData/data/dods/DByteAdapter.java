package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DByte variables to the
 * {@link visad.data.in} context.
 */
public class DByteAdapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DByteAdapter(DByte var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(
		realType(var, table),
		Valuator.instance(table).process(var.getValue()));
    }

    public static DByteAdapter instance(DByte var, AttributeTable table)
	throws VisADException
    {
	return new DByteAdapter(var, table);
    }

    public static MathType mathType(DByte var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
