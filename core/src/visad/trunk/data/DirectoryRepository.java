package visad.data;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import visad.data.FormFamily;
import visad.data.Repository;
import visad.data.netcdf.Plain;


/** 
 * A data object repository implemented as files in a local directory.
 */
public class DirectoryRepository extends Repository
{
    /**
     * The directory.
     * Effectively "final" if the constructor succeeds.
     */
    protected File	dir;

    /**
     * Unambiguous representation of directory for error messages.
     * Effectively "final" if the constructor succeeds.
     */
    protected String	dirString;


    /**
     * Construct a directory repository with support for the default
     * forms of data.
     */
    public
    DirectoryRepository(String name, String location)
	throws BadRepositoryException, IOException
    {
	super(name, location);

	// Check the directory.
	try
	{
	    dir = new File(getLocation());
	}
	catch (NullPointerException e)
	{
	    throw new BadRepositoryException("Null repository name");
	}
	dirString = "\"" + getName() + "\" (path \"" + getLocation() + "\")";

	if (!dir.isDirectory())
	    throw new BadRepositoryException("Repository " + dirString +
		" is not a directory");
	if (!dir.canRead())
	    throw new BadRepositoryException("Repository " + dirString +
		" is not readable");

	// Add the default data forms.
	forms = new FormFamily("netCDF").addFormNode(new Plain());
    }


    // TODO: method(s) for constructing the data form hierarchy


    /**
     * Return an enumeration of the data objects in this repository.
     */
    public Enumeration
    getEnumeration()
	throws	BadRepositoryException, SecurityException
    {
	return new Enumerator();
    }


    /**
     * Inner class for enumerating the files in the directory.
     */
    public class
    Enumerator
	implements	Enumeration
    {
	protected int			i;
	protected final String[]	list;

	protected
	Enumerator()
	    throws SecurityException
	{
	    list = dir.list();
	    i = 0;
	}

	public boolean
	hasMoreElements()
	{
	    return i < list.length;
	}

	public Object
	nextElement()
	    throws NoSuchElementException
	{
	    if (i == list.length)
		throw new NoSuchElementException();

	    return list[i++];
	}
    }


    /**
     * Return the fully-qualified name of a persistent data object.
     */
    protected String fullName(String id)
    {
	return getLocation() + File.separator + id;
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws BadRepositoryException, IOException
    {
	DirectoryRepository	dir = new DirectoryRepository("Test", ".");

	for (Enumeration enum = dir.getEnumeration(); enum.hasMoreElements();)
	    System.out.println((String)enum.nextElement());

	System.out.println("dir.fullName(\"foo.bar\") = " + 
	    dir.fullName("foo.bar"));
    }
}
