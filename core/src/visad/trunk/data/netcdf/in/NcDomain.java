/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcDomain.java,v 1.4 1998-09-16 15:06:37 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import ucar.netcdf.Netcdf;
import visad.Gridded1DSet;
import visad.GriddedSet;
import visad.Integer1DSet;
import visad.IntegerNDSet;
import visad.IntegerSet;
import visad.Linear1DSet;
import visad.LinearLatLonSet;
import visad.LinearNDSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;


/**
 * Provides support for the domain of a VisAD Field.
 *
 * Instances are non-modifiable.
 */
public class
NcDomain
{
    /**
     * The netCDF dimensions constituting this domain (in VisAD order)
     */
    private final NcDim[]	dims;

    /**
     * The Netcdf dataset that contains this domain.
     */
    private final Netcdf	netcdf;

    /**
     * The VisAD MathType of the domain.
     */
    private final MathType	type;

    /**
     * The sampling set of this domain.
     */
    private GriddedSet		set = null;

    /**
     * A cache of NcDomains.
     */
    private static final Map	cache = 
	Collections.synchronizedMap(new WeakHashMap());


    /**
     * Constructs from an adapted netCDF dimension.
     *
     * @param dim		The adapted netCDF dimension.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>dim</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    private
    NcDomain(NcDim dim, Netcdf netcdf)
	throws VisADException
    {
	this(new NcDim[] {dim}, netcdf);
    }


    /**
     * Constructs from an array of adapted netCDF dimensions.
     *
     * @param dims		The array of adapted netCDF dimensions in
     *				netCDF order.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>dims</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    private
    NcDomain(NcDim[] dims, Netcdf netcdf)
	throws VisADException
    {
	int	rank = dims.length;

	if (rank == 0)
	{
	    type = null;		// means scalar domain
	}
	else
	{
	    if (rank == 1)
	    {
		type = dims[0].getMathType();
	    }
	    else
	    {
		/*
		 * Convert the order of the dimensions from netCDF to VisAD.
		 */
		{
		    int j = rank;
		    int	mid = rank/2;

		    for (int i = 0; i < mid; ++i)
		    {
			NcDim	tmp = dims[i];

			dims[i] = dims[--j];
			dims[j] = tmp;
		    }
		}

		// TODO: support coordinate sytem?
		type = new RealTupleType(computeRealTypes(dims));
	    }
	}

	this.dims = dims;
	this.netcdf = netcdf;

	/*
	 * The sampling set isn't set because that is a potentially expensive
	 * operation.  See <code>getSet()</code> below.
	 */
    }


    /**
     * Factory method for constructing the appropriate NcDomain from a
     * single adapted, netCDF dimension.
     *
     * @param dim		The adapted netCDF dimension.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>dim</code>.
     * @return			The NcDomain corresponding to <code>dim</code>.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public static NcDomain
    newNcDomain(NcDim dim, Netcdf netcdf)
	throws VisADException
    {
	return newNcDomain(new NcDim[] {dim}, netcdf);
    }


    /**
     * Factory method for constructing the appropriate NcDomain.
     *
     * @param dims		The array of adapted netCDF dimensions in
     *				netCDF order.
     * @param netcdf		The netCDF dataset that contains 
     *				<code>dims</code>.
     * @return			The NcDomain corresponding to <code>dims</code>;
     *				or <code>null</code> for scalar domains.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public static NcDomain
    newNcDomain(NcDim[] dims, Netcdf netcdf)
	throws VisADException
    {
	NcDomain	domain;

	if (dims.length == 0)
	{
	    domain = null;
	}
	else
	{
	    Key		key = new Key(dims, netcdf);

	    domain = (NcDomain)cache.get(key);

	    if (domain == null)
	    {
		domain = new NcDomain(dims, netcdf);
		cache.put(key, domain);
	    }
	}

	return domain;
    }


    /**
     * Computes the VisAD MathTypes of netCDF dimensions.
     *
     * @param dims		The netCDF dimensions in VisAD order.
     * @return			An array of the VisAD RealTypes of the 
     *				dimensions.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected static RealType[]
    computeRealTypes(NcDim[] dims)
	throws VisADException
    {
	RealType[]	types = new RealType[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    types[idim] = dims[idim].getMathType();

	return types;
    }


    /**
     * Indicates whether or not this domain semantically equals another
     * domain 
     *.
     */
    public boolean
    equals(NcDomain domain)
    {
	return dims.length == 0
		? domain.dims.length == 0
		: type.equals(domain.type);
    }


    /**
     * Gets the VisAD MathType of this domain.
     *
     * @return	The VisAD MathType of this domain or <code>null</code> for
     *		the scalar domain.
     */
    public MathType
    getType()
    {
	return type;
    }


    /**
     * Gets the rank of this domain.
     *
     * @return		The rank of this domain.
     */
    public int
    getRank()
    {
	return dims.length;
    }


    /**
     * Gets the outer domain of this domain.
     *
     * @precondition		<code>getRank() >= 1</code>
     * @return			The outer domain of this domain.
     * @throws NestedException	<code>getRank() < 1</code>
     */
    public NcDomain
    getOuterDomain()
	throws VisADException
    {
	if (getRank() < 1)
	    throw new NestedException("Can't get outer domain of scalar");

	return newNcDomain(new NcDim[] {dims[dims.length-1]}, netcdf);
    }


    /**
     * Gets the VisAD Set of this domain.
     *
     * @return			The VisAD Set of this domain or 
     *				<code>null</code> for scalar domains.
     * @throws IOException	I/O failure.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Set
    getSet()
	throws IOException, VisADException
    {
	if (set == null)
	    if (dims.length >= 1)
		set = computeDomainSet(dims, type);

	return set;
    }


    /**
     * Computes the domain-set of the given, netCDF dimensions.  Potentially
     * expensive.
     *
     * @param dims		The netCDF dimensions of the domain in VisAD
     *				order.
     * @param domain		The VisAD MathType of the domain.
     * @return			The sampling domain-set of the function.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @exception IOException	Data access I/O failure.
     */
    private static GriddedSet
    computeDomainSet(NcDim[] dims, MathType domain)
	throws IOException, VisADException
    {
	GriddedSet	set;
	Gridded1DSet[]	sets = new Gridded1DSet[dims.length];

	for (int i = 0; i < dims.length; ++i)
	    sets[i] = dims[i].getSet();

	boolean	allInteger1DSets = true;

	for (int i = 0; allInteger1DSets && i < dims.length; ++i)
	    allInteger1DSets = sets[i] instanceof Integer1DSet;

	if (allInteger1DSets)
	{
	    set = (GriddedSet)computeIntegerSet(sets, domain);
	}
	else
	{
	    boolean	allLinear1DSets = true;

	    for (int i = 0; allLinear1DSets && i < dims.length; ++i)
		allLinear1DSets = sets[i] instanceof Linear1DSet;

	    if (allLinear1DSets)
	    {
		set = (GriddedSet)computeLinearSet(sets, domain);
	    }
	    else
	    {
		set = computeGriddedSet(sets, domain);
	    }
	}

	return set;
    }


    /**
     * Computes the IntegerSet of combined Integer1DSet-s and domain type.
     *
     * @param sets		The Integer1DSet-s of the domain.
     * @param domain		The MathType of the domain.
     * @return			The IntegerSet of the domain of the function.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    computeIntegerSet(Gridded1DSet[] sets, MathType type)
	throws VisADException
    {
	int	rank = sets.length;
	int[]	lengths = new int[rank];

	for (int idim = 0; idim < rank; ++idim)
	    lengths[idim] = ((Integer1DSet)sets[idim]).getLength(0);

	// TODO: add CoordinateSystem argument
	return IntegerNDSet.create(type, lengths, /*(CoordinateSystem)*/null,
		/*(Unit[])*/null, /*(ErrorEstimate[])*/null);
    }


    /**
     * Computes the LinearSet of combined Linear1DSet-s and domain type.
     *
     * @param dims		The Linear1DSet-s of the domain.
     * @param type		The VisAD math type of the domain set.  
     *				NB: The units of the dimensions needn't be the
     *				same as the units in <code>type</code>.
     * @return			The LinearSet of the domain of the function.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static LinearSet
    computeLinearSet(Gridded1DSet[] sets, MathType type)
	throws VisADException
    {
	LinearSet	set = null;
	int		rank = sets.length;
	double[]	firsts = new double[rank];
	double[]	lasts = new double[rank];
	int[]		lengths = new int[rank];
	Unit[]		units = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	{
	    Linear1DSet	linear1DSet = (Linear1DSet)sets[idim];

	    firsts[idim] = linear1DSet.getFirst();
	    lengths[idim] = linear1DSet.getLength(0);
	    lasts[idim] = linear1DSet.getLast();
	    units[idim] = linear1DSet.getSetUnits()[0];
	}


	// TODO: add CoordinateSystem argument
	if (rank == 2)
	{
	    RealType[]	types = ((RealTupleType)type).getRealComponents();

	    if ((types[0].equalsExceptNameButUnits(RealType.Longitude) &&
		 types[1].equalsExceptNameButUnits(RealType.Latitude)) ||
	        (types[1].equalsExceptNameButUnits(RealType.Longitude) &&
		 types[0].equalsExceptNameButUnits(RealType.Latitude)))
	    {
		set = new LinearLatLonSet(type,
					firsts[0], lasts[0], lengths[0],
					firsts[1], lasts[1], lengths[1],
					/*(CoordinateSystem)*/null,
					units,
					/*(ErrorEstimate[])*/null);
	    }
	}

	if (set == null)
	{
	    set = LinearNDSet.create(type,
				      firsts, lasts, lengths,
				      /*(CoordinateSystem)*/null,
				      units,
				      /*(ErrorEstimate[])*/null);
	}

	return set;
    }


    /**
     * Computes the GriddedSet of combined Gridded1DSet-s and domain type.
     *
     * @param sets		The Gridded1DSet-s of the domain.
     * @param type		The VisAD math type of the domain set.  NB: The
     *				units of the dimensions needn't be the same as 
     *				the units in <code>type</code>.
     * @return			The GriddedSet of the domain of the function.
     * @throws IOException	Data access I/O failure.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    computeGriddedSet(Gridded1DSet[] sets, MathType type)
	throws VisADException, IOException
    {
	int		rank = sets.length;
	int[]		lengths = new int[rank];
	float[][]	values = new float[rank][];
	int		ntotal = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    lengths[idim] = sets[idim].getLength(0);
	    ntotal *= lengths[idim];
	}

        int step = 1;
        int laststep = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    float[]	vals = sets[idim].getSamples(false)[0];

	    values[idim] = new float[ntotal];

/* WLH 4 Aug 98
	    for (int pos = 0; pos < ntotal/vals.length; pos += vals.length)
		System.arraycopy(vals, 0, values[idim], pos, vals.length);
*/
            step *= lengths[idim];
            for (int i=0; i<lengths[idim]; i++) {
              int istep = i * laststep;
              for (int j=0; j<ntotal; j+=step) {
                for (int k=0; k<laststep; k++) {
                  values[idim][istep+j+k] = vals[i];
                }
              }
            }
            laststep = step;
	}

	Unit[]	units = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	    units[idim] = sets[idim].getSetUnits()[0];

	// TODO: add CoordinateSystem argument
	return GriddedSet.create(type, values, lengths,
		 /*(CoordinateSystem)*/null, units, /*(ErrorEstimate[])*/null);
    }


    /**
     * Supports the key field of the domain cache.
     */
    protected static class
    Key
    {
	private NcDim[]	dims;
	private Netcdf	netcdf;

	/*
	 * @param dims		The netCDF dimensions in netCDF order.
	 */
	protected
	Key(NcDim[] dims, Netcdf netcdf)
	{
	    this.dims = dims;
	    this.netcdf = netcdf;
	}

	public int
	hashCode()
	{
	    int	hash = netcdf.hashCode();

	    for (int i = 0; i < dims.length; ++i)
		hash ^= dims[i].hashCode();

	    return hash;
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

		if (netcdf != that.netcdf || dims.length != that.dims.length)
		{
		    equals = false;
		}
		else
		{
		    equals = true;

		    for (int i = 0; equals && i < dims.length; ++i)
			equals = dims[i].equals(that.dims[i]);
		}
	    }

	    return equals;
	}
    }
}
