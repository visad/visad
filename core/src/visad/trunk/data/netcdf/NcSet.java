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


/**
 * Class for implementing a set of netCDF attributes.
 */
class
AttrSet
    extends	NcSet
    implements	AttributeSet
{
    /**
     * Default constructor.
     */
    AttrSet()
    {
	this(1);
    }


    /**
     * Construct from an expected number of elements.
     */
    AttrSet(int num)
    {
	super(num);
    }


    /**
     * Construct from another set.
     */
    AttrSet(AttrSet set)
    {
	super(set);
    }


    /**
     * Put an element into the set.
     */
    public Attribute
    put(Attribute attr)
    {
	return (Attribute)super.put(attr);
    }


    /**
     * Retrieve the attribute identified by name.
     */
    public Attribute
    get(String name)
    {
	return (Attribute)super.getNamed(name);
    }


    /**
     * Inner iterator class.
     */
    private class
    Iterator
	implements	AttributeIterator
    {
	protected final NcSet.Iterator	iter = superIterator();

	public boolean
	hasNext()
	{
	    return iter.hasNext();
	}

	public Attribute
	next()
	{
	    return (Attribute)iter.next();
	}
    }


    /**
     * Return an iterator for the attributes in the set.
     */
    public AttributeIterator
    iterator()
    {
	return new Iterator();
    }


    /**
     * Return a new array containing the attributes of the set. 
     */
    public synchronized Attribute[]
    toArray()
    {
	Attribute[]		array = new Attribute[size()];
	AttributeIterator	iter = iterator();

	for (int i = 0; iter.hasNext(); ++i)
	    array[i] = iter.next();

	return array;
    }
}


/**
 * Class for implementing a set of netCDF dimensions.
 */
class
DimSet
    extends	NcSet
    implements	DimensionSet
{
    /**
     * Default constructor.
     */
    DimSet()
    {
	this(1);
    }


    /**
     * Construct from an expected number of elements.
     */
    DimSet(int num)
    {
	super(num);
    }


    /**
     * Construct from another set.
     */
    DimSet(DimSet set)
    {
	super(set);
    }


    /**
     * Construct from an array of dimensions.
     *
     * @prerequisite	No two dimensions have the same name.
     */
    DimSet(Dimension[] dims)
	throws BadFormException
    {
	this(dims.length);

	for (int idim = 0; idim < dims.length; ++idim)
	    put(dims[idim]);
    }


    /**
     * Add a dimension to the set.  If the dimension is already in the set
     * then don't add it.  Return the dimension in the set.
     *
     * @prerequisite <code>dim</code> is non-<code>null</code>.
     * @exception BadFormException	A dimension with the same name but
     *					different size already exists in the
     *					set.
     */
    public Dimension
    put(Dimension dim)
	throws BadFormException
    {
	Dimension	entry = (Dimension)super.put(dim);

	if (entry != dim && entry.getLength() != dim.getLength())
	    throw new BadFormException("Attempt to replace dimension " +
		entry + " with dimension " + dim);

	return entry;
    }


    /**
     * Inner iterator class.
     */
    private class
    Iterator
	implements	DimensionIterator
    {
	protected final NcSet.Iterator	iter = superIterator();

	public boolean
	hasNext()
	{
	    return iter.hasNext();
	}

	public Dimension
	next()
	{
	    return (Dimension)iter.next();
	}
    }


    /**
     * Add the contents of another set to this set.
     */
    void
    put(DimSet set)
	throws BadFormException
    {
	for (DimensionIterator iter = set.iterator(); iter.hasNext(); )
	    put(iter.next());
    }


    /**
     * Retrieve the dimension identified by name.
     */
    public Dimension
    get(String name)
    {
	return (Dimension)super.getNamed(name);
    }


    /**
     * Return a DimensionIterator for the elements.
     */
    public DimensionIterator
    iterator()
    {
	return new Iterator();
    }


    /**
     * Return a new array containing the dimensions of the set. 
     */
    public synchronized Dimension[]
    toArray()
    {
	Dimension[]		array = new Dimension[size()];
	DimensionIterator	iter = iterator();

	for (int i = 0; iter.hasNext(); ++i)
	    array[i] = iter.next();

	return array;
    }
}


/**
 * Class for implementing a set of netCDF variables.
 */
class
VarSet
    extends	NcSet
    implements	VariableSet
{
    /**
     * Default constructor.
     */
    VarSet()
    {
	this(1);
    }


    /**
     * Construct from an expected number of elements.
     */
    VarSet(int num)
    {
	super(num);
    }


    /**
     * Construct from another set.
     */
    VarSet(VarSet set)
    {
	super(set);
    }


    /**
     * Add a variable to the set.  If the variable is already in the set
     * then don't add it.  Return the variable in the set.
     *
     * @prerequisite <code>var</code> is non-<code>null</code>.
     * @exception BadFormException	A variable with the same name but
     *					different characteristics already
     *					exists in the set.
     */
    public Variable
    put(Variable var)
	throws BadFormException
    {
	// System.out.println("VarSet.put(Variable): Putting variable " + var);
	Variable	entry = (Variable)super.put(var);

	if (entry != var && !entry.equals(var))
	    throw new BadFormException("Attempt to replace variable " +
		entry + " with variable " + var);

	return entry;
    }


    /**
     * Inner iterator class.
     */
    private class
    Iterator
	implements	VariableIterator
    {
	protected final NcSet.Iterator	iter = superIterator();

	public boolean
	hasNext()
	{
	    return iter.hasNext();
	}

	public Variable
	next()
	{
	    return (Variable)iter.next();
	}
    }


    /**
     * Add the contents of another set to this set.
     */
    void
    put(VarSet set)
	throws BadFormException
    {
	for (VariableIterator iter = set.iterator(); iter.hasNext(); )
	    put(iter.next());
    }


    /**
     * Retrieve the variable identified by name.
     */
    public Variable
    get(String name)
    {
	return (Variable)super.getNamed(name);
    }


    /**
     * Return a VariableIterator for the elements.
     */
    public VariableIterator
    iterator()
    {
	return new Iterator();
    }


    /**
     * Return a new array containing the variables of the set. 
     */
    public synchronized Variable[]
    toArray()
    {
	Variable[]		array = new Variable[size()];
	VariableIterator	iter = iterator();

	for (int i = 0; iter.hasNext(); ++i)
	    array[i] = iter.next();

	return array;
    }
}
