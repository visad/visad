/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RealAccessor.java,v 1.1 1998-03-11 16:21:53 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Real;


/**
 * Class for accessing data in a VisAD Real that's been adapted to a
 * netCDF API.
 */
class
RealAccessor
    extends	DataAccessor
{
    /**
     * Construct from an outer accessor.
     */
    protected
    RealAccessor(VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
    }


    /**
     * Return a datum given the split, netCDF indexes.
     */
    protected Object
    get()
	throws IOException
    {
	try
	{
	    return new Double(((Real)outerAccessor.get(outerIndexes)).
		getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
