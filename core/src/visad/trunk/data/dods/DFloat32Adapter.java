package visad.data.dods;

import dods.dap.*;
import visad.*;
import visad.data.in.*;

/**
 * Provides support for adapting DODS DFloat32 variables to the
 * {@link visad.data.in} context.
 */
public class DFloat32Adapter
    extends	VariableAdapter
{
    private final VirtualReal	virtualData;

    private DFloat32Adapter(DFloat32 var, AttributeTable table)
	throws VisADException
    {
	virtualData = 
	    VirtualReal.instance(
		realType(var, table),
		Valuator.instance(table).process(var.getValue()));
    }

    public static DFloat32Adapter instance(DFloat32 var, AttributeTable table)
	throws VisADException
    {
	return new DFloat32Adapter(var, table);
    }

    public static MathType mathType(DFloat32 var, AttributeTable table)
    {
	return realType(var, table);
    }

    public VirtualData getVirtualData()
    {
	return virtualData;
    }
}
