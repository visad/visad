/*
 * Copyright 1998, University Corporation for Atmospheric Research
 * See file LICENSE for copying and redistribution conditions.
 *
 * $Id: CoordVar.java,v 1.4 1998-02-23 15:58:15 steve Exp $
 */

package visad.data.netcdf;


import java.io.IOException;
import ucar.netcdf.Dimension;
import visad.Linear1DSet;
import visad.Unit;
import visad.data.BadFormException;




/*
 * Class for coordinate variables.
 */
class
CoordVar
    extends ExportVar
{
    /**
     * The linear, sampling domain set.
     */
    protected final Linear1DSet	set;


    /**
     * Construct.
     */
    protected
    CoordVar(String name, Dimension dim, Unit unit, Linear1DSet set)
	throws BadFormException
    {
	super(name, Float.TYPE, new Dimension[] {dim}, unit);
	this.set = set;
    }


    /**
     * Return the fill-value object for a co-ordinate variable.  This is
     * necessarily null because VisAD domain sample sets don't have missing
     * values.
     *
     * @param type	netCDF type (e.g. <code>Character.TYPE</code>, 
     *			<code>Float.TYPE</code>).
     * @return		The default fill-value object for the given netCDF
     *			type.
     */
    Number
    getFillValueNumber(Class type)
    {
	return null;
    }


    /**
     * Return an array element identified by position.
     */
     public Object
     get(int[] indexes)
	throws IOException
     {
	int	index = indexes[indexes.length-1];

	try
	{
	    return new Float(set.indexToValue(new int[] {index})[0][0]);
	}
	catch (Exception e)
	{
	    throw new IOException(e.getMessage());
	}
     }


    /**
     * Indicate whether or not this co-ordinate variable is the same as
     * another co-ordinate variable.
     */
    public boolean
    equals(CoordVar that)
    {
	return getName().equals(that.getName()) && 
		getRank() == that.getRank() &&
		getLengths()[0] == that.getLengths()[0] &&
		unit.equals(that.unit) &&
		set.equals(that.set);
    }
}
