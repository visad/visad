/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: Accessor.java,v 1.3 1998-09-11 15:00:50 steve Exp $
 */

package visad.data.netcdf.in;

import visad.Data;
import visad.FlatField;
import visad.FunctionType;
import visad.VisADException;
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
	throws VisADException
    {
	return var.getFunctionType();
    }
}
