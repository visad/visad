/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcDim.java,v 1.8 1998-09-15 21:55:26 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.netcdf.Dimension;
import ucar.netcdf.Netcdf;
import ucar.netcdf.Variable;
import visad.FloatSet;
import visad.Gridded1DSet;
import visad.Integer1DSet;
import visad.Linear1DSet;
import visad.RealType;
import visad.GriddedSet;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * The NcDim class decorates a netCDF dimension.  It's useful when importing
 * a netCDF dataset.
 */
class
NcDim
    extends	Dimension
{
    // PLACEHOLDER
    /**
     * The minimum dimension for the <code>compareTo()</code> method.
     */
    public static final NcDim	MIN = new NcDim("");

    // PLACEHOLDER
    /**
     * The maximum dimension for the <code>compareTo()</code> method.
     */
    public static final NcDim	MAX = new NcDim("\uffff\uffff");

    /**
     * A cache of NcDims.
     */
    private static final Map	cache = 
	Collections.synchronizedMap(new WeakHashMap());


    /**
     * Constructs from a name.  Used to initialize <code>MIN</code> and
     * <code>MAX</code>.
     */
    private
    NcDim(String name)
    {
	super(name, 1);
    }


    /**
     * Construct from a netCDF dimension.  Protected to ensure use of
     * the NcDim factory method.
     *
     * @param dim	The netCDF dimension to be decorated.
     */
    protected
    NcDim(Dimension dim)
    {
	super(dim.getName(), dim.getLength());
    }


    /**
     * Factory method for constructing the right type of dimension decorator.
     *
     * @param dim		The netCDF dimension to be decorated.
     * @param netcdf		The netCDF dataset that contains
     *				<code>dim</code>.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    public static NcDim
    newNcDim(Dimension dim, Netcdf netcdf)
	throws VisADException, IOException
    {
	return newNcDim(dim, netcdf, null);
    }


    /**
     * Factory method for constructing the right type of dimension decorator.
     *
     * @param dim		The netCDF dimension to be decorated.
     * @param netcdf		The netCDF dataset that contains
     *				<code>dim</code>.
     * @param ncVar		The adapted, netCDF variable that *might* be
     *				the coordinate variable of this dimension.
     *				This avoids a circular dependency between
     *				NcVar and NcCoordDim.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    protected static NcDim
    newNcDim(Dimension dim, Netcdf netcdf, NcVar ncVar)
	throws VisADException, IOException
    {
	Key	key = new Key(dim, netcdf);
	NcDim	ncDim = (NcDim)cache.get(key);

	if (ncDim == null)
	{
	    String	name = dim.getName();

	    if (ncVar == null || !name.equals(ncVar.getName()))
	    {
		Variable	var = netcdf.get(name);

		ncVar = var == null
			    ? null
			    : NcVar.newNcVar(var, netcdf);
	    }

	    ncDim = (ncVar == null || ncVar.getRank() != 1 ||
		    ncVar.isText())
			? new NcDim(dim)
			: new NcCoordDim(dim, ncVar);

	    cache.put(key, ncDim);
	}

	return ncDim;
    }


    /**
     * Return the VisAD MathType for this dimension.
     *
     * @return			The VisAD MathType for the dimension.
     * @throws VisADException	Problem in core VisAD.  Probably some
     *				VisAD object couldn't be created.
     */
    RealType
    getMathType()
	throws VisADException
    {
	RealType	mathType = RealType.getRealTypeByName(getName());

	if (mathType == null)
	{
	    mathType = new RealType(getName());

	    // QUESTION: add co-ordinate system?  I don't think so for a netCDF
	    // dimension that doesn't have a co-ordinate variable.
	    Set	set = new FloatSet(mathType);

	    mathType.setDefaultSet(set);
	}
	
	return mathType;
    }


    /**
     * Indicate whether or not this dimension is the same as another.
     *
     * @param that	The other, decorated, netCDF dimension.
     * @return		<code>true</code> if and only if the dimension and
     *			the other dimension are semantically identical.
     */
    boolean
    equals(NcDim that)
    {
	// PLACEHOLDER
	return compareTo(that) == 0;
    }


    /**
     // PLACEHOLDER
     * Compare this dimension to another.
     *
     * @param other	The other, decorated, netCDF dimension.
     * @return		Less than 0, zero, or greater than zero depending on
     *			whether this domain is less than, equal to, or greater
     *			than the other dimension, respectively.
     */
    public int
    compareTo(Object other)
    {
	NcDim	that = (NcDim)other;
	int	result = getName().compareTo(that.getName());

	return result != 0
		? result
		: getLength() - that.getLength();
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
     *
     * @return	<code>true</code> if and only if the dimension represents
     *		time.
     */
    boolean
    isTime()
    {
	String	name = getName();

	return name.equals("time") ||
	       name.equals("Time") ||
	       name.equals("TIME");
    }


    /**
     * Indicate whether or not this dimension corresponds to latitude.
     *
     * @return	<code>true</code> if and only if the dimension corresponds to
     *		latitude.
     */
    boolean
    isLatitude()
    {
	return false;
    }


    /**
     * Indicate whether or not this dimension corresponds to longitude.
     *
     * @return	<code>true</code> if and only if the dimension corresponds to
     *		longitude.
     */
    boolean
    isLongitude()
    {
	return false;
    }


    /**
     * Return the hash code of this dimension.
     *
     * @return	The hash code of the dimension.
     */
    public int
    hashCode()
    {
	return getName().hashCode();
    }


    /**
     * Convert this dimension to a string.
     *
     * @return	The dimension represented as a string.
     * @deprecated
     */
    public String
    toString()
    {
	return getName();
    }


    /**
     * Return the co-ordinate variable associated with this dimension.
     *
     * @return	The netCDF co-ordinate variable associated with the dimension
     *		or <code>null</code> if there isn't one.
     */
    NcVar
    getCoordVar()
    {
	return null;
    }


    /**
     * Return the long name associated with this dimension.
     *
     * @return	The value of the "long_name" attribute of the associated
     *		coordinate variable or <code>null</code> if no such value
     *		exists.  Always returns <code>null</code>.
     */
    public String
    getLongName()
    {
	return null;
    }


    /**
     * Return the unit associated with this dimension.
     *
     * @return	The Unit representation of the value of the "units"
     *		attribute of the associated coordinate variable or
     *		<code>null</code> if no such representation exists.
     *		Always returns <code>null</code>.
     */
    public Unit
    getUnit()
    {
	return null;
    }


    /**
     * Gets the VisAD GriddedSet associated with this dimension.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public Gridded1DSet
    getSet()
	throws VisADException, IOException
    {
	// TODO: add CoordinateSystem argument
	return new Integer1DSet(getMathType(), getLength());
    }


    /**
     * Supports the key field of the dimension cache.
     */
    protected static class
    Key
    {
	private Dimension	dim;
	private Netcdf		netcdf;

	protected
	Key(Dimension dim, Netcdf netcdf)
	{
	    this.dim = dim;
	    this.netcdf = netcdf;
	}

	public int
	hashCode()
	{
	    return dim.getName().hashCode() ^ netcdf.hashCode();
	}

	public boolean
	equals(Object obj)
	{
	    boolean	equals;

	    if (this == obj)
	    {
		equals = true;
	    }
	    else
	    {
		Key	that = (Key)obj;

		equals = dim.getName().equals(that.dim.getName()) &&
			netcdf == that.netcdf;
	    }

	    return equals;
	}
    }
}


/**
 * The NcCoordDim class decorates a netCDF dimension that has a netCDF
 * co-ordinate variable.
 */
class
NcCoordDim
    extends	NcDim
{
    /**
     * The associated, VisAD/netCDF coordinate variable.
     */
    private final NcVar		coordVar;

    /**
     * The associated, VisAD GriddedSet.
     */
    private Gridded1DSet	set;


    /**
     * Construct from a netCDF dimension and dataset.  Protected to ensure
     * use of the NcDim factory method.
     *
     * @param dim		The netCDF dimension that has a co-ordinate
     *				variable.
     * @param var		The adapted, netCDF variable that is
     *				the coordinate variable for <code>dim</code>.
     *				This avoids a circular dependency between
     *				NcVar and NcCoordDim.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @throws IOException	Data access I/O failure.
     */
    protected
    NcCoordDim(Dimension dim, NcVar coordVar)
	throws VisADException, IOException
    {
	super(dim);

	this.coordVar = coordVar;
    }


    /**
     * Gets the coordinate variable associated with this dimension.
     */
    public NcVar
    getCoordVar()
    {
	return coordVar;
    }


    /**
     * Indicate whether or not this dimension is temporal in nature.
     *
     * @return	<code>true</code> if and only if the dimension represents
     *		time.
     */
    boolean
    isTime()
    {
	return super.isTime() || coordVar.isTime();
    }


    /**
     * Indicate whether or not this dimension corresponds to latitude
     *
     * @return	<code>true</code> if and only if the dimension corresponds
     *		to latitude.
     */
    boolean
    isLatitude()
    {
	return coordVar.isLatitude();
    }


    /**
     * Indicate whether or not this dimension corresponds to latitude
     *
     * @return	<code>true</code> if and only if the dimension corresponds
     *		to time.
     */
    boolean
    isLongitude()
    {
	return coordVar.isLongitude();
    }


    /**
     * Return the VisAD MathType for this dimension.  It will be the
     * MathType of the associated co-ordinate variable.
     *
     * @return			The VisAD MathType for the dimension.
     */
    RealType
    getMathType()
    {
	return (RealType)coordVar.getMathType();
    }


    /**
     * Return the long nmae associated with this dimension.
     *
     * @return	The value of the "long_name" attribute of the associated
     *		coordinate variable or <code>null</code> if no such value
     *		exists.  Always returns <code>null</code>.
     */
    public String
    getLongName()
    {
	return coordVar.getLongName();
    }


    /**
     * Return the unit associated with this dimension.
     *
     * @return	The Unit representation of the value of the "units"
     *		attribute of the associated coordinate variable or
     *		<code>null</code> if no such representation exists.
     */
    public Unit
    getUnit()
    {
	return coordVar.getUnit();
    }


    /**
     * Gets the VisAD GriddedSet associated with this dimension.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public Gridded1DSet
    getSet()
	throws VisADException, IOException
    {
	if (set == null)
	    set = computeSet();

	return set;
    }


    /**
     * Computes the VisAD GriddedSet associated with this dimension.
     * Potentially expensive.
     *
     * @throws VisADException	Couldn't create necessary VisAD object.
     * @throws IOException	Data access I/O failure.
     */
    public Gridded1DSet
    computeSet()
	throws IOException, VisADException
    {
	Gridded1DSet	set;
	ArithProg	ap = isLongitude()
				? new LonArithProg()
				: new ArithProg();

	if (ap.accumulate(coordVar.getFloats()))
	{
	    /*
	     * The coordinate-variable is an arithmetic progression.
	     */
	    // TODO: add CoordinateSystem argument
	    set = new Linear1DSet(
		    getMathType(), 
		    ap.getFirst(),
		    ap.getLast(),
		    ap.getNumber(),
		    /*(CoordinateSystem)*/null,
		    new Unit[] {getUnit()},
		    /*(ErrorEstimate[])*/null);
	}
	else
	{
	    /*
	     * The coordinate-variable is not an arithmetic progression.
	     */
	    // TODO: add CoordinateSystem argument
	    set = new Gridded1DSet(
			getMathType(),
			new float[][] {coordVar.getFloats()},
			getLength(),
			/*(CoordinateSystem)*/null,
			new Unit[] {getUnit()},
			/*(ErrorEstimate[])*/null);
	}

	return set;
    }
}
