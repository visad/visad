/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcRegFunction.java,v 1.2 1998-04-02 20:49:45 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.CoordinateSystem;
import visad.DataImpl;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.GriddedSet;
import visad.IntegerNDSet;
import visad.IntegerSet;
import visad.Linear1DSet;
import visad.Linear2DSet;
import visad.Linear3DSet;
import visad.LinearNDSet;
import visad.LinearSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.Set;
import visad.TextType;
import visad.TupleType;
import visad.UnimplementedException;
import visad.Unit;
import visad.VisADException;
import visad.data.FileFlatField;
import visad.data.CacheStrategy;


/**
 * The NcRegFunction class adapts an imported, regular netCDF function
 * to a VisAD function.  A "regular" netCDF function is flat (i.e. it is not
 * nested).
 */
class
NcRegFunction
    extends	NcFunction
{
    /**
     * Construct from an array of adapted, netCDF variables.
     *
     * @param vars	The netCDF variables that are defined over the same
     *			domain and that constitude the range of the function.
     * @precondition	All variables have the same (ordered) set of dimensions.
     * @precondition	The variables aren't scalars.
     * @exception UnimplementedException	Not yet!
     * @exception VisADException		Couldn't create necessary 
     *						VisAD object.
     * @exception IOException			I/O error.
     */
    NcRegFunction(NcVar[] vars)
	throws VisADException
    {
	super(getFunctionType(vars[0].getDimensions(), vars),
	    vars[0].getDimensions(), vars);
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
	FieldImpl	field;

	if (hasTextualComponent)
	{
	    // TODO: support text in Fields
	    field = null;
	}
	else
	{
	    FlatField	flatField =
		new FlatField((FunctionType)mathType, 
		    getDomainSet(domainDims,
			((FunctionType)mathType).getDomain()),
		    (CoordinateSystem)null, getRangeSets(vars),
		    getRangeUnits(vars));

	    flatField.setSamples(getRangeDoubles(), /*copy=*/false);

	    field = flatField;
	}

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
	return new FileFlatField(new Accessor(this), (CacheStrategy)null);
    }


    /**
     * Return the range values of this function as doubles.
     *
     * @return		The range values of the function.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected double[][]
    getRangeDoubles()
	throws VisADException, IOException
    {
	int		nvars = vars.length;
	double[][]	values = new double[nvars][];

	for (int ivar = 0; ivar < nvars; ++ivar)
	    values[ivar] = vars[ivar].getDoubleValues();

	return values;
    }
}
