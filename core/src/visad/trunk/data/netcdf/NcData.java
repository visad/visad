/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcData.java,v 1.2 1998-02-23 15:58:21 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Abstract class for adapting an existing netCDF data object to a
 * VisAD data object.
 */
abstract class
NcData
{
    /**
     * The VisAD MathType of the data object.
     */
    protected MathType	mathType;


    /**
     * Protected initializer.
     */
    protected void
    initialize(MathType mathType)
    {
	this.mathType = mathType;
    }


    /**
     * Return the VisAD MathType of this data object.
     */
    protected MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     */
    abstract DataImpl
    getData()
	throws VisADException, IOException;
}
