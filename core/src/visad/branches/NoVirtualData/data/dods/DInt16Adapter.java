package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DInt16 variables to the
 * {@link visad.data.in} context.
 */
public class DInt16Adapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DInt16Adapter(DInt16 var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(
		realType(var, table),
		Valuator.instance(table).process(var.getValue()));
    }

    public static DInt16Adapter instance(DInt16 var, AttributeTable table)
	throws VisADException
    {
	return new DInt16Adapter(var, table);
    }

    public static MathType mathType(DInt16 var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
