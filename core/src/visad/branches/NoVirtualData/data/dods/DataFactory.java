package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import visad.data.BadFormException;
import visad.*;

/**
 * Uses {@link VirtualDataFactory}.
 * An instance is immutable.
 */
public class DataFactory
{
    private final VirtualDataFactory	virtualDataFactory;
    private static final DataFactory	instance = new DataFactory();

    protected DataFactory()
    {
	this(VirtualDataFactory.instance());
    }

    protected DataFactory(VirtualDataFactory virtualDataFactory)
    {
	this.virtualDataFactory = virtualDataFactory;
    }

    public static DataFactory instance()
    {
	return instance;
    }

    public Data data(BaseType variable, AttributeTable table)
	throws BadFormException, VisADException, RemoteException
    {
	return virtualDataFactory.virtualData(variable, table).getData();
    }
}
