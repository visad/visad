/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcNestedFunction.java,v 1.6 1998-03-12 22:03:09 steve Exp $
 */

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
 * The NcNestedFunction class adapts a netCDF function to a VisAD Field
 * of FlatFields.
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
    NcNestedFunction(ImportVar[] vars)
	throws UnimplementedException, VisADException, IOException
    {
	innerFunction = new NcInnerFunction(vars);
	initialize(new NcDim[] {vars[0].getDimensions()[0]}, vars);
    }


    /**
     * Return the VisAD MathType of the range.
     *
     * @return		The VisAD MathType of the FlatFields.
     * @exception VisADException
     *			Couldn't create necessary VisAD object.
     */
    protected MathType
    getRangeMathType()
	throws VisADException
    {
	return innerFunction.getMathType();
    }


    /**
     * Return the VisAD data object corresponding to this function.
     *
     * @return		The VisAD MathType of the function.
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
	FieldImpl	field =
	    new FieldImpl((FunctionType)mathType, getDomainSet());

	field.setSamples(getRangeFlatFields(), /*copy=*/false);

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
