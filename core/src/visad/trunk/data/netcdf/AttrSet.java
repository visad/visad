package visad.data.netcdf;


import ucar.netcdf.Attribute;
import ucar.netcdf.AttributeIterator;
import ucar.netcdf.AttributeSet;




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
