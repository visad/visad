/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FieldAccessor.java,v 1.1 1998-03-11 16:21:51 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Field;


/**
 * Class for accessing data in a VisAD Field that's been adapted to a
 * netCDF API.
 */
class
FieldAccessor
    extends	DataAccessor
{
    /**
     * The shape of the netCDF variables of the VisAD Field in netCDF order
     * (i.e. outermost dimension first).
     */
    protected final int[]	shape;


    /**
     * Construct from netCDF Dimensions, and an outer VisADAccessor.
     */
    protected
    FieldAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
    {
	super(localDims, outerAccessor);

	shape = new int[localRank];
	for (int idim = 0; idim < localRank; ++idim)
	    shape[idim] = localDims[localRank-1-idim].getLength();
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException
    {
	int	visadIndex = localIndexes[0];

	for (int i = 1; i < localRank; ++i)
	    visadIndex = visadIndex * shape[i] + localIndexes[i];

	try
	{
	    return ((Field)outerAccessor.get(outerIndexes)).
		getSample(visadIndex);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
