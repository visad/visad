package visad.data.dods;

import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.data.in.*;

public class DODSForm
    extends Form
{
    private static DODSForm	instance;
    private final DODSSource	source;
    private final Consolidator	consolidator;

    static
    {
	try
	{
	    instance = new DODSForm();
	}
	catch (VisADException e)
	{
	    throw new VisADError(
		"visad.data.dods.DODSForm.<clinit>: " +
		"Can't initialize class: " + e);
	}
    }

    protected DODSForm()
	throws VisADException
    {
	super("DODS");
	consolidator = new Consolidator();
	source = new DODSSource(new TimeFactorer(consolidator));
    }

    public static DODSForm dodsForm()
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
    public DataImpl open(String id)
	throws BadFormException, RemoteException, VisADException
    {
	source.open(id);
	DataImpl	data = consolidator.getData();
	source.close();		// clears consolidator
	return data;
    }

    /**
     * Open a data object specified as an URL.
     */
    public DataImpl open(URL url)
	throws BadFormException, VisADException, RemoteException
    {
	return open(url.toString());
    }

    /**
     * Return the data forms that are compatible with a data object.
     */
    public FormNode getForms(Data data)
    {
	return null;	// can't save data to a DODS server
    }
}
