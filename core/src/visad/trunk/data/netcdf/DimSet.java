package visad.data.netcdf;


import ucar.netcdf.Dimension;
import ucar.netcdf.DimensionIterator;
import ucar.netcdf.DimensionSet;
import visad.data.BadFormException;


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
