package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;

/**
 * Instances are immutable;
 */
public class ContainerAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;

    public ContainerAttributeAdapter(
	    String name, Attribute attr, AttributeAdapterFactory factory)
	throws VisADException, RemoteException
    {
	ArrayList	list = new ArrayList();
	AttributeTable	table = attr.getContainer();
	boolean		allReals = true;
	for (Enumeration enum = table.getNames(); enum.hasMoreElements(); )
	{
	    name = (String)enum.nextElement();
	    DataImpl	data =
		factory.attributeAdapter(name, table.getAttribute(name)).data();
	    list.add(data);
	    allReals &= data instanceof Real;
	}
	if (list.size() == 1)
	{
	    data = (DataImpl)list.get(0);
	}
	else
	{
	    data =
		allReals
		    ? (DataImpl)new RealTuple((Real[])list.toArray(new Real[0]))
		    : new Tuple((Data[])list.toArray(new Data[0]), false);
	}
    }

    public DataImpl data()
    {
	return data;
    }
}
