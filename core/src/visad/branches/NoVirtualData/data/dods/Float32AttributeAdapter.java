package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class Float32AttributeAdapter
    extends	FloatAttributeAdapter
{
    public Float32AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }

    protected float floatValue(String spec)
    {
	return Float.parseFloat(spec);
    }
}
