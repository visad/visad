/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedFunction.java,v 1.5 1998-06-17 20:30:28 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.Data;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.Set;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
import visad.data.CacheStrategy;
import visad.data.FileAccessor;
import visad.data.FileFlatField;
import visad.data.netcdf.UnsupportedOperationException;


/**
 * The NcNestedFunction class adapts a netCDF function to a VisAD Field
 * with a range of FlatFields.
 */
class
NcNestedFunction
    extends	NcFunction
{
    /**
     * The VisAD FunctionType of the inner function.
     */
    private /*final*/ FunctionType	innerType;

    /**
     * The domain set of the inner function.
     */
    private /*final*/ Set		innerDomainSet;

    /**
     * The range sets of the inner function.
     */
    private /*final*/ Set[]		innerRangeSets;

    /**
     * The units of the range of the inner function.
     */
    private /*final*/ Unit[]		innerRangeUnits;


    /**
     * Construct from an array of adapted, netCDF variables.
     *
     * @param vars	The netCDF variables whose dimensions contitute the
     *			domain of the function and whose values contitute the
     *			range of the function.
     * @precondition	All variables have the same (ordered) set of dimensions.
     * @precondition	The dimensional rank is 2 or greater.
     * @exception UnimplementedException	Not yet!
     * @exception VisADException		Couldn't create necessary VisAD
     *						object.
     * @exception IOException			I/O error.
     */
    NcNestedFunction(NcVar[] vars)
	throws UnimplementedException, VisADException, IOException
    {
	super(getFunctionType(vars), new NcDim[] {vars[0].getDimensions()[0]},
	    vars);

	innerType = (FunctionType)((FunctionType)getMathType()).getRange();
	innerDomainSet = getDomainSet(
	    reverse(getInnerDims(vars[0].getDimensions())),
	    innerType.getDomain());
	innerRangeSets = getRangeSets(vars);
	innerRangeUnits = getRangeUnits(vars);
    }


    /**
     * Return the inner dimensions of the netCDF dimensions (i.e. remove the
     * outermost dimension).
     *
     * @param dims	The netCDF dimensions to have the outermost dimension
     *			removed in netCDF order.
     * @return		The netCDF dimensions with the outermost dimension
     *			removed.
     * @precondition	<code>dims.length >= 2</code>.
     */
    private static NcDim[]
    getInnerDims(NcDim[] dims)
    {
	int	newRank = dims.length - 1;
	NcDim[]	innerDims = new NcDim[newRank];

	System.arraycopy(dims, 1, innerDims, 0, newRank);

	return innerDims;
    }


    /**
     * Return the VisAD MathType of the function.
     *
     * @param vars	The netCDF variables comprising the range of the
     *			function.
     * @exception VisADException		Couldn't create necessary VisAD
     *						object.
     */
    private static FunctionType
    getFunctionType(NcVar[] vars)
	throws VisADException
    {
	NcDim[]		dims = vars[0].getDimensions();
	NcDim[]		innerDims = getInnerDims(dims);
	FunctionType	rangeType = getFunctionType(innerDims, vars);
	MathType	domainType = getDomainMathType(new NcDim[] {dims[0]});

	return new FunctionType(domainType, rangeType);
    }


    /**
     * Return the VisAD data object corresponding to this function.
     *
     * @return		The VisAD data object corresponding to the function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    DataImpl
    getData()
	throws IOException, VisADException
    {
	FunctionType	type = (FunctionType)getMathType();
	FieldImpl	field =
	    new FieldImpl(type, getDomainSet(getDomainDims(),
		type.getDomain()));

	field.setSamples(getRangeData(), /*copy=*/false);

	return field;
    }


    /**
     * Return a proxy for the VisAD data object corresponding to this function.
     *
     * @return		The VisAD data object corresponding to the function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    DataImpl
    getProxy()
	throws IOException, VisADException
    {
	FunctionType	type = (FunctionType)getMathType();
	FieldImpl	field =
	    new FieldImpl(type, getDomainSet(getDomainDims(),
		type.getDomain()));

	field.setSamples(getRangeProxies(), /*copy=*/false);

	return field;
    }


    /**
     * Return the range values of this function.
     *
     * @return		The FlatFields constituting the range of the function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected FlatField[]
    getRangeData()
	throws VisADException, IOException
    {
	int		npts = getDomainDims()[0].getLength();
	FlatField[]	values = new FlatField[npts];

	for (int ipt = 0; ipt < npts; ++ipt)
	    values[ipt] = getFlatField(ipt);

	return values;
    }


    /**
     * Return proxies for the range values of this function.
     *
     * @return		The FlatField proxies constituting the range of the 
     *			function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected FlatField[]
    getRangeProxies()
	throws VisADException, IOException
    {
	int		npts = getDomainDims()[0].getLength();
	FlatField[]	values = new FlatField[npts];

	for (int ipt = 0; ipt < npts; ++ipt)
	    values[ipt] = getFlatFieldProxy(ipt);

	return values;
    }


    /**
     * Return the inner function at the given, outermost dimension index.
     *
     * @param ipt	The index of the outermost dimension.
     * @return		The VisAD FlatField at <code>ipt</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    FlatField
    getFlatField(int ipt)
	throws IOException, VisADException
    {
	FlatField	field;

	if (hasTextualComponent())
	{
	    // TODO: support text in Fields
	    field = null;
	}
	else
	{
	    FlatField	flatField =
		new FlatField(
		    innerType, 
		    innerDomainSet,
		    (CoordinateSystem)null,
		    innerRangeSets,
		    innerRangeUnits);

	    flatField.setSamples(getRangeDoubles(ipt), /*copy=*/false);

	    field = flatField;
	}

	return field;
    }


    /**
     * Return a proxy for the inner function at the given, outermost 
     * dimension index.
     *
     * @param ipt	The index of the outermost dimension.
     * @return		The VisAD FlatField at <code>ipt</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    FlatField
    getFlatFieldProxy(int ipt)
	throws IOException, VisADException
    {
	FlatField	field;

	if (hasTextualComponent())
	{
	    // TODO: support text in Fields
	    field = null;
	}
	else
	{
	    field = new FileFlatField(new Accessor(ipt), new CacheStrategy());
	}

	return field;
    }


    /**
     * Return the range values of the inner function as doubles.
     *
     * @param ipt	The index of the outermost dimension.
     * @return		The range values of the function.
     * @exception IOException
     *			Data access I/O failure.
     */
    private double[][]
    getRangeDoubles(int ipt)
	throws IOException
    {
	NcVar[]		vars = getVars();
	int		nvars = vars.length;
	double[][]	values = new double[nvars][];

	for (int ivar = 0; ivar < nvars; ++ivar)
	    values[ivar] = vars[ivar].getDoubleValues(ipt);

	return values;
    }


    /**
     * FileAccessor for the FlatField range values of nested, adapted,
     * netCDF functions.
     */
    protected class
    Accessor
	extends	FileAccessor
    {
	/**
	 * The range index of the FlatField.
	 */
	protected final int	rangeIndex;


	/**
	 * Construct from a range index.
	 */
	Accessor(int rangeIndex)
	{
	    this.rangeIndex = rangeIndex;
	}


	/*
	 * Write data into the backing file.  Not supported.
	 *
	 * @exception UnsupportedOperationException	Always thrown.
	 */
	public void
	writeFile(int[] fileLocations, Data range)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Read data from the backing file.  Not supported.
	 *
	 * @exception UnsupportedOperationException	Always thrown.
	 */
	public double[][]
	readFlatField(FlatField template, int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Write data to the backing file.  Not supported.
	 *
	 * @exception UnsupportedOperationException	Always thrown.
	 */
	public void
	writeFlatField(double[][] values, FlatField template, 
			int[] fileLocation)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException();
	}


	/*
	 * Return the FlatField.
	 *
	 * @exception VisADException		Couldn't create necessary VisAD
	 *					object.
	 */
	public FlatField
	getFlatField()
	    throws VisADException
	{
	    try
	    {
		return NcNestedFunction.this.getFlatField(rangeIndex);
	    }
	    catch (Exception e)
	    {
		throw new VisADException(e.getMessage());
	    }
	}

	public FunctionType
	getFunctionType()
	{
	    return innerType;
	}
    }
}
