package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.*;
import visad.*;
import visad.data.BadFormException;

/**
 * Instances are immutable;
 */
public class StringAttributeAdapter
    extends	AttributeAdapter
{
    private final DataImpl	data;

    public StringAttributeAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	TextType	textType =
	    TextType.getTextType(scalarName(name));
	ArrayList	list = new ArrayList();
	for (Enumeration enum = attr.getValues(); enum.hasMoreElements(); )
	    list.add(new Text(textType, (String)enum.nextElement()));
	data = 
	    list.size() == 1
		? (DataImpl)list.get(0)
		: new Tuple((Text[])list.toArray(new Text[0]), false);
    }

    public DataImpl data()
    {
	return data;
    }
}
