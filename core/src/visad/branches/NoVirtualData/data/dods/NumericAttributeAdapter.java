package visad.data.dods;

import dods.dap.*;
import java.util.*;
import visad.*;

/**
 * Instances are immutable;
 */
public abstract class NumericAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;
    private final RealType	realType;
    private final String	name;

    protected NumericAttributeAdapter(String name, Attribute attr)
	throws VisADException
    {
	this.name = name;
	realType = RealType.getRealType(scalarName(name));
	ArrayList	list = new ArrayList();
	for (Enumeration enum = attr.getValues(); enum.hasMoreElements(); )
	    list.add(number((String)enum.nextElement()));
	data =
	    list.size() == 0
		? (DataImpl)null
		: list.size() == 1
		    ? (DataImpl)new Real(
			realType, ((Number)list.get(0)).doubleValue())
		    : visadSet(list);
    }

    public RealType getRealType()
    {
	return realType;
    }

    public String getAttributeName()
    {
	return name;
    }

    protected abstract Number number(String spec);

    protected abstract visad.Set visadSet(List list)
	throws VisADException;

    public DataImpl data()
    {
	return data;
    }
}
