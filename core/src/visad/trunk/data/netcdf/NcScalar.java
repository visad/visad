/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcScalar.java,v 1.1 1998-03-17 15:55:27 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.Data;
import visad.DataImpl;
import visad.MathType;
import visad.Real;
import visad.RealType;
import visad.Scalar;
import visad.Tuple;
import visad.TupleType;
import visad.VisADException;


/**
 * The NcScalar class adapts scalars in a netCDF dataset to a VisAD API.
 */
class
NcScalar
    extends	NcData
{
    /** The VisAD scalar corresponding to the netCDF variable being adapted. */
    protected final DataImpl	scalar;

    /**
     * Construct from a netCDF variable.
     *
     * @param var	The scalar netCDF variable to be adapted.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcScalar(ImportVar var)
	throws VisADException, IOException
    {
	RealType	type = new RealType(var.getName());

	initialize(type);
	scalar = new Real(type, var.getDoubleValues()[0], var.getUnit());
    }

    /**
     * Construct from netCDF variables.
     *
     * @param vars	The scalar netCDF variables to be adapted.
     * @precondition	<code>vars.length > 1</code>.
     * @exception VisADException
     *			Problem in core VisAD.  Probably some VisAD object
     *			couldn't be created.
     */
    NcScalar(ImportVar[] vars)
	throws VisADException, RemoteException
    {
	TupleType	type = new TupleType(getMathTypes(vars));

	initialize(type);
	scalar = new Tuple(type, getDatas(vars));
    }

    /**
     * Return the MathTypes of the given scalar netCDF variables.
     *
     * @param vars	The scalar netCDF variables to have their MathTypes
     *			returned.
     */
    protected static MathType[]
    getMathTypes(ImportVar[] vars)
    {
	MathType[]	types = new MathType[vars.length];

	for (int i = 0; i < vars.length; ++i)
	{
	    types[i] = vars[i].getMathType();
	}

	return types;
    }

    /**
     * Return the VisAD data objects the given scalar netCDF variables.
     *
     * @param vars      The scalar netCDF variables to be returned as
     *			VisAD data object.
     */
    protected static Data[]
    getDatas(ImportVar[] vars)
    {
	throw new UnsupportedOperationException("getDatas()");
    }

    /**
     * Return the VisAD data object corresponding to the netCDF scalar.
     *
     * @return		The VisAD data object corresponding to the netCDF 
     *			scalar.
     */
    DataImpl
    getData()
    {
	return scalar;
    }
}
