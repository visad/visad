package visad.data.netcdf;


import java.io.IOException;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.MathType;
import visad.UnimplementedException;
import visad.VisADException;


/**
 * Class for adapting a netCDF function that has an outermost dimension
 * that is to be kept separate, to a VisAD function.
 */
class
NcNestedFunction
    extends	NcFunction
{
    /**
     * The function corresponding to the domain which doesn't include the
     * outermost dimension.
     */
    protected final NcInnerFunction	innerFunction;


    /**
     * Construct from netCDF dimensions and an array of adapted, netCDF
     * variables.
     *
     * @precondition	All variables have the same (ordered) set of dimensions.
     * @precondition	The dimensional rank is 2 or greater.
     * @exception UnimplementedException	Not yet!
     * @exception VisADException		Couldn't create necessary VisAD
     *						object.
     * @exception IOException			I/O error.
     */
    NcNestedFunction(ImportVar[] vars)
	throws UnimplementedException, VisADException, IOException
    {
	innerFunction = new NcInnerFunction(vars);
	initialize(new NcDim[] {vars[0].getDimensions()[0]}, vars);
    }


    /**
     * Return the VisAD MathType of the range.
     *
     * @exception VisADException	Couldn't create necessary VisAD object.
     */
    protected MathType
    getRangeMathType()
	throws VisADException
    {
	return innerFunction.getMathType();
    }


    /**
     * Return the VisAD data object corresponding to this function.
     */
    DataImpl
    getData()
	throws IOException, VisADException
    {
	FieldImpl	field =
	    new FieldImpl((FunctionType)mathType, getDomainSet());

	field.setSamples(getRangeFlatFields(), /*copy=*/false);

	return field;
    }


    /**
     * Return the range values of this function.
     */
    protected FlatField[]
    getRangeFlatFields()
	throws VisADException, IOException
    {
	int		npts = dims[0].getLength();
	FlatField[]	values = new FlatField[npts];

	for (int ipt = 0; ipt < npts; ++ipt)
	    values[ipt] = innerFunction.getData(ipt);

	return values;
    }
}
