package visad.data.dods;

import dods.dap.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import visad.data.BadFormException;
import visad.data.in.*;
import visad.VisADException;

public class DefaultDODSAccess
    extends VirtualDataSource
{
    private DConnect		dConnect;
    private DataDDS		dataDDS;
    private DAS			das;
    private VirtualDataFactory	virtualDataFactory = 
	VirtualDataFactory.instance();

    public DefaultDODSAccess(VirtualDataSink downstream)
    {
	super(downstream);
    }

    public synchronized boolean open(String spec)
    {
	boolean	success;
	try
	{
	    dConnect = new DConnect(spec);
	    dataDDS = dConnect.getData(null);
	    das = dConnect.getDAS();
	    handleGlobalAttributes();
	    handleVariables();
	    success = true;
	}
	catch (Exception e)
	{
	    success = false;
	}
	return success;
    }

    public synchronized void close()
    {
	das = null;
	dataDDS = null;
	dConnect = null;
    }

    protected void handleGlobalAttributes()
	throws BadFormException, VisADException
    {
	AttributeTable	globalTable = das.getAttributeTable("GLOBAL");
	if (globalTable == null)
	    globalTable = das.getAttributeTable("global");
	if (globalTable != null)
	{
	    for (Enumeration enum = globalTable.getNames();
		enum.hasMoreElements(); )
	    {
		String		name = (String)enum.nextElement();
		VirtualData	data =
		    virtualDataFactory.virtualData(
			globalTable.getAttribute(name), name);
		if (data != null)
		    send(data);
	    }
	}
    }

    /**
     * Invokes {@link #handleVariable(BaseType, AttributeTable)} for each
     * variable in the DDS.
     */
    protected void handleVariables()
	throws BadFormException, VisADException, RemoteException
    {
	for (Enumeration enum = dataDDS.getVariables();
	    enum.hasMoreElements(); )
	{
	    BaseType	baseType = (BaseType)enum.nextElement();
	    send(
		virtualDataFactory.virtualData(
		    baseType, das.getAttributeTable(baseType.getName())));
	}
    }
}
