/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: FieldAccessor.java,v 1.3 2000-04-26 15:45:25 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Field;


/**
 * The FieldAccessor class accesses data in a VisAD Field that's being
 * adapted to a netCDF API.
 */
class
FieldAccessor
    extends	DataAccessor
{
    /**
     * The shape of the netCDF variables of the VisAD Field in netCDF order
     * (i.e. outermost dimension first).
     */
    private final int[]	shape;


    /**
     * Construct from netCDF Dimensions, and an outer VisADAccessor.
     *
     * @param localDims		The netCDF dimensions of the Field in netCDF.
     *				order (outermost dimension first).
     * @param outerAccessor	The DataAccessor for accessing the
     *				<code>Field</code>s object of the enclosing,
     *				VisAD data object.
     */
    protected
    FieldAccessor(Dimension[] localDims, VisADAccessor outerAccessor)
    {
	super(localDims, outerAccessor);

	shape = new int[localRank];
	for (int idim = 0; idim < localRank; ++idim)
	    shape[idim] = localDims[idim].getLength();
    }


    /**
     * Return a datum given the split, netCDF indexes.
     *
     * @return	The Object at the position specified by
     *		<code>localIndexes</code> and <code>outerIndexes</code>.
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
