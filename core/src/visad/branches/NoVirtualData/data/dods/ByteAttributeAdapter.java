package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class ByteAttributeAdapter
    extends	Int32AttributeAdapter
{
    public ByteAttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }
}
