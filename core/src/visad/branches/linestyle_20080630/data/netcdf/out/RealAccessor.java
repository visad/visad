/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RealAccessor.java,v 1.3 2000-04-26 15:45:26 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Real;


/**
 * The RealAccessor class accesses data in a VisAD Real that's been adapted
 * to a netCDF API.  It's useful for exporting data to a netCDF dataset.
 */
class
RealAccessor
    extends	DataAccessor
{
    /**
     * Construct from an outer accessor.
     *
     * @param outerAccessor	The DataAccessor for the encompassing VisAD
     *				data object.  Returns VisAD Reals.
     */
    protected
    RealAccessor(VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     *
     * @return		The datum at the position given by
     *			<code>localIndexes</code> and
     *			<code>outerIndexes</code>.
     * @exception IOException
     *			Data access I/O failure.
     */
    protected Object
    get()
	throws IOException
    {
	try
	{
	    return new
		Double(((Real)outerAccessor.get(outerIndexes)).getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
