package visad.data.netcdf;


import java.util.Enumeration;
import java.util.Hashtable;
import ucar.netcdf.Attribute;
import ucar.netcdf.AttributeIterator;
import ucar.netcdf.AttributeSet;
import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import ucar.netcdf.Named;
import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import ucar.netcdf.VariableSet;
import visad.data.BadFormException;


/**
 * Abstract superclass for implementing a set of netCDF objects (i.e.
 * attributes, dimensions, and variables).
 */
abstract class
NcSet
{
    protected /*final*/ Hashtable	table;


    /**
     * Default constructor.
     */
    NcSet()
    {
	this(1);
    }


    /**
     * Construct from an expected number of elements.
     */
    NcSet(int num)
    {
	table = new Hashtable(num == 0 ? 1 : num);
    }


    /**
     * Construct from another set.
     */
    NcSet(NcSet set)
    {
	/*
         * This is done manually rather than by using table.clone()
         * because the Hashtable's clone() method appears to clone
         * (i.e. copy) the values rather than the references and,
         * in a consistent Netcdf, every Dimension instance that
         * is referenced by a Variable must be an object in the
         * DimensionSet.
	 */
	table = new Hashtable(set.size());

	for (Iterator iter = superIterator(); iter.hasNext(); )
	    put(iter.next());
    }


    /**
     * Add an element to the set.  If it's the same as a previously
     * existing one, then don't add it.  Return the set element.
     */
    Named
    put(Named thing)
    {
	Named	entry = (Named)table.get(thing.getName());

	if (entry == null)
	{
	    table.put(thing.getName(), thing);
	    entry = thing;
	}

	return entry;
    }


    /**
     * Add the contents of another set to this set.
     */
    void
    put(NcSet set)
    {
	for (Iterator iter = set.superIterator(); iter.hasNext(); )
	    put(iter.next());
    }


    /**
     * Inner iterator class.
     */
    protected class
    Iterator
    {
	private final Enumeration	enum = table.elements();

	public boolean
	hasNext()
	{
	    return enum.hasMoreElements();
	}

	public Named
	next()
	{
	    return (Named)enum.nextElement();
	}
    }


    /**
     * Return an iterator for the set.
     */
    protected Iterator
    superIterator()
    {
	return new Iterator();
    }


    /**
     * Retrieve the element identified by name.
     */
    protected Named
    getNamed(String name)
    {
	// System.out.println("NcSet.getNamed(String): getting \"" + name +
	    // "\"");
	Named	entry = (Named)table.get(name);
	// System.out.println("NcSet.getNamed(String): \"" + name + "\" " +
	    // (entry == null ? "not found" : "found"));
	return entry;
    }


    /**
     * Indicate whether or not the given object is in the set.
     */
    public boolean
    contains(Object obj)
    {
	Named	target = (Named)obj;

	// System.out.println("NcSet.contains(Object): looking for \"" + 
	    // target.getName() + "\"");

	Named	entry = (Named)table.get(target.getName());

	boolean	exists = entry != null && entry == target;

	// System.out.println("NcSet.contains(Object): \"" +
	    // target.getName() + "\" " + (exists ? "not found" : "found"));

	return exists;
    }


    /**
     * Indicate whether or not the object identified by name is in 
     * this set.
     */
    public boolean
    contains(String name)
    {
	// System.out.println("NcSet.contains(String): looking for \"" +
	    // name + "\"");
	boolean	exists = table.containsKey(name);
	// System.out.println("NcSet.contains(String): \"" +
	    // name + "\" " + (exists ? "not found" : "found"));
	return exists;
    }


    /**
     * Remove an object from the set.  Not supported on read-only, VisAD
     * data objects.
     */
    public boolean
    remove(Object obj)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Remove an object identified by name from the set.  Not supported on
     * read-only, VisAD data objects.
     */
    public boolean
    remove(String name)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    /**
     * Return the number of elements in the set.
     */
    public int
    size()
    {
	return table.size();
    }


    /**
     * Convert to a String.
     */
    public String
    toString()
    {
	return table.toString();
    }
}
