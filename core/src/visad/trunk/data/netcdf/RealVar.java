/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: RealVar.java,v 1.2 1998-02-23 15:58:29 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Field;
import visad.Real;
import visad.Set;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;




/**
 * Inner class for a netCDF variable comprising a Field's range.
 */
class
RealVar
    extends RangeVar
{
    /**
     * Construct from broken-out information.
     */
    protected
    RealVar(String name, Dimension[] dims, Unit unit, Field field, Set set)
	throws BadFormException, VisADException
    {
	super(name, dims, unit, field, set);
    }


    /**
     * Return an array element identified by position.
     *
     * @precondition	<code>indexes</code> != null && 
     *			<code>indexes.length</code> == domain rank
     *			&& indexed point lies within the domain.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	try
	{
	    return getExportObject(
		((Real)field.getSample(visadIndex(indexes))).getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
