/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcInnerFunction.java,v 1.4 1998-03-12 22:03:08 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import visad.FlatField;
import visad.FunctionType;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * The NcInnerFunction class adapts to a VisAD function a netCDF function
 * with a separate outermost dimension.
 */
class
NcInnerFunction
    extends	NcFunction
{
    /**
     * Construct from an array of adapted, netCDF variables.
     *
     * @param vars	The netCDF variables whose dimensions contitute the
     *			domain of the function and whose values contitute the
     *			range of the function.
     * @precondition	All variables have the same (ordered) set of
     *			dimensions and their rank is 2 or greater.
     * @exception UnimplementedException	Not yet!
     * @exception VisADException		Couldn't create necessary 
     *						VisAD object.
     * @exception IOException			I/O error.
     */
    NcInnerFunction(ImportVar[] vars)
	throws UnimplementedException, VisADException, IOException
    {
	NcDim[]	varDims = vars[0].getDimensions();
	int	rank = varDims.length;
	NcDim[]	innerDims = new NcDim[rank-1];

	System.arraycopy(varDims, 1, innerDims, 0, rank-1);

	initialize(innerDims, vars);
    }


    /**
     * Return the VisAD data object corresponding to this function at a
     * given position of the outermost dimension.
     *
     * @param ipt	The position in the outermost dimension.
     * @return		The FlatField corresponding to the given position.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected FlatField
    getData(int ipt)
	throws IOException, VisADException
    {
	FlatField	field = 
	    new FlatField((FunctionType)mathType, getDomainSet());

	field.setSamples(getDoubleValues(ipt), /*copy=*/false);

	return field;
    }


    /**
     * Return the range values of this function -- at a given position of the
     * outermost dimension -- as doubles.
     *
     * @param ipt	The position in the outermost dimension.
     * @return		The range values of the function at the given position.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected double[][]
    getDoubleValues(int ipt)
	throws VisADException, IOException
    {
	int		nvars = vars.length;
	double[][]	values = new double[nvars][];

	for (int ivar = 0; ivar < nvars; ++ivar)
	    values[ivar] = vars[ivar].getDoubleValues(ipt);

	return values;
    }
}
