/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: NcScalar.java,v 1.6 1998-09-11 15:00:54 steve Exp $
 */

package visad.data.netcdf.in;

import java.io.IOException;
import visad.DataImpl;
import visad.MathType;
import visad.VisADException;


/**
 * Adapts scalar netCDF variables to a VisAD Scalar.
 */
public class
NcScalar
    extends	NcData
{
    /**
     * The netCDF variable that constitutes the scalar.
     */
    protected NcVar	var;


    /**
     * Constructs from an adapted, netCDF variable.
     */
    public
    NcScalar(NcVar var)
	throws VisADException, IOException
    {
	this.var = var;
    }


    /**
     * Returns the VisAD MathType of this data object.
     */
    public MathType
    getMathType()
	throws VisADException
    {
	return var.getMathType();
    }


    /**
     * Gets the VisAD data object corresponding to this data object.
     */
    public DataImpl
    getData()
	throws IOException, VisADException
    {
	// TODO: support text
	return ((NcNumber)var).getData();
    }


    /**
     * Gets a proxy for the corresponding VisAD data object.
     *
     * @return			A proxy for the corresponding VisAD data object.
     * @throws NestedException	Data object is in the range of a nested Field.
     * @throws VisADException   Couldn't create necessary VisAD object.
     * @throws IOException      Data access I/O failure.
     */
    public DataImpl
    getProxy()
	throws IOException, NestedException, VisADException
    {
	return getData();	// scalars don't deserve a proxy
    }


    /**
     * Gets the values of this data object as an array of doubles.  The
     * length of the returned, inner arrays will be 1.
     *
     * @postcondition	RETURN_VALUE<code>[i].length == 1</code> for all
     *			<code>i</code>
     */
    public double[][]
    getDoubles()
	throws IOException, VisADException
    {
	return new double[][] {var.getDoubles()};
    }
}
