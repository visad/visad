/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TupleAccessor.java,v 1.1 1998-03-11 16:21:56 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Tuple;


/**
 * Class for accessing data in a VisAD Tuple that's been adapted to a
 * netCDF API.
 */
class
TupleAccessor
    extends	DataAccessor
{
    /**
     * The index of the relevant component.
     */
    protected final int		index;


    /**
     * Construct from a component index and an outer VisADAccessor.
     */
    protected
    TupleAccessor(int index, VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
	this.index = index;
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
	    return ((Tuple)outerAccessor.get(outerIndexes)).getComponent(index);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
