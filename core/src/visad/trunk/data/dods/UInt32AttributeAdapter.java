package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class UInt32AttributeAdapter
    extends	FloatAttributeAdapter
{
    public UInt32AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    protected float floatValue(String spec)
    {
	return Long.decode(spec).floatValue();
    }
}
