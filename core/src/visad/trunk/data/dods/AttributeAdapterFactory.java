package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.*;
import visad.data.BadFormException;
import visad.VisADException;

public class AttributeAdapterFactory
{
    private static final AttributeAdapterFactory	instance =
	new AttributeAdapterFactory();

    protected AttributeAdapterFactory()
    {}

    public static AttributeAdapterFactory attributeAdapterFactory()
    {
	return instance;
    }

    public AttributeAdapter attributeAdapter(String name, Attribute attr)
	throws BadFormException, VisADException, RemoteException
    {
	AttributeAdapter	adapter;
	int			type = attr.getType();
	if (type == Attribute.STRING)
	    adapter = stringAdapter(name, attr);
	else if (type == Attribute.BYTE)
	    adapter = byteAdapter(name, attr);
	else if (type == Attribute.INT16)
	    adapter = int16Adapter(name, attr);
	else if (type == Attribute.UINT16)
	    adapter = uInt16Adapter(name, attr);
	else if (type == Attribute.INT32)
	    adapter = int32Adapter(name, attr);
	else if (type == Attribute.UINT32)
	    adapter = uInt32Adapter(name, attr);
	else if (type == Attribute.FLOAT32)
	    adapter = float32Adapter(name, attr);
	else if (type == Attribute.FLOAT64)
	    adapter = float64Adapter(name, attr);
	else if (type == Attribute.CONTAINER)
	    adapter = containerAdapter(name, attr);
	else if (type == Attribute.UNKNOWN)
	    adapter = unknownAdapter(name, attr);
	else
	    throw new BadFormException(
		getClass().getName() + ".attributeAdapter(): " +
		"Unknown DODS attribute type: " + attr.getTypeString());
	return adapter;
    }

    public StringAttributeAdapter stringAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new StringAttributeAdapter(name, attr);
    }

    public ByteAttributeAdapter byteAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new ByteAttributeAdapter(name, attr);
    }

    public Int16AttributeAdapter int16Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Int16AttributeAdapter(name, attr);
    }

    public UInt16AttributeAdapter uInt16Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new UInt16AttributeAdapter(name, attr);
    }

    public Int32AttributeAdapter int32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Int32AttributeAdapter(name, attr);
    }

    public UInt32AttributeAdapter uInt32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new UInt32AttributeAdapter(name, attr);
    }

    public Float32AttributeAdapter float32Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Float32AttributeAdapter(name, attr);
    }

    public Float64AttributeAdapter float64Adapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new Float64AttributeAdapter(name, attr);
    }

    public ContainerAttributeAdapter containerAdapter(
	String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return new ContainerAttributeAdapter(name, attr, this);
    }

    public UnknownAttributeAdapter unknownAdapter(String name, Attribute attr)
	throws VisADException, RemoteException
    {
	return UnknownAttributeAdapter.unknownAttributeAdapter(name, attr);
    }
}
