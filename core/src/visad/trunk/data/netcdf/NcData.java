/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcData.java,v 1.3 1998-03-12 22:03:05 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * The NcData class provides an abstract class for adapting an existing
 * netCDF data object to a VisAD data object.
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
     *
     * @param mathType	The VisAD MathType of the adapted netCDF data object.
     */
    protected void
    initialize(MathType mathType)
    {
	this.mathType = mathType;
    }


    /**
     * Return the VisAD MathType of this data object.
     *
     * @return	The VisAD MathType of the adapted netCDF data object.
     */
    protected MathType
    getMathType()
    {
	return mathType;
    }


    /**
     * Return the VisAD data object corresponding to this netCDF data object.
     *
     * @return		The VisAD data object corresponding to the netCDF data
     *			object.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    abstract DataImpl
    getData()
	throws VisADException, IOException;
}
