/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: DomainTable.java,v 1.4 1998-03-31 20:46:28 visad Exp $
 */

package visad.data.netcdf.in;


import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;
import visad.data.BadFormException;


/**
 * The DomainTable class manages the correspondence between VisAD domains and
 * netCDF variables.
 */
class
DomainTable
{
    /**
     * The domain/variables Map.
     */
    protected final Map		map;

    /**
     * The current sequence number.
     */
    protected int		seqNo = 0;


    /**
     * Construct.
     */
    DomainTable(int initialNumEntries)
    {
	map = new TreeMap();
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
	    // ": map.size()=" + map.size());

	NcDim[]	dims = var.getDimensions();
	Key	key = new Key(dims, seqNo++);
	Entry	entry = (Entry)map.get(key);

	if (entry == null)
	{
	    // System.out.println(this.getClass().getName() + 
		// ": key NOT found in map: " + key);

	    map.put(key, new Entry(var));
	}
	else
	{
	    // System.out.println(this.getClass().getName() + 
		// ": key FOUND in map: " + key);

	    entry.add(var);
	}
    }


    /**
     * Return an enumeration of the domains in the map.
     *
     * @return	An enumeration of the domains in the map.
     */
    Enumeration
    getEnumeration()
    {
	return new Enumeration();
    }


    /**
     * Inner class for enumerating the domains in the map.
     */
    class
    Enumeration
    {
	Iterator	iter = map.values().iterator();

	public boolean
	hasMoreElements()
	{
	    return iter.hasNext();
	}

	public NcVar[]
	nextElement()
	    throws VisADException
	{
	    return ((Entry)iter.next()).getVariables();
	}
    }


    /**
     * Convert to a string.
     *
     * @return	The map represented as a string.
     */
    public String
    toString()
    {
	return map.toString();
    }


    /**
     * Inner class for a key to the map.
     */
    static class
    Key
	implements	Comparable
    {
	/**
	 * The domain.
	 */
	protected final Domain	domain;

	/**
	 * The sequence number.
	 */
	protected final int	seqNo;


	/**
	 * Construct from an array of adapted, netCDF dimensions and a sequence
	 * number.
	 */
	Key(NcDim[] dims, int seqNo)
	    throws VisADException
	{
	    domain = new Domain(dims);
	    this.seqNo = seqNo;
	}


	/**
	 * Compare this key to another.
	 */
	public int
	compareTo(Object key)
	{
	    Key	that = (Key)key;

	    // System.out.println(this.getClass().getName() + 
		// ": comparing keys: " + this + ": " + that);

	    /*
             * Scalar domains (i.e. domains with rank zero) are forced
             * to compare unequal so that each scalar has its own
             * domain.  This prevents scalars from being composited
             * into their own VisAD Tuple.  Thus, for example, we get
             * the VisAD MathType (scalar1, scalar2, field) rather than
             * ((scalar1, scalar2), field).
	     */
	    return (domain.getRank() == 0 && that.domain.getRank() == 0)
			? seqNo - that.seqNo
			: domain.compareTo(that.domain);
	}


	/**
	 * Return the hash code of the key.
	 */
	public int
	hashCode()
	{
	    int	code = domain.hashCode() ^ seqNo;

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
     * Inner class for an entry in the map.
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

	    for (int i = 0; i < rank; ++i)
	    {
		NcDim		dim = dims[i];
		RealType	type = (RealType)dim.getMathType();

		if (type == null)
		    throw new BadFormException(
			"DomainTable.Entry.Entry(): Dimension \"" +
			dim + "\" not in map");
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
