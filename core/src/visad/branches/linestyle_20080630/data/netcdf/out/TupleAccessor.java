/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * All Rights Reserved.
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TupleAccessor.java,v 1.3 2000-04-26 15:45:26 dglo Exp $
 */

package visad.data.netcdf.out;

import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Tuple;


/**
 * The TupleAccessor class accesses data in a VisAD Tuple that's being
 * adapted to a netCDF API.  It's useful for exporting VisAD data to a
 * netCDF dataset.
 */
class
TupleAccessor
    extends	DataAccessor
{
    /**
     * The index of the relevant component.
     */
    private final int		index;


    /**
     * Construct from a component index and an outer VisADAccessor.
     *
     * @param index		The index of the Tuple component.
     * @param outerAccessor	The DataAccessor of the enclosing VisAD data
     *				object.  Returns a Tuple.
     */
    protected
    TupleAccessor(int index, VisADAccessor outerAccessor)
    {
	super(new Dimension[0], outerAccessor);
	this.index = index;
    }


    /**
     * Return a datum given the split, netCDF indexes.
     *
     * @return		The data object at the position given by
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
	    return ((Tuple)outerAccessor.get(outerIndexes)).getComponent(index);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
    }
}
