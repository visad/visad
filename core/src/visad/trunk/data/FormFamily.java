package visad.data;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;
import visad.DataImpl;
import visad.VisADException;


/**
 * A interior node in the data form hierarchy for the storage of 
 * persistent data objects.
 */
public class
FormFamily
    extends FormNode
{
    /**
     * Construct an interior data-form node with the given name.
     */
    public FormFamily(String name)
    {
	super(name);
    }


    /**
     * Save a VisAD data object.
     */
    public void save(String id, DataImpl data, boolean replace)
	throws BadFormException, RemoteException, IOException, VisADException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		((FormNode)e.nextElement()).save(id, data, replace);
	    }
	    catch (BadFormException xcpt)
	    {
		continue;
	    }
	    return;
	}
	throw new BadFormException("Data object not compatible with \"" +
					getName() + "\" data family");
    }


    /**
     * Add data to an existing data object.
     */
    public void add(String id, DataImpl data, boolean replace)
	throws BadFormException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		((FormNode)e.nextElement()).add(id, data, replace);
	    }
	    catch (BadFormException xcpt)
	    {
		continue;
	    }
	    return;
	}

	throw new BadFormException("Data object not compatible with \"" +
					getName() + "\" data family");
    }


    /**
     * Open an existing data object.
     */
    public DataImpl open(String id)
	throws BadFormException, IOException, VisADException
    {
	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    try
	    {
		return ((FormNode)e.nextElement()).open(id);
	    }
	    catch (BadFormException xcpt)
	    {
	    }
	}

	throw new BadFormException("Data object \"" + id + 
		"\" not compatible with \"" + getName() + "\" data family");
    }


    /**
     * Return the data forms that are compatible with a data object.
     */
    public FormNode getForms(DataImpl data)
    {
	FormFamily	family = new FormFamily(getName());

	for (Enumeration e = forms.elements(); e.hasMoreElements(); )
	{
	    FormNode	node = ((FormNode)e.nextElement()).getForms(data);

	    if (node != null)
		family.addFormNode(node);
	}

	return family.forms.size() == 0
		    ? null
		    : family;
    }


    /**
     * Add a child node to this family of data forms.
     */
    public FormFamily addFormNode(FormNode node)
    {
	forms.addElement(node);
	return this;
    }


    /**
     * The children of this interior node.
     */
    private Vector	forms = new Vector();
}
