/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TupleVar.java,v 1.2 1998-02-23 15:58:30 steve Exp $
 */

package visad.data.netcdf;

import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Dimension;
import visad.Field;
import visad.FunctionType;
import visad.Real;
import visad.RealType;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/*
 * Class for a netCDF variable comprising a Real component of 
 * a Field's Tuple or RealTuple range.
 */
class
TupleVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range corresponding
     * to the variable.
     */
    protected final int		icomp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleVar(String name, Dimension[] dims, Unit unit, Field field, int icomp)
	throws BadFormException, VisADException, RemoteException
    {
	super(name, dims, unit, field,
		((RealType)((TupleType)
		((FunctionType)field.getType()).getRange()).
		getComponent(icomp)).getDefaultSet());
	this.icomp = icomp;
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
	    return getExportObject(((Real)((Tuple)field.
		getSample(visadIndex(indexes))).getComponent(icomp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
