/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: TupleRealVar.java,v 1.2 1998-02-23 15:58:29 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import java.rmi.RemoteException;
import ucar.netcdf.Dimension;
import visad.Field;
import visad.FunctionType;
import visad.Real;
import visad.RealTuple;
import visad.RealTupleType;
import visad.RealType;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.data.BadFormException;


/*
 * Class for a netCDF variable comprising a Real component of 
 * a RealTuple of a Field's Tuple range.
 */
class
TupleRealVar
    extends RealVar
{
    /**
     * The index of the component in the Tuple range containing
     * the variable.
     */
    protected final int	tupleComp;


    /**
     * The index of the component in the RealTuple corresponding
     * to the variable.
     */
    protected final int	realTupleComp;


    /**
     * Construct from broken-out information.
     */
    protected
    TupleRealVar(String name, Dimension[] dims, Unit unit, int tupleComp,
	    int realTupleComp, Field field)
	throws BadFormException, VisADException, RemoteException
    {
	super(name, dims, unit, field,
	    ((RealType)((RealTupleType)((TupleType)
		((FunctionType)field.getType()).getRange()).
		getComponent(tupleComp)).getComponent(realTupleComp)).
		getDefaultSet());
	this.tupleComp = tupleComp;
	this.realTupleComp = realTupleComp;
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
	    return getExportObject(((Real)((RealTuple)((Tuple)field.
		getSample(visadIndex(indexes))).
		    getComponent(tupleComp)).getComponent(realTupleComp)).
		    getValue());
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }
}
