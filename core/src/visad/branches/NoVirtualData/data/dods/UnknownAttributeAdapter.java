package visad.data.dods;

import dods.dap.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class UnknownAttributeAdapter
    extends	AttributeAdapter
{
    private static final UnknownAttributeAdapter	instance =
	new UnknownAttributeAdapter();

    private UnknownAttributeAdapter()
    {}

    public static UnknownAttributeAdapter unknownAttributeAdapter(
	String name, Attribute attr)
    {
	return instance;
    }

    public DataImpl data()
    {
	return null;
    }
}
