/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcData.java,v 1.4 1998-06-17 20:30:25 visad Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import java.rmi.RemoteException;
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
    private MathType	mathType;


    /**
     * Factory method for constructing the proper type of NcData.
     *
     * @param vars	netCDF variables with the same domain.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     * @exception IOException
     *			Data access I/O failure.
     */
    static NcData
    newNcData(NcVar[] vars)
	throws VisADException, IOException
    {
	return (vars[0].getRank() == 0)
		    ? (NcData)NcScalar.newNcScalar(vars)
		    : (NcData)NcFunction.newNcFunction(vars);
    }


    /**
     * Construct from a given VisAD mathtype.
     */
    NcData(MathType type)
    {
	mathType = type;
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


    /**
     * Return a proxy for the VisAD data object corresponding to this 
     * netCDF data object.
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
    getProxy()
	throws VisADException, IOException;
}
