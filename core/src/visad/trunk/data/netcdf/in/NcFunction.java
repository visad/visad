/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcFunction.java,v 1.3 1998-06-17 20:30:27 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.FunctionType;
import visad.GriddedSet;
import visad.IntegerNDSet;
import visad.LinearLatLonSet;
import visad.LinearNDSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SI;
import visad.Set;
import visad.TupleType;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;


/**
 * The NcFunction class adapts an imported netCDF function to a VisAD 
 * function.
 */
abstract class
NcFunction
    extends	NcData
{
    /**
     * The netCDF dimensions of the function domain IN VisAD ORDER.
     */
    private final NcDim[]	domainDims;

    /**
     * The associated netCDF variables.
     */
    private final NcVar[]	vars;

    /**
     * Whether or not at least one of the netCDF variables is textual.
     */
    private final boolean	hasTextualComponent;


    /**
     * Factory method for creating the appropriate NcFunction from an array
     * of adapted, netCDF variables.
     *
     * @param vars	The netCDF variables of the netCDF function.
     * @precondition	All variables have the same netCDF shape.
     * @precondition	<code>vars.length >= 1</code>.
     * @precondition	The rank of the variables >= 1.
     * @return		The adapted netCDF function.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     * @exception IOException
     *			Data access I/O failure.
     * @exception UnimplementedException
     *			Not implemented yet.
     */
    static NcFunction
    newNcFunction(NcVar[] vars)
	throws VisADException, IOException, UnimplementedException
    {
	NcVar	var = vars[0];

	return (var.getRank() < 2 || !var.getDimensions()[0].isTime())
		    ? (NcFunction)new NcRegFunction(vars)
		    : (NcFunction)new NcNestedFunction(vars);
    }


    /**
     * Protected constructor for derived classes.
     *
     * @param type	The FunctionType of the VisAD function.
     * @param dims	The netCDF dimensions constituting the domain of the
     *			function in netCDF order.
     */
    protected
    NcFunction(FunctionType type, NcDim[] dims, NcVar[] vars)
    {
	super(type);

	domainDims = reverse(dims);
	this.vars = vars;
	hasTextualComponent = hasText(vars);
    }


    /**
     * Return the reversed ordering of the given, netCDF dimensions.
     *
     * @param dims	The array of netCDF dimensions to be reversed.
     * @return		<code>varDims</code> with the order reversed.
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
     * Return the VisAD FunctionType for a function mapping from the the
     * given netCDF dimensions to the given netCDF variables.
     *
     * @param dims	The netCDF dimensions comprising the domain in
     *			netCDF order.
     * @param vars	The netCDF variables comprising the function.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static FunctionType
    getFunctionType(NcDim[] dims, NcVar[] vars)
	throws VisADException
    {
	return
	    new FunctionType(getDomainMathType(dims), getRangeMathType(vars));
    }


    /**
     * Return the VisAD MathType of the domain of the given netCDF dimensions.
     *
     * @parm dims	The netCDF dimensions of the domain in netCDF order.
     * @return		The VisAD MathType of the domain of the function.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static MathType
    getDomainMathType(NcDim[] dims)
	throws VisADException
    {
	int		rank = dims.length;
	MathType	type;

	dims = reverse(dims);	// convert to VisAD order

	if (rank == 1)
	{
	    type = dims[0].getMathType();
	}
	else
	{
	    // TODO: support coordinate sytem?
	    type = new RealTupleType(getRealTypes(dims));
	}

	return type;
    }


    /**
     * Return the types of the given, netCDF dimensions.
     *
     * @param dims	The netCDF dimensions.
     * @return		An array of the VisAD MathTypes of the dimensions.
     * @exception VisADException
     *			Couldn't create a necessary MathType.
     */
    protected static RealType[]
    getRealTypes(NcDim[] dims)
	throws VisADException
    {
	RealType[]	types = new RealType[dims.length];

	for (int idim = 0; idim < dims.length; ++idim)
	    types[idim] = dims[idim].getMathType();

	return types;
    }


    /**
     * Return the VisAD MathType of the range.
     *
     * @return		The VisAD MathType of the range of the function.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static MathType
    getRangeMathType(NcVar[] vars)
	throws VisADException
    {
	int		nvars = vars.length;
	MathType	type;

	if (nvars == 0)
	    type = null;
	else
	if (nvars == 1)
	{
	    type = vars[0].getMathType();
	}
	else
	{
	    if (hasText(vars))
	    {
		MathType[]	types = new MathType[nvars];

		for (int i = 0; i < nvars; ++i)
		    types[i] = vars[i].getMathType();

		type = new TupleType(types);
	    }
	    else
	    {
		RealType[]	types = new RealType[nvars];

		for (int i = 0; i < nvars; ++i)
		    types[i] = (RealType)vars[i].getMathType();

		type = new RealTupleType(types);
	    }
	}

	return type;
    }


    /**
     * Indicate whether or not at least one of the given netCDF variables
     * is textual.
     *
     * @param vars	The netCDF variables to be examined.
     * @return		<code>true</code> if and only if at least one of the
     *			netCDF variables is textual.
     */
    protected static boolean
    hasText(NcVar[] vars)
    {
	int		nvars = vars.length;

	for (int i = 0; i < nvars; ++i)
	    if (vars[i].isText())
		return true;

	return false;
    }


    /**
     * Return the domain-set of the given, netCDF dimensions.
     *
     * @param dims	The netCDF dimensions of the domain in VisAD order.
     * @param domain	The VisAD MathType of the domain.
     * @return		The sampling domain-set of the function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected static Set
    getDomainSet(NcDim[] dims, MathType domain)
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

		    if (!aps[idim].accumulate(coordVars[idim].getFloatValues()))
			allCoordVarsAreArithProgs = false;
		}
	    }
	}

	if (noCoordVars)
	{
	    /*
	     * This domain has no co-ordinate variables.
	     */
	    domainSet = getIntegerSet(dims, domain);
	}
	else
	if (allCoordVarsAreArithProgs)
	{
	    /*
	     * This domain has co-ordinate variables -- all of which are
	     * arithmetic progressions.
	     */
	    domainSet = (Set)getLinearSet(dims, aps, coordVars, domain);
	}
	else
	{
	    /*
	     * This domain has at least one co-ordinate variable which is 
	     * not an arithmetic progression.  This is the general case.
	     */
	    domainSet = getGriddedSet(dims, coordVars, domain);
	}

	return domainSet;
    }


    /**
     * Return the IntegerSet of the given, netCDF dimensions and domain.
     *
     * @param dims	The netCDF dimensions of the domain in VisAD order.
     * @param domain	The MathType of the domain.
     * @return		The IntegerSet of the domain of the function.
     * @precondition	The sampling domain-set of the function is (logically)
     *			an IntegerSet.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    getIntegerSet(NcDim[] dims, MathType domain)
	throws VisADException
    {
	int	rank = dims.length;
	int[]	lengths = new int[rank];

	for (int idim = 0; idim < rank; ++idim)
	    lengths[idim] = dims[idim].getLength();

	// TODO: add CoordinateSystem argument
	return IntegerNDSet.create(domain, lengths);
    }


    /**
     * Return the LinearSet of the given dimensions, arithmetic progressions
     * and coordinate variables.
     *
     * @param dims	The netCDF dimensions of the domain in VisAD order.
     * @return		The LinearSet of the domain of the function.
     * @precondition	The sampling domain-set of this function is (logically)
     *			a LinearSet.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static LinearSet
    getLinearSet(NcDim[] dims, ArithProg[] aps, NcVar[] coordVars, 
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

	// TODO: add CoordinateSystem argument
	if (!(rank == 2 &&
	      ((dims[0].isLatitude() && dims[1].isLongitude()) ||
	       (dims[1].isLatitude() && dims[0].isLongitude()))))
	{
	    set = LinearNDSet.create(domainType,
				      firsts, lasts, lengths,
				      null, null, null);
	}
	else
	{
	    double	first0;
	    double	last0;
	    double	first1;
	    double	last1;

	    if (dims[0].isLongitude())
	    {
		first0 = -firsts[0];	// KLUDGE, HACK, WORKAROUND
		last0 = -lasts[0];	// KLUDGE, HACK, WORKAROUND
		first1 = firsts[1];
		last1 = lasts[1];
	    }
	    else
	    {
		first0 = firsts[0];
		last0 = lasts[0];
		first1 = -firsts[1];	// KLUDGE, HACK, WORKAROUND
		last1 = -lasts[1];	// KLUDGE, HACK, WORKAROUND
	    }
	    set = new LinearLatLonSet(domainType,
					first0, last0, lengths[0],
					first1, last1, lengths[1]);
	}

	return set;
    }


    /**
     * Return the GriddedSet of the given dimensions and coordinate
     * variables.
     *
     * @param dims	The netCDF dimensions of the domain in VisAD order.
     * @return		The GriddedSet of the domain of the function.
     * @precondition	The sampling domain-set of this function is a 
     *			GriddedSet.
     * @exception IOException
     *			Data access I/O failure.
     * @exception VisADException
     *			Couldn't create a necessary VisAD object.
     */
    protected static GriddedSet
    getGriddedSet(NcDim[] dims, NcVar[] coordVars, MathType domain)
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

	for (int idim = 0; idim < rank; ++idim)
	{
	    float[]	vals;

	    values[idim] = new float[ntotal];

	    if (coordVars[idim] != null)
		vals = coordVars[idim].getFloatValues();
	    else
	    {
		int	npts = lengths[idim];

		vals = new float[npts];

		for (int ipt = 0; ipt < npts; ++ipt)
		    vals[ipt] = ipt;
	    }

	    for (int pos = 0; pos < ntotal/vals.length; pos += vals.length)
		System.arraycopy(vals, 0, values[idim], pos, vals.length);
	}

	// TODO: add CoordinateSystem argument
	return GriddedSet.create(domain, values, lengths);
    }


    /**
     * Return the range Sets of the given, netCDF variables.
     *
     * @return	The range Sets of the function.
     */
    protected static Set[]
    getRangeSets(NcVar[] vars)
    {
	Set[]	sets = new Set[vars.length];

	for (int i = 0; i < vars.length; ++i)
	    sets[i] = ((NcNumber)vars[i]).getSet();

	return sets;
    }


    /**
     * Return the range Units of the given, netCDF variables.
     *
     * @return	The range units of the function.
     */
    protected static Unit[]
    getRangeUnits(NcVar[] vars)
    {
	Unit[]	units = new Unit[vars.length];

	for (int i = 0; i < vars.length; ++i)
	    units[i] = vars[i].getUnit();

	return units;
    }


    /**
     * Return the netCDF dimensions of the function domain in VisAD order.
     */
    protected NcDim[]
    getDomainDims()
    {
	NcDim[]	newDomainDims = new NcDim[domainDims.length];

	System.arraycopy(domainDims, 0, newDomainDims, 0, domainDims.length);

	return newDomainDims;
    }


    /**
     * Return the netCDF variables associated with the function.
     */
    protected NcVar[]
    getVars()
    {
	NcVar[]	newVars = new NcVar[vars.length];

	System.arraycopy(vars, 0, newVars, 0, vars.length);

	return newVars;
    }


    /**
     * Indicate whether or not the function's range has a textual component.
     */
    protected boolean
    hasTextualComponent()
    {
	return hasTextualComponent;
    }
}
