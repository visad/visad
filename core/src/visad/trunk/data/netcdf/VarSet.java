package visad.data.netcdf;


import ucar.netcdf.Variable;
import ucar.netcdf.VariableIterator;
import ucar.netcdf.VariableSet;
import visad.data.BadFormException;


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
