package visad.data.netcdf;


import java.io.IOException;
import visad.FlatField;
import visad.FunctionType;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * Class for adapting a netCDF function whose outermost dimension is 
 * separate to a VisAD function.
 */
class
NcInnerFunction
    extends	NcFunction
{
    /**
     * Construct from an array of adapted, netCDF variables.
     *
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
