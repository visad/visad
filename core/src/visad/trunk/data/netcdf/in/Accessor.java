/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Accessor.java,v 1.4 1998-09-15 21:55:25 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.VisADException;
import visad.data.BadRepositoryException;
import visad.data.FileAccessor;


/**
 * Supports access to netCDF variables via the FileAccessor API.
 */
public class
Accessor
    extends	FileAccessor
{
    /**
     * The associated netCDF variable.
     */
    private final NcNumber	var;


    /**
     * Constructs from a netCDF numeric variable.
     */
    public
    Accessor(NcNumber var)
    {
	this.var = var;
    }


    public void
    writeFile(int[] fileLocations, Data range)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public double[][]
    readFlatField(FlatField template, int[] fileLocation)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public void
    writeFlatField(double[][] values, FlatField template, 
		    int[] fileLocation)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }


    public FlatField
    getFlatField()
	throws VisADException
    {
	try
	{
	    return (FlatField)var.getData();
	}
	catch (Exception e)
	{
	    throw new VisADException(e.getMessage());
	}
    }

    public FunctionType
    getFunctionType()
	throws VisADException, BadRepositoryException
    {
	try
	{
	    return var.getFunctionType();
	}
	catch (IOException e)
	{
	    throw new BadRepositoryException(e.getMessage());
	}
    }
}
