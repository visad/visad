package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.*;

/**
 * An instance is immutable.
 */
public class DataFactory
{
    private static final DataFactory		instance = new DataFactory();
    private final AttributeAdapterFactory	attributeFactory;
    private final VariableAdapterFactory	variableFactory;

    protected DataFactory()
    {
	this(
	    AttributeAdapterFactory.attributeAdapterFactory(),
	    VariableAdapterFactory.variableAdapterFactory());
    }

    protected DataFactory(
	AttributeAdapterFactory attributeFactory,
	VariableAdapterFactory variableFactory)
    {
	this.attributeFactory = attributeFactory;
	this.variableFactory = variableFactory;
    }

    public static DataFactory dataFactory()
    {
	return instance;
    }

    public static DataFactory dataFactory(
	AttributeAdapterFactory attributeFactory,
	VariableAdapterFactory variableFactory)
    {
	return new DataFactory(attributeFactory, variableFactory);
    }

    /**
     * May return <code>null</code>.
     */
    public DataImpl data(String name, Attribute attribute)
	throws BadFormException, VisADException, RemoteException
    {
	return attributeFactory.attributeAdapter(name, attribute).data();
    }

    public DataImpl data(BaseType var, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return variableFactory.variableAdapter(var, table).data(var);
    }
}
