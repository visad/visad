/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DomainTable.java,v 1.2 1998-03-30 18:20:16 visad Exp $
 */

package visad.data.netcdf.in;


import java.util.Hashtable;
import java.util.Vector;
import visad.data.BadFormException;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;


/**
 * The DomainTable class manages the correspondence between VisAD domains and
 * netCDF variables.
 */
class
DomainTable
{
    /**
     * The hashtable.
     */
    protected final Hashtable		table;


    /**
     * Construct.
     */
    DomainTable(int initialNumEntries)
    {
	table = new Hashtable(initialNumEntries);
    }


    /**
     * Add a variable entry.  Variables with the same domain accumulate.
     *
     * @param var			A netCDF variable that's to be imported.
     * @exception BadFormException	netCDF couldn't handle VisAD object.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    void
    put(NcVar var)
	throws BadFormException, VisADException
    {
	// System.out.println(this.getClass().getName() + 
	    // ": table.size()=" + table.size());

	NcDim[]	dims = var.getDimensions();
	Key		key = new Key(dims);
	Entry		entry = (Entry)table.get(key);

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
     *
     * @return				The Domains in the table.
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    Domain[]
    getDomains()
	throws VisADException
    {
	Domain[]		domains = new Domain[table.size()];
	java.util.Enumeration	enum = table.elements();

	for (int i = 0; i < domains.length; ++i)
	    domains[i] = new Domain(((Entry)enum.nextElement()).getVariables());

	return domains;
    }


    /**
     * Return an enumeration of the domains in the table.
     *
     * @return	An enumeration of the domains in the table.
     */
    Enumeration
    getEnumeration()
    {
	return new Enumeration();
    }


    /**
     * Inner class for enumerating the domains in the table.
     */
    class
    Enumeration
    {
	java.util.Enumeration	enum = table.elements();

	public boolean
	hasMoreElements()
	{
	    return enum.hasMoreElements();
	}

	public Domain
	nextElement()
	    throws VisADException
	{
	    return new Domain(((Entry)enum.nextElement()).getVariables());
	}
    }


    /**
     * Convert to a string.
     *
     * @return	The table represented as a string.
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
	 * The domain.
	 */
	protected final Domain	domain;


	/**
	 * Construct from an array of adapted, netCDF dimensions.
	 */
	Key(NcDim[] dims)
	    throws VisADException
	{
	    domain = new Domain(dims);
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

	    return domain.equals(that.domain);
	}


	/**
	 * Return the hash code of the key.
	 */
	public int
	hashCode()
	{
	    int	code = domain.hashCode();

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
	    return domain.toString();
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
		RealType	type = (RealType)dim.getMathType();

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
}
