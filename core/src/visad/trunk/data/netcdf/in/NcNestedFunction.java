/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedFunction.java,v 1.2 1998-04-02 20:49:44 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.Set;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
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
    private final FunctionType	innerType;

    /**
     * The domain set of the inner function.
     */
    private final Set		innerDomainSet;

    /**
     * The range sets of the inner function.
     */
    private final Set[]		innerRangeSets;

    /**
     * The units of the range of the inner function.
     */
    private final Unit[]	innerRangeUnits;


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

	innerType = (FunctionType)((FunctionType)mathType).getRange();
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
	FieldImpl	field
	    = new FieldImpl((FunctionType)mathType, 
		getDomainSet(domainDims, ((FunctionType)mathType).getDomain()));

	field.setSamples(getRangeFlatFields(), /*copy=*/false);

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
	// TODO: implement NcNestedFunction.getProxy()
	throw new UnsupportedOperationException();
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
    getRangeFlatFields()
	throws VisADException, IOException
    {
	int		npts = domainDims[0].getLength();
	FlatField[]	values = new FlatField[npts];

	for (int ipt = 0; ipt < npts; ++ipt)
	    values[ipt] = getFlatField(ipt);

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

	if (hasTextualComponent)
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
	int		nvars = vars.length;
	double[][]	values = new double[nvars][];

	for (int ivar = 0; ivar < nvars; ++ivar)
	    values[ivar] = vars[ivar].getDoubleValues(ipt);

	return values;
    }
}
