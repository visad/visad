package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DFloat64 variables to the
 * {@link visad.data.in} context.
 */
public class DFloat64Adapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DFloat64Adapter(DFloat64 var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(
		realType(var, table),
		Valuator.instance(table).process(var.getValue()));
    }

    public static DFloat64Adapter instance(DFloat64 var, AttributeTable table)
	throws VisADException
    {
	return new DFloat64Adapter(var, table);
    }

    public static MathType mathType(DFloat64 var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
