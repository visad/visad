package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.*;

public class DODSSource
    extends DataSource
{
    private DataFactory	dataFactory;

    public DODSSource(DataSink downstream)
    {
	this(downstream, DataFactory.dataFactory());
    }

    public DODSSource(DataSink downstream, DataFactory factory)
    {
	super(downstream);
	dataFactory = factory;
    }

    public boolean open(String spec)
    {
	boolean	success;
	System.gc();
	try
	{
	    DConnect	dConnect = new DConnect(spec);
	    DAS		das = dConnect.getDAS();
	    handleGlobalAttributes(das);
	    handleVariables(dConnect.getData(null), das);
	    success = true;
	}
	catch (Exception e)
	{
	    System.err.println(
		getClass().getName() + ".open(String): " +
		"Unable to open dataset \"" + spec + "\": " + e);
	    success = false;
	}
	return success;
    }

    public synchronized void close()
	throws VisADException, RemoteException
    {
	flush();
	System.gc();
    }

    protected void handleGlobalAttributes(DAS das)
	throws BadFormException, VisADException, RemoteException
    {
	AttributeTable	globalTable = das.getAttributeTable("NC_GLOBAL");
	if (globalTable == null)
	    globalTable = das.getAttributeTable("nc_global");
	if (globalTable != null)
	{
	    for (Enumeration enum = globalTable.getNames();
		enum.hasMoreElements(); )
	    {
		String		name = (String)enum.nextElement();
		DataImpl	data =
		    dataFactory.data(name, globalTable.getAttribute(name));
		if (data != null)
		    send(data);
	    }
	}
    }

    /**
     * Invokes {@link DataFactory#data(BaseType, AttributeTable)} for each
     * variable in the DataDDS.
     */
    protected void handleVariables(DataDDS dataDDS, DAS das)
	throws BadFormException, VisADException, RemoteException
    {
	for (Enumeration enum = dataDDS.getVariables();
	    enum.hasMoreElements(); )
	{
	    BaseType	baseType = (BaseType)enum.nextElement();
	    send(
		dataFactory.data(
		    baseType, das.getAttributeTable(baseType.getName())));
	}
    }
}
