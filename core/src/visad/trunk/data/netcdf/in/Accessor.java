/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Accessor.java,v 1.1 1998-04-02 20:48:46 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.UnimplementedException;
import visad.VisADException;
import visad.data.FileAccessor;
import visad.data.netcdf.UnsupportedOperationException;


/**
 * File accessor for regular, adapted, netCDF functions.
 */
public class
Accessor
    extends	FileAccessor
{
    /**
     * The adapted, netCDF function
     */
    protected final NcRegFunction	function;


    /**
     * Construct.
     *
     * @param type	The VisAD MathType of the adapted, netCDF function.
     */
    Accessor(NcRegFunction function)
    {
	this.function = function;
    }


    public void	writeFile(
				    int[]	fileLocations,
				    Data	range)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public double[][]	readFlatField(
				    FlatField	template,
				    int[]	fileLocation)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public void	writeFlatField(
				    double[][]	values,
				    FlatField	template,
				    int[]	fileLocation)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public FlatField getFlatField()
	throws VisADException
    {
	try
	{
	    return (FlatField)function.getData();
	}
	catch (Exception e)
	{
	    throw new VisADException(e.getMessage());
	}
    }

    public FunctionType getFunctionType()
    {
	return (FunctionType)function.getMathType();
    }
}
