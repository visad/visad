package visad.data.netcdf;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import visad.data.BadFormException;
import visad.RealTupleType;
import visad.RealType;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * Class for managing the correspondence between VisAD domains and
 * netCDF variables.
 */
public class
DomainTable
{
    /**
     * The hashtable.
     * Effectively "final".
     */
    protected Hashtable		table;

    /**
     * The dimension table
     * Effectively "final".
     */
    protected DimensionTable 	dimTable;


    /**
     * Construct.
     */
    public
    DomainTable(int initialNumEntries, DimensionTable dimTable)
    {
	table = new Hashtable(initialNumEntries);
	this.dimTable = dimTable;
    }


    /**
     * Add a variable entry.
     * NB: Variables with the same domain accumulate.
     */
    public void
    add(NcVar var)
	throws BadFormException, VisADException
    {
	// System.out.println(this.getClass().getName() + 
	    // ": table.size()=" + table.size());

	Key	key = new Key(var.getDimensions());
	Entry	entry = (Entry)table.get(key);

	if (entry == null)
	{
	    // System.out.println(this.getClass().getName() + 
		// ": key NOT found in table: " + key);

	    table.put(key, new Entry(var));
	}
	else
	{
	    // System.out.println(this.getClass().getName() + 
		// ": key FOUND in table: " + key);

	    entry.add(var);
	}
    }


    /**
     * Return the domains of the table.
     */
    public Domain[]
    getDomains()
	throws UnimplementedException, VisADException, IOException
    {
	Domain[]	domains = new Domain[table.size()];
	Enumeration	enum = table.elements();

	for (int i = 0; i < domains.length; ++i)
	    domains[i] = new Domain(((Entry)enum.nextElement()).getVariables(),
				    dimTable);

	return domains;
    }


    /**
     * Convert to a string.
     */
    public String
    toString()
    {
	return table.toString();
    }


    /**
     * Inner class for a key to the table.
     */
    static class
    Key
    {
	/**
	 * The dimensions:
	 */
	protected final	NcDim[]	dims;


	/**
	 * Construct.
	 */
	Key(NcDim[] dims)
	{
	    this.dims = dims;
	}


	/**
	 * Indicate whether or not this key is the same as another.
	 */
	public boolean
	equals(Object key)
	{
	    Key	that = (Key)key;

	    // System.out.println(this.getClass().getName() + 
		// ": comparing keys: " + this + ": " + that);

	    if (dims.length != that.dims.length)
		return false;

	    for (int i = 0; i < dims.length; ++i)
		if (!dims[i].equals(that.dims[i]))
		    return false;

	    return true;
	}


	/**
	 * Return the hash code of the key.
	 */
	public int
	hashCode()
	{
	    int	code = 0;

	    for (int i = 0; i < dims.length; ++i)
		code ^= dims[i].hashCode();

	    // System.out.println(this.getClass().getName() + 
		// ": hashed key: " + this + ": " + code);

	    return code;
	}


	/**
	 * Convert key to string.
	 */
	public String
	toString()
	{
	    String	id = "(" + dims[0];

	    for (int i = 1; i < dims.length; ++i)
		id += "," + dims[i];

	    id += ")";

	    return id;
	}
    }


    /**
     * Inner class for an entry in the table.
     */
    class
    Entry
    {
	/**
	 * The list of variables in the domain
	 */
	protected final Vector	vars = new Vector();


	/**
	 * Construct.
	 */
	Entry(NcVar var)
	    throws BadFormException, VisADException
	{
	    NcDim[]	dims = var.getDimensions();
	    int		rank = dims.length;
	    RealType[]	types = new RealType[rank];

	    for (int i = 0; i < rank; ++i)
	    {
		NcDim		dim = dims[i];
		RealType	type = dimTable.getRealType(dim);

		if (type == null)
		    throw new BadFormException(
			"DomainTable.Entry.Entry(): Dimension \"" +
			dim + "\" not in table");

		types[rank-1-i] = type;		// NB: reversed order
	    }

	    vars.addElement(var);
	}


	/**
	 * Add a variable to the list of variables in this entry.
	 */
	void
	add(NcVar var)
	{
	    if (!vars.contains(var))
		vars.addElement(var);
	}


	/**
	 * Return the variables in the domain.
	 */
	NcVar[]
	getVariables()
	{
	    NcVar[]	vec = new NcVar[vars.size()];

	    vars.copyInto(vec);

	    return vec;
	}

	/**
	 * Convert to a string.
	 */
	public String
	toString()
	{
	    String	id = "(" + vars.elementAt(0);;

	    for (int i = 1; i < vars.size(); ++i)
		id += "," + vars.elementAt(i);

	    id += ")";

	    return id;
	}
    }


    /**
     * Test this class.
     */
    public static void main(String[] args)
	throws Exception
    {
	NcDim[]		dims = {new NcDim("dim0", 4), new NcDim("dim1", 5)};
	NcVar		var0 = new NcFloat("var0", dims);
	NcVar		var1 = new NcFloat("var1", dims);
	DimensionTable	dimTable = new DimensionTable(dims.length);

	dimTable.put(dims[0]);
	dimTable.put(dims[1]);

	DomainTable	domTable = new DomainTable(7, dimTable);

	domTable.add(var0);
	domTable.add(var0);
	domTable.add(var1);

	System.out.println("main(): domain table:\n" + domTable.toString());
    }
}
