package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class UInt16AttributeAdapter
    extends	Int32AttributeAdapter
{
    public UInt16AttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	super(name, attr);
    }
}
