package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DInt32 variables to the
 * {@link visad.data.in} context.
 */
public class DInt32Adapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DInt32Adapter(DInt32 var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(
		realType(var, table),
		Valuator.instance(table).process(var.getValue()));
    }

    public static DInt32Adapter instance(DInt32 var, AttributeTable table)
	throws VisADException
    {
	return new DInt32Adapter(var, table);
    }

    public static MathType mathType(DInt32 var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
