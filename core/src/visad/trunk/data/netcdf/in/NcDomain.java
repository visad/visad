/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcDomain.java,v 1.2 1998-09-14 13:51:36 billh Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.GriddedSet;
import visad.IntegerNDSet;
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
 */
public class
NcDomain
{
    /**
     * The netCDF dimensions constituting this domain (in netCDF order)
     */
    private final NcDim[]	dims;


    /**
     * The VisAD MathType of the domain.
     */
    private final MathType	type;

    /* WLH 13 Sept 98 */
    private NcVar our_var = null;
    private boolean outer = false;

    /* WLH 13 Sept 98 */
    protected NcDomain(NcVar var, NcDim[] dims) throws VisADException {
      this(dims);
      our_var = var;
    }

    /* WLH 13 Sept 98 */
    protected NcDomain(NcVar var, NcDim[] dims, boolean b) throws VisADException {
      this(dims);
      our_var = var;
      outer = true;
    }

    /* WLH 13 Sept 98 */
    protected NcDomain(NcVar var, NcDim dim) throws VisADException {
      this(dim);
      our_var = var;
    }

    /**
     * Constructs from an adapted netCDF dimension.
     *
     * @param dim		The adapted netCDF dimension.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    NcDomain(NcDim dim)
	throws VisADException
    {
	this(new NcDim[] {dim});
    }


    /**
     * Constructs from an array of adapted netCDF dimensions.
     *
     * @param dims		The array of adapted netCDF dimensions.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    protected
    NcDomain(NcDim[] dims)
	throws VisADException
    {
	int		rank = dims.length;

	if (rank == 0)
	{
	    type = null;		// means scalar domain
	}
	else if (rank == 1)
	{
	    type = dims[0].getMathType();
	}
	else
	{
	    dims = reverse(dims);	// convert to VisAD order

	    // TODO: support coordinate sytem?
	    type = new RealTupleType(computeRealTypes(dims));
	}

	this.dims = dims;
    }


    /**
     * Reverses the order of netCDF dimensions.
     *
     * @param dims	The array of netCDF dimensions to be reversed.
     * @return		The netCDF dimensions in reverse order.
     */
    protected static NcDim[]
    reverse(NcDim[] dims)
    {
	int	rank = dims.length;
	NcDim[]	newDims = new NcDim[rank];

	for (int idim = 0; idim < rank; ++idim)
	    newDims[idim] = dims[rank-1-idim];

	return newDims;
    }


    /**
     * Computes the VisAD MathTypes of netCDF dimensions.
     *
     * @param dims		The netCDF dimensions.
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
     * @precondition	<code>getRank() >= 1</code>
     * @return		The outer domain of this domain.
     */
    public NcDomain
    getOuterDomain()
	throws VisADException
    {
	if (getRank() < 1)
	    throw new NestedException("Can't get outer domain of scalar");

	return new NcDomain(new NcDim[] {dims[0]});
    }


    /**
     * Gets the VisAD Set of this domain.
     *
     * @precondition		<code>getRank() >= 1</code>
     * @throws IOException	I/O failure.
     * @throws VisADException	Couldn't create necessary VisAD object.
     */
    public Set
    getSet()
	throws IOException, VisADException
    {
	if (getRank() < 1)
	    throw new NestedException("Can't get sampling set of scalar");

      /* WLH 13 Sept 98 */
      if (our_var == null) {
        return computeDomainSet(dims, type);
      }
      else {
        if (outer) {
          if (our_var.outerDomainSet == null) {
            our_var.outerDomainSet = computeDomainSet(dims, type);
          }
          return our_var.outerDomainSet;
        }
        else {
          if (our_var.computedDomainSet == null) {
            our_var.computedDomainSet = computeDomainSet(dims, type);
          }
          return our_var.computedDomainSet;
        }
      }

/* WLH 13 Sept 98
	return computeDomainSet(dims, type);
*/
    }


    /**
     * Computes the domain-set of the given, netCDF dimensions.
     *
     * @param dims		The netCDF dimensions of the domain in VisAD
     *				order.
     * @param domain		The VisAD MathType of the domain.
     * @return			The sampling domain-set of the function.
     * @throws VisADException	Problem in core VisAD.  Probably some VisAD
     *				object couldn't be created.
     * @exception IOException	Data access I/O failure.
     */
    protected static Set
    computeDomainSet(NcDim[] dims, MathType domain)
	throws IOException, VisADException
    {
	Set		domainSet;
	int		rank = dims.length;
	NcVar[]		coordVars = new NcVar[rank];
	boolean		noCoordVars = true;
	boolean	 	allCoordVarsAreArithProgs = true;
	ArithProg[]	aps = new ArithProg[rank];

	/*
	 * Because the domain-set can be any one of several subtypes, 
	 * first determine the appropriate subtype.
	 */
	for (int idim = 0; idim < rank; ++idim)
	{
	    coordVars[idim] = dims[idim].getCoordVar();

	    if (coordVars[idim] != null)
	    {
		noCoordVars = false;

		if (allCoordVarsAreArithProgs)
		{
		    if (coordVars[idim].isLongitude())
			aps[idim] = new LonArithProg();
		    else
			aps[idim] = new ArithProg();

		    if (!aps[idim].accumulate(coordVars[idim].getFloats()))
			allCoordVarsAreArithProgs = false;
		}
	    }
	}

	if (noCoordVars)
	{
	    /*
	     * This domain has no co-ordinate variables.
	     */
	    domainSet = computeIntegerSet(dims, domain);
	}
	else
	if (allCoordVarsAreArithProgs)
	{
	    /*
	     * This domain has co-ordinate variables -- all of which are
	     * arithmetic progressions.
	     */
	    domainSet = (Set)computeLinearSet(dims, aps, coordVars, domain);
	}
	else
	{
	    /*
	     * This domain has at least one co-ordinate variable which is 
	     * not an arithmetic progression.  This is the general case.
	     */
	    domainSet = computeGriddedSet(dims, coordVars, domain);
	}

	return domainSet;
    }


    /**
     * Computes the IntegerSet of the given, netCDF dimensions and domain.
     *
     * @param dims		The netCDF dimensions of the domain in VisAD 
     *				order.
     * @param domain		The MathType of the domain.
     * @return			The IntegerSet of the domain of the function.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    computeIntegerSet(NcDim[] dims, MathType domain)
	throws VisADException
    {
	int	rank = dims.length;
	int[]	lengths = new int[rank];
	Unit[]	domainUnits = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	{
	    lengths[idim] = dims[idim].getLength();
	    domainUnits[idim] = dims[idim].getUnit();
	}

	// TODO: add CoordinateSystem argument
	return IntegerNDSet.create(domain, lengths, /*(CoordinateSystem)*/null,
		domainUnits, /*(ErrorEstimate[])*/null);
    }


    /**
     * Computes the LinearSet of the given dimensions, arithmetic progressions
     * and coordinate variables.
     *
     * @param dims		The netCDF dimensions of the domain in VisAD 
     *				order.
     * @param aps		The arithmetic progressions associated with 
     *				<code>dims</code>.
     * @param coordVars		Coordinate variables associated with 
     *				<code>dims</code>.  If(<code>coordVars[i] ==
     *				null</code> then <code>dim[i]</code> 
     *				doesn't have a coordinate variable.
     * @param domainType	The VisAD math type of the domain set.  
     *				NB: The units of the dimensions needn't be the
     *				same as the units in <code>domain</code>.
     * @return			The LinearSet of the domain of the function.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static LinearSet
    computeLinearSet(NcDim[] dims, ArithProg[] aps, NcVar[] coordVars, 
	    MathType domainType)
	throws VisADException
    {
	LinearSet	set;
	int		rank = dims.length;
	double[]	firsts = new double[rank];
	double[]	lasts = new double[rank];
	int[]		lengths = new int[rank];

	for (int idim = 0; idim < rank; ++idim)
	{
	    if (coordVars[idim] == null)
	    {
		/*
		 * The dimension doesn't have a co-ordinate variable.
		 */
		firsts[idim] = 0;
		lengths[idim] = dims[idim].getLength();
		lasts[idim] = lengths[idim] - 1;
	    }
	    else
	    {
		/*
		 * The dimension has a co-ordinate variable.
		 */
		firsts[idim] = aps[idim].getFirst();
		lasts[idim] = aps[idim].getLast();
		lengths[idim] = aps[idim].getNumber();
	    }
	}

	Unit[]	domainUnits = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	    domainUnits[idim] = dims[idim].getUnit();

	// TODO: add CoordinateSystem argument
	if (rank == 2 &&
	      ((dims[0].isLatitude() && dims[1].isLongitude()) ||
	       (dims[1].isLatitude() && dims[0].isLongitude())))
	{
	    set = new LinearLatLonSet(domainType,
					firsts[0], lasts[0], lengths[0],
					firsts[1], lasts[1], lengths[1],
					/*(CoordinateSystem)*/null,
					domainUnits,
					/*(ErrorEstimate[])*/null);
	}
	else
	{
	    set = LinearNDSet.create(domainType,
				      firsts, lasts, lengths,
				      /*(CoordinateSystem)*/null,
				      domainUnits,
				      /*(ErrorEstimate[])*/null);
	}

	return set;
    }


    /**
     * Computes the GriddedSet of the given dimensions and coordinate
     * variables.
     *
     * @param dims		The netCDF dimensions of the domain in VisAD 
     *				order.
     * @param coordVars		Coordinate variables associated with 
     *				<code>dims</code>.  If(<code>coordVars[i] ==
     *				null</code> then <code>dim[i]</code> doesn't
     *				have a coordinate variable.
     * @param domain		The VisAD math type of the domain set.  NB: The
     *				units of the dimensions needn't be the same as 
     *				the units in <code>domain</code>.
     * @return			The GriddedSet of the domain of the function.
     * @throws IOException	Data access I/O failure.
     * @throws VisADException	Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    computeGriddedSet(NcDim[] dims, NcVar[] coordVars, MathType domain)
	throws VisADException, IOException
    {
	int		rank = dims.length;
	int[]		lengths = new int[rank];
	float[][]	values = new float[rank][];
	int		ntotal = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    lengths[idim] = dims[idim].getLength();
	    ntotal *= lengths[idim];
	}

        int step = 1;
        int laststep = 1;

	for (int idim = 0; idim < rank; ++idim)
	{
	    float[]	vals;

	    values[idim] = new float[ntotal];

	    if (coordVars[idim] != null)
		vals = coordVars[idim].getFloats();
	    else
	    {
		int	npts = lengths[idim];

		vals = new float[npts];

		for (int ipt = 0; ipt < npts; ++ipt)
		    vals[ipt] = ipt;
	    }
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

	Unit[]	domainUnits = new Unit[rank];

	for (int idim = 0; idim < rank; ++idim)
	    domainUnits[idim] = dims[idim].getUnit();

	// TODO: add CoordinateSystem argument
	return GriddedSet.create(domain, values, lengths,
		 /*(CoordinateSystem)*/null, domainUnits,
		 /*(ErrorEstimate[])*/null);
    }
}
