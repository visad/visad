package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class Int32AttributeAdapter
    extends	FloatAttributeAdapter
{
    public Int32AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    protected float floatValue(String spec)
    {
	return Integer.decode(spec).floatValue();
    }
}
