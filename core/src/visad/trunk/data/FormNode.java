package visad.data;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/**
 * A node in the data form hierarchy for the storage of persistent data.
 *
 * This class implements the "composite" design pattern; the node will
 * actually be either a "Form" or a "FormFamily".
 */
public abstract class
FormNode
{
    /**
     * Construct a data-form node with the given name.
     */
    public FormNode(String name)
    {
	this.name = name;
    }


    /**
     * Return the name of this node.
     */
    public String getName()
    {
	return name;
    }


    /**
     * Save a VisAD data object in this form.
     */
    public abstract void
    save(String id, DataImpl data, boolean replace)
	throws BadFormException, IOException, RemoteException, VisADException;


    /**
     * Add data to an existing data object.
     */
    public abstract void add(String id, DataImpl data, boolean replace)
	throws BadFormException;


    /**
     * Open an existing data object.
     */
    public abstract DataImpl open(String id)
	throws BadFormException, IOException, VisADException;


    /**
     * Open a data object specified as a URL.
     */
    public abstract DataImpl open(URL url)
	throws BadFormException, VisADException, IOException;


    /**
     * Return the data forms that are compatible with a data object.
     */
    public abstract FormNode getForms(Data data);


    /**
     * The name of this node.
     */
    private final String	name;
}
