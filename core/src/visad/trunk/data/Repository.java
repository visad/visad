package visad.data;


import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;


/** 
 * A repository of persistent data objects.
 * This class implements the "abstract factory" design pattern.
 * The concrete implementation of this class could either be
 * "DirectoryRepository" (for accessing files residing on local disk)
 * or "RemoteRepository" (for accessing remote files via a server) or
 * something else.  The concrete class of the "Form" objects will be
 * determined by this class's concrete class (and, hence, so will the
 * concrete class of any constructed "FileAccessor").
 */
public abstract class Repository
{
    /**
     * The name of this repository.
     */
    private final String	name;

    /**
     * The location of this repository.
     */
    private final String	location;

    /**
     * The data forms supported by this repository.
     */
    protected FormNode		forms;


    /**
     * Construct a data repository.
     */
    public Repository(String name, String location)
    {
	this.name = name;
	this.location = location;
    }

    /**
     * Return the name of this repository.
     */
    public String getName()
    {
	return name;
    }

    /**
     * Return the location of this repository.
     */
    public String getLocation()
    {
	return location;
    }

    /**
     * Return the forms of data that are supported by this repository.
     */
    public FormNode getForms()
    {
	return forms;
    }

    /**
     * Return the forms of data that are supported by this repository
     * and compatible with a data object.
     */
    public FormNode getForms(DataImpl data)
    {
	return forms.getForms(data);
    }

    /**
     * Return an enumeration of the data objects in this repository.
     */
    public abstract Enumeration getEnumeration()
	throws	BadRepositoryException, SecurityException;

    /**
     * Save a data object in the first compatible data form.
     */
    public void save(String id, DataImpl data, boolean replace)
	throws VisADException, IOException, RemoteException
    {
	forms.save(fullName(id), data, replace);
    }

    /**
     * Save a data object in a particular form.
     */
    public void save(String id, DataImpl data, FormNode form, boolean replace)
	throws VisADException, RemoteException, IOException
    {
	form.save(fullName(id), data, replace);
    }

    /**
     * Add a data object to an existing data object in the repository.
     */
    public void add(String id, DataImpl data, boolean replace)
	throws VisADException
    {
	forms.add(fullName(id), data, replace);
    }

    /**
     * Open an existing data object in the repository.
     */
    public Data open(String id)
	throws VisADException, IOException
    {
	return forms.open(fullName(id));
    }


    /**
     * Return the fully-qualified name of a persistent data object.
     */
    protected abstract String fullName(String id);
}
