package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class Int16AttributeAdapter
    extends	Int32AttributeAdapter
{
    public Int16AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }
}
