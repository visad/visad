package visad.data.dods;

import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;

public class DefaultAccess
    extends Form
{
    protected static final DefaultAccess	instance = new DefaultAccess();

    protected DefaultAccess()
    {
	super("DODS");
    }

    public static DefaultAccess instance()
    {
	return instance;
    }

    /**
     * Save a VisAD data object in this form.
     */
    public void
    save(String id, Data data, boolean replace)
	throws UnimplementedException
    {
	throw new UnimplementedException(
	    getClass().getName() + ".save(String,Data,boolean): " +
	    "Can't save data to a DODS server");
    }

    /**
     * Add data to an existing data object.
     */
    public void add(String id, Data data, boolean replace)
	throws BadFormException
    {
	throw new BadFormException(
	    getClass().getName() + ".add(String,Data,boolean): " +
	    "Can't add data to a DODS server");
    }

    /**
     * Open an existing data object.
     */
    public visad.DataImpl open(String id)
	throws BadFormException, RemoteException, VisADException
    {
	return null;	// TODO
    }

    /**
     * Open a data object specified as a URL.
     */
    public visad.DataImpl open(URL url)
	throws BadFormException, VisADException, RemoteException
    {
	return null;	// TODO
    }

    /**
     * Return the data forms that are compatible with a data object.
     */
    public FormNode getForms(Data data)
    {
	return null;	// can't save data to a DODS server
    }
}
